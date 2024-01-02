package de.presti.ree6.backend.utils.data.container;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketContainer {

    public ChannelContainer channel;

    public ChannelContainer category;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public long ticketCount;

    public ChannelContainer logChannel;

    public String ticketMenuMessage;

    public String ticketOpenMessage;
}
