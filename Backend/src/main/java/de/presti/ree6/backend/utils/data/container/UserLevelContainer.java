package de.presti.ree6.backend.utils.data.container;

import de.presti.ree6.sql.entities.level.UserLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.entities.User;

/**
 * Container class to store a UserLevel and the User.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserLevelContainer {

    /**
     * The UserLevel.
     */
    public UserLevel userLevel;

    /**
     * The User.
     */
    public UserContainer user;
}
