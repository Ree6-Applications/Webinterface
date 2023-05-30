package de.presti.ree6.backend.utils.data.container.user;

import de.presti.ree6.sql.entities.level.UserLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
