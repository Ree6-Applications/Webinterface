package de.presti.ree6.backend.utils.data.container;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jagrosh.jdautilities.oauth2.entities.OAuth2Guild;
import de.presti.ree6.backend.bot.BotWorker;
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

    List<GuildChannel> channels;

    List<Role> roles;

    public GuildContainer(long id, String name, String iconUrl, boolean hasBot) {
        this(id, name, iconUrl, hasBot, Collections.emptyList(), Collections.emptyList());
    }

    public GuildContainer(OAuth2Guild oAuth2Guild) {
        this(oAuth2Guild.getIdLong(), oAuth2Guild.getName(), oAuth2Guild.getIconUrl() != null ? oAuth2Guild.getIconUrl() : "",
                oAuth2Guild.botJoined(BotWorker.getShardManager()));
    }

    public GuildContainer(Guild guild) {
        this(guild.getIdLong(), guild.getName(), guild.getIconUrl() != null ? guild.getIconUrl() : "",
                BotWorker.getShardManager().getGuildById(guild.getId()) != null);
    }

    public GuildContainer(Guild guild, boolean retrieveChannels) {
        this(guild, retrieveChannels, false);
    }

    public GuildContainer(Guild guild, boolean retrieveChannels, boolean retrieveRoles) {
        this(guild);

        if (retrieveChannels) {
            channels = guild.getChannels();
        }

        if (retrieveRoles) {
            roles = guild.getRoles();
        }
    }

    public Role getRoleById(String id) {
        return getRoles().stream().filter(role -> role.getId().equals(id)).findFirst().orElse(null);
    }

    public GuildChannel getChannelById(String id) {
        return getChannels().stream().filter(channel -> channel.getId().equals(id)).findFirst().orElse(null);
    }

}
