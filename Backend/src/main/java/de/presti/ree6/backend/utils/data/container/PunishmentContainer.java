package de.presti.ree6.backend.utils.data.container;

import com.fasterxml.jackson.annotation.JsonFormat;
import de.presti.ree6.backend.utils.data.container.guild.GuildContainer;
import de.presti.ree6.backend.utils.data.container.role.RoleContainer;
import de.presti.ree6.sql.entities.Punishments;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PunishmentContainer {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    long punishmentId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    long guildId;
    String neededWarnings;
    String action;
    String timeoutTime;
    RoleContainer role;

    public PunishmentContainer(Punishments punishments) {
        punishmentId = punishments.getId();
        guildId = punishments.getGuild();
        neededWarnings = String.valueOf(punishments.getWarnings());
        action = String.valueOf(punishments.getAction());
        timeoutTime = String.valueOf(punishments.getTimeoutTime());
    }

    public PunishmentContainer(Punishments punishments, GuildContainer guildContainer) {
        this(punishments);
        role = guildContainer.getRoleById(punishments.getRoleId());
    }

}
