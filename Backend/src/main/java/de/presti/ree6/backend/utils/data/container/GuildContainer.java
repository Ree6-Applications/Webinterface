package de.presti.ree6.backend.utils.data.container;

import com.jagrosh.jdautilities.oauth2.entities.OAuth2Guild;
import de.presti.ree6.backend.bot.BotWorker;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;
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

    public GuildContainer(OAuth2Guild oAuth2Guild) {
        this(oAuth2Guild.getIdLong(), oAuth2Guild.getName(), oAuth2Guild.getIconUrl() != null ? oAuth2Guild.getIconUrl() : "",
                oAuth2Guild.botJoined(BotWorker.getShardManager()), Collections.emptyList());
    }

    public GuildContainer(Guild guild) {
        this(guild.getIdLong(), guild.getName(), guild.getIconUrl() != null ? guild.getIconUrl() : "",
                BotWorker.getShardManager().getGuildById(guild.getId()) != null, Collections.emptyList());
    }

    public GuildContainer(Guild guild, boolean retrieveChannels) {
        this(guild);
        if (retrieveChannels) {
            channels = guild.getChannels();
        }
    }

}
