package de.presti.ree6.backend.utils.data.container;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    long uses;

}
