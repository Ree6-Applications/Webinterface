package de.presti.ree6.backend.service;

import de.presti.ree6.backend.bot.BotWorker;
import de.presti.ree6.backend.repository.GuildStatsRepository;
import de.presti.ree6.backend.utils.data.container.ChannelContainer;
import de.presti.ree6.backend.utils.data.container.CommandStatsContainer;
import de.presti.ree6.backend.utils.data.container.GuildContainer;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.webhook.Webhook;
import de.presti.ree6.sql.entities.webhook.WebhookLog;
import de.presti.ree6.sql.entities.webhook.WebhookWelcome;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
        Webhook webhook = SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(guildId);
        if (webhook == null) {
            return new ChannelContainer();
        }
        return new ChannelContainer(BotWorker.getShardManager().getGuildChannelById(webhook.getChannelId()));
    }

    public ChannelContainer getWelcomeChannel(String sessionIdentifier, String guildId) throws IllegalAccessException {
        GuildContainer guildContainer = sessionService.retrieveGuild(sessionIdentifier, guildId);
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

}
