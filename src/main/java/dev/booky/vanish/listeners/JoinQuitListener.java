package dev.booky.vanish.listeners;
// Created by booky10 in CloudVanish (21:34 09.11.22)

import dev.booky.vanish.VanishManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class JoinQuitListener implements Listener {

    private final VanishManager manager;

    public JoinQuitListener(VanishManager manager) {
        this.manager = manager;
    }

    // This is the first event being called after having loaded the player data
    @EventHandler
    public void onInitialSpawn(PlayerSpawnLocationEvent event) {
        this.manager.handleLogin(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        // We sadly don't have any event, which is directly executed after the server connection handler
        // has been set (needed for hiding other players), so we have can only hide other players in the join event

        int vanishLevel = this.manager.getVanishLevelOrCalc(event.getPlayer());
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (event.getPlayer() == target) {
                continue;
            }

            Integer targetLvl = this.manager.getVanishLevel(target);
            if (targetLvl != null) { // check if target is vanished
                if (targetLvl > vanishLevel) { // check if target has higher level than player
                    event.getPlayer().hidePlayer(this.manager.getPlugin(), target);
                }
            }
        }

        if (!this.manager.isVanished(event.getPlayer())) {
            return;
        }

        Component joinMessage = event.joinMessage();
        if (joinMessage != null) {
            Bukkit.getConsoleSender().sendMessage(joinMessage);
            event.joinMessage(null);

            Component broadcastMessage = VanishManager.getPrefix().append(joinMessage);
            for (Player player : this.manager.getViewers(event.getPlayer())) {
                player.sendMessage(broadcastMessage);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        if (!this.manager.isVanished(event.getPlayer())) {
            return;
        }

        Component quitMessage = event.quitMessage();
        if (quitMessage != null) {
            Bukkit.getConsoleSender().sendMessage(quitMessage);
            event.quitMessage(null);

            Component broadcastMessage = VanishManager.getPrefix().append(quitMessage);
            for (Player player : this.manager.getViewers(event.getPlayer())) {
                player.sendMessage(broadcastMessage);
            }
        }
        this.manager.handleQuit(event.getPlayer());
    }
}
