package dev.booky.vanish;
// Created by booky10 in CloudVanish (20:11 09.11.22)

import dev.booky.vanish.commands.VanishCommand;
import dev.booky.vanish.listeners.JoinQuitListener;
import dev.booky.vanish.listeners.ProtectionListener;
import dev.booky.vanish.util.TranslationLoader;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class CloudVanishMain extends JavaPlugin {

    private TranslationLoader i18n;
    private VanishManager manager;
    private VanishCommand command;

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

        if (Bukkit.getPluginManager().getPlugin("CommandAPI") == null) {
            super.getLogger().severe("###################################################################");
            super.getLogger().severe("# Install CommandAPI (https://commandapi.jorel.dev/) for commands #");
            super.getLogger().severe("###################################################################");
        } else {
            this.command = new VanishCommand(this.manager);
        }
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new JoinQuitListener(this.manager), this);
        Bukkit.getPluginManager().registerEvents(new ProtectionListener(this.manager), this);

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

            if (this.command != null) {
                this.command.unregister();
            }
        } finally {
            this.i18n.unload();
        }
    }
}
