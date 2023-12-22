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
    long guildId;
    int warnings;

    public WarningContainer(Warning warning) {
        guildId = warning.getGuildId();
        warnings = warning.getWarnings();
    }

    public WarningContainer(Warning warning, UserContainer userContainer) {
        this(warning);
        user = userContainer;
    }
}
