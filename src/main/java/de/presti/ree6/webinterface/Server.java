package de.presti.ree6.webinterface;

import com.jagrosh.jdautilities.oauth2.OAuth2Client;
import de.presti.ree6.webinterface.bot.BotWorker;
import de.presti.ree6.webinterface.bot.version.BotVersion;
import de.presti.ree6.webinterface.sql.SQLConnector;
import de.presti.ree6.webinterface.sql.entities.Recording;
import de.presti.ree6.webinterface.utils.data.Config;
import de.presti.ree6.webinterface.utils.others.ThreadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

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

        // Create a new JDA Session.
        try {
            BotWorker.createBot(BotVersion.RELEASE, "2.0.8");
            logger.info("Service (JDA) has been started. Creation was successful.");
        } catch (Exception exception) {
            //Inform if not successful.
            logger.error("Service (JDA) couldn't be started. Creation was unsuccessful.", exception);
        }

        // Creating OAuth2 Instance.
        oAuth2Client = new OAuth2Client.Builder().setClientId(config.getConfiguration().getLong("discord.client.id")).setClientSecret(config.getConfiguration().getString("discord.client.secret")).build();

        // Creating a new SQL-Connector Instance.
        sqlConnector = new SQLConnector(config.getConfiguration().getString("hikari.sql.user"), config.getConfiguration().getString("hikari.sql.db"),
                config.getConfiguration().getString("hikari.sql.pw"), config.getConfiguration().getString("hikari.sql.host"), config.getConfiguration().getInt("hikari.sql.port"));

        // Add onShutdown as call methode when Shutdown.
        Runtime.getRuntime().addShutdownHook(new Thread(this::onShutdown));

        ThreadUtil.createNewThread(x -> {
            List<Recording> recordings = sqlConnector.getSqlWorker().getEntityList(new Recording(), "SELECT * FROM Recording", null);
            if (recordings != null && !recordings.isEmpty()) {
                for (Recording recording : recordings) {
                    if (recording.getCreation() < System.currentTimeMillis() - Duration.ofDays(1).toMillis()) {
                        sqlConnector.getSqlWorker().deleteEntity(recording);
                    }
                }
            }
        }, throwable -> logger.error("Failed running Data clear Thread", throwable), Duration.ofMinutes(30), true, false);
    }

    /**
     * Call when the Application shutdowns.
     */
    public void onShutdown() {
        // Shutdown Bot Instance.
        BotWorker.shutdown();

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
