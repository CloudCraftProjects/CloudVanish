package dev.booky.vanish.commands;
// Created by booky10 in CloudVanish (12:34 11.11.22)

import dev.booky.vanish.VanishManager;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.TriState;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.ApiStatus;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApiStatus.Internal
public final class VanishCommand {

    private static final String MAIN_NAME = "vanish";
    private static final Set<String> ALIASES = Set.of("cloudvanish", "cv", "v");
    private static final Set<String> ALL_ALIASES = Stream.concat(Stream.of(MAIN_NAME), ALIASES.stream())
            .collect(Collectors.toUnmodifiableSet());

    private final NamespacedKey pickupKey;
    private final VanishManager manager;

    private VanishCommand(VanishManager manager) {
        this.pickupKey = new NamespacedKey(manager.getPlugin(), "pickup");
        this.manager = manager;
    }

    public static void create(VanishManager manager) {
        VanishCommand command = new VanishCommand(manager);
        command.unregister();
        command.register();
    }

    private WrapperCommandSyntaxException fail(Component message) {
        return CommandAPIBukkit.failWithAdventureComponent(this.manager.getPrefix()
                .append(message.colorIfAbsent(NamedTextColor.RED)));
    }

    private void success(CommandSender sender, Component message) {
        sender.sendMessage(this.manager.getPrefix()
                .append(message.colorIfAbsent(NamedTextColor.YELLOW)));
    }

    private void unregister() {
        for (String alias : ALL_ALIASES) {
            CommandAPI.unregister(alias, true);
        }
    }

    private void register() {
        new CommandTree(MAIN_NAME)
                .withAliases(ALIASES.toArray(String[]::new))
                .withPermission("cloudvanish.command")
                .then(new EntitySelectorArgument.OnePlayer("target").setOptional(true)
                        .withPermission("cloudvanish.command.other")
                        .executesNative(this::toggleVanish))
                .then(new LiteralArgument("pickup")
                        .withPermission("cloudvanish.command.pickup")
                        .then(new EntitySelectorArgument.OnePlayer("target").setOptional(true)
                                .withPermission("cloudvanish.command.pickup.other")
                                .executesNative(this::togglePickup)))
                .then(new LiteralArgument("list")
                        .withPermission("cloudvanish.command.list")
                        .executesNative(this::listVanished))
                .register();
    }

    private void toggleVanish(NativeProxyCommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
        Player player = args.getOrDefaultUnchecked("target", () -> (Player) sender.getCallee());
        boolean couldSee = sender.getCallee() instanceof Player && ((Player) sender.getCallee()).canSee(player);
        TriState result = this.manager.toggleVanish(player);

        // only have to cover this case, otherwise a "broadcast" message will be sent anyway
        if (result == TriState.NOT_SET) {
            throw this.fail(Component.translatable("vanish.command.toggle.fail"));
        }

        // check if the target is the same as the sender, or if the sender isn't an actual player
        if (sender.getCaller() == player || !(sender.getCaller() instanceof Player playerSender)) {
            return;
        }

        if (result == TriState.TRUE) { // vanished
            if (!playerSender.canSee(player)) {
                this.success(sender, Component.translatable("vanish.vanished", player.teamDisplayName()));
            }
        } else if (!couldSee) { // unvanished
            this.success(sender, Component.translatable("vanish.unvanished", player.teamDisplayName()));
        }
    }

    private void togglePickup(NativeProxyCommandSender sender, CommandArguments args) {
        Player player = args.getOrDefaultUnchecked("target", () -> (Player) sender.getCallee());
        if (player.getPersistentDataContainer().has(this.pickupKey)) {
            player.getPersistentDataContainer().remove(this.pickupKey);
            this.success(sender, Component.translatable("vanish.command.pickup.off"));
        } else {
            player.getPersistentDataContainer().set(this.pickupKey, PersistentDataType.BYTE, (byte) 0);
            this.success(sender, Component.translatable("vanish.command.pickup.on"));
        }
    }

    private void listVanished(NativeProxyCommandSender sender, CommandArguments args) {
        TextComponent.Builder message = Component.text();
        int vanishLevel = sender.getCaller() instanceof Player player ? this.manager.getVanishLevelOrCalc(player) : 1337;
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
                    .colorIfAbsent(NamedTextColor.WHITE)
                    .hoverEvent(Component.translatable(
                            "vanish.command.list.level", NamedTextColor.AQUA, Component.text(level))));
        }

        String key = playerCount == 0 ? "vanish.command.list.none"
                : playerCount == 1 ? "vanish.command.list.one"
                : "vanish.command.list.multiple";
        this.success(sender, Component.translatable(key, Component.text(playerCount), message));
    }
}
