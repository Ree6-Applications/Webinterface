package de.presti.ree6.backend.utils.data.container;

import de.presti.ree6.sql.entities.stats.GuildCommandStats;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommandStatsContainer {

    public CommandStatsContainer(GuildCommandStats commandStats) {
        this(commandStats.getCommand(), commandStats.getUses());
    }

    String command;
    long uses;

}
