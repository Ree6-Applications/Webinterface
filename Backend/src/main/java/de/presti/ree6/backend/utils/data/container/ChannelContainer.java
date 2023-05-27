package de.presti.ree6.backend.utils.data.container;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.attribute.IWebhookContainer;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChannelContainer {

    String id;
    String guildId;
    String name;
    ChannelType type;

    public ChannelContainer(GuildChannel guildChannel) {
        this.id = guildChannel.getId();
        this.guildId = guildChannel.getGuild().getId();
        this.name = guildChannel.getName();
        this.type = guildChannel.getType();
    }

    public ChannelContainer(Webhook webhook) {
        IWebhookContainer webhookContainer = webhook.getChannel();
        this.id = webhookContainer.getId();
        this.guildId = webhook.getSourceGuild().getId();
        this.name = webhookContainer.getName();
        this.type = webhookContainer.getType();
    }
}
