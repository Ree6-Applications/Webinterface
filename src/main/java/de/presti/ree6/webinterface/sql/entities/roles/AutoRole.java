package de.presti.ree6.webinterface.sql.entities.roles;

import de.presti.ree6.webinterface.sql.base.annotations.Table;

/**
 * SQL Entity for the Auto-Roles.
 */
@Table(name = "AutoRoles")
public class AutoRole extends Role {

    /**
     * Constructor.
     */
    public AutoRole() {
    }

    /**
     * @inheritDoc
     */
    public AutoRole(String guildId, String roleId) {
        super(guildId, roleId);
    }
}