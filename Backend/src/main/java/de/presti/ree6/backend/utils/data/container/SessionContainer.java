package de.presti.ree6.backend.utils.data.container;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jagrosh.jdautilities.oauth2.entities.OAuth2User;
import com.jagrosh.jdautilities.oauth2.session.Session;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SessionContainer {

    public SessionContainer(String identifier, Session session, OAuth2User user) {
        this(identifier, session, new UserContainer(user), user);
    }

    String identifier;

    Session session;

    UserContainer user;

    @JsonIgnore
    OAuth2User oAuthUser;
}
