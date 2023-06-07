package de.presti.ree6.backend.utils.data.container;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Message;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageContainer {

    String id;
    String channelId;
    String guildId;
    String message;
    String embedJson;

    public MessageContainer(Message message) {
        this.id = message.getId();
        this.channelId = message.getChannel().getId();
        this.guildId = message.getGuild().getId();
        this.message = message.getContentRaw();
        this.embedJson = message.getEmbeds().isEmpty() ? null : message.getEmbeds().get(0).toData().toString();
    }
}
