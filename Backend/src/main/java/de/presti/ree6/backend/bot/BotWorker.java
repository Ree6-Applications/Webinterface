package de.presti.ree6.backend.bot;

import de.presti.ree6.backend.Server;
import de.presti.ree6.backend.bot.version.BotState;
import de.presti.ree6.backend.bot.version.BotVersion;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

/**
 * Class to store information about the bot.
 */
@Getter
@Setter
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
     * @param version1    the current Bot Version "typ".
     * @param build1      the current Bot Version.
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
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
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
}
