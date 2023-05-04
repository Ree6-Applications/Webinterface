package de.presti.ree6.backend.utils.data.container;

import com.jagrosh.jdautilities.oauth2.entities.OAuth2User;
import com.jagrosh.jdautilities.oauth2.session.Session;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class SessionContainer {

    @Getter
    @Setter
    String identifier;

    @Getter
    @Setter
    Session session;

    @Getter
    @Setter
    OAuth2User user;
}
