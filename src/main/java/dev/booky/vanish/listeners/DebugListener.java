package dev.booky.vanish.listeners;
// Created by booky10 in CloudVanish (21:45 10.11.22)

import dev.booky.vanish.VanishManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class DebugListener implements Listener {

    private final VanishManager manager;

    public DebugListener(VanishManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().equals("/vanish")) {
            this.manager.toggleVanish(event.getPlayer());
            event.setCancelled(true);
        }
    }
}
