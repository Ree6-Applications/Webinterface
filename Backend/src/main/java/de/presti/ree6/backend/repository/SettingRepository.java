package de.presti.ree6.backend.repository;

import de.presti.ree6.sql.entities.Setting;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface SettingRepository extends ReactiveCrudRepository<Setting, Integer> {

    Flux<Setting> getSettingsByGuild(@Param("gid") String guildId);

    Mono<Setting> getSettingByGuildAndName(@Param("gid") String guildId, @Param("name") String name);
}
