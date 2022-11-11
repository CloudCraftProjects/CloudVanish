package dev.booky.vanish.listeners;
// Created by booky10 in CloudVanish (20:07 11.11.22)

import dev.booky.vanish.VanishManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class ProtectionListener implements Listener {

    private final VanishManager manager;

    public ProtectionListener(VanishManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (this.manager.isVanished(player)) {
                // Damage done if using /kill
                if (event.getDamage() != 3.4028234663852886E38D) {
                    event.setDamage(0d);
                }
            }
        }
    }

    @EventHandler
    public void onTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player player) {
            if (this.manager.isVanished(player)) {
                event.setCancelled(true);
            }
        }
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

        Component broadcastMessage = VanishManager.getPrefix().append(deathMessage.color(NamedTextColor.YELLOW));
        for (Player viewer : this.manager.getViewers(event.getPlayer())) {
            viewer.sendMessage(broadcastMessage);
        }
    }
}
