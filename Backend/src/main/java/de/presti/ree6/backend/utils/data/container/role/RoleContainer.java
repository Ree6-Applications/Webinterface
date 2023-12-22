package de.presti.ree6.backend.utils.data.container.role;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Role;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleContainer {

    long id;
    long guildId;
    String name;
    String iconUrl;
    int color;

    public RoleContainer(Role role) {
        id = role.getIdLong();
        name = role.getName();
        guildId = role.getGuild().getIdLong();
        iconUrl = role.getIcon() != null ? role.getIcon().getIconUrl() : "";
        color = role.getColorRaw();
    }
}
