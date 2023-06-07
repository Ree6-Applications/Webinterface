package de.presti.ree6.backend.utils.data;

import lombok.extern.slf4j.Slf4j;
import org.simpleyaml.configuration.MemorySection;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

        Path storage = Path.of("storage/");
        if (Files.notExists(storage)) {
            try {
                Files.createDirectory(storage);
            } catch (IOException e) {
                log.error("Could not create storage", e);
            }
        }

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
            yamlFile.path("config")
                    .comment("Do not change this!")
                    .path("version").addDefault("4.0.0")
                    .parent().path("creation").addDefault(System.currentTimeMillis());

            yamlFile.path("hikari")
                    .comment("HikariCP Configuration").blankLine()
                    .path("sql").comment("SQL Configuration").blankLine()
                    .path("user").addDefault("root")
                    .parent().path("db").addDefault("root")
                    .parent().path("pw").addDefault("yourpw")
                    .parent().path("host").addDefault("localhost")
                    .parent().path("port").addDefault(3306)
                    .parent().parent().path("misc").comment("Misc Configuration").blankLine()
                    .path("storage").addDefault("sqlite").commentSide("Possible entries: sqlite, mariadb, postgresql, h2, h2-server")
                    .parent().path("storageFile").addDefault("storage/Ree6.db")
                    .parent().path("createEmbeddedServer").addDefault(false).commentSide("Should an instance of an embedded Server be created? Only used for H2-Server.")
                    .parent().path("poolSize").addDefault(10);

            yamlFile.path("twitch")
                    .comment("Twitch Application Configuration, used for the StreamTools and Twitch Notifications.").blankLine()
                    .path("client").path("id").addDefault("yourtwitchclientidhere")
                    .parent().path("secret").addDefault("yourtwitchclientsecrethere");

            yamlFile.path("discord").comment("Discord Application Configuration, used for OAuth and Bot Authentication.").blankLine()
                    .path("bot").comment("Bot Configuration").blankLine()
                    .path("tokens").path("release").addDefault("ReleaseTokenhere").commentSide("Token used when set to release build.")
                    .parent().path("beta").addDefault("BetaTokenhere").commentSide("Token used when set to beta build.")
                    .parent().path("dev").addDefault("DevTokenhere").commentSide("Token used when set to dev build.")
                    .parent().parent().parent()
                    .path("client").comment("OAuth Configuration").blankLine()
                    .path("id").addDefault(0).commentSide("Client ID of the Discord Application.")
                    .parent().path("secret").addDefault("yourDiscordClientSecrethere").commentSide("Client Secret of the Discord Application.")
                    .parent().path("shards").addDefault(1).commentSide("The shard amount of the Bot. Check out https://anidiots.guide/understanding/sharding/#sharding for more information.");

            yamlFile.path("webinterface").comment("Basic Configurations for the Webinterface").blankLine()
                    .path("discordRedirect").addDefault("https://cp.ree6.de/login").commentSide("Redirect URL for Discord OAuth.")
                    .parent().path("twitchRedirect").addDefault("https://cp.ree6.de/twitch").commentSide("Redirect URL for Twitch OAuth.")
                    .parent().path("errorRedirect").addDefault("https://cp.ree6.de/error").commentSide("Redirect URL for errors.")
                    .parent().path("loginRedirect").addDefault("https://cp.ree6.de/login").commentSide("Redirect URL if the user is not logged-in.")
                    .parent().path("allowedDomains").addDefault("https://*.ree6.de,http://localhost:[5173,8888]").commentSide("""
                            Domains that are allowed to request the backend.
                            We recommend doing *.HOST.TLD, and then putting the backend behind something like API.HOST.TLD!""");

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
     * Migrate configs to newer versions
     */
    public void migrateOldConfig() {
        String configVersion = yamlFile.getString("config.version", "4.0.0");

        if (compareVersion(configVersion, "4.0.0") || configVersion.equals("4.0.0"))
            return;

        Map<String, Object> resources = yamlFile.getValues(true);

        try {
            Files.copy(getFile().toPath(), new File("config-old.yml").toPath());
        } catch (Exception ignore) {
            log.warn("Could not move the old configuration file to config.yml!");
            log.warn("This means the config file is not backed up by us!");
        }

        // Migrate configs
        if (getFile().delete()) {
            init();

            for (Map.Entry<String, Object> entry : resources.entrySet()) {
                String key = entry.getKey();

                boolean modified = false;

                if (key.startsWith("config"))
                    continue;

                if (entry.getValue() instanceof MemorySection)
                    continue;

                // Migrate to 3.0.6
                if (compareVersion("3.0.6", configVersion)) {

                    if (key.startsWith("raygun"))
                        continue;

                    if (key.startsWith("mysql"))
                        key = key.replace("mysql", "hikari.sql");

                    if (key.startsWith("discord") && key.endsWith("rel"))
                        key = key.replace("rel", "release");


                    yamlFile.set(key, entry.getValue());
                    modified = true;
                }

                if (!modified) {
                    yamlFile.set(key, entry.getValue());
                }
            }

            try {
                yamlFile.save(getFile());
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    /**
     * Compare two version that are based on the x.y.z format.
     *
     * @param versionA the base version.
     * @param versionB the version that should be tested against versionA.
     * @return True if versionA is above versionB.
     */
    public boolean compareVersion(String versionA, String versionB) {
        if (versionA == null) return false;
        if (versionB == null) return true;

        String[] split = versionA.split("\\.");

        int mayor = Integer.parseInt(split[0]);
        int minor = Integer.parseInt(split[1]);
        int patch = Integer.parseInt(split[2]);

        String[] split2 = versionB.split("\\.");
        int otherMayor = Integer.parseInt(split2[0]);
        int otherMinor = Integer.parseInt(split2[1]);
        int otherPatch = Integer.parseInt(split2[2]);

        if (mayor > otherMayor) return true;
        if (mayor == otherMayor && minor > otherMinor) return true;
        return mayor == otherMayor && minor == otherMinor && patch > otherPatch;
    }

    /**
     * Create a new Configuration.
     *
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
     *
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
     *
     * @return The Configuration File as {@link File}.
     */
    public File getFile() {
        return new File("config.yml");
    }

}
