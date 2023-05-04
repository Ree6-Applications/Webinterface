package de.presti.ree6.backend.repository;

import de.presti.ree6.sql.entities.level.VoiceUserLevel;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface VoiceLevelRepository extends ReactiveCrudRepository<VoiceUserLevel, Integer> {

    Flux<VoiceUserLevel> getChatUserLevelsByGuild(@Param("gid") String guildId);

}
