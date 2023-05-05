package de.presti.ree6.backend.utils.data;

import de.presti.ree6.backend.Server;

/**
 * Utility class to save long term used Data.
 */
public class Data {

    // Completely remove this, and migrate it into the main class.

    /**
     * Constructor for the Data Utility class.
     */
    private Data() {
        throw new IllegalStateException("Utility class");
    }

    public static String getHostname() {
        return Server.getInstance().getConfig().getConfiguration().getString("webinterface.hostname", "cp.ree6.de");
    }

    public static String getHostUrl() {
        boolean useSSL = Server.getInstance().getConfig().getConfiguration().getBoolean("webinterface.usingSSL", true);
        return (useSSL ? "https://" : "http://") + getHostname();
    }

    public static String getDiscordRedirectUrl() {
        return Server.getInstance().getConfig().getConfiguration().getString("webinterface.discordRedirectUrl", "https://cp.ree6.de/auth/discord/callback");
    }

    public static String getTwitchRedirectUrl() {
        return Server.getInstance().getConfig().getConfiguration().getString("webinterface.twitchRedirect", "https://cp.ree6.de/auth/twitch/callback");
    }

    // Current Domain of the Website.
    public static final String WEBSITE = "https://ree6.de";

    // Advertisement, because of Sponsors.
    public static final String ADVERTISEMENT = "powered by Tube-Hosting";

    public static final String defaultIconUrl = "https://i0.wp.com/www.alphr.com/wp-content/uploads/2019/02/Discord-Spoiler-Tag-Featured.jpg?resize=1200%2C1080&ssl=1";
}

