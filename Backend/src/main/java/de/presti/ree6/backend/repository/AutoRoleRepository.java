package de.presti.ree6.backend.repository;

import de.presti.ree6.sql.entities.roles.AutoRole;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutoRoleRepository extends CrudRepository<AutoRole, Integer> {

    List<AutoRole> getAutoRoleByGuildId(@Param("gid")String guildId);

    AutoRole getAutoRoleByGuildIdAndRoleId(@Param("gid")String guildId, @Param("rid")String roleId);

}
