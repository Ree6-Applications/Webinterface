package de.presti.ree6.backend.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import de.presti.ree6.backend.utils.data.container.*;
import de.presti.ree6.backend.utils.data.container.api.GenericNotifierRequest;
import de.presti.ree6.backend.utils.data.container.guild.GuildContainer;
import de.presti.ree6.backend.utils.data.container.guild.GuildStatsContainer;
import de.presti.ree6.backend.utils.data.container.role.RoleLevelContainer;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.Recording;
import de.presti.ree6.sql.entities.TemporalVoicechannel;
import de.presti.ree6.sql.entities.webhook.*;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class GuildService {

    private final SessionService sessionService;


    @Autowired
    public GuildService(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    //region Stats

    public GuildStatsContainer getStats(String sessionIdentifier, String guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
        return new GuildStatsContainer(SQLSession.getSqlConnector().getSqlWorker().getInvites(guildId).size(),
                SQLSession.getSqlConnector().getSqlWorker().getStats(guildId).stream().map(CommandStatsContainer::new).toList());
    }

    public List<CommandStatsContainer> getCommandStats(String sessionIdentifier, String guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
        return SQLSession.getSqlConnector().getSqlWorker().getStats(guildId).stream().map(CommandStatsContainer::new).toList();
    }

    public int getInviteCount(String sessionIdentifier, String guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
        return SQLSession.getSqlConnector().getSqlWorker().getInvites(guildId).size();
    }

    //endregion

    //region Log channel

    public ChannelContainer getLogChannel(String sessionIdentifier, String guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, true);
        Webhook webhook = SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(guildId);
        if (webhook == null) {
            return new ChannelContainer();
        }
        return new ChannelContainer(guildContainer.getGuildChannelById(webhook.getChannelId()));
    }

    public void updateLogChannel(String sessionIdentifier, String guildId, String channelId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
        Guild guild = guildContainer.getGuild();
        StandardGuildMessageChannel channel = guild.getChannelById(StandardGuildMessageChannel.class, channelId);

        net.dv8tion.jda.api.entities.Webhook newWebhook = channel.createWebhook("Ree6-Log").complete();

        deleteLogChannel(guild);

        WebhookLog webhook = new WebhookLog(guildId, newWebhook.getId(), newWebhook.getToken());
        SQLSession.getSqlConnector().getSqlWorker().updateEntity(webhook);
    }

    public void removeLogChannel(String sessionIdentifier, String guildId) throws IllegalAccessException {
        deleteWelcomeChannel(sessionService.retrieveGuild(sessionIdentifier, guildId).getGuild());
    }

    private void deleteLogChannel(Guild guild) {
        Webhook webhook = SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(guild.getId());

        if (webhook != null) {
            guild.retrieveWebhooks().queue(c -> c.stream().filter(entry -> entry.getToken() != null)
                    .filter(entry -> entry.getId().equalsIgnoreCase(webhook.getChannelId()) && entry.getToken().equalsIgnoreCase(webhook.getToken()))
                    .forEach(entry -> entry.delete().queue()));
        }
    }

    //endregion

    //region Welcome channel

    public ChannelContainer getWelcomeChannel(String sessionIdentifier, String guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, true);
        Webhook webhook = SQLSession.getSqlConnector().getSqlWorker().getWelcomeWebhook(guildId);
        if (webhook == null) {
            return new ChannelContainer();
        }
        return new ChannelContainer(guildContainer.getGuildChannelById(webhook.getChannelId()));
    }

    public void updateWelcomeChannel(String sessionIdentifier, String guildId, String channelId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
        Guild guild = guildContainer.getGuild();

        StandardGuildMessageChannel channel = guild.getChannelById(StandardGuildMessageChannel.class, channelId);

        deleteWelcomeChannel(guild);

        net.dv8tion.jda.api.entities.Webhook newWebhook = channel.createWebhook("Ree6-Welcome").complete();

        SQLSession.getSqlConnector().getSqlWorker().updateEntity(new WebhookWelcome(guildId, newWebhook.getId(), newWebhook.getToken()));
    }

    public void removeWelcomeChannel(String sessionIdentifier, String guildId) throws IllegalAccessException {
        deleteWelcomeChannel(sessionService.retrieveGuild(sessionIdentifier, guildId).getGuild());
    }

    private void deleteWelcomeChannel(Guild guild) {
        WebhookWelcome webhook = SQLSession.getSqlConnector().getSqlWorker().getWelcomeWebhook(guild.getId());

        if (webhook != null) {
            guild.retrieveWebhooks().queue(c -> c.stream().filter(entry -> entry.getToken() != null)
                    .filter(entry -> entry.getId().equalsIgnoreCase(webhook.getChannelId()) && entry.getToken().equalsIgnoreCase(webhook.getToken()))
                    .forEach(entry -> entry.delete().queue()));
        }
    }

    //endregion

    //region Notifier

    //region Reddit Notifications

    public List<NotifierContainer> getRedditNotifier(String sessionIdentifier, String guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
        List<WebhookReddit> subreddits = SQLSession.getSqlConnector().getSqlWorker().getAllRedditWebhooks(guildId);

        return subreddits.stream().map(subreddit -> new NotifierContainer(subreddit.getSubreddit(), subreddit.getMessage(), guildContainer.getGuild().retrieveWebhooks()
                .complete().stream().filter(c -> c.getId().equals(subreddit.getGuildId())).map(ChannelContainer::new).findFirst().orElse(null))).toList();
    }

    public void addRedditNotifier(String sessionIdentifier, String guildId, GenericNotifierRequest notifierRequest) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, true);
        Guild guild = guildContainer.getGuild();
        StandardGuildMessageChannel channel = guild.getChannelById(StandardGuildMessageChannel.class, notifierRequest.channelId());

        net.dv8tion.jda.api.entities.Webhook newWebhook = channel.createWebhook("Ree6-RedditNotifier-" + notifierRequest.name()).complete();

        SQLSession.getSqlConnector().getSqlWorker().addRedditWebhook(guildId, newWebhook.getId(), newWebhook.getToken(),
                notifierRequest.name(), notifierRequest.message());
    }

    // TODO:: make a universal delete method for webhooks, safe code.
    public void removeRedditNotifier(String sessionIdentifier, String guildId, String subreddit) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
        SQLSession.getSqlConnector().getSqlWorker().removeRedditWebhook(guildId, subreddit);
    }

    //endregion

    //region Twitch Notifications

    public List<NotifierContainer> getTwitchNotifier(String sessionIdentifier, String guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
        List<WebhookTwitch> twitchChannels = SQLSession.getSqlConnector().getSqlWorker().getAllTwitchWebhooks(guildId);

        return twitchChannels.stream().map(twitchChannel -> new NotifierContainer(twitchChannel.getName(), twitchChannel.getMessage(), guildContainer.getGuild().retrieveWebhooks()
                .complete().stream().filter(c -> c.getId().equals(twitchChannel.getGuildId())).map(ChannelContainer::new).findFirst().orElse(null))).toList();
    }

    public void addTwitchNotifier(String sessionIdentifier, String guildId, GenericNotifierRequest notifierRequest) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, true);
        Guild guild = guildContainer.getGuild();
        StandardGuildMessageChannel channel = guild.getChannelById(StandardGuildMessageChannel.class, notifierRequest.channelId());

        net.dv8tion.jda.api.entities.Webhook newWebhook = channel.createWebhook("Ree6-TwitchNotifier-" + notifierRequest.name()).complete();

        SQLSession.getSqlConnector().getSqlWorker().addTwitchWebhook(guildId, newWebhook.getId(), newWebhook.getToken(),
                notifierRequest.name(), notifierRequest.message());
    }

    public void removeTwitchNotifier(String sessionIdentifier, String guildId, String channelId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
        SQLSession.getSqlConnector().getSqlWorker().removeTwitchWebhook(guildId, channelId);
    }

    //endregion

    //region YouTube Notifications

    public List<NotifierContainer> getYouTubeNotifier(String sessionIdentifier, String guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
        List<WebhookYouTube> youtubers = SQLSession.getSqlConnector().getSqlWorker().getAllYouTubeWebhooks(guildId);

        return youtubers.stream().map(youtuber -> new NotifierContainer(youtuber.getName(), youtuber.getMessage(), guildContainer.getGuild().retrieveWebhooks()
                .complete().stream().filter(c -> c.getId().equals(youtuber.getGuildId())).map(ChannelContainer::new).findFirst().orElse(null))).toList();
    }

    public void addYouTubeNotifier(String sessionIdentifier, String guildId, GenericNotifierRequest notifierRequest) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, true);
        Guild guild = guildContainer.getGuild();
        StandardGuildMessageChannel channel = guild.getChannelById(StandardGuildMessageChannel.class, notifierRequest.channelId());

        net.dv8tion.jda.api.entities.Webhook newWebhook = channel.createWebhook("Ree6-YoutubeNotifier-" + notifierRequest.name()).complete();

        SQLSession.getSqlConnector().getSqlWorker().addYouTubeWebhook(guildId, newWebhook.getId(), newWebhook.getToken(),
                notifierRequest.name(), notifierRequest.message());
    }

    public void removeYouTubeNotifier(String sessionIdentifier, String guildId, String channelId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
        SQLSession.getSqlConnector().getSqlWorker().removeYouTubeWebhook(guildId, channelId);
    }

    //endregion

    //region Twitter Notifications

    public List<NotifierContainer> getTwitterNotifier(String sessionIdentifier, String guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
        List<WebhookTwitter> twitterUsers = SQLSession.getSqlConnector().getSqlWorker().getAllTwitterWebhooks(guildId);

        return twitterUsers.stream().map(twitterUser -> new NotifierContainer(twitterUser.getName(), twitterUser.getMessage(), guildContainer.getGuild().retrieveWebhooks()
                .complete().stream().filter(c -> c.getId().equals(twitterUser.getGuildId())).map(ChannelContainer::new).findFirst().orElse(null))).toList();
    }

    public void addTwitterNotifier(String sessionIdentifier, String guildId, GenericNotifierRequest notifierRequest) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, true);
        Guild guild = guildContainer.getGuild();
        StandardGuildMessageChannel channel = guild.getChannelById(StandardGuildMessageChannel.class, notifierRequest.channelId());

        net.dv8tion.jda.api.entities.Webhook newWebhook = channel.createWebhook("Ree6-TwitterNotifier-" + notifierRequest.name()).complete();

        SQLSession.getSqlConnector().getSqlWorker().addTwitterWebhook(guildId, newWebhook.getId(), newWebhook.getToken(),
                notifierRequest.name(), notifierRequest.message());
    }

    public void removeTwitterNotifier(String sessionIdentifier, String guildId, String name) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
        SQLSession.getSqlConnector().getSqlWorker().removeTwitterWebhook(guildId, name);
    }

    //endregion

    //region Instagram Notifications

    public List<NotifierContainer> getInstagramNotifier(String sessionIdentifier, String guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
        List<WebhookInstagram> instagramUsers = SQLSession.getSqlConnector().getSqlWorker().getAllInstagramWebhooks(guildId);

        return instagramUsers.stream().map(instagramUser -> new NotifierContainer(instagramUser.getName(), instagramUser.getMessage(), guildContainer.getGuild().retrieveWebhooks()
                .complete().stream().filter(c -> c.getId().equals(instagramUser.getGuildId())).map(ChannelContainer::new).findFirst().orElse(null))).toList();
    }

    public void addInstagramNotifier(String sessionIdentifier, String guildId, GenericNotifierRequest notifierRequest) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, true);
        Guild guild = guildContainer.getGuild();
        StandardGuildMessageChannel channel = guild.getChannelById(StandardGuildMessageChannel.class, notifierRequest.channelId());

        net.dv8tion.jda.api.entities.Webhook newWebhook = channel.createWebhook("Ree6-InstagramNotifier-" + notifierRequest.name()).complete();

        SQLSession.getSqlConnector().getSqlWorker().addInstagramWebhook(guildId, newWebhook.getId(), newWebhook.getToken(),
                notifierRequest.name(), notifierRequest.message());
    }

    public void removeInstagramNotifier(String sessionIdentifier, String guildId, String name) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
        SQLSession.getSqlConnector().getSqlWorker().removeInstagramWebhook(guildId, name);
    }

    //endregion

    //endregion

    //region LevelRewards

    //region Chat

    public List<RoleLevelContainer> getChatAutoRoles(String sessionIdentifier, String guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, false, true);
        return SQLSession.getSqlConnector().getSqlWorker().getChatLevelRewards(guildId).entrySet().stream().map(x -> new RoleLevelContainer(x.getKey(), guildContainer.getRoleById(x.getValue()))).toList();
    }

    public void addChatAutoRole(String sessionIdentifier, String guildId, String roleId, long level) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, false, false);

        if (guildContainer.getRoleById(roleId) == null)
            throw new IllegalAccessException("Role not found");

        SQLSession.getSqlConnector().getSqlWorker().addChatLevelReward(guildId, roleId, level);
    }

    public void removeChatAutoRole(String sessionIdentifier, String guildId, long level) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, false, false);
        SQLSession.getSqlConnector().getSqlWorker().removeChatLevelReward(guildId, level);
    }

    //endregion

    //region Voice

    public List<RoleLevelContainer> getVoiceAutoRoles(String sessionIdentifier, String guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, false, true);
        return SQLSession.getSqlConnector().getSqlWorker().getVoiceLevelRewards(guildId).entrySet().stream().map(x -> new RoleLevelContainer(x.getKey(), guildContainer.getRoleById(x.getValue()))).toList();
    }

    public void addVoiceAutoRole(String sessionIdentifier, String guildId, String roleId, long level) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, false, true);

        if (guildContainer.getRoleById(roleId) == null)
            throw new IllegalAccessException("Role not found");

        SQLSession.getSqlConnector().getSqlWorker().addVoiceLevelReward(guildId, roleId, level);
    }

    public void removeVoiceAutoRole(String sessionIdentifier, String guildId, long level) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, false, false);
        SQLSession.getSqlConnector().getSqlWorker().removeVoiceLevelReward(guildId, level);
    }

    //endregion

    //endregion

    public RecordContainer getRecording(String sessionIdentifier, String recordId) throws IllegalAccessException {
        SessionContainer sessionContainer = sessionService.retrieveSession(sessionIdentifier);
        List<GuildContainer> guilds = sessionService.retrieveGuilds(sessionIdentifier, false);

        Recording recording = SQLSession.getSqlConnector().getSqlWorker().getEntity(new Recording(), "SELECT * FROM Recording WHERE ID=:id", Map.of("id", recordId));

        if (guilds.stream().anyMatch(g -> g.getId().equalsIgnoreCase(recording.getGuildId()))) {
            boolean found = false;

            for (JsonElement element : recording.getJsonArray()) {
                if (element.isJsonPrimitive()) {
                    JsonPrimitive primitive = element.getAsJsonPrimitive();
                    if (primitive.isString() && primitive.getAsString().equalsIgnoreCase(sessionContainer.getUser().getId())) {
                        found = true;
                        break;
                    }
                }
            }

            if (found) {
                SQLSession.getSqlConnector().getSqlWorker().deleteEntity(recording);
                return new RecordContainer(recording);
            } else {
                throw new IllegalAccessException("You were not part of this recording.");
            }
        } else {
            throw new IllegalAccessException("You were not part of the Guild this recording was made in!");
        }
    }

    //region Temporal Voice

    public ChannelContainer getTemporalVoice(String sessionIdentifier, String guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, true, false);
        TemporalVoicechannel temporalVoicechannel = SQLSession.getSqlConnector().getSqlWorker()
                .getEntity(new TemporalVoicechannel(), "SELECT * FROM TemporalVoicechannel WHERE GID=:gid", Map.of("gid", guildId));

        return guildContainer.getChannelById(temporalVoicechannel.getVoiceChannelId());
    }

    public void updateTemporalVoice(String sessionIdentifier, String guildId, String channelId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, true, false);

        if (guildContainer.getChannelById(channelId) == null)
            throw new IllegalAccessException("Channel not found");

        TemporalVoicechannel temporalVoicechannel = SQLSession.getSqlConnector().getSqlWorker()
                .getEntity(new TemporalVoicechannel(), "SELECT * FROM TemporalVoicechannel WHERE GID=:gid", Map.of("gid", guildId));

        if (temporalVoicechannel != null) {
            temporalVoicechannel.setVoiceChannelId(channelId);
        } else {
            temporalVoicechannel = new TemporalVoicechannel(guildId, channelId);
        }

        SQLSession.getSqlConnector().getSqlWorker().updateEntity(temporalVoicechannel);
    }

    public void removeTemporalVoice(String sessionIdentifier, String guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, false, false);

        TemporalVoicechannel temporalVoicechannel = SQLSession.getSqlConnector().getSqlWorker()
                .getEntity(new TemporalVoicechannel(), "SELECT * FROM TemporalVoicechannel WHERE GID=:gid", Map.of("gid", guildId));

        if (temporalVoicechannel != null) {
            SQLSession.getSqlConnector().getSqlWorker().deleteEntity(temporalVoicechannel);
        }
    }

    //endregion

    //region OptOut

    public String checkOptOut(String sessionIdentifier, String guildId) throws IllegalAccessException {
        SessionContainer sessionContainer = sessionService.retrieveSession(sessionIdentifier);
        return SQLSession.getSqlConnector().getSqlWorker().isOptOut(guildId, sessionContainer.getUser().getId()) ? "optedOut" : "optedIn";
    }

    public String optOut(String sessionIdentifier, String guildId) throws IllegalAccessException {
        SessionContainer sessionContainer = sessionService.retrieveSession(sessionIdentifier);
        if (SQLSession.getSqlConnector().getSqlWorker().isOptOut(guildId, sessionContainer.getUser().getId())) {
            SQLSession.getSqlConnector().getSqlWorker().optOut(guildId, sessionContainer.getUser().getId());
            return "Opted out!";
        } else {
            SQLSession.getSqlConnector().getSqlWorker().optIn(guildId, sessionContainer.getUser().getId());
            return "Opted in!";
        }
    }

    //endregion

}
