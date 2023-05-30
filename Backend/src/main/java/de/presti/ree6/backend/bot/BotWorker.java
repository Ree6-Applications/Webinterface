package de.presti.ree6.backend.bot;

import de.presti.ree6.backend.Server;
import de.presti.ree6.backend.bot.version.BotState;
import de.presti.ree6.backend.bot.version.BotVersion;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

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
     * Create a new {@link net.dv8tion.jda.api.sharding.ShardManager} instance and set the rest information for later use.
     *
     * @param version1 the current Bot Version "typ".
     * @param build1   the current Bot Version.
     * @param shardAmount the amount of shards to use.
     */
    public static void createBot(BotVersion version1, String build1, int shardAmount) {
        version = version1;
        token = Server.getInstance().getConfig().getConfiguration().getString(getVersion().getTokenPath());
        state = BotState.INIT;
        build = build1;

        shardManager = DefaultShardManagerBuilder
                .createDefault(token)
                .setShardsTotal(shardAmount)
                .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_INVITES, GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.GUILD_INVITES, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_WEBHOOKS, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_BANS)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .disableCache(CacheFlag.EMOJI, CacheFlag.ACTIVITY)
                .build();
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
     * @param listenerAdapters the Listener Adapter(s) that should be added.
     */
    public static void addEvent(ListenerAdapter... listenerAdapters) {
        for (ListenerAdapter listenerAdapter : listenerAdapters) {
            shardManager.addEventListener(listenerAdapter);
        }
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
     *
     * @param botState the new {@link BotState}
     */
    public static void setState(BotState botState) {
        state = botState;
    }

    /**
     * Get the current Bot State.
     *
     * @return the {@link BotState}.
     */
    public static BotState getState() {
        return state;
    }

    /**
     * Get the current Bot Version.
     *
     * @return the {@link BotVersion}
     */
    public static BotVersion getVersion() {
        if (version == null) return BotVersion.RELEASE;
        return version;
    }

    /**
     * Get the ShardManager of Ree6.
     *
     * @return the {@link ShardManager}
     */
    public static ShardManager getShardManager() {
        return shardManager;
    }

    /**
     * Get the build / the actual version in the x.y.z format.
     *
     * @return the Build.
     */
    public static String getBuild() {
        return build;
    }

    /**
     * Set the start Time of the Bot.
     *
     * @param startTime1 the new start Time.
     */
    public static void setStartTime(long startTime1) {
        startTime = startTime1;
    }

    /**
     * Get the start Time of the Bot.
     *
     * @return the start Time.
     */
    public static long getStartTime() {
        return startTime;
    }

    /**
     * Get the current Bot Token.
     *
     * @return the Bot Token.
     */
    public static String getToken() {
        return token;
    }
}
