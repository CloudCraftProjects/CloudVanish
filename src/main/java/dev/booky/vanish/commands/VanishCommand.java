package dev.booky.vanish.commands;
// Created by booky10 in CloudVanish (12:34 11.11.22)

import dev.booky.vanish.VanishManager;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.TriState;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class VanishCommand {

    private static final String MAIN_NAME = "vanish";
    private static final Set<String> ALIASES = Set.of("cloudvanish", "cv", "v");
    private static final Set<String> ALL_ALIASES = Stream.concat(Stream.of(MAIN_NAME), ALIASES.stream())
            .collect(Collectors.toUnmodifiableSet());

    private final VanishManager manager;

    public VanishCommand(VanishManager manager) {
        this.manager = manager;
        this.register();
    }

    public void register() {
        new CommandTree(MAIN_NAME)
                .withAliases(ALIASES.toArray(String[]::new))
                .withPermission("cloudvanish.command")
                .executesPlayer((Player player, Object[] args) -> toggleVanish(player))
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
}
