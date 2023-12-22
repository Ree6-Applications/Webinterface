package de.presti.ree6.backend.utils.data.container;

import de.presti.ree6.backend.utils.data.container.guild.GuildContainer;
import de.presti.ree6.sql.entities.custom.CustomCommand;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomCommandContainer {

    String name;
    ChannelContainer channel;
    String message;
    String embedJson;

    public CustomCommandContainer(CustomCommand command) {
        name = command.getName();
        message = command.getMessageResponse();
        embedJson = command.getEmbedResponse().toString();
    }

    public CustomCommandContainer(CustomCommand command, GuildContainer guildContainer) {
        this(command);
        if (command.getChannelId() != -1) {
            channel = guildContainer.getChannelById(command.getChannelId());
        }
    }
}
