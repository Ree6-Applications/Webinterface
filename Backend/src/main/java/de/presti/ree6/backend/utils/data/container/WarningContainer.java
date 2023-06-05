package de.presti.ree6.backend.utils.data.container;

import de.presti.ree6.sql.entities.Warning;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WarningContainer {

    String userId;
    String guildId;
    String warnings;

    public WarningContainer(Warning warning) {
        userId = String.valueOf(warning.getUserId());
        guildId = String.valueOf(warning.getGuildId());
        warnings = String.valueOf(warning.getWarnings());
    }
}
