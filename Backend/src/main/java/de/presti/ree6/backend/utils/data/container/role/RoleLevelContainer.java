package de.presti.ree6.backend.utils.data.container.role;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonRawValue;
import de.presti.ree6.backend.bot.BotWorker;
import de.presti.ree6.sql.entities.roles.ChatAutoRole;
import de.presti.ree6.sql.entities.roles.VoiceAutoRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleLevelContainer {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    long level;

    RoleContainer role;

    public RoleLevelContainer(VoiceAutoRole voiceAutoRole) {
        this(voiceAutoRole.getLevel(), new RoleContainer(BotWorker.getShardManager().getRoleById(voiceAutoRole.getRoleId())));
    }

    public RoleLevelContainer(ChatAutoRole chatAutoRole) {
        this(chatAutoRole.getLevel(), new RoleContainer(BotWorker.getShardManager().getRoleById(chatAutoRole.getRoleId())));
    }
}
