package de.presti.ree6.backend.utils.data.container.guild;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jagrosh.jdautilities.oauth2.entities.OAuth2Guild;
import de.presti.ree6.backend.bot.BotWorker;
import de.presti.ree6.backend.utils.data.Data;
import de.presti.ree6.backend.utils.data.container.ChannelContainer;
import de.presti.ree6.backend.utils.data.container.role.RoleContainer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GuildContainer {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    long id;

    String name;

    String iconUrl;

    boolean hasBot;

    boolean isAdmin;

    List<ChannelContainer> channels;

    List<RoleContainer> roles;

    @JsonIgnore
    ArrayList<Role> guildRoles = new ArrayList<>();

    @JsonIgnore
    ArrayList<GuildChannel> guildChannels = new ArrayList<>();

    @JsonIgnore
    Guild guild;

    public GuildContainer(long id, String name, String iconUrl, boolean hasBot, boolean isAdmin) {
        this(id, name, iconUrl, hasBot, isAdmin, Collections.emptyList(), Collections.emptyList(), new ArrayList<>(), new ArrayList<>(), null);
    }

    public GuildContainer(OAuth2Guild oAuth2Guild) {
        this(oAuth2Guild.getIdLong(), oAuth2Guild.getName(), oAuth2Guild.getIconUrl() != null ? oAuth2Guild.getIconUrl() : Data.defaultIconUrl,
                oAuth2Guild.botJoined(BotWorker.getShardManager()), oAuth2Guild.hasPermission(Permission.ADMINISTRATOR));
    }

    public GuildContainer(Guild guild) {
        this(guild.getIdLong(), guild.getName(), guild.getIconUrl() != null ? guild.getIconUrl() : Data.defaultIconUrl,
                BotWorker.getShardManager().getGuildById(guild.getId()) != null, false);
        this.guild = guild;
    }

    public GuildContainer(Guild guild, boolean retrieveChannels) {
        this(guild, retrieveChannels, false);
    }

    public GuildContainer(Guild guild, boolean retrieveChannels, boolean retrieveRoles) {
        this(guild);

        if (retrieveChannels) {
            getGuildChannels().clear();
            getGuildChannels().addAll(guild.getChannels());
            setChannels(getGuildChannels().stream().map(ChannelContainer::new).toList());
        } else {
            setChannels(Collections.emptyList());
        }

        if (retrieveRoles) {
            getGuildRoles().clear();
            getGuildRoles().addAll(guild.getRoles());
            getGuildRoles().remove(getGuildRoles().size() - 1);
            setRoles(getGuildRoles().stream().map(RoleContainer::new).toList());
        } else {
            setRoles(Collections.emptyList());
        }
    }

    public RoleContainer getRoleById(long id) {
        return getRoles().stream().filter(role -> role.getId() == id).findFirst().orElse(null);
    }

    public ChannelContainer getChannelById(long id) {
        return getChannels().stream().filter(channel -> channel.getId() == id).findFirst().orElse(null);
    }

    public Role getGuildRoleById(long id) {
        return getGuildRoles().stream().filter(role -> role.getIdLong() == id).findFirst().orElse(null);
    }

    public GuildChannel getGuildChannelById(long id) {
        return getGuildChannels().stream().filter(channel -> channel.getIdLong() == id).findFirst().orElse(null);
    }

    public ChannelContainer getCategoryById(long id) {
        return getChannels().stream().filter(channel -> channel.getId() == id && channel.getType() == ChannelType.CATEGORY).findFirst().orElse(null);
    }

    public Guild getGuild() {
        if (guild == null) {
            return BotWorker.getShardManager().getGuildById(getId());
        }

        return guild;
    }

}
