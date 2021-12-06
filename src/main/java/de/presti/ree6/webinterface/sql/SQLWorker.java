package de.presti.ree6.webinterface.sql;

import de.presti.ree6.webinterface.bot.BotInfo;
import de.presti.ree6.webinterface.invite.InviteContainer;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A Class to actually handle the SQL data.
 * Used to provide Data from the Database and to save Data into the Database.
 */
public class SQLWorker {

    // Instance of the SQL Connector to actually access the SQL Database.
    private final SQLConnector sqlConnector;

    /**
     * Constructor to create a new Instance of the SQLWorker with a ref to the SQL-Connector.
     *
     * @param sqlConnector an Instance of the SQL-Connector to retrieve the data from.
     */
    public SQLWorker(SQLConnector sqlConnector) {
        this.sqlConnector = sqlConnector;
    }

    //region Level

    //region Chat

    /**
     * Get the Chat XP Count of the give UserID from the given Guild.
     *
     * @param guildId the ID of the Guild.
     * @param userId  the ID of the User.
     * @return {@link Long} as XP Count.
     */
    public Long getChatXP(String guildId, String userId) {

        // Creating a SQL Statement to get the User from the Level Table by the GuildID and UserID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM Level WHERE GID='" + guildId + "' AND UID='" + userId + "'").executeQuery()) {

            // Return the XP as Long if found.
            if (rs != null && rs.next()) return Long.parseLong(rs.getString("XP"));
        } catch (Exception ignore) {
        }

        // Return 0 if there was an error OR if the user isn't in the database.
        return 0L;
    }

    /**
     * Check if the given combination of UserID and GuildID is saved in our Database.
     *
     * @param guildId the ID of the Guild.
     * @param userId  the ID of the User.
     * @return {@link Boolean} true if there was a match | false if there wasn't a match.
     */
    public boolean existsInChatXP(String guildId, String userId) {

        // Creating a SQL Statement to get the User from the Level Table by the GuildID and UserID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM Level WHERE GID='" + guildId + "' AND UID='" + userId + "'").executeQuery()) {

            // Return if there was a match.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        // Return if there wasn't a match.
        return false;
    }

    /**
     * Give the wanted User more XP.
     *
     * @param guildId the ID of the Guild.
     * @param userId  the ID of the User.
     * @param xp      the wanted XP.
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
     *
     * @param guildId the ID of the Guild.
     * @param limit   the Limit of how many should be given back.
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
        } catch (Exception ignore) {
        }

        // Return the list.
        return userIds;
    }

    //endregion

    //region Voice

    /**
     * Get the Voice XP Count of the give UserID from the given Guild.
     *
     * @param guildId the ID of the Guild.
     * @param userId  the ID of the User.
     * @return {@link Long} as XP Count.
     */
    public Long getVoiceXP(String guildId, String userId) {

        // Creating a SQL Statement to get the User from the VCLevel Table by the GuildID and UserID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM VCLevel WHERE GID='" + guildId + "' AND UID='" + userId + "'").executeQuery()) {

            // Return the XP as Long if found.
            if (rs != null && rs.next()) return Long.parseLong(rs.getString("XP"));
        } catch (Exception ignore) {
        }

        // Return 0 if there was an error OR if the user isn't in the database.
        return 0L;
    }

    /**
     * Check if the given combination of UserID and GuildID is saved in our Database.
     *
     * @param guildId the ID of the Guild.
     * @param userId  the ID of the User.
     * @return {@link Boolean} true if there was a match | false if there wasn't a match.
     */
    public boolean existsInVoiceXP(String guildId, String userId) {

        // Creating a SQL Statement to get the User from the VCLevel Table by the GuildID and UserID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM VCLevel WHERE GID='" + guildId + "' AND UID='" + userId + "'").executeQuery()) {

            // Return if there was a match.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        // Return if there wasn't a match.
        return false;
    }

    /**
     * Give the wanted User more XP.
     *
     * @param guildId the ID of the Guild.
     * @param userId  the ID of the User.
     * @param xp      the wanted XP.
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
     *
     * @param guildId the ID of the Guild.
     * @param limit   the Limit of how many should be given back.
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
        } catch (Exception ignore) {
        }

        // Return the list.
        return userIds;
    }

    //endregion

    //endregion

    //region Webhooks

    //region Logs

    /**
     * Get the LogWebhook data.
     *
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
                        return new String[]{"0", "Not setuped!"};
                    else
                        return new String[]{rs.getString("CID"), rs.getString("TOKEN")};
                }
            } catch (Exception ignore) {
            }
        }

        return new String[]{"0", "Not setuped!"};
    }

    /**
     * Set the LogWebhook in our Database.
     *
     * @param guildId   the ID of the Guild.
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
     *
     * @param guildId the ID of the Guild.
     * @return {@link Boolean} if true, it has been set | if false, it hasn't been set.
     */
    public boolean isLogSetup(String guildId) {

        // Creating a SQL Statement to get the Entry from the LogWebhooks Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM LogWebhooks WHERE GID='" + guildId + "'").executeQuery()) {

            // Return if there was a match.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        // Return if there wasn't a match.
        return false;
    }

    /**
     * Check if the Log Webhook data is in our Database.
     *
     * @param webhookId the ID of the Webhook.
     * @param authToken the Auth-Token of the Webhook.
     * @return {@link Boolean} if true, it has been set | if false, it hasn't been set.
     */
    public boolean existsLogData(long webhookId, String authToken) {

        // Creating a SQL Statement to get the Entry from the LogWebhooks Table by the WebhookID and its Auth-Token.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM LogWebhooks WHERE CID='" + webhookId + "' AND TOKEN='" + authToken + "'").executeQuery()) {

            // Return if there was a match.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        // Return if there wasn't a match.
        return false;
    }

    /**
     * Set the LogWebhook in our Database.
     *
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
     *
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
                        return new String[]{"0", "Not setuped!"};
                    else
                        return new String[]{rs.getString("CID"), rs.getString("TOKEN")};
                }
            } catch (Exception ignore) {
            }
        }

        return new String[]{"0", "Not setuped!"};
    }

    /**
     * Set the WelcomeWebhooks in our Database.
     *
     * @param guildId   the ID of the Guild.
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
     *
     * @param guildId the ID of the Guild.
     * @return {@link Boolean} if true, it has been set | if false, it hasn't been set.
     */
    public boolean isWelcomeSetup(String guildId) {

        // Creating a SQL Statement to get the Entry from the WelcomeWebhooks Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM WelcomeWebhooks WHERE GID='" + guildId + "'").executeQuery()) {

            // Return if there was a match.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        // Return if there wasn't a match.
        return false;
    }

    //endregion

    //region News

    /**
     * Get the NewsWebhooks data.
     *
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
                        return new String[]{"0", "Not setuped!"};
                    else
                        return new String[]{rs.getString("CID"), rs.getString("TOKEN")};
                }
            } catch (Exception ignore) {
            }
        }

        return new String[]{"0", "Not setuped!"};
    }

    /**
     * Set the NewsWebhooks in our Database.
     *
     * @param guildId   the ID of the Guild.
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
     *
     * @param guildId the ID of the Guild.
     * @return {@link Boolean} if true, it has been set | if false, it hasn't been set.
     */
    public boolean isNewsSetup(String guildId) {

        // Creating a SQL Statement to get the Entry from the NewsWebhooks Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM NewsWebhooks WHERE GID='" + guildId + "'").executeQuery()) {

            // Return if there was a match.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        // Return if there wasn't a match.
        return false;
    }

    //endregion

    //region Rainbow

    /**
     * Get the RainbowWebhooks data.
     *
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
                        return new String[]{"0", "Not setuped!"};
                    else
                        return new String[]{rs.getString("CID"), rs.getString("TOKEN")};
                }
            } catch (Exception ignore) {
            }
        }

        return new String[]{"0", "Not setuped!"};
    }

    /**
     * Set the RainbowWebhooks in our Database.
     *
     * @param guildId   the ID of the Guild.
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
     *
     * @param guildId the ID of the Guild.
     * @return {@link Boolean} if true, it has been set | if false, it hasn't been set.
     */
    public boolean isRainbowSetup(String guildId) {

        // Creating a SQL Statement to get the Entry from the WelcomeWebhooks Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM RainbowWebhooks WHERE GID='" + guildId + "'").executeQuery()) {

            // Return if there was a match.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        // Return if there wasn't a match.
        return false;
    }

    //endregion

    //region Twitch Notifier

    /**
     * Get the TwitchNotify data.
     *
     * @param guildId    the ID of the Guild.
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
                        return new String[]{"0", "Not setuped!"};
                    else
                        return new String[]{rs.getString("CID"), rs.getString("TOKEN")};
                }
            } catch (Exception ignore) {
            }
        }

        return new String[]{"0", "Not setuped!"};
    }

    /**
     * Get the TwitchNotify data.
     *
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
                    webhooks.add(new String[]{rs.getString("CID"), rs.getString("TOKEN")});
            }
        } catch (Exception ignore) {
        }

        return webhooks;
    }

    /**
     * Set the TwitchNotify in our Database.
     *
     * @param guildId    the ID of the Guild.
     * @param webhookId  the ID of the Webhook.
     * @param authToken  the Auth-token to verify the access.
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
     *
     * @param guildId    the ID of the Guild.
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
     *
     * @param guildId the ID of the Guild.
     * @return {@link Boolean} if true, it has been set | if false, it hasn't been set.
     */
    public boolean isTwitchSetup(String guildId) {

        // Creating a SQL Statement to get the Entry from the WelcomeWebhooks Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM TwitchNotify WHERE GID='" + guildId + "'").executeQuery()) {

            // Return if there was a match.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        // Return if there wasn't a match.
        return false;
    }

    /**
     * Check if the Twitch Webhook has been set for the given User in our Database for this Server.
     *
     * @param guildId    the ID of the Guild.
     * @param twitchName the Username of the Twitch User.
     * @return {@link Boolean} if true, it has been set | if false, it hasn't been set.
     */
    public boolean isTwitchSetup(String guildId, String twitchName) {

        // Creating a SQL Statement to get the Entry from the WelcomeWebhooks Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM TwitchNotify WHERE GID='" + guildId + "' AND='" + twitchName + "'").executeQuery()) {

            // Return if there was a match.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        // Return if there wasn't a match.
        return false;
    }

    //endregion

    //endregion

    //region Roles

    //region Mute

    /**
     * Get the Mute Role ID from the given Guild.
     *
     * @param guildId the ID of the Guild.
     * @return {@link String} as Role ID.
     */
    public String getMuteRole(String guildId) {

        // Check if there is a role in the database.
        if (isMuteSetup(guildId)) {
            // Creating a SQL Statement to get the RoleID from the MuteRoles Table by the GuildID.
            try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM MuteRoles WHERE GID='" + guildId + "'").executeQuery()) {

                // Return the Role ID as String if found.
                if (rs != null && rs.next()) return rs.getString("RID");
            } catch (Exception ignore) {
            }
        }

        // Return Error if there was an error OR if the role isn't in the database.
        return "Error";
    }

    /**
     * Set the MuteRole in our Database.
     *
     * @param guildId the ID of the Guild.
     * @param roleId  the ID of the Role.
     */
    public void setMuteRole(String guildId, String roleId) {
        // Check if there is a role in the database.
        if (isMuteSetup(guildId)) {
            // Replace the entry with the new Data.
            querySQL("UPDATE MuteRoles SET RID='" + roleId + "' WHERE GID='" + guildId + "'");
        } else {
            // Add a new entry into the Database.
            querySQL("INSERT INTO MuteRoles (GID, RID) VALUES ('" + guildId + "', '" + roleId + "')");
        }
    }

    /**
     * Check if a Mute Role has been set in our Database for this Server.
     *
     * @param guildId the ID of the Guild.
     * @return {@link Boolean} as result if true, there is a role in our Database | if false, we couldn't find anything.
     */
    public boolean isMuteSetup(String guildId) {

        // Creating a SQL Statement to get the RoleID from the MuteRoles Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM MuteRoles WHERE GID='" + guildId + "'").executeQuery()) {

            // Return if there was an entry or not.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        // Return false if there was an error OR if the role isn't in the database.
        return false;
    }

    //endregion

    //region AutoRoles

    /**
     * Get the all AutoRoles saved in our Database from the given Guild.
     *
     * @param guildId the ID of the Guild.
     * @return {@link ArrayList<String>} as List with all Role IDs.
     */
    public ArrayList<String> getAutoRoles(String guildId) {

        // Create a new ArrayList to save the Role Ids.
        ArrayList<String> roleIds = new ArrayList<>();

        // Check if there is a role in the database.
        if (isAutoRoleSetup(guildId)) {
            // Creating a SQL Statement to get the RoleID from the AutoRoles Table by the GuildID.
            try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM AutoRoles WHERE GID='" + guildId + "'").executeQuery()) {

                // Add the Role ID to the List if found.
                while (rs != null && rs.next()) roleIds.add(rs.getString("RID"));
            } catch (Exception ignore) {
            }
        }

        // Return the Arraylist.
        return roleIds;
    }

    /**
     * Add a AutoRole in our Database.
     *
     * @param guildId the ID of the Guild.
     * @param roleId  the ID of the Role.
     */
    public void addAutoRole(String guildId, String roleId) {
        // Check if there is a role in the database.
        if (!isAutoRoleSetup(guildId, roleId)) {
            // Add a new entry into the Database.
            querySQL("INSERT INTO AutoRoles (GID, RID) VALUES ('" + guildId + "', '" + roleId + "')");
        }
    }

    /**
     * Remove a AutoRole from our Database.
     *
     * @param guildId the ID of the Guild.
     * @param roleId  the ID of the Role.
     */
    public void removeAutoRole(String guildId, String roleId) {
        // Check if there is a role in the database.
        if (isAutoRoleSetup(guildId, roleId)) {
            // Add a new entry into the Database.
            querySQL("DELETE FROM AutoRoles WHERE GID='" + guildId + "' AND RID='" + roleId + "'");
        }
    }

    /**
     * Check if a AutoRole has been set in our Database for this Server.
     *
     * @param guildId the ID of the Guild.
     * @return {@link Boolean} as result if true, there is a role in our Database | if false, we couldn't find anything.
     */
    public boolean isAutoRoleSetup(String guildId) {

        // Creating a SQL Statement to get the RoleID from the AutoRoles Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM AutoRoles WHERE GID='" + guildId + "'").executeQuery()) {

            // Return if there was an entry or not.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        // Return false if there was an error OR if the role isn't in the database.
        return false;
    }

    /**
     * Check if a AutoRole has been set in our Database for this Server.
     *
     * @param guildId the ID of the Guild.
     * @param roleId  the ID of the Role.
     * @return {@link Boolean} as result if true, there is a role in our Database | if false, we couldn't find anything.
     */
    public boolean isAutoRoleSetup(String guildId, String roleId) {

        // Creating a SQL Statement to get the RoleID from the AutoRoles Table by the GuildID and its ID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM AutoRoles WHERE GID='" + guildId + "' AND RID='" + roleId + "'").executeQuery()) {

            // Return if there was an entry or not.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        // Return false if there was an error OR if the role isn't in the database.
        return false;
    }

    //endregion

    //region Level Rewards

    //region Chat Rewards

    /**
     * Get the all Chat Rewards saved in our Database from the given Guild.
     *
     * @param guildId the ID of the Guild.
     * @return {@link HashMap<>} as List with all Role IDs and the needed Level.
     */
    public HashMap<Integer, String> getChatLevelRewards(String guildId) {

        // Create a new HashMap to save the Role Ids and their needed level.
        HashMap<Integer, String> rewards = new HashMap<>();

        // Check if there is a role in the database.
        if (isChatLevelRewardSetup(guildId)) {
            // Creating a SQL Statement to get the RoleID and the needed level from the ChatLevelAutoRoles Table by the GuildID.
            try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM ChatLevelAutoRoles WHERE GID='" + guildId + "'").executeQuery()) {

                // Add the Role ID and its needed level to the List if found.
                while (rs != null && rs.next()) rewards.put(Integer.parseInt(rs.getString("LVL")), rs.getString("RID"));
            } catch (Exception ignore) {
            }
        }

        // Return the HashMap.
        return rewards;
    }

    /**
     * Add a Chat Level Reward Role in our Database.
     *
     * @param guildId the ID of the Guild.
     * @param roleId  the ID of the Role.
     * @param level   the Level required to get this Role.
     */
    public void addChatLevelReward(String guildId, String roleId, int level) {
        // Check if there is a role in the database.
        if (!isChatLevelRewardSetup(guildId, roleId)) {
            // Add a new entry into the Database.
            querySQL("INSERT INTO ChatLevelAutoRoles (GID, RID) VALUES ('" + guildId + "', '" + roleId + "')");
        } else {
            // Update the entry.
            querySQL("UPDATE ChatLevelAutoRoles SET LVL='" + level + "' WHERE GID='" + guildId + "' AND RID='" + roleId + "'");
        }
    }

    /**
     * Remove a Chat Level Reward Role from our Database.
     *
     * @param guildId the ID of the Guild.
     * @param level   the Level required to get this Role.
     */
    public void removeChatLevelReward(String guildId, int level) {
        // Check if there is a role in the database.
        if (isChatLevelRewardSetup(guildId)) {
            // Add a new entry into the Database.
            querySQL("DELETE FROM ChatLevelAutoRoles WHERE GID='" + guildId + "' AND LVL='" + level + "'");
        }
    }

    /**
     * Check if a Chat Level Reward has been set in our Database for this Server.
     *
     * @param guildId the ID of the Guild.
     * @return {@link Boolean} as result if true, there is a role in our Database | if false, we couldn't find anything.
     */
    public boolean isChatLevelRewardSetup(String guildId) {

        // Creating a SQL Statement to get the RoleID from the ChatLevelAutoRoles Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM ChatLevelAutoRoles WHERE GID='" + guildId + "'").executeQuery()) {

            // Return if there was an entry or not.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        // Return false if there was an error OR if the role isn't in the database.
        return false;
    }

    /**
     * Check if a Chat Level Reward has been set in our Database for this Server.
     *
     * @param guildId the ID of the Guild.
     * @param roleId  the ID of the Role.
     * @return {@link Boolean} as result if true, there is a role in our Database | if false, we couldn't find anything.
     */
    public boolean isChatLevelRewardSetup(String guildId, String roleId) {

        // Creating a SQL Statement to get the RoleID from the ChatLevelAutoRoles Table by the GuildID and its ID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM ChatLevelAutoRoles WHERE GID='" + guildId + "' AND RID='" + roleId + "'").executeQuery()) {

            // Return if there was an entry or not.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        // Return false if there was an error OR if the role isn't in the database.
        return false;
    }

    //endregion

    //region Voice Rewards

    /**
     * Get the all Voice Rewards saved in our Database from the given Guild.
     *
     * @param guildId the ID of the Guild.
     * @return {@link HashMap<>} as List with all Role IDs and the needed Level.
     */
    public HashMap<Integer, String> getVoiceLevelRewards(String guildId) {

        // Create a new HashMap to save the Role Ids and their needed level.
        HashMap<Integer, String> rewards = new HashMap<>();

        // Check if there is a role in the database.
        if (isVoiceLevelRewardSetup(guildId)) {
            // Creating a SQL Statement to get the RoleID and the needed level from the VCLevelAutoRoles Table by the GuildID.
            try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM VCLevelAutoRoles WHERE GID='" + guildId + "'").executeQuery()) {

                // Add the Role ID and its needed level to the List if found.
                while (rs != null && rs.next()) rewards.put(Integer.parseInt(rs.getString("LVL")), rs.getString("RID"));
            } catch (Exception ignore) {
            }
        }

        // Return the HashMap.
        return rewards;
    }

    /**
     * Add a Voice Level Reward Role in our Database.
     *
     * @param guildId the ID of the Guild.
     * @param roleId  the ID of the Role.
     * @param level   the Level required to get this Role.
     */
    public void addVoiceLevelReward(String guildId, String roleId, int level) {
        // Check if there is a role in the database.
        if (!isVoiceLevelRewardSetup(guildId, roleId)) {
            // Add a new entry into the Database.
            querySQL("INSERT INTO VCLevelAutoRoles (GID, RID) VALUES ('" + guildId + "', '" + roleId + "')");
        } else {
            // Update the entry.
            querySQL("UPDATE VCLevelAutoRoles SET LVL='" + level + "' WHERE GID='" + guildId + "' AND RID='" + roleId + "'");
        }
    }

    /**
     * Remove a Voice Level Reward Role from our Database.
     *
     * @param guildId the ID of the Guild.
     * @param level   the Level required to get this Role.
     */
    public void removeVoiceLevelReward(String guildId, int level) {
        // Check if there is a role in the database.
        if (isVoiceLevelRewardSetup(guildId)) {
            // Add a new entry into the Database.
            querySQL("DELETE FROM VCLevelAutoRoles WHERE GID='" + guildId + "' AND LVL='" + level + "'");
        }
    }

    /**
     * Check if a Voice Level Reward has been set in our Database for this Server.
     *
     * @param guildId the ID of the Guild.
     * @return {@link Boolean} as result if true, there is a role in our Database | if false, we couldn't find anything.
     */
    public boolean isVoiceLevelRewardSetup(String guildId) {

        // Creating a SQL Statement to get the RoleID from the VCLevelAutoRoles Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM VCLevelAutoRoles WHERE GID='" + guildId + "'").executeQuery()) {

            // Return if there was an entry or not.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        // Return false if there was an error OR if the role isn't in the database.
        return false;
    }

    /**
     * Check if a Voice Level Reward has been set in our Database for this Server.
     *
     * @param guildId the ID of the Guild.
     * @param roleId  the ID of the Role.
     * @return {@link Boolean} as result if true, there is a role in our Database | if false, we couldn't find anything.
     */
    public boolean isVoiceLevelRewardSetup(String guildId, String roleId) {

        // Creating a SQL Statement to get the RoleID from the ChatLevelAutoRoles Table by the GuildID and its ID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM VCLevelAutoRoles WHERE GID='" + guildId + "' AND RID='" + roleId + "'").executeQuery()) {

            // Return if there was an entry or not.
            return (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        // Return false if there was an error OR if the role isn't in the database.
        return false;
    }

    //endregion

    //endregion

    //endregion

    //region Invite

    /**
     * Get a List of every saved Invite from our Database.
     * @param guildId the ID of the Guild.
     * @return {@link ArrayList<String>} as List with {@link InviteContainer}.
     */
    public ArrayList<InviteContainer> getInvites(String guildId) {

        // Create a new ArrayList to save the Invites.
        ArrayList<InviteContainer> inviteContainers = new ArrayList<>();

        // Creating a SQL Statement to get the Invites from the Invites Table by the GuildID.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM Invites WHERE GID='" + guildId + "'").executeQuery()) {

            // Add the Invite to the List if found.
            while (rs != null && rs.next()) inviteContainers.add(
                    new InviteContainer(rs.getString("UID"), rs.getString("GID"), rs.getString("CODE"),
                            Integer.parseInt(rs.getString("USES"))));
        } catch (Exception ignore) {
        }

        // Return an Arraylist with all Invites.
        return inviteContainers;
    }

    /**
     * Check if the given Invite Data is saved in our Database.
     * @param guildId the ID of the Guild.
     * @param inviteCreator the ID of the Invite Creator.
     * @param inviteCode the Code of the Invite.
     * @return {@link Boolean} as Result if true, then it's saved in our Database | if false, we couldn't find anything.
     */
    public boolean existsInvite(String guildId, String inviteCreator, String inviteCode) {
        // Creating a SQL Statement to get the Invite from the Invites Table by the GuildID, Invite Creator ID and Invite Code.
        try (ResultSet rs = sqlConnector.getConnection().prepareStatement("SELECT * FROM Invites WHERE GID='" + guildId + "' AND UID='"
        + inviteCreator + "' AND CODE='" + inviteCode + "'").executeQuery()) {

            // Return if found.
            return  (rs != null && rs.next());
        } catch (Exception ignore) {
        }

        // Return if there was an error or if it couldn't be found in our Database.
        return false;
    }

    /**
     * Change the data of a saved Invite or create a new entry in our Database.
     * @param guildId the ID of the Guild.
     * @param inviteCreator the ID of the Invite Creator.
     * @param inviteCode the Code of the Invite Code.
     * @param inviteUsage the Usage count of the Invite.
     */
    public void setInvite(String guildId, String inviteCreator, String inviteCode, int inviteUsage) {
        // Check if there is an entry with the same data.
        if (existsInvite(guildId, inviteCreator, inviteCode)) {
            // Update entry.
            querySQL("UPDATE Invites SET USES='" + inviteUsage + "' WHERE GID='" + guildId + "' AND UID='" + inviteCreator +
                    "' AND CODE='" + inviteCode + "'");
        } else {
            // Create new entry.
            querySQL("INSERT INTO Invites (GID, UID, USES, CODE) VALUES ('" + guildId + "', '" + inviteCode + "', '" + inviteUsage + "', " +
                    "'" + inviteCode + "');");
        }
    }

    /**
     * Remove an entry from our Database.
     * @param guildId the ID of the Guild.
     * @param inviteCreator the ID of the Invite Creator.
     * @param inviteCode the Code of the Invite.
     */
    public void removeInvite(String guildId, String inviteCreator, String inviteCode) {
        querySQL("DELETE FROM Invites WHERE GID='" + guildId + "' AND UID='" + inviteCreator + "' AND CODE='" + inviteCode + "'");
    }

    /**
     * Remove an entry from our Database.
     * @param guildId the ID of the Guild.
     * @param inviteCreator the ID of the Invite Creator.
     * @param inviteCode the Code of the Invite.
     */
    public void removeInvite(String guildId, String inviteCreator, String inviteCode, int inviteUsage) {
        querySQL("DELETE FROM Invites WHERE GID='" + guildId + "' AND UID='" + inviteCreator + "' AND CODE='" + inviteCode + "' " +
                "AND USES='" + inviteUsage + "'");
    }

    //endregion

    /**
     * Send an SQL-Query to SQL-Server.
     *
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
