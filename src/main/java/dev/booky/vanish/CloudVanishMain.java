package dev.booky.vanish;
// Created by booky10 in CloudVanish (20:11 09.11.22)

import dev.booky.vanish.listeners.DebugListener;
import dev.booky.vanish.listeners.JoinQuitListener;
import dev.booky.vanish.util.TranslationLoader;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class CloudVanishMain extends JavaPlugin {

    private TranslationLoader i18n;
    private VanishManager manager;

    public CloudVanishMain() {
        try {
            Class.forName("io.papermc.paper.configuration.Configuration");
        } catch (ClassNotFoundException exception) {
            throw new RuntimeException("Please use paper for this plugin to function! Download it at https://papermc.io/.");
        }
    }

    @Override
    public void onLoad() {
        (this.i18n = new TranslationLoader(this)).load();
        this.manager = new VanishManager(this);
        new Metrics(this, 16837);

        Bukkit.getServicesManager().register(VanishManager.class, this.manager, this, ServicePriority.Normal);
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new JoinQuitListener(this.manager), this);
        Bukkit.getPluginManager().registerEvents(new DebugListener(this.manager), this);

        for (Player player : Bukkit.getOnlinePlayers()) {
            this.manager.handleLogin(player);
        }
    }

    @Override
    public void onDisable() {
        try {
            if (this.manager != null) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    this.manager.handleQuit(player);
                }
            }
        } finally {
            this.i18n.unload();
        }
    }
}
