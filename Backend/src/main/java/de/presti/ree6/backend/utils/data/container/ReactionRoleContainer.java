package de.presti.ree6.backend.utils.data.container;

import de.presti.ree6.backend.utils.data.container.role.RoleContainer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReactionRoleContainer {

    RoleContainer role;
    EmojiUnion emoji;

}
