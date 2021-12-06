package de.presti.ree6.webinterface.utils;

import org.simpleyaml.configuration.file.FileConfiguration;
import org.simpleyaml.configuration.file.YamlConfiguration;

import java.io.File;

public class Config {

    FileConfiguration cfg;

    public void init() {

        cfg = getConfig();

        if (!getFile().exists()) {
            cfg.options().copyDefaults(true);
            cfg.options().copyHeader(true);
            cfg.options().header("################################\n" +
                    "#                              #\n" +
                    "# Ree6 Config File             #\n" +
                    "# by Presti                    #\n" +
                    "#                              #\n" +
                    "################################\n");
            cfg.addDefault("mysql.user", "root");
            cfg.addDefault("mysql.db", "root");
            cfg.addDefault("mysql.pw", "yourpw");
            cfg.addDefault("mysql.host", "localhost");
            cfg.addDefault("mysql.port", 3306);
            cfg.addDefault("discord.bot.tokens.rel", "ReleaseTokenhere");
            cfg.addDefault("discord.bot.tokens.dev", "DevTokenhere");
            cfg.addDefault("discord.client.id", 0L);
            cfg.addDefault("discord.client.secret", "yourDiscordClientSecrethere");
            cfg.addDefault("raygun.apitoken", "yourRaygunApiToken");

            try {
                cfg.save(getFile());
            } catch (Exception ignored) {
            }

        }
    }

    public FileConfiguration getConfig() {
        return YamlConfiguration.loadConfiguration(getFile());
    }

    public File getFile() {
        return new File("config.yml");
    }

}
