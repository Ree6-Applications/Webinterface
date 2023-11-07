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

    public static String getDiscordRedirectUrl() {
        return Server.getInstance().getConfig().getConfiguration().getString("webinterface.discordRedirect", "https://cp.ree6.de/auth/discord/callback");
    }

    public static String getTwitchRedirectUrl() {
        return Server.getInstance().getConfig().getConfiguration().getString("webinterface.twitchRedirect", "https://cp.ree6.de/auth/twitch/callback");
    }

    public static String getErrorRedirectUrl() {
        return Server.getInstance().getConfig().getConfiguration().getString("webinterface.errorRedirect", "https://cp.ree6.de/error");
    }

    public static String getLoginRedirectUrl() {
        return Server.getInstance().getConfig().getConfiguration().getString("webinterface.loginRedirect", "https://cp.ree6.de/login");
    }

    public static String getAllowedDomains() {
        return Server.getInstance().getConfig().getConfiguration().getString("webinterface.allowedDomains", "https://*.ree6.de,http://localhost:[5173,8888]");
    }

    public static int getLeaderboardTop() {
        return Server.getInstance().getConfig().getConfiguration().getInt("customization.leaderboardTop", 5);
    }

    public static final String defaultIconUrl = "https://i0.wp.com/www.alphr.com/wp-content/uploads/2019/02/Discord-Spoiler-Tag-Featured.jpg?resize=1200%2C1080&ssl=1";
}

