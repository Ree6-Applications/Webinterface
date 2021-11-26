package de.presti.ree6.webinterface;

import com.jagrosh.jdautilities.oauth2.OAuth2Client;
import de.presti.ree6.webinterface.sql.SQLConnector;

public class Server {

    // Class Instance.
    private static Server instance;

    // OAuth Instance.
    OAuth2Client oAuth2Client;

    // SQL-Connector Instance.
    SQLConnector sqlConnector;

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

        // Creating a new SQL-Connector Instance.
        sqlConnector = new SQLConnector("dbu", "db", "dbp", "localhost", 3306);
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
     * @return {@link OAuth2Client} Instance of OAuthClient.
     */
    public OAuth2Client getOAuth2Client() {
        return oAuth2Client;
    }

    /**
     * Retrieve the Instance of the SQL-Connector.
     * @return {@link SQLConnector} Instance of the SQL-Connector.
     */
    public SQLConnector getSqlConnector() { return sqlConnector; }
}
