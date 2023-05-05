package de.presti.ree6.backend.repository;

import de.presti.ree6.sql.entities.Blacklist;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlacklistRepository extends CrudRepository<Blacklist, Integer> {


    List<Blacklist> getBlacklistByGuildId(@Param("gid")String guildId);
    Blacklist getBlacklistByGuildIdAndWord(@Param("gid")String guildId, @Param("word")String word);
}
