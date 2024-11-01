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
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GuildService {

    private final SessionService sessionService;

    @Autowired
    public GuildService(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    //region Stats

    public Mono<Optional<GuildStatsContainer>> getStats(String sessionIdentifier, long guildId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId).mapNotNull(guildContainerOptional -> {
            if (guildContainerOptional.isEmpty()) {
                return Optional.empty();
            }

            return Mono.zip(SQLSession.getSqlConnector().getSqlWorker().getInvites(guildId), SQLSession.getSqlConnector().getSqlWorker().getStats(guildId))
                    .map(tuple2 -> Optional.of(new GuildStatsContainer(tuple2.getT1().size(), tuple2.getT2().stream().map(CommandStatsContainer::new).toList()))).block();
        });
    }

    public Mono<Optional<List<CommandStatsContainer>>> getCommandStats(String sessionIdentifier, long guildId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId).publishOn(Schedulers.boundedElastic()).mapNotNull(guildContainerOptional -> {
            if (guildContainerOptional.isEmpty()) {
                return Optional.empty();
            }

            return SQLSession.getSqlConnector().getSqlWorker().getStats(guildId)
                    .map(list -> Optional.of(list.stream().map(CommandStatsContainer::new).toList())).block();
        });
    }

    public Mono<Integer> getInviteCount(String sessionIdentifier, long guildId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId).publishOn(Schedulers.boundedElastic()).mapNotNull(guildContainerOptional -> {
            if (guildContainerOptional.isEmpty()) {
                return 0;
            }

            return SQLSession.getSqlConnector().getSqlWorker().getInvites(guildId)
                    .map(List::size).block();
        });
    }

    //endregion

    //region Log channel

    public Mono<Optional<ChannelContainer>> getLogChannel(String sessionIdentifier, long guildId) {
        ChannelContainer errorReturnValue = null;
        return sessionService.retrieveGuild(sessionIdentifier, guildId, true).publishOn(Schedulers.boundedElastic()).mapNotNull(guildOptional -> {
            if (guildOptional.isEmpty()) {
                return Optional.empty();
            }

            GuildContainer guildContainer = guildOptional.get();

            return SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(guildId).publishOn(Schedulers.boundedElastic()).mapNotNull(webhookLogOptional -> {
                if (webhookLogOptional.isEmpty()) {
                    return Optional.ofNullable(ChannelContainer.DEFAULT);
                }

                WebhookLog webhook = webhookLogOptional.get();

                if (webhook.getChannelId() != 0) {
                    return Optional.of(new ChannelContainer(guildContainer.getGuildChannelById(webhook.getChannelId())));
                } else {
                    net.dv8tion.jda.api.entities.Webhook webhook1 = guildContainer.getGuild().retrieveWebhooks().complete().stream()
                            .filter(entry -> entry.getIdLong() == webhook.getWebhookId() && entry.getToken().equalsIgnoreCase(webhook.getToken())).findFirst().orElse(null);

                    if (webhook1 != null) {
                        webhook.setChannelId(webhook1.getChannel().getIdLong());
                        SQLSession.getSqlConnector().getSqlWorker().updateEntity(webhook).block();
                        return Optional.of(new ChannelContainer(webhook1));
                    }
                }

                return Optional.ofNullable(errorReturnValue);
            }).block();
        });
    }

    public Mono<Boolean> updateLogChannel(String sessionIdentifier, long guildId, String channelId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, true).publishOn(Schedulers.boundedElastic()).mapNotNull(guildOptional -> {
            if (guildOptional.isEmpty()) {
                return false;
            }

            GuildContainer guildContainer = guildOptional.get();

            Guild guild = guildContainer.getGuild();
            StandardGuildMessageChannel channel = guild.getChannelById(StandardGuildMessageChannel.class, channelId);

            net.dv8tion.jda.api.entities.Webhook newWebhook = channel.createWebhook("Ree6-Log").complete();

            deleteLogChannel(guild).block();

            SQLSession.getSqlConnector().getSqlWorker().setLogWebhook(guildId, channel.getIdLong(), newWebhook.getIdLong(), newWebhook.getToken());

            return true;
        });
    }

    public Mono<Optional<WebhookLog>> removeLogChannel(String sessionIdentifier, long guildId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId).publishOn(Schedulers.boundedElastic()).mapNotNull(x -> {
            if (x.isEmpty()) {
                return Optional.empty();
            }

            return deleteLogChannel(x.get().getGuild()).block();
        });
    }

    private Mono<Optional<WebhookLog>> deleteLogChannel(Guild guild) {
        return SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(guild.getIdLong())
                .publishOn(Schedulers.boundedElastic())
                .mapNotNull(webhookLogOptional -> {
            if (webhookLogOptional.isEmpty()) {
                return Optional.empty();
            }

            WebhookLog webhook = webhookLogOptional.get();
            guild.retrieveWebhooks().queue(c -> c.stream().filter(entry -> entry.getToken() != null)
                    .filter(entry -> entry.getIdLong() == webhook.getWebhookId() && entry.getToken().equalsIgnoreCase(webhook.getToken()))
                    .forEach(entry -> entry.delete().queue()));

            return SQLSession.getSqlConnector().getSqlWorker().deleteEntity(webhook).thenReturn(webhookLogOptional).block();
        });
    }

    //endregion

    //region Welcome channel

    public Mono<Optional<ChannelContainer>> getWelcomeChannel(String sessionIdentifier, long guildId) {
        ChannelContainer errorReturnValue = null;
        return sessionService.retrieveGuild(sessionIdentifier, guildId, true).publishOn(Schedulers.boundedElastic()).mapNotNull(guildOptional -> {
            if (guildOptional.isEmpty()) {
                return Optional.empty();
            }

            GuildContainer guildContainer = guildOptional.get();

            return SQLSession.getSqlConnector().getSqlWorker().getWelcomeWebhook(guildId).publishOn(Schedulers.boundedElastic()).mapNotNull(webhookLogOptional -> {
                if (webhookLogOptional.isEmpty()) {
                    return Optional.ofNullable(ChannelContainer.DEFAULT);
                }

                WebhookWelcome webhook = webhookLogOptional.get();

                if (webhook.getChannelId() != 0) {
                    return Optional.of(new ChannelContainer(guildContainer.getGuildChannelById(webhook.getChannelId())));
                } else {
                    net.dv8tion.jda.api.entities.Webhook webhook1 = guildContainer.getGuild().retrieveWebhooks().complete().stream()
                            .filter(entry -> entry.getIdLong() == webhook.getWebhookId() && entry.getToken().equalsIgnoreCase(webhook.getToken())).findFirst().orElse(null);

                    if (webhook1 != null) {
                        webhook.setChannelId(webhook1.getChannel().getIdLong());
                        SQLSession.getSqlConnector().getSqlWorker().updateEntity(webhook).block();
                        return Optional.of(new ChannelContainer(webhook1));
                    }
                }

                return Optional.ofNullable(errorReturnValue);
            }).block();
        });
    }

    public Mono<Boolean> updateWelcomeChannel(String sessionIdentifier, long guildId, String channelId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, true).publishOn(Schedulers.boundedElastic()).mapNotNull(guildOptional -> {
            if (guildOptional.isEmpty()) {
                return false;
            }

            GuildContainer guildContainer = guildOptional.get();

            Guild guild = guildContainer.getGuild();
            StandardGuildMessageChannel channel = guild.getChannelById(StandardGuildMessageChannel.class, channelId);

            net.dv8tion.jda.api.entities.Webhook newWebhook = channel.createWebhook("Ree6-Welcome").complete();

            deleteWelcomeChannel(guild).block();

            SQLSession.getSqlConnector().getSqlWorker().setWelcomeWebhook(guildId, channel.getIdLong(), newWebhook.getIdLong(), newWebhook.getToken());

            return true;
        });
    }

    public Mono<Optional<WebhookWelcome>> removeWelcomeChannel(String sessionIdentifier, long guildId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId).publishOn(Schedulers.boundedElastic()).mapNotNull(x -> {
            if (x.isEmpty()) {
                return Optional.empty();
            }

            return deleteWelcomeChannel(x.get().getGuild()).block();
        });
    }

    private Mono<Optional<WebhookWelcome>> deleteWelcomeChannel(Guild guild) {
        return SQLSession.getSqlConnector().getSqlWorker().getWelcomeWebhook(guild.getIdLong())
                .publishOn(Schedulers.boundedElastic())
                .mapNotNull(webhookLogOptional -> {
            if (webhookLogOptional.isEmpty()) {
                return Optional.empty();
            }

            WebhookWelcome webhook = webhookLogOptional.get();
            guild.retrieveWebhooks().queue(c -> c.stream().filter(entry -> entry.getToken() != null)
                    .filter(entry -> entry.getIdLong() == webhook.getWebhookId() && entry.getToken().equalsIgnoreCase(webhook.getToken()))
                    .forEach(entry -> entry.delete().queue()));

            return SQLSession.getSqlConnector().getSqlWorker().deleteEntity(webhook).thenReturn(webhookLogOptional).block();
        });
    }

    //endregion

    //region Notifier

    //region Reddit Notifications

    public Mono<List<NotifierContainer>> getRedditNotifier(String sessionIdentifier, long guildId) {
        return getNotifier(sessionIdentifier, guildId, 1);
    }

    public Mono<Boolean> addRedditNotifier(String sessionIdentifier, long guildId, GenericNotifierRequest notifierRequest) {
        return addNotifier(sessionIdentifier, guildId, notifierRequest, 1);
    }

    public Mono<Boolean> removeRedditNotifier(String sessionIdentifier, long guildId, String subreddit) {
        return removeNotifier(sessionIdentifier, guildId, subreddit, 1);
    }

    //endregion

    //region Twitch Notifications

    public Mono<List<NotifierContainer>> getTwitchNotifier(String sessionIdentifier, long guildId) {
        return getNotifier(sessionIdentifier, guildId, 2);
    }

    public Mono<Boolean> addTwitchNotifier(String sessionIdentifier, long guildId, GenericNotifierRequest notifierRequest) {
        return addNotifier(sessionIdentifier, guildId, notifierRequest, 2);
    }

    public Mono<Boolean> removeTwitchNotifier(String sessionIdentifier, long guildId, String channelId) {
        return removeNotifier(sessionIdentifier, guildId, channelId, 2);
    }

    //endregion

    //region YouTube Notifications

    public Mono<List<NotifierContainer>> getYouTubeNotifier(String sessionIdentifier, long guildId) {
        return getNotifier(sessionIdentifier, guildId, 0);
    }

    public Mono<Boolean> addYouTubeNotifier(String sessionIdentifier, long guildId, GenericNotifierRequest notifierRequest) {
        return addNotifier(sessionIdentifier, guildId, notifierRequest, 0);
    }

    public Mono<Boolean> removeYouTubeNotifier(String sessionIdentifier, long guildId, String channelId) {
        return removeNotifier(sessionIdentifier, guildId, channelId, 0);
    }

    //endregion

    //region Twitter Notifications

    public Mono<List<NotifierContainer>> getTwitterNotifier(String sessionIdentifier, long guildId) {
        return getNotifier(sessionIdentifier, guildId, 3);
    }

    public Mono<Boolean> addTwitterNotifier(String sessionIdentifier, long guildId, GenericNotifierRequest notifierRequest) {
        return addNotifier(sessionIdentifier, guildId, notifierRequest, 3);
    }

    public Mono<Boolean> removeTwitterNotifier(String sessionIdentifier, long guildId, String name) {
        return removeNotifier(sessionIdentifier, guildId, name, 3);
    }

    //endregion

    //region Instagram Notifications

    public Mono<List<NotifierContainer>> getInstagramNotifier(String sessionIdentifier, long guildId) {
        return getNotifier(sessionIdentifier, guildId, 4);
    }

    public Mono<Boolean> addInstagramNotifier(String sessionIdentifier, long guildId, GenericNotifierRequest notifierRequest) {
        return addNotifier(sessionIdentifier, guildId, notifierRequest, 4);
    }

    public Mono<Boolean> removeInstagramNotifier(String sessionIdentifier, long guildId, String name) {
        return removeNotifier(sessionIdentifier, guildId, name, 4);
    }

    //region General

    /**
     * Get the notifier based on the typ.
     *
     * @param sessionIdentifier the Session Identifier.
     * @param guildId           the Guild ID.
     * @param type              the typ. 0 -> YT, 1 -> Reddit, 2 -> Twitch, 3 -> Twitter, 4 -> Instagram
     * @return a list of the given notifier.
     */
    public Mono<List<NotifierContainer>> getNotifier(String sessionIdentifier, long guildId, int type) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId)
                .publishOn(Schedulers.boundedElastic())
                .mapNotNull(x -> {
                    if (x.isEmpty()) {
                        return Collections.emptyList();
                    }

                    GuildContainer guildContainer = x.get();

                    switch (type) {
                        case 1 -> {
                            return SQLSession.getSqlConnector().getSqlWorker().getAllRedditWebhooks(guildId)
                                    .map(webhooks -> webhooks.stream()
                                            .map(hooks -> new NotifierContainer(hooks.getSubreddit(), hooks.getMessage(), guildContainer.getGuild().retrieveWebhooks()
                                                    .complete().stream().filter(c -> c.getIdLong() == hooks.getGuild()).map(ChannelContainer::new).findFirst().orElse(null))).toList())
                                    .block();
                        }

                        case 2 -> {
                            return SQLSession.getSqlConnector().getSqlWorker().getAllTwitchWebhooks(guildId)
                                    .map(webhooks -> webhooks.stream()
                                            .map(hooks -> new NotifierContainer(hooks.getName(), hooks.getMessage(), guildContainer.getGuild().retrieveWebhooks()
                                                    .complete().stream().filter(c -> c.getIdLong() == hooks.getGuild()).map(ChannelContainer::new).findFirst().orElse(null))).toList())
                                    .block();
                        }

                        case 3 -> {
                            return SQLSession.getSqlConnector().getSqlWorker().getAllTwitterWebhooks(guildId)
                                    .map(webhooks -> webhooks.stream()
                                            .map(hooks -> new NotifierContainer(hooks.getName(), hooks.getMessage(), guildContainer.getGuild().retrieveWebhooks()
                                                    .complete().stream().filter(c -> c.getIdLong() == hooks.getGuild()).map(ChannelContainer::new).findFirst().orElse(null))).toList())
                                    .block();
                        }

                        case 4 -> {
                            return SQLSession.getSqlConnector().getSqlWorker().getAllInstagramWebhooks(guildId)
                                    .map(webhooks -> webhooks.stream()
                                            .map(hooks -> new NotifierContainer(hooks.getName(), hooks.getMessage(), guildContainer.getGuild().retrieveWebhooks()
                                                    .complete().stream().filter(c -> c.getIdLong() == hooks.getGuild()).map(ChannelContainer::new).findFirst().orElse(null))).toList())
                                    .block();
                        }

                        default -> {
                            return SQLSession.getSqlConnector().getSqlWorker().getAllYouTubeWebhooks(guildId)
                                    .map(webhooks -> webhooks.stream()
                                            .map(hooks -> new NotifierContainer(hooks.getName(), hooks.getMessage(), guildContainer.getGuild().retrieveWebhooks()
                                                    .complete().stream().filter(c -> c.getIdLong() == hooks.getGuild()).map(ChannelContainer::new).findFirst().orElse(null))).toList())
                                    .block();
                        }

                    }
                });
    }

    /**
     * Get the notifier based on the typ.
     *
     * @param sessionIdentifier the Session Identifier.
     * @param guildId           the Guild ID.
     * @param type              the typ. 0 -> YT, 1 -> Reddit, 2 -> Twitch, 3 -> Twitter, 4 -> Instagram
     * @return a list of the given notifier.
     */
    public Mono<Boolean> addNotifier(String sessionIdentifier, long guildId, GenericNotifierRequest notifierRequest, int type) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, true)
                .publishOn(Schedulers.boundedElastic())
                .mapNotNull(x -> {
                    if (x.isEmpty()) {
                        return false;
                    }

                    GuildContainer guildContainer = x.get();

                    Guild guild = guildContainer.getGuild();
                    StandardGuildMessageChannel channel = guild.getChannelById(StandardGuildMessageChannel.class, notifierRequest.channelId());

                    if (channel == null) {
                        return false;
                    }

                    String suffix;

                    switch (type) {
                        case 1 -> suffix = "RedditNotifier";
                        case 2 -> suffix = "TwitterNotifier";
                        case 3 -> suffix = "TwitchNotifier";
                        case 4 -> suffix = "InstagramNotifier";
                        default -> suffix = "YoutubeNotifier";
                    }

                    net.dv8tion.jda.api.entities.Webhook newWebhook = channel.createWebhook("Ree6-" + suffix + "-" + notifierRequest.name()).complete();


                    switch (type) {
                        case 1 ->
                                SQLSession.getSqlConnector().getSqlWorker().addRedditWebhook(guildId, channel.getIdLong(), newWebhook.getIdLong(), newWebhook.getToken(),
                                        notifierRequest.name(), notifierRequest.message());

                        case 2 ->
                                SQLSession.getSqlConnector().getSqlWorker().addTwitchWebhook(guildId, channel.getIdLong(), newWebhook.getIdLong(), newWebhook.getToken(),
                                        notifierRequest.name(), notifierRequest.message());

                        case 3 ->
                                SQLSession.getSqlConnector().getSqlWorker().addTwitterWebhook(guildId, channel.getIdLong(), newWebhook.getIdLong(), newWebhook.getToken(),
                                        notifierRequest.name(), notifierRequest.message());

                        case 4 ->
                                SQLSession.getSqlConnector().getSqlWorker().addInstagramWebhook(guildId, channel.getIdLong(), newWebhook.getIdLong(), newWebhook.getToken(),
                                        notifierRequest.name(), notifierRequest.message());

                        default ->
                                SQLSession.getSqlConnector().getSqlWorker().addYouTubeWebhook(guildId, channel.getIdLong(), newWebhook.getIdLong(), newWebhook.getToken(),
                                        notifierRequest.name(), notifierRequest.message());

                    }

                    return true;
                });
    }

    /**
     * Get the notifier based on the typ.
     *
     * @param sessionIdentifier the Session Identifier.
     * @param guildId           the Guild ID.
     * @param type              the typ. 0 -> YT, 1 -> Reddit, 2 -> Twitch, 3 -> Twitter, 4 -> Instagram
     * @return a list of the given notifier.
     */
    public Mono<Boolean> removeNotifier(String sessionIdentifier, long guildId, String name, int type) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, true)
                .publishOn(Schedulers.boundedElastic())
                .mapNotNull(x -> {
                    if (x.isEmpty()) {
                        return false;
                    }

                    switch (type) {
                        case 1 -> SQLSession.getSqlConnector().getSqlWorker().removeInstagramWebhook(guildId, name);

                        case 2 -> SQLSession.getSqlConnector().getSqlWorker().removeTwitchWebhook(guildId, name);

                        case 3 -> SQLSession.getSqlConnector().getSqlWorker().removeTwitterWebhook(guildId, name);

                        case 4 -> SQLSession.getSqlConnector().getSqlWorker().removeInstagramWebhook(guildId, name);

                        default -> SQLSession.getSqlConnector().getSqlWorker().removeYouTubeWebhook(guildId, name);

                    }

                    return true;
                });
    }

    //endregion

    //endregion

    //endregion

    //region LevelRewards

    //region Chat

    public Mono<Optional<List<RoleLevelContainer>>> getChatAutoRoles(String sessionIdentifier, long guildId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, false, true).publishOn(Schedulers.boundedElastic())
                .mapNotNull(guildContainerOptional -> {
                    if (guildContainerOptional.isEmpty()) {
                        return Optional.empty();
                    }

                    GuildContainer guildContainer = guildContainerOptional.get();

                    return SQLSession.getSqlConnector().getSqlWorker().getChatLevelRewards(guildId)
                            .mapNotNull(levelRewardMap -> Optional.of(levelRewardMap.entrySet().stream()
                                    .map(x -> new RoleLevelContainer(x.getKey(), guildContainer.getRoleById(x.getValue()))).toList())).block();
                });
    }

    public Mono<Boolean> addChatAutoRole(String sessionIdentifier, long guildId, long roleId, long level) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, false, true).map(x -> {
            if (x.isEmpty()) {
                return false;
            }

            GuildContainer guildContainer = x.get();

            if (guildContainer.getRoleById(roleId) == null)
                return false;

            SQLSession.getSqlConnector().getSqlWorker().addChatLevelReward(guildId, roleId, level);
            return true;
        });
    }

    public Mono<Boolean> removeChatAutoRole(String sessionIdentifier, long guildId, long level) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, false, false).map(x -> {
            if (x.isEmpty()) {
                return false;
            }

            SQLSession.getSqlConnector().getSqlWorker().removeChatLevelReward(guildId, level);
            return true;
        });
    }

    //endregion

    //region Voice

    public Mono<Optional<List<RoleLevelContainer>>> getVoiceAutoRoles(String sessionIdentifier, long guildId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, false, true).publishOn(Schedulers.boundedElastic())
                .mapNotNull(guildContainerOptional -> {
                    if (guildContainerOptional.isEmpty()) {
                        return Optional.empty();
                    }

                    GuildContainer guildContainer = guildContainerOptional.get();

                    return SQLSession.getSqlConnector().getSqlWorker().getVoiceLevelRewards(guildId)
                            .mapNotNull(levelRewardMap -> Optional.of(levelRewardMap.entrySet().stream()
                                    .map(x -> new RoleLevelContainer(x.getKey(), guildContainer.getRoleById(x.getValue()))).toList())).block();
                });
    }

    public Mono<Boolean> addVoiceAutoRole(String sessionIdentifier, long guildId, long roleId, long level) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, false, true).map(x -> {
            if (x.isEmpty()) {
                return false;
            }

            GuildContainer guildContainer = x.get();

            if (guildContainer.getRoleById(roleId) == null)
                return false;

            SQLSession.getSqlConnector().getSqlWorker().addVoiceLevelReward(guildId, roleId, level);
            return true;
        });
    }

    public Mono<Boolean> removeVoiceAutoRole(String sessionIdentifier, long guildId, long level) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, false, false).map(x -> {
            if (x.isEmpty()) {
                return false;
            }

            SQLSession.getSqlConnector().getSqlWorker().removeVoiceLevelReward(guildId, level);
            return true;
        });
    }

    //endregion

    //endregion

    //region Recording

    public Mono<Optional<Recording>> getRecording(String sessionIdentifier, String recordId) {
        Recording errorReturnValue = null;
        return sessionService.retrieveSession(sessionIdentifier).publishOn(Schedulers.boundedElastic()).mapNotNull(x -> {
            if (x.isEmpty()) {
                return Optional.ofNullable(errorReturnValue);
            }

            SessionContainer sessionContainer = x.get();

            return sessionService.retrieveGuilds(sessionIdentifier, false).publishOn(Schedulers.boundedElastic()).mapNotNull(y -> {
                if (y.isEmpty()) {
                    return Optional.ofNullable(errorReturnValue);
                }

                List<GuildContainer> guilds = y.get();

                return SQLSession.getSqlConnector().getSqlWorker().getEntity(new Recording(), "FROM Recording WHERE identifier=:id",
                        Map.of("id", recordId)).map(recordingOptional -> {
                    if (recordingOptional.isEmpty()) {
                        return Optional.ofNullable(errorReturnValue);
                    }

                    Recording recording = recordingOptional.get();

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
                            return recordingOptional;
                        } else {
                            log.warn("User {} tried accessing a recording he is not part of.", sessionContainer.getUser().getId());
                            return Optional.ofNullable(errorReturnValue);
                        }
                    } else {
                        log.warn("User {} tried accessing a recording he is not part of.", sessionContainer.getUser().getId());
                        return Optional.ofNullable(errorReturnValue);
                    }
                }).block();
            }).block();
        });
    }

    public Mono<Optional<RecordContainer>> getRecordingContainer(String sessionIdentifier, String recordId) {
        return getRecording(sessionIdentifier, recordId).map(x -> x.map(RecordContainer::new));
    }

    public Mono<Optional<byte[]>> getRecordingBytes(String sessionIdentifier, String recordId) {
        return getRecording(sessionIdentifier, recordId).publishOn(Schedulers.boundedElastic()).mapNotNull(x -> {
            if (x.isEmpty()) {
                return Optional.empty();
            }

            return SQLSession.getSqlConnector().getSqlWorker().deleteEntity(x.get()).thenReturn(Optional.ofNullable(x.get().getRecording())).block();
        });
    }

    //endregion

    //region Temporal Voice

    public Mono<Optional<ChannelContainer>> getTemporalVoice(String sessionIdentifier, long guildId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, true, false).publishOn(Schedulers.boundedElastic()).mapNotNull(guildContainerOptional -> {
            if (guildContainerOptional.isEmpty()) {
                return Optional.empty();
            }

            return SQLSession.getSqlConnector().getSqlWorker()
                    .getEntity(new TemporalVoicechannel(), "FROM TemporalVoicechannel WHERE guildChannelId.guildId=:gid", Map.of("gid", guildId))
                    .map(temporalVoicechannelOptional -> temporalVoicechannelOptional
                            .map(temporalVoicechannel -> guildContainerOptional.get().getChannelById(temporalVoicechannel.getVoiceChannelId()))
                            .or(() -> Optional.ofNullable(ChannelContainer.DEFAULT))).block();
        });
    }

    public Mono<Boolean> updateTemporalVoice(String sessionIdentifier, long guildId, long channelId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, true, false).publishOn(Schedulers.boundedElastic()).mapNotNull(guildContainerOptional -> {
            if (guildContainerOptional.isEmpty()) {
                return false;
            }

            GuildContainer guildContainer = guildContainerOptional.get();

            if (guildContainer.getChannelById(channelId) == null)
                return false;

            return SQLSession.getSqlConnector().getSqlWorker()
                    .getEntity(new TemporalVoicechannel(), "FROM TemporalVoicechannel WHERE guildChannelId.guildId=:gid", Map.of("gid", guildId))
                    .publishOn(Schedulers.boundedElastic())
                    .mapNotNull(temporalVoicechannelOptional -> {
                        TemporalVoicechannel temporalVoicechannel = temporalVoicechannelOptional.orElse(new TemporalVoicechannel(guildId, channelId));
                        if (temporalVoicechannelOptional.isPresent()) {
                            SQLSession.getSqlConnector().getSqlWorker().deleteEntity(temporalVoicechannel).block();
                            temporalVoicechannel.getGuildChannelId().setChannelId(channelId);
                        }

                        return SQLSession.getSqlConnector().getSqlWorker().updateEntity(temporalVoicechannel).thenReturn(true).block();
                    }).block();
        });
    }

    public Mono<Boolean> removeTemporalVoice(String sessionIdentifier, long guildId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, false, false).publishOn(Schedulers.boundedElastic()).mapNotNull(guildContainerOptional -> {
            if (guildContainerOptional.isEmpty()) {
                return false;
            }

            return SQLSession.getSqlConnector().getSqlWorker()
                    .getEntity(new TemporalVoicechannel(), "FROM TemporalVoicechannel WHERE guildId=:gid", Map.of("gid", guildId))
                    .publishOn(Schedulers.boundedElastic())
                    .mapNotNull(temporalVoicechannelOptional -> {
                        if (temporalVoicechannelOptional.isEmpty()) {
                            return false;
                        }

                        return SQLSession.getSqlConnector().getSqlWorker().deleteEntity(temporalVoicechannelOptional.get()).thenReturn(true).block();
                    }).block();
        });
    }

    //endregion

    //region OptOut

    public Mono<Optional<String>> checkOptOut(String sessionIdentifier, long guildId) {
        return sessionService.retrieveSession(sessionIdentifier).publishOn(Schedulers.boundedElastic()).mapNotNull(x -> {
            if (x.isEmpty()) {
                return Optional.empty();
            }

            return SQLSession.getSqlConnector().getSqlWorker().isOptOut(guildId, x.get().getUser().getId()).map(y -> Optional.of(y ? "optedOut" : "optedIn")).block();
        });
    }

    public Mono<Optional<String>> optOut(String sessionIdentifier, long guildId) {
        return sessionService.retrieveSession(sessionIdentifier).publishOn(Schedulers.boundedElastic()).mapNotNull(x -> {
            if (x.isEmpty()) {
                return Optional.empty();
            }

            SessionContainer sessionContainer = x.get();

            return SQLSession.getSqlConnector().getSqlWorker().isOptOut(guildId, sessionContainer.getUser().getId()).map(y -> {
                if (y) {
                    SQLSession.getSqlConnector().getSqlWorker().optIn(guildId, sessionContainer.getUser().getId());
                    return Optional.of("Opted in!");
                } else {
                    SQLSession.getSqlConnector().getSqlWorker().optOut(guildId, sessionContainer.getUser().getId());
                    return Optional.of("Opted out!");
                }

            }).block();
        });
    }

    //endregion

    //region Ticket

    public Mono<Optional<TicketContainer>> getTicket(String sessionIdentifier, long guildId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, true, false)
                .publishOn(Schedulers.boundedElastic())
                .mapNotNull(guildContainerOptional -> {
                    if (guildContainerOptional.isEmpty()) {
                        return Optional.empty();
                    }

                    GuildContainer guildContainer = guildContainerOptional.get();

                    return SQLSession.getSqlConnector().getSqlWorker().getEntity(new Tickets(), "FROM Tickets WHERE guildId=:gid", Map.of("gid", guildId))
                            .map(ticketOptional -> {
                                if (ticketOptional.isEmpty()) {
                                    return Optional.ofNullable(TicketContainer.DEFAULT);
                                }

                                Tickets tickets = ticketOptional.get();

                                TicketContainer ticketContainer = new TicketContainer();
                                ticketContainer.setTicketCount(tickets.getTicketCount());
                                ticketContainer.setChannel(guildContainer.getChannelById(tickets.getChannelId()));
                                ticketContainer.setCategory(guildContainer.getCategoryById(tickets.getTicketCategory()));

                                ChannelContainer logChannel = guildContainer.getChannelById(tickets.getLogChannelId());

                                if (tickets.getLogChannelId() == 0) {
                                    guildContainer.getGuild().retrieveWebhooks().queue(x -> {
                                        Optional<Tickets> updateTicketOptional = SQLSession.getSqlConnector().getSqlWorker().getEntity(new Tickets(), "FROM Tickets WHERE guildId=:gid", Map.of("gid", guildId)).block();
                                        if (updateTicketOptional == null || updateTicketOptional.isEmpty()) return;

                                        Tickets updateTickets = updateTicketOptional.get();

                                        net.dv8tion.jda.api.entities.Webhook webhook = x.stream().filter(entry -> entry.getToken() != null)
                                                .filter(entry -> entry.getToken().equalsIgnoreCase(tickets.getLogChannelWebhookToken()))
                                                .findFirst().orElse(null);

                                        if (webhook != null) {
                                            updateTickets.setLogChannelId(webhook.getChannel().getIdLong());
                                            SQLSession.getSqlConnector().getSqlWorker().updateEntity(updateTickets).block();
                                        }
                                    });
                                }

                                if (logChannel == null) {
                                    logChannel = new ChannelContainer();
                                }

                                ticketContainer.setLogChannel(logChannel);
                                Optional<Setting> setting = SQLSession.getSqlConnector().getSqlWorker().getSetting(guildId, "message_ticket_open").block();
                                if (setting.isPresent()) {
                                    ticketContainer.setTicketOpenMessage(setting.get().getStringValue());
                                }

                                setting = SQLSession.getSqlConnector().getSqlWorker().getSetting(guildId, "message_ticket_menu").block();
                                if (setting.isPresent()) {
                                    ticketContainer.setTicketMenuMessage(setting.get().getStringValue());
                                }

                                return Optional.of(ticketContainer);
                            })
                            .block();
                });
    }

    public Mono<Boolean> updateTicket(String sessionIdentifier, long guildId, long channelId, long logChannelId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, true, false)
                .publishOn(Schedulers.boundedElastic())
                .mapNotNull(guildContainerOptional -> {
                    if (guildContainerOptional.isEmpty()) {
                        return false;
                    }

                    GuildContainer guildContainer = guildContainerOptional.get();
                    Guild guild = guildContainer.getGuild();

                    return SQLSession.getSqlConnector().getSqlWorker().getEntity(new Tickets(),
                                    "FROM Tickets WHERE guildId=:gid", Map.of("gid", guildId))
                            .publishOn(Schedulers.boundedElastic())
                            .mapNotNull(ticketOptional -> {
                                if (ticketOptional.isEmpty()) {
                                    return false;
                                }

                                AtomicBoolean requireChannel = new AtomicBoolean(false);

                                Tickets tickets = ticketOptional.orElseGet(() -> {
                                    Tickets ticket = new Tickets();
                                    ticket.setChannelId(channelId);
                                    requireChannel.set(true);
                                    return ticket;
                                });

                                if (channelId != 0) {
                                    if (guildContainer.getChannelById(channelId) == null)
                                        return false;

                                    tickets.setChannelId(channelId);
                                } else if (requireChannel.get()) {
                                    return false;
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

                                return SQLSession.getSqlConnector().getSqlWorker().updateEntity(tickets).thenReturn(true).block();
                            })
                            .block();
                });
    }

    public Mono<Boolean> removeTicket(String sessionIdentifier, long guildId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, false, false)
                .publishOn(Schedulers.boundedElastic())
                .mapNotNull(guildContainerOptional -> {
                    if (guildContainerOptional.isEmpty()) {
                        return false;
                    }

                    GuildContainer guildContainer = guildContainerOptional.get();

                    return SQLSession.getSqlConnector().getSqlWorker().getEntity(new Tickets(),
                                    "FROM Tickets WHERE guildId=:gid", Map.of("gid", guildId))
                            .publishOn(Schedulers.boundedElastic())
                            .mapNotNull(ticketOptional -> {
                                if (ticketOptional.isEmpty()) {
                                    return false;
                                }

                                Tickets tickets = ticketOptional.get();

                                guildContainer.getGuild().retrieveWebhooks().queue(c -> c.stream().filter(entry -> entry.getToken() != null)
                                        .filter(entry -> entry.getIdLong() == tickets.getLogChannelId() && entry.getToken().equalsIgnoreCase(tickets.getLogChannelWebhookToken()))
                                        .forEach(entry -> entry.delete().queue()));

                                return SQLSession.getSqlConnector().getSqlWorker().deleteEntity(tickets).thenReturn(true).block();
                            })
                            .block();
                });
    }

    //endregion

    //region Suggestion

    public Mono<Optional<ChannelContainer>> getSuggestion(String sessionIdentifier, long guildId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, true, false).publishOn(Schedulers.boundedElastic())
                .mapNotNull(guildContainerOptional -> {
                    if (guildContainerOptional.isEmpty()) {
                        return Optional.empty();
                    }

                    GuildContainer guildContainer = guildContainerOptional.get();

                    return SQLSession.getSqlConnector().getSqlWorker().getEntity(new Suggestions(),
                                    "FROM Suggestions WHERE guildChannelId.guildId = :id", Map.of("id", guildId))
                            .map(suggestionOptional -> suggestionOptional
                                    .map(x -> guildContainer.getChannelById(suggestionOptional.get().getChannelId())).or(() -> Optional.of(ChannelContainer.DEFAULT)))
                            .block();
                });
    }

    public Mono<Boolean> updateSuggestion(String sessionIdentifier, long guildId, long channelId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, true, false).publishOn(Schedulers.boundedElastic())
                .mapNotNull(guildContainerOptional -> {
                    if (guildContainerOptional.isEmpty()) {
                        return false;
                    }

                    GuildContainer guildContainer = guildContainerOptional.get();

                    return SQLSession.getSqlConnector().getSqlWorker().getEntity(new Suggestions(),
                                    "FROM Suggestions WHERE guildChannelId.guildId = :id", Map.of("id", guildId))
                            .publishOn(Schedulers.boundedElastic())
                            .mapNotNull(suggestionOptional -> {
                                AtomicBoolean requireChannel = new AtomicBoolean(false);

                                Suggestions suggestion = suggestionOptional.orElseGet(() -> {
                                    Suggestions suggestions = new Suggestions();
                                    suggestions.setGuildId(guildId);
                                    requireChannel.set(true);
                                    return suggestions;
                                });

                                if (channelId != 0) {
                                    if (guildContainer.getChannelById(channelId) == null)
                                        return false;
                                } else if (requireChannel.get()) {
                                    return false;
                                }

                                return SQLSession.getSqlConnector().getSqlWorker().updateEntity(suggestion).thenReturn(true).block();
                            }).block();
                });
    }

    public Mono<Boolean> removeSuggestion(String sessionIdentifier, long guildId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, false, false).publishOn(Schedulers.boundedElastic())
                .mapNotNull(guildContainerOptional -> {
                    if (guildContainerOptional.isEmpty()) {
                        return false;
                    }

                    GuildContainer guildContainer = guildContainerOptional.get();

                    return SQLSession.getSqlConnector().getSqlWorker().getEntity(new Tickets(),
                                    "FROM Tickets WHERE guildId=:gid", Map.of("gid", guildId))
                            .publishOn(Schedulers.boundedElastic())
                            .mapNotNull(ticketOptional -> {
                                if (ticketOptional.isEmpty()) {
                                    return false;
                                }

                                Tickets tickets = ticketOptional.get();

                                guildContainer.getGuild().retrieveWebhooks().queue(c -> c.stream().filter(entry -> entry.getToken() != null)
                                        .filter(entry -> entry.getIdLong() == tickets.getLogChannelId() && entry.getToken().equalsIgnoreCase(tickets.getLogChannelWebhookToken()))
                                        .forEach(entry -> entry.delete().queue()));

                                return SQLSession.getSqlConnector().getSqlWorker().deleteEntity(tickets).thenReturn(true).block();
                            }).block();
                });
    }

//endregion

    //region Warnings

    public Mono<List<WarningContainer>> getWarnings(String sessionIdentifier, long guildId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, false, false)
                .publishOn(Schedulers.boundedElastic())
                .mapNotNull(guildContainerOptional -> {
                    if (guildContainerOptional.isEmpty()) {
                        return Collections.emptyList();
                    }

                    GuildContainer guildContainer = guildContainerOptional.get();

                    return SQLSession.getSqlConnector().getSqlWorker().getEntityList(new Warning(),
                                    "FROM Warning WHERE guildUserId.guildId = :gid",
                                    Map.of("gid", guildId))
                            .map(warnings -> warnings.stream()
                                    .map(c -> new WarningContainer(c, new UserContainer(guildContainer.getGuild().retrieveMemberById(c.getUserId()).complete()))).toList())
                            .block();
                });
    }

    public Mono<Optional<WarningContainer>> addWarnings(String sessionIdentifier, long guildId, long userId, String warnings) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, false, false)
                .publishOn(Schedulers.boundedElastic())
                .mapNotNull(guildContainerOptional -> {
                    if (guildContainerOptional.isEmpty()) {
                        return Optional.empty();
                    }

                    GuildContainer guildContainer = guildContainerOptional.get();

                    Member member = guildContainer.getGuild().retrieveMemberById(userId).complete();

                    if (member == null)
                        return Optional.empty();

                    return SQLSession.getSqlConnector().getSqlWorker().getEntity(new Warning(),
                                    "FROM Warning WHERE guildId = :gid AND userId = :uid",
                                    Map.of("gid", guildId, "uid", userId))
                            .publishOn(Schedulers.boundedElastic())
                            .mapNotNull(warningOptional -> {
                                Warning warning = warningOptional.orElse(new Warning(new GuildUserId(guildId, userId), 0));

                                int additionWarnings = 1;

                                try {
                                    additionWarnings = Integer.parseInt(warnings);
                                } catch (NumberFormatException ignored) {
                                }

                                warning.setWarnings(warning.getWarnings() + additionWarnings);

                                return SQLSession.getSqlConnector().getSqlWorker().updateEntity(warning)
                                        .map(updatedWarning -> Optional.of(new WarningContainer(updatedWarning, new UserContainer(member))))
                                        .block();
                            })
                            .block();
                });
    }

    public Mono<Optional<WarningContainer>> removeWarnings(String sessionIdentifier, long guildId, long userId, String warnings) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, false, false)
                .publishOn(Schedulers.boundedElastic())
                .mapNotNull(guildContainerOptional -> {
                    if (guildContainerOptional.isEmpty()) {
                        return Optional.empty();
                    }

                    GuildContainer guildContainer = guildContainerOptional.get();

                    Member member = guildContainer.getGuild().retrieveMemberById(userId).complete();

                    if (member == null) {
                        return Optional.empty();
                    }

                    return SQLSession.getSqlConnector().getSqlWorker().getEntity(new Warning(),
                                    "FROM Warning WHERE guildUserId.guildId = :gid AND guildUserId.userId = :uid",
                                    Map.of("gid", guildId, "uid", userId))
                            .publishOn(Schedulers.boundedElastic())
                            .mapNotNull(warningOptional -> {
                                Warning warning = warningOptional.orElse(new Warning(new GuildUserId(guildId, userId), 0));
                                int additionWarnings = 1;

                                try {
                                    additionWarnings = Integer.parseInt(warnings);
                                } catch (NumberFormatException ignored) {
                                }

                                warning.setWarnings(warning.getWarnings() - additionWarnings);

                                if (warning.getWarnings() < 0)
                                    warning.setWarnings(0);

                                return SQLSession.getSqlConnector().getSqlWorker().updateEntity(warning)
                                        .map(updatedWarning -> Optional.of(new WarningContainer(updatedWarning, new UserContainer(member))))
                                        .block();
                            })
                            .block();
                });
    }

    public Mono<Boolean> clearWarnings(String sessionIdentifier, long guildId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, false, false)
                .publishOn(Schedulers.boundedElastic())
                .mapNotNull(guildContainerOptional -> {
                    if (guildContainerOptional.isEmpty()) {
                        return false;
                    }

                    return SQLSession.getSqlConnector().getSqlWorker().getEntityList(new Warning(),
                            "FROM Warning WHERE guildUserId.guildId = :gid",
                            Map.of("gid", guildId)).map(warnings -> {
                        warnings.forEach(warning ->
                                SQLSession.getSqlConnector().getSqlWorker().deleteEntity(warning).block());
                        return true;
                    }).block();
                });
    }

    //region Punishments

    public Mono<List<PunishmentContainer>> getPunishments(String sessionIdentifier, long guildId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, false, true)
                .publishOn(Schedulers.boundedElastic())
                .mapNotNull(guildContainerOptional -> {
                    if (guildContainerOptional.isEmpty()) {
                        return Collections.emptyList();
                    }

                    GuildContainer guildContainer = guildContainerOptional.get();

                    return SQLSession.getSqlConnector().getSqlWorker().getEntityList(new Punishments(),
                                    "FROM Punishments WHERE guildAndId.guildId = :gid",
                                    Map.of("gid", guildId)).map(punishments -> punishments.stream().map(punishment -> new PunishmentContainer(punishment, guildContainer)).toList())
                            .block();
                });
    }

    public Mono<Boolean> clearPunishments(String sessionIdentifier, long guildId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, false, false)
                .publishOn(Schedulers.boundedElastic())
                .mapNotNull(guildContainerOptional -> {
                    if (guildContainerOptional.isEmpty()) {
                        return false;
                    }

                    return SQLSession.getSqlConnector().getSqlWorker().getEntityList(new Punishments(),
                            "FROM Punishments WHERE guildAndId.guildId = :gid",
                            Map.of("gid", guildId)).map(x -> {
                        x.forEach(punishment -> SQLSession.getSqlConnector().getSqlWorker().deleteEntity(punishment).block());
                        return true;
                    }).block();
                });
    }

    public Mono<Boolean> removePunishments(String sessionIdentifier, long guildId, String punishmentId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, false, false)
                .publishOn(Schedulers.boundedElastic())
                .mapNotNull(guildContainerOptional -> {
                    if (guildContainerOptional.isEmpty()) {
                        return false;
                    }

                    GuildContainer guildContainer = guildContainerOptional.get();

                    return SQLSession.getSqlConnector().getSqlWorker().getEntity(new Punishments(),
                                    "FROM Punishments WHERE guildAndId.guildId = :gid AND id = :id",
                                    Map.of("gid", guildId, "id", punishmentId))
                            .publishOn(Schedulers.boundedElastic())
                            .map(punishmentOptional -> {
                                if (punishmentOptional.isEmpty()) {
                                    return false;
                                }

                                Punishments punishments = punishmentOptional.get();

                                if (punishments.getGuild() != guildContainer.getGuild().getIdLong())
                                    return false;

                                SQLSession.getSqlConnector().getSqlWorker().deleteEntity(punishments).block();
                                return true;
                            })
                            .block();
                });
    }

    public Mono<Optional<PunishmentContainer>> addPunishments(String sessionIdentifier, long guildId, String neededWarnings, String action, String timeoutTime, long roleId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, false, true)
                .publishOn(Schedulers.boundedElastic())
                .mapNotNull(guildContainerOptional -> {
                    if (guildContainerOptional.isEmpty()) {
                        return Optional.empty();
                    }

                    GuildContainer guildContainer = guildContainerOptional.get();

                    Punishments punishments = new Punishments();
                    punishments.setGuildId(guildId);

                    try {
                        int warnings = Integer.parseInt(neededWarnings);
                        if (warnings < 0)
                            return Optional.empty();

                        int actionInt = Integer.parseInt(action);

                        if (actionInt < 0 || actionInt > 5)
                            return Optional.empty();

                        if (actionInt == 2 || actionInt == 3) {
                            if (roleId != 0 || guildContainer.getGuild().getRoleById(roleId) == null)
                                return Optional.empty();
                        }

                        long timeout = timeoutTime != null ? Long.parseLong(timeoutTime) : 0;

                        punishments.setWarnings(warnings);
                        punishments.setAction(actionInt);

                        if (timeoutTime != null)
                            punishments.setTimeoutTime(timeout);

                        if (roleId != 0)
                            punishments.setRoleId(roleId);
                    } catch (NumberFormatException e) {
                        return Optional.empty();
                    }

                    return SQLSession.getSqlConnector().getSqlWorker().updateEntity(punishments)
                            .map(x -> Optional.of(new PunishmentContainer(x, guildContainer)))
                            .block();
                });
    }

//endregion

//endregion

    //region Custom Command

    public Mono<List<CustomCommandContainer>> getCustomCommand(String sessionIdentifier, long guildId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, true, false)
                .publishOn(Schedulers.boundedElastic())
                .mapNotNull(guildContainerOptional -> {
                    if (guildContainerOptional.isEmpty()) {
                        return Collections.emptyList();
                    }

                    GuildContainer guildContainer = guildContainerOptional.get();

                    return SQLSession.getSqlConnector().getSqlWorker().getEntityList(new CustomCommand(),
                                    "FROM CustomCommand WHERE guildId = :gid",
                                    Map.of("gid", guildId))
                            .map(customCommands -> customCommands.stream().map(command -> new CustomCommandContainer(command, guildContainer)).toList())
                            .block();
                });
    }

    public Mono<Boolean> removeCustomCommand(String sessionIdentifier, long guildId, String commandId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, false, false)
                .publishOn(Schedulers.boundedElastic())
                .mapNotNull(guildContainerOptional -> {
                    if (guildContainerOptional.isEmpty()) {
                        return false;
                    }

                    return SQLSession.getSqlConnector().getSqlWorker().getEntity(new CustomCommand(),
                                    "FROM CustomCommand WHERE guildId = :gid AND id = :id",
                                    Map.of("gid", guildId, "id", commandId))
                            .publishOn(Schedulers.boundedElastic())
                            .mapNotNull(customCommandOptional -> {
                                if (customCommandOptional.isEmpty()) {
                                    return false;
                                }

                                CustomCommand customCommand = customCommandOptional.get();

                                return SQLSession.getSqlConnector().getSqlWorker().deleteEntity(customCommand).thenReturn(true).block();
                            }).block();
                });
    }

    public Mono<Optional<CustomCommandContainer>> addCustomCommand(String sessionIdentifier, long guildId, String commandName, String channelId, String response, String embedJson) {
        CustomCommandContainer errorReturnValue = null;
        return sessionService.retrieveGuild(sessionIdentifier, guildId, true, false)
                .publishOn(Schedulers.boundedElastic())
                .mapNotNull(guildContainerOptional -> {
                    if (guildContainerOptional.isEmpty()) {
                        return Optional.ofNullable(errorReturnValue);
                    }

                    return SQLSession.getSqlConnector().getSqlWorker().getEntity(new CustomCommand(),
                                    "FROM CustomCommand WHERE guildId = :gid AND command = :name",
                                    Map.of("gid", guildId, "name", commandName))
                            .publishOn(Schedulers.boundedElastic())
                            .mapNotNull(customCommandOptional -> {
                                CustomCommand customCommand = customCommandOptional.orElseGet(() -> {
                                    CustomCommand command = new CustomCommand();
                                    command.setGuildId(guildId);
                                    command.setName(commandName);
                                    return command;
                                });

                                if (response != null) {
                                    customCommand.setMessageResponse(response);
                                }

                                if (embedJson != null) {
                                    customCommand.setEmbedResponse(JsonParser.parseString(embedJson));
                                }

                                try {
                                    long channelIdNumber = Long.parseLong(channelId);
                                    customCommand.setChannelId(channelIdNumber);
                                } catch (NumberFormatException e) {
                                    return Optional.ofNullable(errorReturnValue);
                                }

                                return SQLSession.getSqlConnector().getSqlWorker().updateEntity(customCommand)
                                        .map(updated -> Optional.of(new CustomCommandContainer(updated, guildContainerOptional.get())))
                                        .block();
                            })
                            .block();
                });
    }


//endregion

    //region Reaction role

    public Mono<List<MessageReactionRoleContainer>> retrieveReactionRoles(String sessionIdentifier, long guildId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, true, true)
                .publishOn(Schedulers.boundedElastic())
                .mapNotNull(guildContainerOptional -> {
                    if (guildContainerOptional.isEmpty()) {
                        return Collections.emptyList();
                    }

                    GuildContainer guildContainer = guildContainerOptional.get();

                    Guild guild = guildContainer.getGuild();

                    return SQLSession.getSqlConnector().getSqlWorker().getEntityList(new ReactionRole(),
                                    "FROM ReactionRole WHERE guildUserId.guildId = :gid",
                                    Map.of("gid", guildId))
                            .map(roles -> {
                                Map<Long, List<ReactionRole>> map = roles.stream().collect(Collectors.groupingBy(ReactionRole::getMessageId));

                                List<MessageReactionRoleContainer> messageReactionRoleContainers = new ArrayList<>();
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
                            })
                            .block();
                });
    }

    public Mono<Boolean> addReactionRole(String sessionIdentifier, long guildId, String emojiId, String formattedEmoji, long channelId, String messageId, long roleId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, true, true)
                .publishOn(Schedulers.boundedElastic())
                .mapNotNull(guildContainerOptional -> {
                    if (guildContainerOptional.isEmpty()) {
                        return false;
                    }

                    GuildContainer guildContainer = guildContainerOptional.get();
                    Guild guild = guildContainer.getGuild();

                    RoleContainer role = guildContainer.getRoleById(roleId);

                    if (role == null) return false;

                    try {
                        long messageIdNumber = Long.parseLong(messageId);
                        long emojiIdNumber = Long.parseLong(emojiId);

                        if (formattedEmoji == null || formattedEmoji.isBlank()) {
                            return false;
                        }

                        Message message = guild.getTextChannelById(channelId).retrieveMessageById(messageIdNumber).complete();

                        if (message == null)
                            return false;

                        //message.addReaction(Emoji.fromFormatted(emojiIdNumber)).queue();

                        ReactionRole reactionRole = new ReactionRole();
                        reactionRole.setChannelId(channelId);
                        reactionRole.setEmoteId(emojiIdNumber);
                        reactionRole.setFormattedEmote(formattedEmoji);
                        reactionRole.setGuildId(guild.getIdLong());
                        reactionRole.setMessageId(messageIdNumber);
                        reactionRole.getGuildRoleId().setRoleId(role.getId());

                        return SQLSession.getSqlConnector().getSqlWorker().updateEntity(reactionRole).thenReturn(true).block();
                    } catch (NumberFormatException e) {
                        return false;
                    }
                });
    }

    public Mono<Boolean> removeReactionRole(String sessionIdentifier, long guildId, String emojiId, String messageId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId, true, true)
                .publishOn(Schedulers.boundedElastic())
                .mapNotNull(guildContainerOptional -> {
                    if (guildContainerOptional.isEmpty()) {
                        return false;
                    }

                    GuildContainer guildContainer = guildContainerOptional.get();

                    try {
                        long messageIdNumber = Long.parseLong(messageId);
                        long emojiIdNumber = Long.parseLong(emojiId);

                        //message.removeReaction(Emoji.fromFormatted(emojiIdNumber)).queue();

                        return SQLSession.getSqlConnector().getSqlWorker().getEntity(new ReactionRole(),
                                        "FROM ReactionRole WHERE guildAndId.guildId = :gid AND messageId = :mid AND emoteId = :eid",
                                        Map.of("gid", guildId, "mid", messageId, "eid", emojiId))
                                .publishOn(Schedulers.boundedElastic())
                                .mapNotNull(reactionRoleOptional -> {
                                    if (reactionRoleOptional.isEmpty()) {
                                        return false;
                                    }

                                    return SQLSession.getSqlConnector().getSqlWorker().deleteEntity(reactionRoleOptional.get()).thenReturn(true).block();
                                }).block();
                    } catch (NumberFormatException e) {
                        return false;
                    }
                });
    }

//endregion
}