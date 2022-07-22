package de.presti.ree6.webinterface.utils.data;

import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;

/**
 * Config.
 */
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
            yamlFile.addDefault("mysql.user", "root");
            yamlFile.addDefault("mysql.db", "root");
            yamlFile.addDefault("mysql.pw", "yourpw");
            yamlFile.addDefault("mysql.host", "localhost");
            yamlFile.addDefault("mysql.port", 3306);
            yamlFile.addDefault("raygun.apitoken", "yourrayguntokenherepog");
            yamlFile.addDefault("discord.bot.tokens.rel", "ReleaseTokenhere");
            yamlFile.addDefault("discord.bot.tokens.dev", "DevTokenhere");
            yamlFile.addDefault("discord.client.id", 0);
            yamlFile.addDefault("discord.client.secret", "yourDiscordClientSecrethere");

            try {
                yamlFile.save(getFile());
            } catch (Exception ignored) {
            }
        } else {
            try {
                yamlFile.load();
            } catch (Exception ignored) {
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
