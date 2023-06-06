package de.presti.ree6.backend.utils.data.container;

import de.presti.ree6.backend.utils.data.container.user.UserContainer;
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

    UserContainer user;
    String guildId;
    String warnings;

    public WarningContainer(Warning warning) {
        guildId = String.valueOf(warning.getGuildId());
        warnings = String.valueOf(warning.getWarnings());
    }

    public WarningContainer(Warning warning, UserContainer userContainer) {
        this(warning);
        user = userContainer;
    }
}
