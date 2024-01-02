package de.presti.ree6.backend.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import de.presti.ree6.backend.utils.data.container.*;
import de.presti.ree6.backend.utils.data.container.api.GenericNotifierRequest;
import de.presti.ree6.backend.utils.data.container.guild.GuildContainer;
import de.presti.ree6.backend.utils.data.container.guild.GuildStatsContainer;
import de.presti.ree6.backend.utils.data.container.role.RoleContainer;
import de.presti.ree6.backend.utils.data.container.role.RoleLevelContainer;
import de.presti.ree6.backend.utils.data.container.user.UserContainer;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.*;
import de.presti.ree6.sql.entities.custom.CustomCommand;
import de.presti.ree6.sql.entities.webhook.*;
import de.presti.ree6.sql.keys.GuildUserId;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GuildService {

    private final SessionService sessionService;

    @Autowired
    public GuildService(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    //region Stats

    public GuildStatsContainer getStats(String sessionIdentifier, long guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
        return new GuildStatsContainer(SQLSession.getSqlConnector().getSqlWorker().getInvites(guildId).size(),
                SQLSession.getSqlConnector().getSqlWorker().getStats(guildId).stream().map(CommandStatsContainer::new).toList());
    }

    public List<CommandStatsContainer> getCommandStats(String sessionIdentifier, long guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
        return SQLSession.getSqlConnector().getSqlWorker().getStats(guildId).stream().map(CommandStatsContainer::new).toList();
    }

    public int getInviteCount(String sessionIdentifier, long guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
        return SQLSession.getSqlConnector().getSqlWorker().getInvites(guildId).size();
    }

    //endregion

    //region Log channel

    public ChannelContainer getLogChannel(String sessionIdentifier, long guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, true);
        WebhookLog webhook = SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(guildId);
        if (webhook == null) {
            return new ChannelContainer();
        }

        if (webhook.getChannelId() != 0) {
            return new ChannelContainer(guildContainer.getGuildChannelById(webhook.getChannelId()));
        } else {
            net.dv8tion.jda.api.entities.Webhook webhook1 = guildContainer.getGuild().retrieveWebhooks().complete().stream()
                    .filter(entry -> entry.getIdLong() == webhook.getWebhookId() && entry.getToken().equalsIgnoreCase(webhook.getToken())).findFirst().orElse(null);

            if (webhook1 != null) {
                webhook.setChannelId(webhook1.getChannel().getIdLong());
                SQLSession.getSqlConnector().getSqlWorker().updateEntity(webhook);
                return new ChannelContainer(webhook1);
            }
        }

        return new ChannelContainer();
    }

    public void updateLogChannel(String sessionIdentifier, long guildId, String channelId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
        Guild guild = guildContainer.getGuild();
        StandardGuildMessageChannel channel = guild.getChannelById(StandardGuildMessageChannel.class, channelId);

        net.dv8tion.jda.api.entities.Webhook newWebhook = channel.createWebhook("Ree6-Log").complete();

        WebhookWelcome welcome = deleteWelcomeChannel(guild);

        SQLSession.getSqlConnector().getSqlWorker().setLogWebhook(guildId, channel.getIdLong(), newWebhook.getIdLong(), newWebhook.getToken());
    }

    public WebhookLog removeLogChannel(String sessionIdentifier, long guildId) throws IllegalAccessException {
        return deleteLogChannel(sessionService.retrieveGuild(sessionIdentifier, guildId).getGuild());
    }

    private WebhookLog deleteLogChannel(Guild guild) {
        WebhookLog webhook = SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(guild.getIdLong());

        if (webhook != null) {
            guild.retrieveWebhooks().queue(c -> c.stream().filter(entry -> entry.getToken() != null)
                    .filter(entry -> entry.getIdLong() == webhook.getWebhookId() && entry.getToken().equalsIgnoreCase(webhook.getToken()))
                    .forEach(entry -> entry.delete().queue()));
        }

        return webhook;
    }

    //endregion

    //region Welcome channel

    public ChannelContainer getWelcomeChannel(String sessionIdentifier, long guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, true);
        WebhookWelcome webhook = SQLSession.getSqlConnector().getSqlWorker().getWelcomeWebhook(guildId);
        if (webhook == null) {
            return new ChannelContainer();
        }

        if (webhook.getChannelId() != 0) {
            return new ChannelContainer(guildContainer.getGuildChannelById(webhook.getChannelId()));
        } else {
            net.dv8tion.jda.api.entities.Webhook webhook1 = guildContainer.getGuild().retrieveWebhooks().complete().stream()
                    .filter(entry -> entry.getIdLong() == webhook.getWebhookId() && entry.getToken().equalsIgnoreCase(webhook.getToken())).findFirst().orElse(null);

            if (webhook1 != null) {
                webhook.setChannelId(webhook1.getChannel().getIdLong());
                SQLSession.getSqlConnector().getSqlWorker().updateEntity(webhook);
                return new ChannelContainer(webhook1);
            }
        }

        return new ChannelContainer();
    }

    public void updateWelcomeChannel(String sessionIdentifier, long guildId, String channelId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
        Guild guild = guildContainer.getGuild();

        StandardGuildMessageChannel channel = guild.getChannelById(StandardGuildMessageChannel.class, channelId);

        WebhookWelcome welcome = deleteWelcomeChannel(guild);

        net.dv8tion.jda.api.entities.Webhook newWebhook = channel.createWebhook("Ree6-Welcome").complete();

        SQLSession.getSqlConnector().getSqlWorker().setWelcomeWebhook(guildId, channel.getIdLong(), newWebhook.getIdLong(), newWebhook.getToken());
    }

    public WebhookWelcome removeWelcomeChannel(String sessionIdentifier, long guildId) throws IllegalAccessException {
        return deleteWelcomeChannel(sessionService.retrieveGuild(sessionIdentifier, guildId).getGuild());
    }

    private WebhookWelcome deleteWelcomeChannel(Guild guild) {
        WebhookWelcome webhook = SQLSession.getSqlConnector().getSqlWorker().getWelcomeWebhook(guild.getIdLong());

        if (webhook != null) {
            guild.retrieveWebhooks().queue(c -> c.stream().filter(entry -> entry.getToken() != null)
                    .filter(entry -> entry.getIdLong() == webhook.getWebhookId() && entry.getToken().equalsIgnoreCase(webhook.getToken()))
                    .forEach(entry -> entry.delete().queue()));
        }

        return webhook;
    }

    //endregion

    //region Notifier

    //region Reddit Notifications

    public List<NotifierContainer> getRedditNotifier(String sessionIdentifier, long guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
        List<WebhookReddit> subreddits = SQLSession.getSqlConnector().getSqlWorker().getAllRedditWebhooks(guildId);

        return subreddits.stream().map(subreddit -> new NotifierContainer(subreddit.getSubreddit(), subreddit.getMessage(), guildContainer.getGuild().retrieveWebhooks()
                .complete().stream().filter(c -> c.getIdLong() == subreddit.getGuildId()).map(ChannelContainer::new).findFirst().orElse(null))).toList();
    }

    public void addRedditNotifier(String sessionIdentifier, long guildId, GenericNotifierRequest notifierRequest) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, true);
        Guild guild = guildContainer.getGuild();
        StandardGuildMessageChannel channel = guild.getChannelById(StandardGuildMessageChannel.class, notifierRequest.channelId());

        net.dv8tion.jda.api.entities.Webhook newWebhook = channel.createWebhook("Ree6-RedditNotifier-" + notifierRequest.name()).complete();

        SQLSession.getSqlConnector().getSqlWorker().addRedditWebhook(guildId, channel.getIdLong(), newWebhook.getIdLong(), newWebhook.getToken(),
                notifierRequest.name(), notifierRequest.message());
    }

    // TODO:: make a universal delete method for webhooks, safe code.
    public void removeRedditNotifier(String sessionIdentifier, long guildId, String subreddit) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
        SQLSession.getSqlConnector().getSqlWorker().removeRedditWebhook(guildId, subreddit);
    }

    //endregion

    //region Twitch Notifications

    public List<NotifierContainer> getTwitchNotifier(String sessionIdentifier, long guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
        List<WebhookTwitch> twitchChannels = SQLSession.getSqlConnector().getSqlWorker().getAllTwitchWebhooks(guildId);

        return twitchChannels.stream().map(twitchChannel -> new NotifierContainer(twitchChannel.getName(), twitchChannel.getMessage(), guildContainer.getGuild().retrieveWebhooks()
                .complete().stream().filter(c -> c.getIdLong() == twitchChannel.getGuildId()).map(ChannelContainer::new).findFirst().orElse(null))).toList();
    }

    public void addTwitchNotifier(String sessionIdentifier, long guildId, GenericNotifierRequest notifierRequest) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, true);
        Guild guild = guildContainer.getGuild();
        StandardGuildMessageChannel channel = guild.getChannelById(StandardGuildMessageChannel.class, notifierRequest.channelId());

        net.dv8tion.jda.api.entities.Webhook newWebhook = channel.createWebhook("Ree6-TwitchNotifier-" + notifierRequest.name()).complete();

        SQLSession.getSqlConnector().getSqlWorker().addTwitchWebhook(guildId, channel.getIdLong(), newWebhook.getIdLong(), newWebhook.getToken(),
                notifierRequest.name(), notifierRequest.message());
    }

    public void removeTwitchNotifier(String sessionIdentifier, long guildId, String channelId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
        SQLSession.getSqlConnector().getSqlWorker().removeTwitchWebhook(guildId, channelId);
    }

    //endregion

    //region YouTube Notifications

    public List<NotifierContainer> getYouTubeNotifier(String sessionIdentifier, long guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
        List<WebhookYouTube> youtubers = SQLSession.getSqlConnector().getSqlWorker().getAllYouTubeWebhooks(guildId);

        return youtubers.stream().map(youtuber -> new NotifierContainer(youtuber.getName(), youtuber.getMessage(), guildContainer.getGuild().retrieveWebhooks()
                .complete().stream().filter(c -> c.getIdLong()  == youtuber.getGuildId()).map(ChannelContainer::new).findFirst().orElse(null))).toList();
    }

    public void addYouTubeNotifier(String sessionIdentifier, long guildId, GenericNotifierRequest notifierRequest) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, true);
        Guild guild = guildContainer.getGuild();
        StandardGuildMessageChannel channel = guild.getChannelById(StandardGuildMessageChannel.class, notifierRequest.channelId());

        net.dv8tion.jda.api.entities.Webhook newWebhook = channel.createWebhook("Ree6-YoutubeNotifier-" + notifierRequest.name()).complete();

        SQLSession.getSqlConnector().getSqlWorker().addYouTubeWebhook(guildId, channel.getIdLong(), newWebhook.getIdLong(), newWebhook.getToken(),
                notifierRequest.name(), notifierRequest.message());
    }

    public void removeYouTubeNotifier(String sessionIdentifier, long guildId, String channelId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
        SQLSession.getSqlConnector().getSqlWorker().removeYouTubeWebhook(guildId, channelId);
    }

    //endregion

    //region Twitter Notifications

    public List<NotifierContainer> getTwitterNotifier(String sessionIdentifier, long guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
        List<WebhookTwitter> twitterUsers = SQLSession.getSqlConnector().getSqlWorker().getAllTwitterWebhooks(guildId);

        return twitterUsers.stream().map(twitterUser -> new NotifierContainer(twitterUser.getName(), twitterUser.getMessage(), guildContainer.getGuild().retrieveWebhooks()
                .complete().stream().filter(c -> c.getIdLong() == twitterUser.getGuildId()).map(ChannelContainer::new).findFirst().orElse(null))).toList();
    }

    public void addTwitterNotifier(String sessionIdentifier, long guildId, GenericNotifierRequest notifierRequest) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, true);
        Guild guild = guildContainer.getGuild();
        StandardGuildMessageChannel channel = guild.getChannelById(StandardGuildMessageChannel.class, notifierRequest.channelId());

        net.dv8tion.jda.api.entities.Webhook newWebhook = channel.createWebhook("Ree6-TwitterNotifier-" + notifierRequest.name()).complete();

        SQLSession.getSqlConnector().getSqlWorker().addTwitterWebhook(guildId, channel.getIdLong(), newWebhook.getIdLong(), newWebhook.getToken(),
                notifierRequest.name(), notifierRequest.message());
    }

    public void removeTwitterNotifier(String sessionIdentifier, long guildId, String name) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
        SQLSession.getSqlConnector().getSqlWorker().removeTwitterWebhook(guildId, name);
    }

    //endregion

    //region Instagram Notifications

    public List<NotifierContainer> getInstagramNotifier(String sessionIdentifier, long guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
        List<WebhookInstagram> instagramUsers = SQLSession.getSqlConnector().getSqlWorker().getAllInstagramWebhooks(guildId);

        return instagramUsers.stream().map(instagramUser -> new NotifierContainer(instagramUser.getName(), instagramUser.getMessage(), guildContainer.getGuild().retrieveWebhooks()
                .complete().stream().filter(c -> c.getIdLong() == instagramUser.getGuildId()).map(ChannelContainer::new).findFirst().orElse(null))).toList();
    }

    public void addInstagramNotifier(String sessionIdentifier, long guildId, GenericNotifierRequest notifierRequest) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, true);
        Guild guild = guildContainer.getGuild();
        StandardGuildMessageChannel channel = guild.getChannelById(StandardGuildMessageChannel.class, notifierRequest.channelId());

        net.dv8tion.jda.api.entities.Webhook newWebhook = channel.createWebhook("Ree6-InstagramNotifier-" + notifierRequest.name()).complete();

        SQLSession.getSqlConnector().getSqlWorker().addInstagramWebhook(guildId, channel.getIdLong(), newWebhook.getIdLong(), newWebhook.getToken(),
                notifierRequest.name(), notifierRequest.message());
    }

    public void removeInstagramNotifier(String sessionIdentifier, long guildId, String name) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
        SQLSession.getSqlConnector().getSqlWorker().removeInstagramWebhook(guildId, name);
    }

    //endregion

    //endregion

    //region LevelRewards

    //region Chat

    public List<RoleLevelContainer> getChatAutoRoles(String sessionIdentifier, long guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, false, true);
        return SQLSession.getSqlConnector().getSqlWorker().getChatLevelRewards(guildId).entrySet().stream().map(x -> new RoleLevelContainer(x.getKey(), guildContainer.getRoleById(x.getValue()))).toList();
    }

    public void addChatAutoRole(String sessionIdentifier, long guildId, long roleId, long level) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, false, true);

        if (guildContainer.getRoleById(roleId) == null)
            throw new IllegalAccessException("Role not found");

        SQLSession.getSqlConnector().getSqlWorker().addChatLevelReward(guildId, roleId, level);
    }

    public void removeChatAutoRole(String sessionIdentifier, long guildId, long level) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, false, false);
        SQLSession.getSqlConnector().getSqlWorker().removeChatLevelReward(guildId, level);
    }

    //endregion

    //region Voice

    public List<RoleLevelContainer> getVoiceAutoRoles(String sessionIdentifier, long guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, false, true);
        return SQLSession.getSqlConnector().getSqlWorker().getVoiceLevelRewards(guildId).entrySet().stream().map(x -> new RoleLevelContainer(x.getKey(), guildContainer.getRoleById(x.getValue()))).toList();
    }

    public void addVoiceAutoRole(String sessionIdentifier, long guildId, long roleId, long level) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, false, true);

        if (guildContainer.getRoleById(roleId) == null)
            throw new IllegalAccessException("Role not found");

        SQLSession.getSqlConnector().getSqlWorker().addVoiceLevelReward(guildId, roleId, level);
    }

    public void removeVoiceAutoRole(String sessionIdentifier, long guildId, long level) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, false, false);
        SQLSession.getSqlConnector().getSqlWorker().removeVoiceLevelReward(guildId, level);
    }

    //endregion

    //endregion

    //region Recording

    public Recording getRecording(String sessionIdentifier, String recordId) throws IllegalAccessException {
        SessionContainer sessionContainer = sessionService.retrieveSession(sessionIdentifier);
        List<GuildContainer> guilds = sessionService.retrieveGuilds(sessionIdentifier, false);

        Recording recording = SQLSession.getSqlConnector().getSqlWorker().getEntity(new Recording(), "FROM Recording WHERE identifier=:id", Map.of("id", recordId));

        if (recording == null)
            throw new IllegalAccessException("Recording not found!");

        if (guilds.stream().anyMatch(g -> g.getId() == recording.getGuildId())) {
            boolean found = false;

            for (JsonElement element : recording.getJsonArray()) {
                if (element.isJsonPrimitive()) {
                    JsonPrimitive primitive = element.getAsJsonPrimitive();
                    if (primitive.isString() && primitive.getAsString().equalsIgnoreCase(String.valueOf(sessionContainer.getUser().getId()))) {
                        found = true;
                        break;
                    }
                }
            }

            if (found) {
                return recording;
            } else {
                throw new IllegalAccessException("You were not part of this recording.");
            }
        } else {
            throw new IllegalAccessException("You were not part of the Guild this recording was made in!");
        }
    }

    public RecordContainer getRecordingContainer(String sessionIdentifier, String recordId) throws IllegalAccessException {
        return new RecordContainer(getRecording(sessionIdentifier, recordId));
    }

    public byte[] getRecordingBytes(String sessionIdentifier, String recordId) throws IllegalAccessException {
        Recording recording = getRecording(sessionIdentifier, recordId);
        SQLSession.getSqlConnector().getSqlWorker().deleteEntity(recording);
        return recording.getRecording();
    }

    //endregion

    //region Temporal Voice

    public ChannelContainer getTemporalVoice(String sessionIdentifier, long guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, true, false);
        TemporalVoicechannel temporalVoicechannel = SQLSession.getSqlConnector().getSqlWorker()
                .getEntity(new TemporalVoicechannel(), "FROM TemporalVoicechannel WHERE guildChannelId.guildId=:gid", Map.of("gid", guildId));

        if (temporalVoicechannel == null)
            return new ChannelContainer();

        return guildContainer.getChannelById(temporalVoicechannel.getVoiceChannelId());
    }

    public void updateTemporalVoice(String sessionIdentifier, long guildId, long channelId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, true, false);

        if (guildContainer.getChannelById(channelId) == null)
            throw new IllegalAccessException("Channel not found");

        TemporalVoicechannel temporalVoicechannel = SQLSession.getSqlConnector().getSqlWorker()
                .getEntity(new TemporalVoicechannel(), "FROM TemporalVoicechannel WHERE guildChannelId.guildId=:gid", Map.of("gid", guildId));

        if (temporalVoicechannel != null) {
            SQLSession.getSqlConnector().getSqlWorker().deleteEntity(temporalVoicechannel);
            temporalVoicechannel.getGuildChannelId().setChannelId(channelId);
        } else {
            temporalVoicechannel = new TemporalVoicechannel(guildId, channelId);
        }

        SQLSession.getSqlConnector().getSqlWorker().updateEntity(temporalVoicechannel);
    }

    public void removeTemporalVoice(String sessionIdentifier, long guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, false, false);

        TemporalVoicechannel temporalVoicechannel = SQLSession.getSqlConnector().getSqlWorker()
                .getEntity(new TemporalVoicechannel(), "FROM TemporalVoicechannel WHERE guildId=:gid", Map.of("gid", guildId));

        if (temporalVoicechannel != null) {
            SQLSession.getSqlConnector().getSqlWorker().deleteEntity(temporalVoicechannel);
        }
    }

    //endregion

    //region OptOut

    public String checkOptOut(String sessionIdentifier, long guildId) throws IllegalAccessException {
        SessionContainer sessionContainer = sessionService.retrieveSession(sessionIdentifier);
        return SQLSession.getSqlConnector().getSqlWorker().isOptOut(guildId, sessionContainer.getUser().getId()) ? "optedOut" : "optedIn";
    }

    public String optOut(String sessionIdentifier, long guildId) throws IllegalAccessException {
        SessionContainer sessionContainer = sessionService.retrieveSession(sessionIdentifier);
        if (!SQLSession.getSqlConnector().getSqlWorker().isOptOut(guildId, sessionContainer.getUser().getId())) {
            SQLSession.getSqlConnector().getSqlWorker().optOut(guildId, sessionContainer.getUser().getId());
            return "Opted out!";
        } else {
            SQLSession.getSqlConnector().getSqlWorker().optIn(guildId, sessionContainer.getUser().getId());
            return "Opted in!";
        }
    }

    //endregion

    //region Ticket

    public TicketContainer getTicket(String sessionIdentifier, long guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, true, false);
        Tickets tickets = SQLSession.getSqlConnector().getSqlWorker().getEntity(new Tickets(), "FROM Tickets WHERE guildId=:gid", Map.of("gid", guildId));

        if (tickets == null) {
            return new TicketContainer();
        }

        TicketContainer ticketContainer = new TicketContainer();
        ticketContainer.setTicketCount(tickets.getTicketCount());
        ticketContainer.setChannel(guildContainer.getChannelById(tickets.getChannelId()));
        ticketContainer.setCategory(guildContainer.getCategoryById(tickets.getTicketCategory()));

        ChannelContainer logChannel = guildContainer.getChannelById(tickets.getLogChannelId());

        if (logChannel == null) {
            logChannel = new ChannelContainer();
        }

        ticketContainer.setLogChannel(logChannel);
        ticketContainer.setTicketOpenMessage(SQLSession.getSqlConnector().getSqlWorker().getSetting(guildId, "message_ticket_open").getStringValue());
        ticketContainer.setTicketMenuMessage(SQLSession.getSqlConnector().getSqlWorker().getSetting(guildId, "message_ticket_menu").getStringValue());

        return ticketContainer;
    }

    public void updateTicket(String sessionIdentifier, long guildId, long channelId, long logChannelId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, true, false);

        Guild guild = guildContainer.getGuild();

        Tickets tickets = SQLSession.getSqlConnector().getSqlWorker().getEntity(new Tickets(),
                "FROM Tickets WHERE guildId=:gid", Map.of("gid", guildId));

        boolean requireChannel = false;

        if (tickets == null) {
            tickets = new Tickets();
            tickets.setGuildId(guildId);
            requireChannel = true;
        }

        if (channelId != 0) {
            if (guildContainer.getChannelById(channelId) == null)
                throw new IllegalAccessException("Channel not found");

            tickets.setChannelId(channelId);
        } else if (requireChannel) {
            throw new IllegalAccessException("Channel not found");
        }

        if (logChannelId != 0) {
            StandardGuildMessageChannel channel = guild.getChannelById(StandardGuildMessageChannel.class, logChannelId);

            Tickets finalTickets = tickets;
            guild.retrieveWebhooks().queue(c -> c.stream().filter(entry -> entry.getToken() != null)
                    .filter(entry -> entry.getIdLong() == finalTickets.getLogChannelId() && entry.getToken().equalsIgnoreCase(finalTickets.getLogChannelWebhookToken()))
                    .forEach(entry -> entry.delete().queue()));

            net.dv8tion.jda.api.entities.Webhook newWebhook = channel.createWebhook("Ticket-Log").complete();
            tickets.setLogChannelWebhookToken(newWebhook.getToken());
            tickets.setLogChannelWebhookId(newWebhook.getIdLong());
            tickets.setLogChannelId(channel.getIdLong());
        }

        SQLSession.getSqlConnector().getSqlWorker().updateEntity(tickets);
    }

    public void removeTicket(String sessionIdentifier, long guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, false, false);

        Tickets tickets = SQLSession.getSqlConnector().getSqlWorker().getEntity(new Tickets(),
                "FROM Tickets WHERE guildId=:gid", Map.of("gid", guildId));

        if (tickets != null) {
            guildContainer.getGuild().retrieveWebhooks().queue(c -> c.stream().filter(entry -> entry.getToken() != null)
                    .filter(entry -> entry.getIdLong() == tickets.getLogChannelId() && entry.getToken().equalsIgnoreCase(tickets.getLogChannelWebhookToken()))
                    .forEach(entry -> entry.delete().queue()));

            SQLSession.getSqlConnector().getSqlWorker().deleteEntity(tickets);
        }
    }

    //endregion

    //region Suggestion

    public ChannelContainer getSuggestion(String sessionIdentifier, long guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, true, false);

        Suggestions suggestions = SQLSession.getSqlConnector().getSqlWorker().getEntity(new Suggestions(),
                "FROM Suggestions WHERE guildChannelId.guildId = :id", Map.of("id", guildId));

        if (suggestions == null)
            return new ChannelContainer();

        return guildContainer.getChannelById(suggestions.getGuildChannelId().getChannelId());
    }

    public void updateSuggestion(String sessionIdentifier, long guildId, long channelId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, true, false);

        Guild guild = guildContainer.getGuild();

        Suggestions suggestions = SQLSession.getSqlConnector().getSqlWorker().getEntity(new Suggestions(),
                "FROM Suggestions WHERE guildChannelId.guildId = :id", Map.of("id", guildId));

        boolean requireChannel = false;

        if (suggestions == null) {
            suggestions = new Suggestions();
            suggestions.setGuildId(guildId);
            requireChannel = true;
        }

        if (channelId != 0) {
            if (guildContainer.getChannelById(channelId) == null)
                throw new IllegalAccessException("Channel not found");
        } else if (requireChannel) {
            throw new IllegalAccessException("Channel not found");
        }

        SQLSession.getSqlConnector().getSqlWorker().updateEntity(suggestions);
    }

    public void removeSuggestion(String sessionIdentifier, long guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, false, false);

        Tickets tickets = SQLSession.getSqlConnector().getSqlWorker().getEntity(new Tickets(),
                "FROM Tickets WHERE guildId=:gid", Map.of("gid", guildId));

        if (tickets != null) {
            guildContainer.getGuild().retrieveWebhooks().queue(c -> c.stream().filter(entry -> entry.getToken() != null)
                    .filter(entry -> entry.getIdLong() == tickets.getLogChannelId() && entry.getToken().equalsIgnoreCase(tickets.getLogChannelWebhookToken()))
                    .forEach(entry -> entry.delete().queue()));

            SQLSession.getSqlConnector().getSqlWorker().deleteEntity(tickets);
        }
    }

    //endregion

    //region Warnings

    public List<WarningContainer> getWarnings(String sessionIdentifier, long guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, false, false);

        return SQLSession.getSqlConnector().getSqlWorker().getEntityList(new Warning(),
                "FROM Warning WHERE guildUserId.guildId = :gid",
                Map.of("gid", guildId)).stream().map(c -> new WarningContainer(c, new UserContainer(guildContainer.getGuild().retrieveMemberById(c.getUserId()).complete()))).toList();
    }

    public WarningContainer addWarnings(String sessionIdentifier, long guildId, long userId, String warnings) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, false, false);

        Member member = guildContainer.getGuild().retrieveMemberById(userId).complete();

        if (member == null) {
            throw new IllegalAccessException("Member not found");
        }

        Warning warning = SQLSession.getSqlConnector().getSqlWorker().getEntity(new Warning(),
                "FROM Warning WHERE guildId = :gid AND userId = :uid",
                Map.of("gid", guildId, "uid", userId));

        if (warning == null) {
            warning = new Warning(new GuildUserId(guildId, userId), 0);
        }

        int additionWarnings = 1;

        try {
            additionWarnings = Integer.parseInt(warnings);
        } catch (NumberFormatException ignored) {
        }

        warning.setWarnings(warning.getWarnings() + additionWarnings);

        warning = SQLSession.getSqlConnector().getSqlWorker().updateEntity(warning);

        return new WarningContainer(warning, new UserContainer(member));
    }

    public WarningContainer removeWarnings(String sessionIdentifier, long guildId, long userId, String warnings) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, false, false);

        Member member = guildContainer.getGuild().retrieveMemberById(userId).complete();

        if (member == null) {
            throw new IllegalAccessException("Member not found");
        }

        Warning warning = SQLSession.getSqlConnector().getSqlWorker().getEntity(new Warning(),
                "FROM Warning WHERE guildUserId.guildId = :gid AND guildUserId.userId = :uid",
                Map.of("gid", guildId, "uid", userId));

        if (warning == null) {
            warning = new Warning(new GuildUserId(guildId, userId), 0);
        }

        int additionWarnings = 1;

        try {
            additionWarnings = Integer.parseInt(warnings);
        } catch (NumberFormatException ignored) {
        }

        warning.setWarnings(warning.getWarnings() - additionWarnings);

        if (warning.getWarnings() < 0)
            warning.setWarnings(0);

        warning = SQLSession.getSqlConnector().getSqlWorker().updateEntity(warning);

        return new WarningContainer(warning, new UserContainer(member));
    }

    public void clearWarnings(String sessionIdentifier, long guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, false, false);

        SQLSession.getSqlConnector().getSqlWorker().getEntityList(new Warning(),
                "FROM Warning WHERE guildUserId.guildId = :gid",
                Map.of("gid", guildId)).forEach(SQLSession.getSqlConnector().getSqlWorker()::deleteEntity);
    }

    //region Punishments

    public List<PunishmentContainer> getPunishments(String sessionIdentifier, long guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, false, true);

        return SQLSession.getSqlConnector().getSqlWorker().getEntityList(new Punishments(),
                "FROM Punishments WHERE guildUserId.guildId = :gid",
                Map.of("gid", guildId)).stream().map(c -> new PunishmentContainer(c, guildContainer)).toList();
    }

    public void clearPunishments(String sessionIdentifier, long guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, false, false);

        SQLSession.getSqlConnector().getSqlWorker().getEntityList(new Punishments(),
                "FROM Punishments WHERE guildUserId.guildId = :gid",
                Map.of("gid", guildId)).forEach(c -> SQLSession.getSqlConnector().getSqlWorker().deleteEntity(c));
    }

    public void removePunishments(String sessionIdentifier, long guildId, String punishmentId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, false, false);

        Punishments punishments = SQLSession.getSqlConnector().getSqlWorker().getEntity(new Punishments(),
                "FROM Punishments WHERE guildUserId.guildId = :gid AND id = :id",
                Map.of("gid", guildId, "id", punishmentId));

        if (punishments == null)
            throw new IllegalAccessException("Punishment not found");

        if (punishments.getGuild() != guildContainer.getGuild().getIdLong())
            throw new IllegalAccessException("Punishment not found");

        SQLSession.getSqlConnector().getSqlWorker().deleteEntity(punishments);
    }

    public PunishmentContainer addPunishments(String sessionIdentifier, long guildId, String neededWarnings, String action, String timeoutTime, long roleId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, false, true);

        Punishments punishments = new Punishments();
        punishments.setGuildId(guildId);

        try {
            int warnings = Integer.parseInt(neededWarnings);
            if (warnings < 0)
                throw new IllegalAccessException("Invalid warnings");

            int actionInt = Integer.parseInt(action);

            if (actionInt < 0 || actionInt > 5)
                throw new IllegalAccessException("Invalid action");

            if (actionInt == 2 || actionInt == 3) {
                if (roleId != 0 || guildContainer.getGuild().getRoleById(roleId) == null)
                    throw new IllegalAccessException("Role not found");
            }

            long timeout = timeoutTime != null ? Long.parseLong(timeoutTime) : 0;

            punishments.setWarnings(warnings);
            punishments.setAction(actionInt);

            if (timeoutTime != null)
                punishments.setTimeoutTime(timeout);

            if (roleId != 0)
                punishments.setRoleId(roleId);
        } catch (NumberFormatException e) {
            throw new IllegalAccessException("Invalid number format");
        }

        return new PunishmentContainer(SQLSession.getSqlConnector().getSqlWorker().updateEntity(punishments), guildContainer);
    }

    //endregion

    //endregion

    //region Custom Command

    public List<CustomCommandContainer> getCustomCommand(String sessionIdentifier, long guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, true, false);

        return SQLSession.getSqlConnector().getSqlWorker().getEntityList(new CustomCommand(),
                "FROM CustomCommand WHERE guildId = :gid",
                Map.of("gid", guildId)).stream().map(command -> new CustomCommandContainer(command, guildContainer)).toList();
    }

    public void removeCustomCommand(String sessionIdentifier, long guildId, String commandId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, false, false);

        CustomCommand command = SQLSession.getSqlConnector().getSqlWorker().getEntity(new CustomCommand(),
                "FROM CustomCommand WHERE guildId = :gid AND id = :id",
                Map.of("gid", guildId, "id", commandId));

        if (command == null)
            throw new IllegalAccessException("Command not found");

        SQLSession.getSqlConnector().getSqlWorker().deleteEntity(command);
    }

    public CustomCommandContainer addCustomCommand(String sessionIdentifier, long guildId, String commandName, String channelId, String response, String embedJson) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, true, false);

        CustomCommand command = SQLSession.getSqlConnector().getSqlWorker().getEntity(new CustomCommand(),
                "FROM CustomCommand WHERE guildId = :gid AND command = :name",
                Map.of("gid", guildId, "name", commandName));

        if (command == null) {
            command = new CustomCommand();
            command.setGuildId(guildId);
            command.setName(commandName);
        }

        if (response != null) {
            command.setMessageResponse(response);
        }

        if (embedJson != null) {
            command.setEmbedResponse(JsonParser.parseString(embedJson));
        }

        try {
            long channelIdNumber = Long.parseLong(channelId);
            command.setChannelId(channelIdNumber);
        } catch (NumberFormatException e) {
            throw new IllegalAccessException("Invalid channel id");
        }

        return new CustomCommandContainer(SQLSession.getSqlConnector().getSqlWorker().updateEntity(command), guildContainer);
    }


    //endregion

    //region Reaction role

    public List<MessageReactionRoleContainer> retrieveReactionRoles(String sessionIdentifier, long guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, true, true);

        List<ReactionRole> roles = SQLSession.getSqlConnector().getSqlWorker().getEntityList(new ReactionRole(),
                "FROM ReactionRole WHERE guildUserId.guildId = :gid",
                Map.of("gid", guildId));

        Map<Long, List<ReactionRole>> map = roles.stream().collect(Collectors.groupingBy(ReactionRole::getMessageId));

        List<MessageReactionRoleContainer> messageReactionRoleContainers = new ArrayList<>();

        Guild guild = guildContainer.getGuild();

        map.forEach((key, value) -> {
            if (value.isEmpty()) return;

            StandardGuildMessageChannel channel = guild.getChannelById(StandardGuildMessageChannel.class, value.get(0).getChannelId());

            if (channel == null) return;

            Message message = channel.retrieveMessageById(key).complete();

            MessageReactionRoleContainer messageReactionRoleContainer = new MessageReactionRoleContainer();
            messageReactionRoleContainer.setRoleReactions(value.stream()
                    .map(role -> new ReactionRoleContainer(role, guildContainer))
                    .toList());
            messageReactionRoleContainer.setMessage(new MessageContainer(message));
            messageReactionRoleContainers.add(messageReactionRoleContainer);
        });

        return messageReactionRoleContainers;
    }

    public void addReactionRole(String sessionIdentifier, long guildId, String emojiId, String formattedEmoji, long channelId, String messageId, long roleId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, true, true);

        Guild guild = guildContainer.getGuild();

        RoleContainer role = guildContainer.getRoleById(roleId);

        if (role == null)
            throw new IllegalAccessException("Role not found");

        try {
            long messageIdNumber = Long.parseLong(messageId);
            long emojiIdNumber = Long.parseLong(emojiId);

            if (formattedEmoji == null || formattedEmoji.isBlank()) {
                throw new IllegalAccessException("Invalid emoji");
            }

            Message message = guild.getTextChannelById(channelId).retrieveMessageById(messageIdNumber).complete();

            if (message == null)
                throw new IllegalAccessException("Message not found");

            //message.addReaction(Emoji.fromFormatted(emojiIdNumber)).queue();

            ReactionRole reactionRole = new ReactionRole();
            reactionRole.setChannelId(channelId);
            reactionRole.setEmoteId(emojiIdNumber);
            reactionRole.setFormattedEmote(formattedEmoji);
            reactionRole.setGuildId(guild.getIdLong());
            reactionRole.setMessageId(messageIdNumber);
            reactionRole.getGuildRoleId().setRoleId(role.getId());

            SQLSession.getSqlConnector().getSqlWorker().updateEntity(reactionRole);
        } catch (NumberFormatException e) {
            throw new IllegalAccessException("Invalid number format");
        }

    }

    public void removeReactionRole(String sessionIdentifier, long guildId, String emojiId, String messageId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, true, true);

        Guild guild = guildContainer.getGuild();


        try {
            long messageIdNumber = Long.parseLong(messageId);
            long emojiIdNumber = Long.parseLong(emojiId);

            //message.removeReaction(Emoji.fromFormatted(emojiIdNumber)).queue();

            ReactionRole reactionRole = SQLSession.getSqlConnector().getSqlWorker().getEntity(new ReactionRole(),
                    "FROM ReactionRole WHERE guildAndId.guildId = :gid AND messageId = :mid AND emoteId = :eid",
                    Map.of("gid", guildId, "mid", messageId, "eid", emojiId));

            if (reactionRole == null)
                throw new IllegalAccessException("Reaction role not found");

            SQLSession.getSqlConnector().getSqlWorker().deleteEntity(reactionRole);
        } catch (NumberFormatException e) {
            throw new IllegalAccessException("Invalid number format");
        }
    }

    //endregion
}
