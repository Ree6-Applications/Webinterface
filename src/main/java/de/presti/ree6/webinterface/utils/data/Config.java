package de.presti.ree6.webinterface.utils.data;

import lombok.extern.slf4j.Slf4j;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.util.Map;

/**
 * Config.
 */
@Slf4j
public class Config {

    /**
     * The Configuration.
     */
    private YamlFile yamlFile;

    /**
     * Initialize the Configuration.
     */
    public void init() {

        yamlFile = createConfiguration();

        if (!getFile().exists()) {
            yamlFile.options().copyHeader();
            yamlFile.options().copyDefaults();
            yamlFile.options().header("""
                    ################################
                    #                              #
                    # Ree6 Config File             #
                    # by Presti                    #
                    #                              #
                    ################################
                    """);
            yamlFile.addDefault("config.version", "3.0.6");
            yamlFile.addDefault("config.creation", System.currentTimeMillis());
            yamlFile.addDefault("hikari.sql.user", "root");
            yamlFile.addDefault("hikari.sql.db", "root");
            yamlFile.addDefault("hikari.sql.pw", "yourpw");
            yamlFile.addDefault("hikari.sql.host", "localhost");
            yamlFile.addDefault("hikari.sql.port", 3306);
            yamlFile.addDefault("hikari.misc.storage", "sqlite");
            yamlFile.addDefault("hikari.misc.storageFile", "storage/Ree6.db");
            yamlFile.addDefault("hikari.misc.poolSize", 10);
            yamlFile.addDefault("twitch.client.id", "yourtwitchclientidhere");
            yamlFile.addDefault("twitch.client.secret", "yourtwitchclientsecrethere");
            yamlFile.addDefault("discord.bot.tokens.release", "ReleaseTokenhere");
            yamlFile.addDefault("discord.bot.tokens.dev", "DevTokenhere");
            yamlFile.addDefault("discord.bot.tokens.beta", "BetaTokenhere");
            yamlFile.addDefault("discord.client.id", 0);
            yamlFile.addDefault("discord.client.secret", "yourDiscordClientSecrethere");

            try {
                yamlFile.save(getFile());
            } catch (Exception ignored) {
            }
        } else {
            try {
                yamlFile.load();
                migrateOldConfig();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Migrate from 3.0.2 config to 3.0.3 config.
     */
    public void migrateOldConfig() {
        if (yamlFile.getString("config.version") == null) {
            Map<String, Object> resources = yamlFile.getValues(true);
            if (getFile().delete()) {
                init();

                for (Map.Entry<String, Object> entry : resources.entrySet()) {
                    String key = entry.getKey();

                    if (key.startsWith("mysql"))
                        key = key.replace("mysql", "hikari.sql");

                    if (key.startsWith("discord") && key.endsWith("rel"))
                        key = key.replace("rel", "release");

                    if (key.startsWith("raygun"))
                        continue;

                    yamlFile.set(key, entry.getValue());
                }

                try {
                    yamlFile.save(getFile());
                } catch (Exception exception) {
                    log.error("Could not save config file!", exception);
                }
            }
        }
    }

    /**
     * Create a new Configuration.
     * @return The Configuration as {@link YamlFile}.
     */
    public YamlFile createConfiguration() {
        try {
            return new YamlFile(getFile());
        } catch (Exception e) {
            return new YamlFile();
        }
    }

    /**
     * Get the Configuration.
     * @return The Configuration as {@link YamlFile}.
     */
    public YamlFile getConfiguration() {
        if (yamlFile == null) {
            init();
        }

        return yamlFile;
    }

    /**
     * Get the Configuration File.
     * @return The Configuration File as {@link File}.
     */
    public File getFile() {
        return new File("config.yml");
    }

}
