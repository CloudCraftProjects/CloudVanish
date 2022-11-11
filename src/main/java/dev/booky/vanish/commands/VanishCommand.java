package dev.booky.vanish.commands;
// Created by booky10 in CloudVanish (12:34 11.11.22)

import dev.booky.vanish.VanishManager;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.LiteralArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.TriState;
import org.bukkit.NamespacedKey;
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
                .executesPlayer((Player player, Object[] args) -> toggleVanish(player))
                .then(new LiteralArgument("pickup")
                        .withPermission("cloudvanish.command.pickup")
                        .executesPlayer((Player player, Object[] args) -> togglePickup(player)))
                .register();
    }

    public void unregister() {
        for (String alias : ALL_ALIASES) {
            CommandAPI.unregister(alias, true);
        }
    }

    private void toggleVanish(Player player) {
        TriState result = this.manager.toggleVanish(player);

        // We only have to cover this case, otherwise a "broadcast" message will be sent anyway
        if (result == TriState.NOT_SET) {
            player.sendMessage(VanishManager.getPrefix().append(Component.translatable("vanish.command.toggle.fail", NamedTextColor.RED)));
        }
    }

    private void togglePickup(Player player) {
        if (player.getPersistentDataContainer().has(this.pickupKey)) {
            player.getPersistentDataContainer().remove(this.pickupKey);
            player.sendMessage(VanishManager.getPrefix().append(Component.translatable("vanish.command.pickup.off", NamedTextColor.YELLOW)));
        } else {
            player.getPersistentDataContainer().set(this.pickupKey, PersistentDataType.BYTE, (byte) 0);
            player.sendMessage(VanishManager.getPrefix().append(Component.translatable("vanish.command.pickup.on", NamedTextColor.YELLOW)));
        }
    }
}
