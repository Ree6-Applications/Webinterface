package de.presti.ree6.backend.utils.data.container;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
}
