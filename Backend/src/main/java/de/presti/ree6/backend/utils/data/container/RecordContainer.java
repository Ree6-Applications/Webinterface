package de.presti.ree6.backend.utils.data.container;

import com.google.gson.JsonArray;
import de.presti.ree6.backend.bot.BotWorker;
import de.presti.ree6.backend.utils.data.container.guild.GuildContainer;
import de.presti.ree6.backend.utils.data.container.user.UserContainer;
import de.presti.ree6.sql.entities.Recording;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.tomcat.util.codec.binary.Base64;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecordContainer {

    String data;
    UserContainer creator;

    public RecordContainer(Recording recording) {
        this.data = Base64.encodeBase64String(recording.getRecording());
        this.creator = new UserContainer(BotWorker.getShardManager().retrieveUserById(recording.getCreatorId()).complete());
    }
}
