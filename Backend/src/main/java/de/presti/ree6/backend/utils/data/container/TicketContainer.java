package de.presti.ree6.backend.utils.data.container;

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

    public long ticketCount;

    public ChannelContainer logChannel;

    public String ticketMenuMessage;

    public String ticketOpenMessage;
}
