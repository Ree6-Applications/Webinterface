package de.presti.ree6.webinterface;

import com.mindscapehq.raygun4java.core.RaygunClient;
import com.jagrosh.jdautilities.oauth2.OAuth2Client;
import de.presti.ree6.webinterface.bot.BotUtil;
import de.presti.ree6.webinterface.bot.BotVersion;
import de.presti.ree6.webinterface.sql.SQLConnector;
import de.presti.ree6.webinterface.utils.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The "Main" Class used to store Instance of the needed Classes.
 */
public class Server {

    // Class Instance.
    private static Server instance;

    // OAuth Instance.
    OAuth2Client oAuth2Client;

    // SQL-Connector Instance.
    SQLConnector sqlConnector;

    // Config Instance
    Config config;

    // Logger Instance.
    Logger logger;

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

        // Create the Logger with a LoggerFactory.
        logger = LoggerFactory.getLogger(Server.class);

        // Create Config Instance.
        config = new Config();

        // Initialize the Config.
        config.init();

        // Add Raygun for external Exceptions Information.
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> new RaygunClient(config.getConfig().getString("raygun.apitoken")).send(e));

        // Create a new JDA Session.
        try {
            BotUtil.createBot(BotVersion.PUBLIC, "1.5.5");
            logger.info("Service (JDA) has been started. Creation was successful.");
        } catch (Exception ignore) {
            //Inform if not successful.
            logger.error("Service (JDA) couldn't be started. Creation was unsuccessful.");
        }

        // Creating OAuth2 Instance.
        oAuth2Client = new OAuth2Client.Builder().setClientId(config.getConfig().getLong("discord.client.id")).setClientSecret(config.getConfig().getString("discord.client.secret")).build();

        // Creating a new SQL-Connector Instance.
        sqlConnector = new SQLConnector(config.getConfig().getString("mysql.user"), config.getConfig().getString("mysql.db"),
                config.getConfig().getString("mysql.pw"), config.getConfig().getString("mysql.host"), config.getConfig().getInt("mysql.port"));

        // Add onShutdown as call methode when Shutdown.
        Runtime.getRuntime().addShutdownHook(new Thread(this::onShutdown));
    }

    /**
     * Call when the Application shutdowns.
     */
    public void onShutdown() {
        // Shutdown Bot Instance.
        BotUtil.shutdown();

        // Shutdown the SQL Connection.
        getSqlConnector().close();
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

    /**
     * Retrieve the Instance of the Config.
     * @return {@link Config} Instance of the Config.
     */
    public Config getConfig() { return config; }

    /**
     * Retrieve the Instance of the Logger.
     * @return {@link Logger} Instance of the Logger.
     */
    public Logger getLogger() { return logger; }
}
