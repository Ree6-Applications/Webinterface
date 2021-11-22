package de.presti.ree6.webinterface;

import com.jagrosh.jdautilities.oauth2.OAuth2Client;

public class Server {

    // Class Instance.
    private static Server instance;

    // OAuth Instance.
    OAuth2Client oAuth2Client;

    /**
     * Call when the Class should be Initialized.
     */
    public Server() {
        instance = this;
        load();
    }

    /**
     * Call to load and Initialize Data.
     */
    public void load() {

        // Creating OAuth2 Instance.
        oAuth2Client = new OAuth2Client.Builder().setClientId(723655291681505380L).setClientSecret("x5OcNv7J8iJ8cZQC5lJOrZsQvlLEMp7n").build();
    }

    /**
     * Retrieve an Instance of the Server.
     * @return instance of Server.
     */
    public static Server getInstance() {
        return instance;
    }

    /**
     * Retrieve an Instance of the OAuthClient-
     * @return instance of OAuthClient.
     */
    public OAuth2Client getOAuth2Client() {
        return oAuth2Client;
    }
}
