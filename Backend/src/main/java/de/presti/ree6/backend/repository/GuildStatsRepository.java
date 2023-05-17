package de.presti.ree6.backend.repository;

import de.presti.ree6.sql.entities.stats.GuildCommandStats;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuildStatsRepository extends CrudRepository<GuildCommandStats, Integer> {

    List<GuildCommandStats> getGuildCommandStatsByGuildId(@Param("gid") String guildId);

    GuildCommandStats getGuildCommandStatsByGuildIdAndCommand(@Param("gid") String guildId, @Param("command") String command);
}
