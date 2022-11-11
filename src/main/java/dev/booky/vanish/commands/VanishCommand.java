package dev.booky.vanish.commands;
// Created by booky10 in CloudVanish (12:34 11.11.22)

import dev.booky.vanish.VanishManager;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.TriState;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class VanishCommand {

    private static final String MAIN_NAME = "vanish";
    private static final Set<String> ALIASES = Set.of("cloudvanish", "cv", "v");
    private static final Set<String> ALL_ALIASES = Stream.concat(Stream.of(MAIN_NAME), ALIASES.stream())
            .collect(Collectors.toUnmodifiableSet());

    private final NamespacedKey pickupKey;
    private final VanishManager manager;

    public VanishCommand(VanishManager manager) {
        this.pickupKey = new NamespacedKey(manager.getPlugin(), "pickup");
        this.manager = manager;
        this.register();
    }

    public void register() {
        new CommandTree(MAIN_NAME)
                .withAliases(ALIASES.toArray(String[]::new))
                .withPermission("cloudvanish.command")
                .executesPlayer((Player player, Object[] args) -> toggleVanish(player, player))
                .then(new PlayerArgument("target")
                        .withPermission("cloudvanish.command.other")
                        .executes((CommandSender sender, Object[] args) -> toggleVanish(sender, (Player) args[0])))
                .then(new LiteralArgument("pickup")
                        .withPermission("cloudvanish.command.pickup")
                        .executesPlayer((Player player, Object[] args) -> togglePickup(player, player))
                        .then(new PlayerArgument("target")
                                .withPermission("cloudvanish.command.pickup.other")
                                .executes((CommandSender sender, Object[] args) -> togglePickup(sender, (Player) args[0]))))
                .then(new LiteralArgument("list")
                        .withPermission("cloudvanish.command.list")
                        .executes((CommandSender sender, Object[] args) -> listVanished(sender)))
                .register();
    }

    public void unregister() {
        for (String alias : ALL_ALIASES) {
            CommandAPI.unregister(alias, true);
        }
    }

    private void toggleVanish(CommandSender sender, Player player) {
        boolean couldSee = sender instanceof Player && ((Player) sender).canSee(player);
        TriState result = this.manager.toggleVanish(player);

        // We only have to cover this case, otherwise a "broadcast" message will be sent anyway
        if (result == TriState.NOT_SET) {
            sender.sendMessage(VanishManager.getPrefix().append(Component.translatable("vanish.command.toggle.fail", NamedTextColor.RED)));
            return;
        }

        // check if the target is the same as the sender, or if the sender isn't an actual player
        if (sender == player || !(sender instanceof Player playerSender)) {
            return;
        }

        if (result == TriState.TRUE) { // vanished
            if (!playerSender.canSee(player)) {
                sender.sendMessage(VanishManager.getPrefix()
                        .append(Component.translatable()
                                .color(NamedTextColor.YELLOW)
                                .key("vanish.vanished")
                                .args(player.teamDisplayName())
                                .build()));
            }
        } else if (!couldSee) { // unvanished
            sender.sendMessage(VanishManager.getPrefix()
                    .append(Component.translatable()
                            .color(NamedTextColor.YELLOW)
                            .key("vanish.unvanished")
                            .args(player.teamDisplayName())
                            .build()));
        }
    }

    private void togglePickup(CommandSender sender, Player player) {
        if (player.getPersistentDataContainer().has(this.pickupKey)) {
            player.getPersistentDataContainer().remove(this.pickupKey);
            sender.sendMessage(VanishManager.getPrefix().append(Component.translatable("vanish.command.pickup.off", NamedTextColor.YELLOW)));
        } else {
            player.getPersistentDataContainer().set(this.pickupKey, PersistentDataType.BYTE, (byte) 0);
            sender.sendMessage(VanishManager.getPrefix().append(Component.translatable("vanish.command.pickup.on", NamedTextColor.YELLOW)));
        }
    }

    private void listVanished(CommandSender sender) {
        TextComponent.Builder message = Component.text();
        int vanishLevel = sender instanceof Player ? this.manager.getVanishLevelOrCalc((Player) sender) : 1337;
        int playerCount = 0;

        for (Player player : Bukkit.getOnlinePlayers()) {
            Integer level = this.manager.getVanishLevel(player);
            if (level == null) {
                continue;
            }

            // sender couldn't see the player, so ignore them
            if (level > vanishLevel) {
                continue;
            }

            if (!message.children().isEmpty()) {
                message.append(Component.text(", "));
            }

            playerCount++;
            message.append(player.teamDisplayName()
                    .hoverEvent(HoverEvent.showText(Component.translatable()
                            .key("vanish.command.list.level")
                            .color(NamedTextColor.AQUA)
                            .args(Component.text(level)))));
        }

        String key = playerCount == 0 ? "vanish.command.list.none"
                : playerCount == 1 ? "vanish.command.list.one"
                : "vanish.command.list.multiple";

        sender.sendMessage(Component.text()
                .color(NamedTextColor.YELLOW)
                .append(VanishManager.getPrefix())
                .append(Component.translatable(key, Component.text(playerCount), message))
                .build()
        );
    }
}
