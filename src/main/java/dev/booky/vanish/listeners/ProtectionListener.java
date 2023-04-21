package dev.booky.vanish.listeners;
// Created by booky10 in CloudVanish (20:07 11.11.22)

import dev.booky.vanish.VanishManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;

public class ProtectionListener implements Listener {

    private final NamespacedKey pickupKey;
    private final VanishManager manager;

    public ProtectionListener(VanishManager manager) {
        this.pickupKey = new NamespacedKey(manager.getPlugin(), "pickup");
        this.manager = manager;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!this.manager.isVanished(player)) {
            return;
        }

        // Damage done if using /kill
        if (event.getDamage() != 3.4028234663852886E38D) {
            event.setDamage(0d);
        }
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!this.manager.isVanished(player)) {
            return;
        }

        // Allow players to eat food
        if (player.getFoodLevel() > event.getFoodLevel()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTarget(EntityTargetEvent event) {
        if (!(event.getTarget() instanceof Player player)) {
            return;
        }
        if (!this.manager.isVanished(player)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        if (!this.manager.isVanished(event.getPlayer())) {
            return;
        }

        Component deathMessage = event.deathMessage();
        if (deathMessage == null) {
            return;
        }

        Bukkit.getConsoleSender().sendMessage(deathMessage);
        event.deathMessage(null);

        Component broadcastMessage = this.manager.getPrefix().append(deathMessage.color(NamedTextColor.YELLOW));
        for (Player viewer : this.manager.getViewers(event.getPlayer())) {
            viewer.sendMessage(broadcastMessage);
        }
    }

    @EventHandler
    public void onItemPickup(PlayerAttemptPickupItemEvent event) {
        if (!this.manager.isVanished(event.getPlayer())) {
            return;
        }
        if (event.getPlayer().getPersistentDataContainer().has(this.pickupKey)) {
            return;
        }

        event.setCancelled(true);
    }
}
