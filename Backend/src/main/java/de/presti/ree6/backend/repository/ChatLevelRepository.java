package de.presti.ree6.backend.repository;

import de.presti.ree6.sql.entities.level.ChatUserLevel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatLevelRepository extends CrudRepository<ChatUserLevel, Integer> {

    List<ChatUserLevel> getFirst5getChatUserLevelsByGuildIdOrderByExperienceDesc(@Param("gid") String guildId);

}
