package de.presti.ree6.backend.utils.data.container.guild;

import de.presti.ree6.backend.utils.data.container.CommandStatsContainer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GuildStatsContainer {

    private long invites;
    private List<CommandStatsContainer> commandStats;

}
