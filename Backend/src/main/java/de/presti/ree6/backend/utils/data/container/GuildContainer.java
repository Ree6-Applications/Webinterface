package de.presti.ree6.backend.utils.data.container;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jagrosh.jdautilities.oauth2.entities.OAuth2Guild;
import de.presti.ree6.backend.bot.BotWorker;
import de.presti.ree6.backend.utils.data.Data;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GuildContainer {

    long id;

    String name;

    String iconUrl;

    boolean hasBot;

    List<ChannelContainer> channels;

    List<RoleContainer> roles;

    @JsonIgnore
    List<Role> guildRoles;

    @JsonIgnore
    List<GuildChannel> guildChannels;

    public GuildContainer(long id, String name, String iconUrl, boolean hasBot) {
        this(id, name, iconUrl, hasBot, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    public GuildContainer(OAuth2Guild oAuth2Guild) {
        this(oAuth2Guild.getIdLong(), oAuth2Guild.getName(), oAuth2Guild.getIconUrl() != null ? oAuth2Guild.getIconUrl() : Data.defaultIconUrl,
                oAuth2Guild.botJoined(BotWorker.getShardManager()));
    }

    public GuildContainer(Guild guild) {
        this(guild.getIdLong(), guild.getName(), guild.getIconUrl() != null ? guild.getIconUrl() : Data.defaultIconUrl,
                BotWorker.getShardManager().getGuildById(guild.getId()) != null);
    }

    public GuildContainer(Guild guild, boolean retrieveChannels) {
        this(guild, retrieveChannels, false);
    }

    public GuildContainer(Guild guild, boolean retrieveChannels, boolean retrieveRoles) {
        setId(guild.getIdLong());
        setName(guild.getName());
        setIconUrl(guild.getIconUrl() != null ? guild.getIconUrl() : Data.defaultIconUrl);
        setHasBot(BotWorker.getShardManager().getGuildById(guild.getId()) != null);

        if (retrieveChannels) {
            setGuildChannels(guild.getChannels());
            setChannels(getGuildChannels().stream().map(ChannelContainer::new).toList());
        } else {
            setChannels(Collections.emptyList());
        }

        if (retrieveRoles) {
            setGuildRoles(guild.getRoles());
            setRoles(getGuildRoles().stream().map(RoleContainer::new).toList());
        } else {
            setRoles(Collections.emptyList());
        }
    }

    public RoleContainer getRoleById(String id) {
        return getRoles().stream().filter(role -> role.getId().equals(id)).findFirst().orElse(null);
    }

    public ChannelContainer getChannelById(String id) {
        return getChannels().stream().filter(channel -> channel.getId().equals(id)).findFirst().orElse(null);
    }

    public Role getGuildRoleById(String id) {
        return getGuildRoles().stream().filter(role -> role.getId().equals(id)).findFirst().orElse(null);
    }

    public GuildChannel getGuildChannelById(String id) {
        return getGuildChannels().stream().filter(channel -> channel.getId().equals(id)).findFirst().orElse(null);
    }

}