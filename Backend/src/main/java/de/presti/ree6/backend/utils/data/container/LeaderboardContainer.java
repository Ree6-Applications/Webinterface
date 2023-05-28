package de.presti.ree6.backend.utils.data.container;

import de.presti.ree6.backend.utils.data.container.user.UserLevelContainer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardContainer {

    String guildId;

    List<UserLevelContainer> chatLeaderboard;

    List<UserLevelContainer> voiceLeaderboard;
}
