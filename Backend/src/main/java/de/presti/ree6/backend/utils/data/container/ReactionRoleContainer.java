package de.presti.ree6.backend.utils.data.container;

import de.presti.ree6.backend.utils.data.container.guild.GuildContainer;
import de.presti.ree6.backend.utils.data.container.role.RoleContainer;
import de.presti.ree6.sql.entities.ReactionRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReactionRoleContainer {

    RoleContainer role;
    String emoji;
    EmojiUnion emojiUnion;

    public ReactionRoleContainer(ReactionRole reactionRole, GuildContainer guildContainer) {
        role = guildContainer.getRoleById(String.valueOf(reactionRole.getRoleId()));
        emoji = reactionRole.getFormattedEmote();
        emojiUnion = Emoji.fromFormatted(reactionRole.getFormattedEmote());
    }

}
