package de.presti.ree6.backend.repository;

import de.presti.ree6.sql.entities.Setting;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettingRepository extends CrudRepository<Setting, Integer> {

    List<Setting> getSettingsByGuildId(@Param("gid") String guildId);

    Setting getSettingByGuildIdAndName(@Param("gid") String guildId, @Param("name") String name);
}
