package de.presti.ree6.backend.utils.data.container.user;

import com.jagrosh.jdautilities.oauth2.entities.OAuth2User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserContainer {

    long id;

    String name;

    String discriminator;

    String avatarUrl;

    public UserContainer(OAuth2User oAuth2User) {
        this(oAuth2User.getIdLong(), oAuth2User.getName(), oAuth2User.getDiscriminator(), oAuth2User.getEffectiveAvatarUrl());
    }

    public UserContainer(User user) {
        this(user.getIdLong(), user.getName(), user.getDiscriminator(), user.getEffectiveAvatarUrl());
    }

    public UserContainer(Member user) {
        if (user == null) return;

        User user1 = user.getUser();
        this.id = user1.getIdLong();
        this.name = user1.getName();
        this.discriminator = user1.getDiscriminator();
        this.avatarUrl = user1.getEffectiveAvatarUrl();
    }

}
