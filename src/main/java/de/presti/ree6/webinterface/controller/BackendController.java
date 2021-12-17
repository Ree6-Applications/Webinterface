package de.presti.ree6.webinterface.controller;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Used as an BackendController for API Request for Ree6.
 */
@RestController
public class BackendController {

    //TODO add more endpoints and make them work.

    //region API 1.0

    //region Level API

    /**
     * Request mapper for a getLeaderboard Request.
     * @param guildId the GuildID of the wanted Guild.
     * @param count count of the max users in the response.
     * @return {@link String} a stringified JsonObject.
     */
    @RequestMapping(value = "/api/v1/level/leaderboard", produces = "application/json", method = RequestMethod.GET)
    public String getLeaderboard(@RequestParam String guildId, @RequestParam int count) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("count", count);

        JsonArray jsonArray = new JsonArray();

        jsonObject.add("list", jsonArray);

        return new GsonBuilder().setPrettyPrinting().create().toJson(jsonObject);
    }

    /**
     * Request mapper for a User Guild Level Request.
     * @param guildID the GuildID of the wanted Guild.
     * @param userID the UserID of the wanted User.
     * @return {@link String} a stringified JsonObject.
     */
    @RequestMapping(value = "/api/v1/level/member", produces = "application/json", method = RequestMethod.GET)
    public String getLeaderboard(@RequestParam String guildID, @RequestParam String userID) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("userId", userID);

        JsonObject voiceJsonObject = new JsonObject();

        voiceJsonObject.addProperty("voice.xp", 0L);
        voiceJsonObject.addProperty("voice.level", 0L);
        voiceJsonObject.addProperty("voice.rank", 0L);

        JsonObject chatJsonObject = new JsonObject();

        chatJsonObject.addProperty("chat.xp", 0L);
        chatJsonObject.addProperty("chat.level", 0L);
        chatJsonObject.addProperty("chat.rank", 0L);

        jsonObject.add("chat", chatJsonObject);
        jsonObject.add("voice", voiceJsonObject);

        return new GsonBuilder().setPrettyPrinting().create().toJson(jsonObject);
    }
    //endregion

    //endregion
}
