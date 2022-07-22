package de.presti.ree6.webinterface.bot;

import de.presti.ree6.webinterface.Server;
import de.presti.ree6.webinterface.bot.version.BotState;
import de.presti.ree6.webinterface.bot.version.BotVersion;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Class to store information about the bot.
 */
public class BotWorker {

    /**
     * Constructor should not be called, since it is a utility class that doesn't need an instance.
     *
     * @throws IllegalStateException it is a utility class.
     */
    private BotWorker() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Current Bot state.
     */
    private static BotState state;

    /**
     * Current Bot version.
     */
    private static BotVersion version;

    /**
     * Current {@link ShardManager}.
     */
    private static ShardManager shardManager;

    /**
     * Current Bot-Token.
     */
    private static String token;

    /**
     * Current Bot build.
     */
    private static String build;

    /**
     * Bot start time.
     */
    private static long startTime;

    /**
     * Create a new {@link net.dv8tion.jda.api.JDA} instance and set the rest information for later use.
     *
     * @param version1 the current Bot Version "typ".
     * @param build1   the current Bot Version.
     * @throws LoginException when there is a problem with creating the Session.
     */
    public static void createBot(BotVersion version1, String build1) throws LoginException {
        version = version1;
        token = BotWorker.version == BotVersion.DEVELOPMENT_BUILD ? Server.getInstance().getConfig().getConfiguration().getString("discord.bot.tokens.dev") : Server.getInstance().getConfig().getConfiguration().getString("discord.bot.tokens.rel");
        state = BotState.INIT;
        build = build1;

        shardManager = DefaultShardManagerBuilder.createDefault(token).setShardsTotal(getVersion() == BotVersion.DEVELOPMENT_BUILD ? 1 : 10)
                .enableIntents(GatewayIntent.GUILD_INVITES, GatewayIntent.GUILD_INVITES, GatewayIntent.GUILD_WEBHOOKS,
                GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_BANS).setMemberCachePolicy(MemberCachePolicy.ALL)
                .disableCache(CacheFlag.EMOJI, CacheFlag.ACTIVITY).build();
    }

    /**
     * Change the current Activity of the Bot.
     *
     * @param message      the Message of the Activity.
     * @param activityType the Activity type.
     */
    public static void setActivity(String message, Activity.ActivityType activityType) {
        // If the Bot Instance is null, if not set.
        if (shardManager != null)
            shardManager.setActivity(Activity.of(activityType, message.replace("%shards%", shardManager.getShardsTotal() + "").replace("%guilds%", shardManager.getGuilds().size() + "")));
    }

    /**
     * Change the current Activity of the Bot.
     *
     * @param message      the Message of the Activity.
     * @param activityType the Activity type.
     */
    public static void setActivity(JDA jda, String message, Activity.ActivityType activityType) {
        // If the Bot Instance is null, if not set.
        if (jda != null)
            jda.getPresence().setActivity(Activity.of(activityType, message.replace("%shards%", shardManager.getShardsTotal() + "")
                    .replace("%shard%", "" + jda.getShardInfo().getShardId()).replace("%guilds%", shardManager.getGuilds().size() + "").replace("%shard_guilds%", jda.getGuilds().size() + "")));
    }

    /**
     * Called when the Bot should Shut down.
     */
    public static void shutdown() {
        // Check if the Instance of null if not, shutdown.
        if (shardManager != null) {
            shardManager.shutdown();
        }
    }

    /**
     * Called to add a ListenerAdapter to the EventListener.
     *
     * @param listenerAdapter the Listener Adapter that should be added.
     */
    public static void addEvent(ListenerAdapter listenerAdapter) {
        shardManager.addEventListener(listenerAdapter);
    }

    /**
     * Called to get a random Embed supported Color.
     *
     * @return a {@link Color}.
     */
    public static Color randomEmbedColor() {
        String zeros = "000000";
        String s = Integer.toString(ThreadLocalRandom.current().nextInt(0X1000000), 16);
        s = zeros.substring(s.length()) + s;
        return Color.decode("#" + s);
    }

    /**
     * Change the current Bot State.
     * @param botState the new {@link BotState}
     */
    public static void setState(BotState botState) {
        state = botState;
    }

    /**
     * Get the current Bot State.
     * @return the {@link BotState}.
     */
    public static BotState getState() {
        return state;
    }

    /**
     * Get the current Bot Version.
     * @return the {@link BotVersion}
     */
    public static BotVersion getVersion() {
        return version;
    }

    /**
     * Get the ShardManager of Ree6.
     * @return the {@link ShardManager}
     */
    public static ShardManager getShardManager() {
        return shardManager;
    }

    /**
     * Get the build / the actual version in the x.y.z format.
     * @return the Build.
     */
    public static String getBuild() {
        return build;
    }

    /**
     * Get the Bot Token.
     * @return the Token.
     */
    public static String getToken() {
        return token;
    }

    /**
     * Set the start Time of the Bot.
     * @param startTime1 the new start Time.
     */
    public static void setStartTime(long startTime1) {
        startTime = startTime1;
    }

    /**
     * Get the start Time of the Bot.
     * @return the start Time.
     */
    public static long getStartTime() {
        return startTime;
    }
}
