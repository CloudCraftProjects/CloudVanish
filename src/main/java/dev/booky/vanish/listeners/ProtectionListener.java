package dev.booky.vanish.listeners;
// Created by booky10 in CloudVanish (20:07 11.11.22)

import dev.booky.vanish.VanishManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class ProtectionListener implements Listener {

    private final VanishManager manager;

    public ProtectionListener(VanishManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (this.manager.isVanished(player)) {
                event.setDamage(0d);
            }
        }
    }
}
