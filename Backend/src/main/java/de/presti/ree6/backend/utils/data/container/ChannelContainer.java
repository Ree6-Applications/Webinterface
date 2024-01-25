package de.presti.ree6.backend.utils.data.container;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.attribute.IWebhookContainer;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChannelContainer {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    long guildId;

    String name;
    ChannelType type;

    public ChannelContainer(StandardGuildMessageChannel guildChannel) {
        this.id = guildChannel.getIdLong();
        this.guildId = guildChannel.getGuild().getIdLong();
        this.name = guildChannel.getName();
        this.type = guildChannel.getType();
    }

    public ChannelContainer(GuildChannel guildChannel) {
        this.id = guildChannel.getIdLong();
        this.guildId = guildChannel.getGuild().getIdLong();
        this.name = guildChannel.getName();
        this.type = guildChannel.getType();
    }

    public ChannelContainer(Webhook webhook) {
        IWebhookContainer webhookContainer = webhook.getChannel();
        this.id = webhookContainer.getIdLong();
        this.guildId = webhook.getGuild().getIdLong();
        this.name = webhookContainer.getName();
        this.type = webhookContainer.getType();
    }

    public ChannelContainer(Category category) {
        this.id = category.getIdLong();
        this.guildId = category.getGuild().getIdLong();
        this.name = category.getName();
        this.type = category.getType();
    }
}
