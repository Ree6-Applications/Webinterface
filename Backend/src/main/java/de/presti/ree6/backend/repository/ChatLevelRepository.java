package de.presti.ree6.backend.repository;

import de.presti.ree6.sql.entities.level.ChatUserLevel;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ChatLevelRepository extends ReactiveCrudRepository<ChatUserLevel, Integer> {

    Flux<ChatUserLevel> getChatUserLevelsByGuild(@Param("gid") String guildId);

}
