package de.presti.ree6.webinterface.controller;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.webinterface.bot.BotWorker;
import de.presti.ree6.sql.entities.level.UserLevel;
import de.presti.ree6.sql.entities.stats.CommandStats;
import de.presti.ree6.sql.entities.stats.GuildCommandStats;
import net.dv8tion.jda.api.entities.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Used as an BackendController for API Request for Ree6.
 */
@RestController
public class BackendController {

    //region API 1.0

    //region Level API

    /**
     * Request mapper for a getLeaderboard Request.
     * @param guildId the GuildID of the wanted Guild.
     * @param count count of the max users in the response.
     * @return {@link String} a stringified JsonObject.
     */
    @GetMapping(value = "/api/v1/level/leaderboard", produces = "application/json")
    public String getLeaderboard(@RequestParam(name = "guildId") String guildId, @RequestParam int count) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("count", count);

        JsonArray chatLeaderboardArray = new JsonArray();

        JsonArray voiceLeaderboardArray = new JsonArray();

        for (UserLevel userLevel : SQLSession.getSqlConnector().getSqlWorker().getTopChat(guildId, count)) {
            JsonObject userObject = new JsonObject();

            User user = BotWorker.getShardManager().getUserById(userLevel.getUserId());

            if (user == null) continue;

            userObject.addProperty("user.name", user.getName());
            userObject.addProperty("user.tag", user.getAsTag());

            long xp = userLevel.getExperience();

            userObject.addProperty("xp",
                    xp);

            int level = 1;

            while (xp > 1000) {
                xp -= 1000;
                level++;
            }

            userObject.addProperty("level", level);

            chatLeaderboardArray.add(userObject);
        }

        for (UserLevel userLevel : SQLSession.getSqlConnector().getSqlWorker().getTopVoice(guildId, count)) {
            JsonObject userObject = new JsonObject();

            User user = BotWorker.getShardManager().getUserById(userLevel.getUserId());

            if (user == null) continue;

            userObject.addProperty("user.name", user.getName());
            userObject.addProperty("user.tag", user.getAsTag());

            long xp = userLevel.getExperience();

            userObject.addProperty("xp",
                    xp);

            int level = 1;

            while (xp > 1000) {
                xp -= 1000;
                level++;
            }

            userObject.addProperty("level", level);

            voiceLeaderboardArray.add(userObject);
        }

        jsonObject.add("list.chat", chatLeaderboardArray);
        jsonObject.add("list.voice", voiceLeaderboardArray);

        return new GsonBuilder().setPrettyPrinting().create().toJson(jsonObject);
    }

    /**
     * Request mapper for a User Guild Level Request.
     * @param guildId the GuildID of the wanted Guild.
     * @param userID the UserID of the wanted User.
     * @return {@link String} a stringified JsonObject.
     */
    @GetMapping(value = "/api/v1/level/member", produces = "application/json")
    public String getLeaderboard(@RequestParam(name = "guildId") String guildId, @RequestParam String userID) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("userId", userID);

        JsonObject voiceJsonObject = new JsonObject();

        long xp = SQLSession.getSqlConnector().getSqlWorker().getVoiceLevelData(guildId, userID).getExperience();

        int level = 1;

        while (xp > 1000) {
            xp -= 1000;
            level++;
        }

        voiceJsonObject.addProperty("voice.xp", xp);
        voiceJsonObject.addProperty("voice.level", level);
        voiceJsonObject.addProperty("voice.rank", 0L);

        JsonObject chatJsonObject = new JsonObject();

        xp = SQLSession.getSqlConnector().getSqlWorker().getChatLevelData(guildId, userID).getExperience();

        level = 1;

        while (xp > 1000) {
            xp -= 1000;
            level++;
        }

        chatJsonObject.addProperty("chat.xp", xp);
        chatJsonObject.addProperty("chat.level", level);
        chatJsonObject.addProperty("chat.rank", 0L);

        jsonObject.add("chat", chatJsonObject);
        jsonObject.add("voice", voiceJsonObject);

        return new GsonBuilder().setPrettyPrinting().create().toJson(jsonObject);
    }
    //endregion

    //region Stats

    //region Global

    /**
     * Request mapper for a Command Stats Request.
     * @param command the Name of the Command.
     * @return {@link String} a stringified JsonObject.
     */
    @GetMapping(value = "/api/v1/stats/command", produces = "application/json")
    public String getStatsCommand(@RequestParam String command) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("command", command);
        jsonObject.addProperty("usage", SQLSession.getSqlConnector().getSqlWorker().getStatsCommandGlobal(command).getUses());

        return new GsonBuilder().setPrettyPrinting().create().toJson(jsonObject);
    }

    /**
     * Request mapper for a Command Stats Request.
     * @return {@link String} a stringified JsonObject.
     */
    @GetMapping(value = "/api/v1/stats/all", produces = "application/json")
    public String getStatsGlobal() {
        JsonObject jsonObject = new JsonObject();

        for (CommandStats entry : SQLSession.getSqlConnector().getSqlWorker().getStatsGlobal()) {
            jsonObject.addProperty(entry.getCommand(), entry.getUses());
        }

        return new GsonBuilder().setPrettyPrinting().create().toJson(jsonObject);
    }

    //endregion

    //region Guild.

    /**
     * Request mapper for a Command Guild Stats Request.
     * @param guildId the GuildID of the wanted Guild.
     * @param command the Name of the Command.
     * @return {@link String} a stringified JsonObject.
     */
    @GetMapping(value = "/api/v1/stats/guild/command", produces = "application/json")
    public String getStatsGuildCommand(@RequestParam(name = "guildId") String guildId, @RequestParam String command) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("command", command);
        jsonObject.addProperty("usage", SQLSession.getSqlConnector().getSqlWorker().getStatsCommand(guildId, command).getUses());

        return new GsonBuilder().setPrettyPrinting().create().toJson(jsonObject);
    }

    /**
     * Request mapper for a Guild Stats Request.
     * @param guildId the GuildID of the wanted Guild.
     * @return {@link String} a stringified JsonObject.
     */
    @GetMapping(value = "/api/v1/stats/guild/all", produces = "application/json")
    public String getStatsGuildAll(@RequestParam(name = "guildId") String guildId) {
        JsonObject jsonObject = new JsonObject();

        for (GuildCommandStats entry : SQLSession.getSqlConnector().getSqlWorker().getStats(guildId)) {
            jsonObject.addProperty(entry.getCommand(), entry.getUses());
        }

        return new GsonBuilder().setPrettyPrinting().create().toJson(jsonObject);
    }

    //endregion

    //endregion

    //endregion
}
