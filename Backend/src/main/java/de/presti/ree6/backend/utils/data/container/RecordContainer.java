package de.presti.ree6.backend.utils.data.container;

import de.presti.ree6.backend.bot.BotWorker;
import de.presti.ree6.backend.utils.data.container.user.UserContainer;
import de.presti.ree6.sql.entities.Recording;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecordContainer {

    String creationTime;
    String guildId;
    ChannelContainer voiceChannel;
    UserContainer creator;

    public RecordContainer(Recording recording) {
        ///this.data = Base64.encodeBase64String(recording.getRecording());
        this.creationTime = String.valueOf(recording.getCreation());
        this.guildId = recording.getGuildId();
        this.voiceChannel = new ChannelContainer(BotWorker.getShardManager().getChannelById(StandardGuildMessageChannel.class, recording.getVoiceId()));
        this.creator = new UserContainer(BotWorker.getShardManager().retrieveUserById(recording.getCreatorId()).complete());
    }
}
