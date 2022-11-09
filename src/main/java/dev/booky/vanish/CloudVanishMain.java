package dev.booky.vanish;
// Created by booky10 in CloudVanish (20:11 09.11.22)

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public class CloudVanishMain extends JavaPlugin {

    public CloudVanishMain() {
        try {
            Class.forName("io.papermc.paper.configuration.Configuration");
        } catch (ClassNotFoundException exception) {
            throw new RuntimeException("Please use paper for this plugin to function! Download it at https://papermc.io/.");
        }
    }

    @Override
    public void onLoad() {
        new Metrics(this, 16837);
    }
}
