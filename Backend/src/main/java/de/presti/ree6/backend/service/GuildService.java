package de.presti.ree6.backend.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import de.presti.ree6.backend.bot.BotWorker;
import de.presti.ree6.backend.repository.GuildStatsRepository;
import de.presti.ree6.backend.utils.data.container.*;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.Recording;
import de.presti.ree6.sql.entities.webhook.Webhook;
import de.presti.ree6.sql.entities.webhook.WebhookLog;
import de.presti.ree6.sql.entities.webhook.WebhookWelcome;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class GuildService {

    private final SessionService sessionService;

    private final GuildStatsRepository guildStatsRepository;

    @Autowired
    public GuildService(SessionService sessionService, GuildStatsRepository guildStatsRepository) {
        this.sessionService = sessionService;
        this.guildStatsRepository = guildStatsRepository;
    }

    public List<CommandStatsContainer> getStats(String sessionIdentifier, String guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
        return guildStatsRepository.getGuildCommandStatsByGuildId(guildId).stream().map(CommandStatsContainer::new).toList();
    }

    public int getInviteCount(String sessionIdentifier, String guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
        return SQLSession.getSqlConnector().getSqlWorker().getInvites(guildId).size();
    }

    public ChannelContainer getLogChannel(String sessionIdentifier, String guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, true);
        Webhook webhook = SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(guildId);
        if (webhook == null) {
            return new ChannelContainer();
        }
        return new ChannelContainer(guildContainer.getGuildChannelById(webhook.getChannelId()));
    }

    public ChannelContainer getWelcomeChannel(String sessionIdentifier, String guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, true);
        Webhook webhook = SQLSession.getSqlConnector().getSqlWorker().getWelcomeWebhook(guildId);
        if (webhook == null) {
            return new ChannelContainer();
        }
        return new ChannelContainer(guildContainer.getGuildChannelById(webhook.getChannelId()));
    }

    public void updateLogChannel(String sessionIdentifier, String guildId, String channelId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
        Guild guild = guildContainer.getGuild();
        StandardGuildMessageChannel channel = guild.getChannelById(StandardGuildMessageChannel.class, channelId);
        WebhookLog webhook = (WebhookLog) SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(guildId);

        net.dv8tion.jda.api.entities.Webhook newWebhook = channel.createWebhook("Ree6-Log").complete();

        if (webhook != null) {
            Webhook finalWebhook = webhook;
            guild.retrieveWebhooks().queue(c -> c.stream().filter(entry -> entry.getToken() != null).filter(entry -> entry.getId().equalsIgnoreCase(finalWebhook.getChannelId()) && entry.getToken().equalsIgnoreCase(finalWebhook.getToken())).forEach(entry -> entry.delete().queue()));
        }

        webhook = new WebhookLog(guildId, newWebhook.getId(), newWebhook.getToken());
        SQLSession.getSqlConnector().getSqlWorker().updateEntity(webhook);
    }

    public void updateWelcomeChannel(String sessionIdentifier, String guildId, String channelId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
        Guild guild = guildContainer.getGuild();
        StandardGuildMessageChannel channel = guild.getChannelById(StandardGuildMessageChannel.class, channelId);
        WebhookWelcome webhook = SQLSession.getSqlConnector().getSqlWorker().getWelcomeWebhook(guildId);

        net.dv8tion.jda.api.entities.Webhook newWebhook = channel.createWebhook("Ree6-Welcome").complete();

        if (webhook != null) {
            Webhook finalWebhook = webhook;
            guild.retrieveWebhooks().queue(c -> c.stream().filter(entry -> entry.getToken() != null).filter(entry -> entry.getId().equalsIgnoreCase(finalWebhook.getChannelId()) && entry.getToken().equalsIgnoreCase(finalWebhook.getToken())).forEach(entry -> entry.delete().queue()));
        }

        webhook = new WebhookWelcome(guildId, newWebhook.getId(), newWebhook.getToken());
        SQLSession.getSqlConnector().getSqlWorker().updateEntity(webhook);
    }

    public List<RoleLevelContainer> getChatAutoRoles(String sessionIdentifier, String guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, false, true);
        return SQLSession.getSqlConnector().getSqlWorker().getChatLevelRewards(guildId).entrySet().stream().map(x -> new RoleLevelContainer(x.getKey(), guildContainer.getRoleById(x.getValue()))).toList();
    }

    public List<RoleLevelContainer> getVoiceAutoRoles(String sessionIdentifier, String guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId, false, true);
        return SQLSession.getSqlConnector().getSqlWorker().getVoiceLevelRewards(guildId).entrySet().stream().map(x -> new RoleLevelContainer(x.getKey(), guildContainer.getRoleById(x.getValue()))).toList();
    }

    public RecordContainer getRecording(String sessionIdentifier, String recordId) throws IllegalAccessException {
        SessionContainer sessionContainer = sessionService.retrieveSession(sessionIdentifier);
        List<GuildContainer> guilds = sessionService.retrieveGuilds(sessionIdentifier);

        // TODO:: fix this filters the list wrong!!!!!

        Recording recording = SQLSession.getSqlConnector().getSqlWorker().getEntity(new Recording(), "SELECT * FROM Recording WHERE ID=:id", Map.of("id", recordId));

        if (guilds.stream().map(GuildContainer::getId).anyMatch(g -> g.equalsIgnoreCase(recording.getGuildId()))) {
            boolean found = false;

            for (JsonElement element : recording.getJsonArray()) {
                if (found) break;

                if (element.isJsonPrimitive()) {
                    JsonPrimitive primitive = element.getAsJsonPrimitive();
                    if (primitive.isString() && primitive.getAsString().equalsIgnoreCase(sessionContainer.getUser().getId())) {
                        found = true;
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

}
