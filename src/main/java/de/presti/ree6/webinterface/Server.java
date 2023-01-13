package de.presti.ree6.webinterface;

import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.CredentialManagerBuilder;
import com.github.philippheuer.credentialmanager.storage.TemporaryStorageBackend;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.auth.TwitchAuth;
import com.jagrosh.jdautilities.oauth2.OAuth2Client;
import de.presti.ree6.sql.DatabaseTyp;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.webinterface.bot.BotWorker;
import de.presti.ree6.webinterface.bot.version.BotVersion;
import de.presti.ree6.sql.entities.Recording;
import de.presti.ree6.webinterface.utils.data.Config;
import de.presti.ree6.webinterface.utils.others.ThreadUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * The "Main" Class used to store Instance of the needed Classes.
 */
@Slf4j
public class Server {

    /**
     * Class Instance.
     */
    private static Server instance;

    /**
     * Discord OAuth2 Client instance.
     */
    OAuth2Client oAuth2Client;

    /**
     * Twitch Client instance.
     */
    @Getter(AccessLevel.PUBLIC)
    TwitchClient twitchClient;

    /**
     * Twitch Credential Manager instance.
     */
    @Getter(AccessLevel.PUBLIC)
    CredentialManager credentialManager;

    /**
     * Yaml Config Manager instance.
     */
    Config config;

    /**
     * Call when the Class should be Initialized.
     *
     * @param args {@link String[]} used as List of the Arguments given at the start of the Application.
     */
    public Server(String[] args) {
        instance = this;

        load(args);
    }

    /**
     * Call to load and Initialize Data.
     *
     * @param args {@link String[]} used as List of the Arguments given at the start of the Application.
     */
    public void load(String[] args) {

        // Create Config Instance.
        config = new Config();

        // Initialize the Config.
        config.init();

        // Create a new JDA Session.
        try {
            List<String> argList = Arrays.stream(args).map(String::toLowerCase).toList();

            if (argList.contains("--dev")) {
                BotWorker.createBot(BotVersion.DEVELOPMENT_BUILD, "2.1.3");
            } else if (argList.contains("--prod")) {
                BotWorker.createBot(BotVersion.RELEASE,"2.1.3");
            } else if (argList.contains("--beta")) {
                BotWorker.createBot(BotVersion.BETA_BUILD,"2.1.3");
            } else {
                BotWorker.createBot(BotVersion.RELEASE,"2.1.3");
            }

            log.info("Service (JDA) has been started. Creation was successful.");
        } catch (Exception exception) {
            //Inform if not successful.
            log.error("Service (JDA) couldn't be started. Creation was unsuccessful.", exception);
        }

        // Creating OAuth2 Instance.
        oAuth2Client = new OAuth2Client.Builder().setClientId(config.getConfiguration().getLong("discord.client.id")).setClientSecret(config.getConfiguration().getString("discord.client.secret")).build();

        CredentialManager credentialManager = CredentialManagerBuilder.builder()
                .withStorageBackend(new TemporaryStorageBackend())
                .build();

        TwitchAuth.registerIdentityProvider(credentialManager, getConfig().getConfiguration().getString("twitch.client.id"),
                getConfig().getConfiguration().getString("twitch.client.secret"),
                (BotWorker.getVersion() != BotVersion.DEVELOPMENT_BUILD ? "https://cp.ree6.de" : "http://localhost:8888") + "/twitch/auth/callback");

        twitchClient = TwitchClientBuilder.builder()
                .withClientId(getConfig().getConfiguration().getString("twitch.client.id"))
                .withClientSecret(getConfig().getConfiguration().getString("twitch.client.secret"))
                .withCredentialManager(credentialManager)
                .withEnablePubSub(false)
                .build();

        // Creating a new SQL-Connector Instance.
        DatabaseTyp databaseTyp;

        switch (getInstance().getConfig().getConfiguration().getString("hikari.misc.storage").toLowerCase()) {
            case "mariadb" -> databaseTyp = DatabaseTyp.MariaDB;

            default -> databaseTyp = DatabaseTyp.SQLite;
        }

        new SQLSession(getConfig().getConfiguration().getString("hikari.sql.user"),
                getConfig().getConfiguration().getString("hikari.sql.db"), getConfig().getConfiguration().getString("hikari.sql.pw"),
                getConfig().getConfiguration().getString("hikari.sql.host"), getConfig().getConfiguration().getInt("hikari.sql.port"),
                getConfig().getConfiguration().getString("hikari.misc.storageFile"), databaseTyp,
                getConfig().getConfiguration().getInt("hikari.misc.poolSize"));


        // Add onShutdown as call methode when Shutdown.
        Runtime.getRuntime().addShutdownHook(new Thread(this::onShutdown));

        ThreadUtil.createNewThread(x -> {
            List<Recording> recordings = SQLSession.getSqlConnector().getSqlWorker().getEntityList(new Recording(), "SELECT * FROM Recording", null);
            if (recordings != null && !recordings.isEmpty()) {
                for (Recording recording : recordings) {
                    if (recording.getCreation() < System.currentTimeMillis() - Duration.ofDays(1).toMillis()) {
                        SQLSession.getSqlConnector().getSqlWorker().deleteEntity(recording);
                    }
                }
            }
        }, throwable -> log.error("Failed running Data clear Thread", throwable), Duration.ofMinutes(30), true, false);
    }

    /**
     * Call when the Application shutdowns.
     */
    public void onShutdown() {
        // Shutdown Bot Instance.
        BotWorker.shutdown();

        // Shutdown the SQL Connection.
        SQLSession.getSqlConnector().close();
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
     * Retrieve the Instance of the Config.
     * @return {@link Config} Instance of the Config.
     */
    public Config getConfig() { return config; }
}
