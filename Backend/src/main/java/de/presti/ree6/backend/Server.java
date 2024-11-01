package de.presti.ree6.backend;

import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.CredentialManagerBuilder;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.auth.TwitchAuth;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.jagrosh.jdautilities.oauth2.OAuth2Client;
import de.presti.ree6.backend.bot.BotWorker;
import de.presti.ree6.backend.bot.version.BotVersion;
import de.presti.ree6.backend.utils.data.*;
import de.presti.ree6.sql.DatabaseTyp;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.Recording;
import de.presti.ree6.sql.entities.TwitchIntegration;
import de.presti.ree6.backend.utils.ThreadUtil;
import de.presti.ree6.sql.util.SQLConfig;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * The "Main" Class used to store Instance of the needed Classes.
 */
@Slf4j
public class Server {

    /**
     * Class Instance.
     */
    @Getter
    private static Server instance;

    /**
     * Discord OAuth2 Client instance.
     */
    @Getter
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
     * Twitch Identity Provider instance.
     */
    @Getter(AccessLevel.PUBLIC)
    TwitchIdentityProvider twitchIdentityProvider;

    /**
     * Backend Version.
     */
    @Getter
    private String backendVersion;

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

        String tempVersion = getInstance().getClass().getPackage().getImplementationVersion();
        backendVersion = tempVersion == null ? "5.0.0" : tempVersion;
        load(args);
    }

    /**
     * Call to load and initialize Data.
     *
     * @param args {@link String[]} used as List of the Arguments given at the start of the Application.
     */
    public void load(String[] args) {
        log.info("Starting Backend {}", backendVersion);

        // Create Config Instance.
        config = new Config();

        // Initialize the Config.
        config.init();

        // Creating a new SQL-Connector Instance.
        DatabaseTyp databaseTyp;

        switch (getInstance().getConfig().getConfiguration().getString("hikari.misc.storage").toLowerCase()) {
            case "mariadb" -> databaseTyp = DatabaseTyp.MariaDB;

            case "h2" -> databaseTyp = DatabaseTyp.H2;

            case "h2-server", "h2_server" -> databaseTyp = DatabaseTyp.H2_Server;

            case "postgresql", "postgres" -> databaseTyp = DatabaseTyp.PostgreSQL;

            default -> databaseTyp = DatabaseTyp.SQLite;
        }


        log.info("Using {} as Database", databaseTyp.name());

        SQLConfig sqlConfig = SQLConfig.builder()
                .username(getConfig().getConfiguration().getString("hikari.sql.user"))
                .database(getConfig().getConfiguration().getString("hikari.sql.db"))
                .password(getConfig().getConfiguration().getString("hikari.sql.pw"))
                .host(getConfig().getConfiguration().getString("hikari.sql.host"))
                .port(getConfig().getConfiguration().getInt("hikari.sql.port"))
                .path(getConfig().getConfiguration().getString("hikari.misc.storageFile"))
                .typ(databaseTyp)
                .poolSize(getConfig().getConfiguration().getInt("hikari.misc.poolSize"))
                .createEmbeddedServer(getConfig().getConfiguration().getBoolean("hikari.misc.createEmbeddedServer"))
                .debug(false)
                .build();

        new SQLSession(sqlConfig);

        // Creating OAuth2 Instance.
        oAuth2Client = new OAuth2Client.Builder().setClientId(config.getConfiguration().getLong("discord.client.id")).setClientSecret(config.getConfiguration().getString("discord.client.secret")).build();

        // Create a new JDA Session.
        try {
            List<String> argList = Arrays.stream(args).map(String::toLowerCase).toList();

            int shards = instance.config.getConfiguration().getInt("discord.bot.client.shards", 1);

            BotVersion version = BotVersion.RELEASE;

            if (argList.contains("--dev")) {
                version = BotVersion.DEVELOPMENT_BUILD;
            } else if (argList.contains("--beta")) {
                version = BotVersion.BETA_BUILD;
            }

            BotWorker.createBot(version, "3.0.0", shards);

            log.info("Service (JDA) has been started. Creation was successful.");
        } catch (Exception exception) {
            //Inform if not successful.
            log.error("Service (JDA) couldn't be started. Creation was unsuccessful.", exception);
        }

        credentialManager = CredentialManagerBuilder.builder()
                .withStorageBackend(new DatabaseStorageBackend())
                .build();

        TwitchAuth.registerIdentityProvider(credentialManager, getConfig().getConfiguration().getString("twitch.client.id"),
                getConfig().getConfiguration().getString("twitch.client.secret"), Data.getTwitchRedirectUrl(), false);

        twitchIdentityProvider = (TwitchIdentityProvider) credentialManager.getIdentityProviderByName("twitch").orElse(null);

        twitchClient = TwitchClientBuilder.builder()
                .withClientId(getConfig().getConfiguration().getString("twitch.client.id"))
                .withClientSecret(getConfig().getConfiguration().getString("twitch.client.secret"))
                .withCredentialManager(credentialManager)
                .withEnablePubSub(false)
                .build();

        // Add onShutdown as call methode when Shutdown.
        Runtime.getRuntime().addShutdownHook(new Thread(this::onShutdown));

        ThreadUtil.createNewThread(x -> {
            SQLSession.getSqlConnector().getSqlWorker().getEntityList(new Recording(), "FROM Recording", null).subscribe(recordings -> {
                if (recordings != null && !recordings.isEmpty()) {
                    for (Recording recording : recordings) {
                        if (recording.getCreation() < System.currentTimeMillis() - Duration.ofDays(1).toMillis()) {
                            SQLSession.getSqlConnector().getSqlWorker().deleteEntity(recording).block();
                        }
                    }
                }
            });

            SQLSession.getSqlConnector().getSqlWorker().getEntityList(new TwitchIntegration(), "FROM TwitchIntegration", null).subscribe(twitchIntegrations -> {
                twitchIntegrations.forEach(twitchIntegration -> {
                    if (twitchIntegration.getLastUpdated().getTime() + (twitchIntegration.getExpiresIn() * 1000L) - Duration.ofMinutes(10).toMillis() <= System.currentTimeMillis()) {
                        Optional<OAuth2Credential> cred = twitchIdentityProvider.refreshCredential(CustomOAuth2Util.convertToOriginal(twitchIntegration));

                        credentialManager.getCredentials().removeIf(credential -> {
                            if (credential instanceof CustomOAuth2Credential customOAuth2CredentialLocal) {
                                return customOAuth2CredentialLocal.getDiscordId() == twitchIntegration.getUserId();
                            }
                            return false;
                        });

                        cred.ifPresent(oAuth2Credential -> credentialManager.addCredential("twitch", CustomOAuth2Util.convert(twitchIntegration.getUserId(), oAuth2Credential)));
                    }
                });
                credentialManager.save();
            });
        }, throwable -> log.error("Failed running Data clear Thread", throwable), Duration.ofMinutes(5), true, false);
    }

    /**
     * Call when the Application shutdowns.
     */
    public void onShutdown() {
        // Shutdown Bot Instance.
        BotWorker.shutdown();

        credentialManager.save();

        // Shutdown the SQL Connection.
        SQLSession.getSqlConnector().close();
    }

    /**
     * Retrieve the Instance of the Config.
     *
     * @return {@link Config} Instance of the Config.
     */
    public Config getConfig() {
        if (config == null) {
            config = new Config();
            config.init();
        }

        return config;
    }
}
