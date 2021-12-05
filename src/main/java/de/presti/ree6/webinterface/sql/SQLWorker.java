package de.presti.ree6.webinterface.sql;

import de.presti.ree6.webinterface.bot.BotInfo;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * A Class to actually handle the SQL data.
 * Used to provide Data from the Database and to save Data into the Database.
 */
public class SQLWorker {

    // Instance of the SQL Connector to actually access the SQL Database.
    private final SQLConnector sqlConnector;

    /**
     * Constructor to create a new Instance of the SQLWorker with a ref to the SQL-Connector.
     * @param sqlConnector an Instance of the SQL-Connector to retrieve the data from.
     */
    public SQLWorker (SQLConnector sqlConnector) {
        this.sqlConnector = sqlConnector;
    }

    //region Level

    //region Chat

    /**
     * Get the Chat XP Count of the give UserID from the given Guild.
     * @param guildId the ID of the Guild.
     * @param userId the ID of the User.
     * @return {@link Long} as XP Count.
     */
    public Long getChatXP(String guildId, String userId) {

        // Creating a SQL Statement to get the User from the Level Table by the GuildID and UserID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM Level WHERE GID='" + guildId + "' AND UID='" + userId + "'").executeQuery()) {

            // Return the XP as Long if found.
            if (rs != null && rs.next()) return Long.parseLong(rs.getString("XP"));
        } catch (Exception ignore) {}

        // Return 0 if there was an error OR if the user isn't in the database.
        return 0L;
    }

    /**
     * Check if the given combination of UserID and GuildID is saved in our Database.
     * @param guildId the ID of the Guild.
     * @param userId the ID of the User.
     * @return {@link Boolean} true if there was a match | false if there wasn't a match.
     */
    public boolean existsInChatXP(String guildId, String userId) {

        // Creating a SQL Statement to get the User from the Level Table by the GuildID and UserID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM Level WHERE GID='" + guildId + "' AND UID='" + userId + "'").executeQuery()) {

            // Return if there was a match.
            return (rs != null && rs.next());
        } catch (Exception ignore) {}

        // Return if there wasn't a match.
        return false;
    }

    /**
     * Give the wanted User more XP.
     * @param guildId the ID of the Guild.
     * @param userId the ID of the User.
     * @param xp the wanted XP.
     */
    public void addChatXP(String guildId, String userId, long xp) {

        // Add the current XP to the new XP.
        xp += getChatXP(guildId, userId);

        // Check if the User is already saved in the Database.
        if (existsInChatXP(guildId, userId)) {

            // If so change the current XP to the new.
            querySQL("UPDATE Level SET XP='" + xp + "' WHERE GID='" + guildId + "' AND UID='" + userId + "'");
        } else {

            // If not create a new entry and add the data.
            querySQL("INSERT INTO Level (GID, UID, XP) VALUES ('" + guildId + "', '" + userId + "', '" + xp + "');");
        }
    }

    /**
     * Get the Top list of the Guild Chat XP.
     * @param guildId the ID of the Guild.
     * @param limit the Limit of how many should be given back.
     * @return {@link ArrayList<String>} as container of the User IDs.
     */
    public ArrayList<String> getTopChat(String guildId, int limit) {

        // Create the List.
        ArrayList<String> userIds = new ArrayList<>();

        // Creating a SQL Statement to get the Entries from the Level Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM Level WHERE GID='" + guildId + "' ORDER BY cast(xp as unsigned) DESC LIMIT " + limit).executeQuery()) {

            // While there are still entries it should add them to the list.
            while (rs != null && rs.next()) {
                userIds.add(rs.getString("UID"));
            }
        } catch (Exception ignore) {}

        // Return the list.
        return userIds;
    }

    //endregion

    //region Voice

    /**
     * Get the Voice XP Count of the give UserID from the given Guild.
     * @param guildId the ID of the Guild.
     * @param userId the ID of the User.
     * @return {@link Long} as XP Count.
     */
    public Long getVoiceXP(String guildId, String userId) {

        // Creating a SQL Statement to get the User from the VCLevel Table by the GuildID and UserID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM VCLevel WHERE GID='" + guildId + "' AND UID='" + userId + "'").executeQuery()) {

            // Return the XP as Long if found.
            if (rs != null && rs.next()) return Long.parseLong(rs.getString("XP"));
        } catch (Exception ignore) {}

        // Return 0 if there was an error OR if the user isn't in the database.
        return 0L;
    }

    /**
     * Check if the given combination of UserID and GuildID is saved in our Database.
     * @param guildId the ID of the Guild.
     * @param userId the ID of the User.
     * @return {@link Boolean} true if there was a match | false if there wasn't a match.
     */
    public boolean existsInVoiceXP(String guildId, String userId) {

        // Creating a SQL Statement to get the User from the VCLevel Table by the GuildID and UserID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM VCLevel WHERE GID='" + guildId + "' AND UID='" + userId + "'").executeQuery()) {

            // Return if there was a match.
            return (rs != null && rs.next());
        } catch (Exception ignore) {}

        // Return if there wasn't a match.
        return false;
    }

    /**
     * Give the wanted User more XP.
     * @param guildId the ID of the Guild.
     * @param userId the ID of the User.
     * @param xp the wanted XP.
     */
    public void addVoiceXP(String guildId, String userId, long xp) {

        // Add the current XP to the new XP.
        xp += getChatXP(guildId, userId);

        // Check if the User is already saved in the Database.
        if (existsInChatXP(guildId, userId)) {

            // If so change the current XP to the new.
            querySQL("UPDATE VCLevel SET XP='" + xp + "' WHERE GID='" + guildId + "' AND UID='" + userId + "'");
        } else {

            // If not create a new entry and add the data.
            querySQL("INSERT INTO VCLevel (GID, UID, XP) VALUES ('" + guildId + "', '" + userId + "', '" + xp + "');");
        }
    }

    /**
     * Get the Top list of the Guild Voice XP.
     * @param guildId the ID of the Guild.
     * @param limit the Limit of how many should be given back.
     * @return {@link ArrayList<String>} as container of the User IDs.
     */
    public ArrayList<String> getTopVoice(String guildId, int limit) {

        // Create the List.
        ArrayList<String> userIds = new ArrayList<>();

        // Creating a SQL Statement to get the Entries from the VCLevel Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM VCLevel WHERE GID='" + guildId + "' ORDER BY cast(xp as unsigned) DESC LIMIT " + limit).executeQuery()) {

            // While there are still entries it should add them to the list.
            while (rs != null && rs.next()) {
                userIds.add(rs.getString("UID"));
            }
        } catch (Exception ignore) {}

        // Return the list.
        return userIds;
    }

    //endregion

    //endregion

    //region Webhooks

    //region Logs

    /**
     * Get the LogWebhook data.
     * @param guildId the ID of the Guild.
     * @return {@link String[]} in the first index is the Webhook ID and in the second the Auth-Token.
     */
    public String[] getLogWebhook(String guildId) {
        if (isLogSetup(guildId)) {
            // Creating a SQL Statement to get the Entry from the LogWebhooks Table by the GuildID.
            try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM LogWebhooks WHERE GID='" + guildId + "'").executeQuery()) {

                // Return if there was a match.
                if (rs != null && rs.next()) {
                    if (rs.getString("CID").isEmpty() || rs.getString("TOKEN").isEmpty())
                        return new String[] { "0", "Not setuped!"};
                    else
                        return new String[] { rs.getString("CID"), rs.getString("TOKEN") };
                }
            } catch (Exception ignore) {}
        }

        return new String[] { "0", "Not setuped!"};
    }

    /**
     * Set the LogWebhook in our Database.
     * @param guildId the ID of the Guild.
     * @param webhookId the ID of the Webhook.
     * @param authToken the Auth-token to verify the access.
     */
    public void setLogWebhook(String guildId, String webhookId, String authToken) {

        // Check if there is already a Webhook set.
        if (isLogSetup(guildId)) {

            // Delete the existing Webhook.
            BotInfo.botInstance.getGuildById(guildId).retrieveWebhooks().queue(webhooks -> webhooks.stream().filter(webhook ->
                    webhook.getId().equalsIgnoreCase(getLogWebhook(guildId)[0]) && webhook.getToken().equalsIgnoreCase(getLogWebhook(guildId)[1]))
                    .forEach(webhook -> webhook.delete().queue()));

            // Delete the entry.
            querySQL("DELETE FROM LogWebhooks WHERE GID='" + guildId + "'");
        }

        // Add a new entry into the Database.
        querySQL("INSERT INTO LogWebhooks (GID, CID, TOKEN) VALUES ('" + guildId + "', '" + webhookId + "', '" + authToken + "');");

    }

    /**
     * Check if the Log Webhook has been set in our Database for this Server.
     * @param guildId the ID of the Guild.
     * @return {@link Boolean} if true, it has been set | if false, it hasn't been set.
     */
    public boolean isLogSetup(String guildId) {

        // Creating a SQL Statement to get the Entry from the LogWebhooks Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM LogWebhooks WHERE GID='" + guildId + "'").executeQuery()) {

            // Return if there was a match.
            return (rs != null && rs.next());
        } catch (Exception ignore) {}

        // Return if there wasn't a match.
        return false;
    }

    /**
     * Check if the Log Webhook data is in our Database.
     * @param webhookId the ID of the Webhook.
     * @param authToken the Auth-Token of the Webhook.
     * @return {@link Boolean} if true, it has been set | if false, it hasn't been set.
     */
    public boolean existsLogData(long webhookId, String authToken) {

        // Creating a SQL Statement to get the Entry from the LogWebhooks Table by the WebhookID and its Auth-Token.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM LogWebhooks WHERE CID='" + webhookId + "' AND TOKEN='" + authToken + "'").executeQuery()) {

            // Return if there was a match.
            return (rs != null && rs.next());
        } catch (Exception ignore) {}

        // Return if there wasn't a match.
        return false;
    }

    /**
     * Set the LogWebhook in our Database.
     * @param webhookId the ID of the Webhook.
     * @param authToken the Auth-Token of the Webhook.
     */
    public void deleteLogWebhook(long webhookId, String authToken) {

        // Check if there is a Webhook with this data.
        if (existsLogData(webhookId, authToken)) {

            // Delete if so.
            querySQL("DELETE FROM LogWebhooks WHERE CID='" + webhookId + "' AND TOKEN='" + authToken + "'");
        }

    }

    //endregion

    //region Welcome

    /**
     * Get the WelcomeWebhooks data.
     * @param guildId the ID of the Guild.
     * @return {@link String[]} in the first index is the Webhook ID and in the second the Auth-Token.
     */
    public String[] getWelcomeWebhook(String guildId) {
        if (isWelcomeSetup(guildId)) {
            // Creating a SQL Statement to get the Entry from the WelcomeWebhooks Table by the GuildID.
            try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM WelcomeWebhooks WHERE GID='" + guildId + "'").executeQuery()) {

                // Return if there was a match.
                if (rs != null && rs.next()) {
                    if (rs.getString("CID").isEmpty() || rs.getString("TOKEN").isEmpty())
                        return new String[] { "0", "Not setuped!"};
                    else
                        return new String[] { rs.getString("CID"), rs.getString("TOKEN") };
                }
            } catch (Exception ignore) {}
        }

        return new String[] { "0", "Not setuped!"};
    }

    /**
     * Set the WelcomeWebhooks in our Database.
     * @param guildId the ID of the Guild.
     * @param webhookId the ID of the Webhook.
     * @param authToken the Auth-token to verify the access.
     */
    public void setWelcomeWebhook(String guildId, String webhookId, String authToken) {

        // Check if there is already a Webhook set.
        if (isWelcomeSetup(guildId)) {

            // Delete the existing Webhook.
            BotInfo.botInstance.getGuildById(guildId).retrieveWebhooks().queue(webhooks -> webhooks.stream().filter(webhook ->
                            webhook.getId().equalsIgnoreCase(getLogWebhook(guildId)[0]) && webhook.getToken().equalsIgnoreCase(getLogWebhook(guildId)[1]))
                    .forEach(webhook -> webhook.delete().queue()));

            // Delete the entry.
            querySQL("DELETE FROM WelcomeWebhooks WHERE GID='" + guildId + "'");
        }

        // Add a new entry into the Database.
        querySQL("INSERT INTO WelcomeWebhooks (GID, CID, TOKEN) VALUES ('" + guildId + "', '" + webhookId + "', '" + authToken + "');");

    }

    /**
     * Check if the Welcome Webhook has been set in our Database for this Server.
     * @param guildId the ID of the Guild.
     * @return {@link Boolean} if true, it has been set | if false, it hasn't been set.
     */
    public boolean isWelcomeSetup(String guildId) {

        // Creating a SQL Statement to get the Entry from the WelcomeWebhooks Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM WelcomeWebhooks WHERE GID='" + guildId + "'").executeQuery()) {

            // Return if there was a match.
            return (rs != null && rs.next());
        } catch (Exception ignore) {}

        // Return if there wasn't a match.
        return false;
    }

    //endregion

    //region News

    /**
     * Get the NewsWebhooks data.
     * @param guildId the ID of the Guild.
     * @return {@link String[]} in the first index is the Webhook ID and in the second the Auth-Token.
     */
    public String[] getNewsWebhook(String guildId) {
        if (isNewsSetup(guildId)) {
            // Creating a SQL Statement to get the Entry from the NewsWebhooks Table by the GuildID.
            try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM NewsWebhooks WHERE GID='" + guildId + "'").executeQuery()) {

                // Return if there was a match.
                if (rs != null && rs.next()) {
                    if (rs.getString("CID").isEmpty() || rs.getString("TOKEN").isEmpty())
                        return new String[] { "0", "Not setuped!"};
                    else
                        return new String[] { rs.getString("CID"), rs.getString("TOKEN") };
                }
            } catch (Exception ignore) {}
        }

        return new String[] { "0", "Not setuped!"};
    }

    /**
     * Set the NewsWebhooks in our Database.
     * @param guildId the ID of the Guild.
     * @param webhookId the ID of the Webhook.
     * @param authToken the Auth-token to verify the access.
     */
    public void setNewsWebhook(String guildId, String webhookId, String authToken) {

        // Check if there is already a Webhook set.
        if (isNewsSetup(guildId)) {

            // Delete the existing Webhook.
            BotInfo.botInstance.getGuildById(guildId).retrieveWebhooks().queue(webhooks -> webhooks.stream().filter(webhook ->
                            webhook.getId().equalsIgnoreCase(getLogWebhook(guildId)[0]) && webhook.getToken().equalsIgnoreCase(getLogWebhook(guildId)[1]))
                    .forEach(webhook -> webhook.delete().queue()));

            // Delete the entry.
            querySQL("DELETE FROM NewsWebhooks WHERE GID='" + guildId + "'");
        }

        // Add a new entry into the Database.
        querySQL("INSERT INTO NewsWebhooks (GID, CID, TOKEN) VALUES ('" + guildId + "', '" + webhookId + "', '" + authToken + "');");

    }

    /**
     * Check if the News Webhook has been set in our Database for this Server.
     * @param guildId the ID of the Guild.
     * @return {@link Boolean} if true, it has been set | if false, it hasn't been set.
     */
    public boolean isNewsSetup(String guildId) {

        // Creating a SQL Statement to get the Entry from the NewsWebhooks Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM NewsWebhooks WHERE GID='" + guildId + "'").executeQuery()) {

            // Return if there was a match.
            return (rs != null && rs.next());
        } catch (Exception ignore) {}

        // Return if there wasn't a match.
        return false;
    }

    //endregion

    //region Rainbow

    /**
     * Get the RainbowWebhooks data.
     * @param guildId the ID of the Guild.
     * @return {@link String[]} in the first index is the Webhook ID and in the second the Auth-Token.
     */
    public String[] getRainbowWebhook(String guildId) {
        if (isRainbowSetup(guildId)) {
            // Creating a SQL Statement to get the Entry from the RainbowWebhooks Table by the GuildID.
            try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM RainbowWebhooks WHERE GID='" + guildId + "'").executeQuery()) {

                // Return if there was a match.
                if (rs != null && rs.next()) {
                    if (rs.getString("CID").isEmpty() || rs.getString("TOKEN").isEmpty())
                        return new String[] { "0", "Not setuped!"};
                    else
                        return new String[] { rs.getString("CID"), rs.getString("TOKEN") };
                }
            } catch (Exception ignore) {}
        }

        return new String[] { "0", "Not setuped!"};
    }

    /**
     * Set the RainbowWebhooks in our Database.
     * @param guildId the ID of the Guild.
     * @param webhookId the ID of the Webhook.
     * @param authToken the Auth-token to verify the access.
     */
    public void setRainbowWebhook(String guildId, String webhookId, String authToken) {

        // Check if there is already a Webhook set.
        if (isRainbowSetup(guildId)) {

            // Delete the existing Webhook.
            BotInfo.botInstance.getGuildById(guildId).retrieveWebhooks().queue(webhooks -> webhooks.stream().filter(webhook ->
                            webhook.getId().equalsIgnoreCase(getLogWebhook(guildId)[0]) && webhook.getToken().equalsIgnoreCase(getLogWebhook(guildId)[1]))
                    .forEach(webhook -> webhook.delete().queue()));

            querySQL("DELETE FROM RainbowWebhooks WHERE GID='" + guildId + "'");
        }

        // Add a new entry into the Database.
        querySQL("INSERT INTO RainbowWebhooks (GID, CID, TOKEN) VALUES ('" + guildId + "', '" + webhookId + "', '" + authToken + "');");

    }

    /**
     * Check if the Rainbow Webhook has been set in our Database for this Server.
     * @param guildId the ID of the Guild.
     * @return {@link Boolean} if true, it has been set | if false, it hasn't been set.
     */
    public boolean isRainbowSetup(String guildId) {

        // Creating a SQL Statement to get the Entry from the WelcomeWebhooks Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM RainbowWebhooks WHERE GID='" + guildId + "'").executeQuery()) {

            // Return if there was a match.
            return (rs != null && rs.next());
        } catch (Exception ignore) {}

        // Return if there wasn't a match.
        return false;
    }

    //endregion

    //region Twitch Notifier

    /**
     * Get the TwitchNotify data.
     * @param guildId the ID of the Guild.
     * @param twitchName the Username of the Twitch User.
     * @return {@link String[]} in the first index is the Webhook ID and in the second the Auth-Token.
     */
    public String[] getTwitchWebhook(String guildId, String twitchName) {
        if (isTwitchSetup(guildId)) {
            // Creating a SQL Statement to get the Entry from the RainbowWebhooks Table by the GuildID.
            try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM TwitchNotify WHERE GID='" + guildId + "' AND NAME='" + twitchName + "'").executeQuery()) {

                // Return if there was a match.
                if (rs != null && rs.next()) {
                    if (rs.getString("CID").isEmpty() || rs.getString("TOKEN").isEmpty())
                        return new String[] { "0", "Not setuped!"};
                    else
                        return new String[] { rs.getString("CID"), rs.getString("TOKEN") };
                }
            } catch (Exception ignore) {}
        }

        return new String[] { "0", "Not setuped!"};
    }

    /**
     * Get the TwitchNotify data.
     * @param twitchName the Username of the Twitch User.
     * @return {@link ArrayList<>} in the first index is the Webhook ID and in the second the Auth-Token.
     */
    public ArrayList<String[]> getTwitchWebhooksByName(String twitchName) {

        ArrayList<String[]> webhooks = new ArrayList<>();

            // Creating a SQL Statement to get the Entry from the RainbowWebhooks Table by the GuildID.
            try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM TwitchNotify WHERE NAME='" + twitchName + "'").executeQuery()) {

                // Return if there was a match.
                while (rs != null && rs.next()) {
                    if (!rs.getString("CID").isEmpty() && !rs.getString("TOKEN").isEmpty())
                        webhooks.add(new String[] { rs.getString("CID"), rs.getString("TOKEN") });
                }
            } catch (Exception ignore) {}

        return webhooks;
    }

    /**
     * Set the TwitchNotify in our Database.
     * @param guildId the ID of the Guild.
     * @param webhookId the ID of the Webhook.
     * @param authToken the Auth-token to verify the access.
     * @param twitchName the Username of the Twitch User.
     */
    public void addTwitchWebhook(String guildId, String webhookId, String authToken, String twitchName) {

        // Check if there is already a Webhook set.
        if (isTwitchSetup(guildId, twitchName)) {

            // Delete the existing Webhook.
            BotInfo.botInstance.getGuildById(guildId).retrieveWebhooks().queue(webhooks -> webhooks.stream().filter(webhook ->
                            webhook.getId().equalsIgnoreCase(getLogWebhook(guildId)[0]) && webhook.getToken().equalsIgnoreCase(getLogWebhook(guildId)[1]))
                    .forEach(webhook -> webhook.delete().queue()));

            // Delete the entry.
            querySQL("DELETE FROM TwitchNotify WHERE GID='" + guildId + "' AND NAME='" + twitchName + "'");
        }

        // Add a new entry into the Database.
        querySQL("INSERT INTO TwitchNotify (GID, NAME, CID, TOKEN) VALUES ('" + guildId + "', '" + twitchName + "', '" + webhookId + "', '" + authToken + "');");
    }

    /**
     * Remove a Twitch Notifier entry from our Database.
     * @param guildId the ID of the Guild.
     * @param twitchName the Name of the Twitch User.
     */
    public void removeTwitchWebhook(String guildId, String twitchName) {

        // Check if there is a Webhook set.
        if (isTwitchSetup(guildId, twitchName)) {

            // Delete the existing Webhook.
            BotInfo.botInstance.getGuildById(guildId).retrieveWebhooks().queue(webhooks -> webhooks.stream().filter(webhook ->
                            webhook.getId().equalsIgnoreCase(getLogWebhook(guildId)[0]) && webhook.getToken().equalsIgnoreCase(getLogWebhook(guildId)[1]))
                    .forEach(webhook -> webhook.delete().queue()));

            // Delete the entry.
            querySQL("DELETE FROM TwitchNotify WHERE GID='" + guildId + "' AND NAME='" + twitchName + "'");
        }
    }

    /**
     * Check if the Twitch Webhook has been set in our Database for this Server.
     * @param guildId the ID of the Guild.
     * @return {@link Boolean} if true, it has been set | if false, it hasn't been set.
     */
    public boolean isTwitchSetup(String guildId) {

        // Creating a SQL Statement to get the Entry from the WelcomeWebhooks Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM TwitchNotify WHERE GID='" + guildId + "'").executeQuery()) {

            // Return if there was a match.
            return (rs != null && rs.next());
        } catch (Exception ignore) {}

        // Return if there wasn't a match.
        return false;
    }

    /**
     * Check if the Twitch Webhook has been set for the given User in our Database for this Server.
     * @param guildId the ID of the Guild.
     * @param twitchName the Username of the Twitch User.
     * @return {@link Boolean} if true, it has been set | if false, it hasn't been set.
     */
    public boolean isTwitchSetup(String guildId, String twitchName) {

        // Creating a SQL Statement to get the Entry from the WelcomeWebhooks Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM TwitchNotify WHERE GID='" + guildId + "' AND='" + twitchName + "'").executeQuery()) {

            // Return if there was a match.
            return (rs != null && rs.next());
        } catch (Exception ignore) {}

        // Return if there wasn't a match.
        return false;
    }

    //endregion

    //endregion

    /**
     * Send an SQL-Query to SQL-Server.
     * @param sqlQuery the SQL-Query.
     */
    public void querySQL(String sqlQuery) {
        if (!sqlConnector.IsConnected()) return;

        try (Statement statement = sqlConnector.getConnection().createStatement()) {
            statement.executeUpdate(sqlQuery);
        } catch (Exception ignore) {
            System.out.println("Couldn't send Query to SQL-Server");
        }
    }

}
