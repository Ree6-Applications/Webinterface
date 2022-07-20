package de.presti.ree6.webinterface.sql.entities.level;

import de.presti.ree6.webinterface.Server;
import de.presti.ree6.webinterface.sql.base.annotations.Table;

/**
 * Utility class to store information about a Users
 * Experience and their Level.
 */
@Table(name = "Level")
public class ChatUserLevel extends UserLevel {

    /**
     * Constructor.
     */
    public ChatUserLevel() {
    }


    /**
     * Constructor to create a UserLevel with the needed Data.
     *
     * @param guildId    the ID of the Guild.
     * @param userId     the ID of the User.
     * @param experience his XP count.
     */
    public ChatUserLevel(String guildId, String userId, long experience) {
        super(guildId, userId, experience, Server.getInstance().getSqlConnector().getSqlWorker().getAllChatLevelSorted(guildId).indexOf(userId));
    }

    /**
     * Constructor to create a UserLevel with the needed Data.
     *
     * @param guildId    the ID of the Guild.
     * @param userId     the ID of the User.
     * @param experience his XP count.
     * @param level      his Level.
     */
    public ChatUserLevel(String guildId, String userId, long experience, long level) {
        super(guildId, userId, experience, level, Server.getInstance().getSqlConnector().getSqlWorker().getAllChatLevelSorted(guildId).indexOf(userId));
    }

    /**
     * @inheritDoc
     */
    @Override
    public long getExperienceForLevel(long level) {
        return (long) (1000 + (1000 * Math.pow(level, 0.55)));
    }

}
