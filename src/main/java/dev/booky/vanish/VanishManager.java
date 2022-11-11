package dev.booky.vanish;
// Created by booky10 in CloudVanish (15:17 10.11.22)

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.TriState;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class VanishManager {

    // <gray>[<gradient:#3bd151:#2ea640>Vanish</gradient>]</gray><space>
    private static final Component PREFIX = Component.text()
            .append(Component.text('[', NamedTextColor.GRAY))
            .append(Component.text('V', TextColor.color(0x3bd151)))
            .append(Component.text('a', TextColor.color(0x39ca4e)))
            .append(Component.text('n', TextColor.color(0x37c34b)))
            .append(Component.text('i', TextColor.color(0x35bc49)))
            .append(Component.text('s', TextColor.color(0x32b446)))
            .append(Component.text('h', TextColor.color(0x30ad43)))
            .append(Component.text(']', NamedTextColor.GRAY))
            .append(Component.space()).build();

    private final Map<UUID, Integer> vanishLevels = new HashMap<>();
    private final NamespacedKey vanishedKey;
    private final Plugin plugin;

    public VanishManager(Plugin plugin) {
        this.vanishedKey = new NamespacedKey(plugin, "vanished");
        this.plugin = plugin;
    }

    private int readVanishLevel(Player player) {
        if (!player.hasPermission("cloudvanish.use")) {
            return 0;
        }

        int level = 0;
        for (int i = 0; i < 100; i++) { // * perms and operator status would fuck up a while true
            if (!player.hasPermission("cloudvanish.level." + ++level)) {
                break;
            }
        }
        return level;
    }

    @ApiStatus.Internal
    public void handleLogin(Player player) {
        // Check if the player was vanished on logout
        if (!player.getPersistentDataContainer().has(this.vanishedKey)) {
            return;
        }
        player.getPersistentDataContainer().remove(this.vanishedKey);

        // Re-Checks for the permission on join
        int vanishLevel = this.readVanishLevel(player);
        if (vanishLevel > 0) {
            this.vanishLevels.put(player.getUniqueId(), vanishLevel);

            for (Player nonViewer : this.getNonViewers0(player, vanishLevel)) {
                nonViewer.hidePlayer(this.plugin, player);
            }
        }
    }

    @ApiStatus.Internal
    public void handleQuit(Player player) {
        Integer level = this.vanishLevels.remove(player.getUniqueId());
        if (level != null) {
            player.getPersistentDataContainer().set(this.vanishedKey, PersistentDataType.BYTE, (byte) 0);
        }
    }

    public Collection<? extends Player> getNonViewers(Player player) {
        return this.getNonViewers0(player, this.getVanishLevel(player));
    }

    private Collection<? extends Player> getNonViewers0(Player player, Integer vanishLevel) {
        if (vanishLevel == null) { // not vanished
            return Collections.emptySet();
        }

        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        int maxNonViewerCount = players.size() - (player.isOnline() ? 1 : 0);
        if (maxNonViewerCount < 1) {
            return Collections.emptySet();
        }

        Set<Player> nonViewers = new HashSet<>(maxNonViewerCount);
        for (Player target : players) {
            if (target == player) {
                continue;
            }

            Integer targetLvl = this.getVanishLevel(target);
            if (targetLvl == null) {
                targetLvl = readVanishLevel(target);
            }

            if (vanishLevel > targetLvl) {
                nonViewers.add(target);
            }
        }

        return Collections.unmodifiableSet(nonViewers);
    }

    public Collection<? extends Player> getViewers(Player player) {
        Integer vanishLevel = this.getVanishLevel(player);
        if (vanishLevel == null) { // not vanished
            return Bukkit.getOnlinePlayers();
        }

        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        Set<Player> viewers = new HashSet<>(players.size());

        for (Player target : players) {
            if (target == player) {
                viewers.add(player);
                continue;
            }

            Integer targetLvl = this.getVanishLevel(target);
            if (targetLvl == null) {
                targetLvl = readVanishLevel(target);
            }

            if (vanishLevel <= targetLvl) {
                viewers.add(target);
            }
        }

        return Collections.unmodifiableSet(viewers);
    }

    public TriState toggleVanish(Player player) {
        boolean nextStatus = !this.isVanished(player);
        boolean success = this.setVanished(player, nextStatus);

        if (!success) {
            return TriState.NOT_SET;
        }

        return TriState.byBoolean(nextStatus);
    }

    public boolean setVanished(Player player, boolean vanish) {
        if (vanish) {
            return this.vanish(player);
        } else {
            return this.unvanish(player);
        }
    }

    public boolean vanish(Player player) {
        if (this.isVanished(player)) {
            return false;
        }

        int vanishLevel = this.readVanishLevel(player);
        if (vanishLevel <= 0) {
            return false;
        }

        Component vanishMessage = Component.translatable()
                .key("vanish.vanished")
                .args(player.teamDisplayName())
                .build();
        Bukkit.getConsoleSender().sendMessage(vanishMessage);

        Component broadcastMessage = Component.text()
                .color(NamedTextColor.YELLOW)
                .append(getPrefix())
                .append(vanishMessage)
                .build();

        this.vanishLevels.put(player.getUniqueId(), vanishLevel);
        for (Player viewer : this.getViewers(player)) {
            viewer.sendMessage(broadcastMessage);
        }

        Component joinMsg = Component.translatable()
                .color(NamedTextColor.YELLOW)
                .key("multiplayer.player.left")
                // We sadly can't use the team name, because it has a prefix because of CloudChat
                .args(Component.text(player.getName())).build();

        for (Player nonViewer : this.getNonViewers(player)) {
            nonViewer.sendMessage(joinMsg);
            nonViewer.hidePlayer(this.plugin, player);
        }

        return true;
    }

    public boolean unvanish(Player player) {
        Integer vanishLevel = this.getVanishLevel(player);
        if (vanishLevel == null) {
            return false;
        }

        Component unvanishMessage = Component.translatable()
                .key("vanish.unvanished")
                .args(player.teamDisplayName())
                .build();
        Bukkit.getConsoleSender().sendMessage(unvanishMessage);

        Component broadcastMessage = Component.text()
                .color(NamedTextColor.YELLOW)
                .append(getPrefix())
                .append(unvanishMessage)
                .build();

        for (Player viewer : this.getViewers(player)) {
            viewer.sendMessage(broadcastMessage);
        }

        Component joinMsg = Component.translatable()
                .color(NamedTextColor.YELLOW)
                .key("multiplayer.player.joined")
                // We sadly can't use the team name, because it has a prefix because of CloudChat
                .args(Component.text(player.getName())).build();

        for (Player nonViewer : this.getNonViewers(player)) {
            nonViewer.sendMessage(joinMsg);
            nonViewer.showPlayer(this.plugin, player);
        }

        this.vanishLevels.remove(player.getUniqueId());
        return true;
    }

    public int getVanishLevelOrCalc(Player player) {
        Integer vanishLevel = this.getVanishLevel(player);
        if (vanishLevel == null) {
            vanishLevel = readVanishLevel(player);
        }
        return vanishLevel;
    }

    public @Nullable Integer getVanishLevel(Player player) {
        return this.vanishLevels.get(player.getUniqueId());
    }

    public boolean isVanished(Player player) {
        return this.vanishLevels.containsKey(player.getUniqueId());
    }

    public static Component getPrefix() {
        return PREFIX;
    }

    public Plugin getPlugin() {
        return this.plugin;
    }
}
