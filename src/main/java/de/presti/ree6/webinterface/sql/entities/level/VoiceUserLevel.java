package de.presti.ree6.webinterface.sql.entities.level;

import de.presti.ree6.webinterface.Server;
import de.presti.ree6.webinterface.sql.base.annotations.Table;

/**
 * Utility class to store information about a Users
 * Experience and their Level.
 */
@Table(name = "VCLevel")
public class VoiceUserLevel extends UserLevel {

    /**
     * Constructor.
     */
    public VoiceUserLevel() {
    }

    /**
     * Constructor to create a UserLevel with the needed Data.
     *
     * @param guildId    the ID of the Guild.
     * @param userId     the ID of the User.
     * @param experience his XP count.
     */
    public VoiceUserLevel(String guildId, String userId, long experience) {
        super(guildId, userId, experience, Server.getInstance().getSqlConnector().getSqlWorker().getAllVoiceLevelSorted(guildId).indexOf(userId));
    }


    /**
     * Constructor to create a UserLevel with the needed Data.
     *
     * @param guildId    the ID of the Guild.
     * @param userId     the ID of the User.
     * @param experience his XP count.
     * @param level      his Level.
     */
    public VoiceUserLevel(String guildId, String userId, long experience, long level) {
        super(guildId, userId, experience, level, Server.getInstance().getSqlConnector().getSqlWorker().getAllVoiceLevelSorted(guildId).indexOf(userId));
    }

    /**
     * @inheritDoc
     */
    @Override
    public long getExperienceForLevel(long level) {
        return (long) (1000 + (1000 * Math.pow(level, 1.05)));
    }
}
