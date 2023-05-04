package de.presti.ree6.backend.repository;

import de.presti.ree6.sql.entities.level.VoiceUserLevel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoiceLevelRepository extends CrudRepository<VoiceUserLevel, Integer> {

    List<VoiceUserLevel> getChatUserLevelsByGuildId(@Param("gid") String guildId);

}
