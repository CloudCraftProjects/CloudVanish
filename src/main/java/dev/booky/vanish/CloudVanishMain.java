package dev.booky.vanish;
// Created by booky10 in CloudVanish (20:11 09.11.22)

import dev.booky.cloudcore.i18n.CloudTranslator;
import dev.booky.vanish.commands.VanishCommand;
import dev.booky.vanish.listeners.JoinQuitListener;
import dev.booky.vanish.listeners.ProtectionListener;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

public class CloudVanishMain extends JavaPlugin {

    private CloudTranslator i18n;
    private VanishManager manager;

    @Override
    public void onLoad() {
        this.manager = new VanishManager(this);
        new Metrics(this, 16837);

        this.i18n = new CloudTranslator(this.getClassLoader(),
                new NamespacedKey(this, "i18n"),
                Locale.ENGLISH, Locale.GERMAN);
        this.i18n.load();

        Bukkit.getServicesManager().register(VanishManager.class,
                this.manager, this, ServicePriority.Normal);
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new JoinQuitListener(this.manager), this);
        Bukkit.getPluginManager().registerEvents(new ProtectionListener(this.manager), this);

        for (Player player : Bukkit.getOnlinePlayers()) {
            this.manager.handleLogin(player);
        }

        VanishCommand.create(this.manager);
    }

    @Override
    public void onDisable() {
        try {
            for (Player player : Bukkit.getOnlinePlayers()) {
                this.manager.handleQuit(player);
            }
        } finally {
            this.i18n.unload();
        }
    }
}
