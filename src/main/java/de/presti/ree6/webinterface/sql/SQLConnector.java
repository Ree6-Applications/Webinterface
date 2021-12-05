package de.presti.ree6.webinterface.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * A "Connector" Class which connect with the used Database Server.
 * Used to manage the connection between Server and Client.
 */
public class SQLConnector {

    // Various String that keep connection information to use for a connection.
    private final String databaseUser, databaseName, databasePassword, databaseServerIP;

    // The port of the Server.
    private final int databaseServerPort;

    // An Instance of the actual Java SQL Connection.
    private Connection connection;

    // An Instance of the SQL-Worker which works with the Data in the Database.
    private SQLWorker sqlWorker;

    /**
     * Constructor with the needed data to open an SQL connection.
     * @param databaseUser the Database Username
     * @param databaseName the Database name
     * @param databasePassword the Database User password
     * @param databaseServerIP the Address of the Database Server.
     * @param databaseServerPort the Port of the Database Server.
     */
    public SQLConnector(String databaseUser, String databaseName, String databasePassword, String databaseServerIP, int databaseServerPort) {
        this.databaseUser = databaseUser;
        this.databaseName = databaseName;
        this.databasePassword = databasePassword;
        this.databaseServerIP = databaseServerIP;
        this.databaseServerPort = databaseServerPort;
        connectToSQLServer();
        createTables();
    }

    /**
     * Try to open a connection to the SQL Server with the given data.
     */
    public void connectToSQLServer() {

        // Check if there is already an open Connection.
        if (IsConnected()) {
            try {
                // Close if there is and notify.
                connection.close();
                System.out.println("Service (MariaDB) has been stopped.");
            } catch (Exception ignore) {
                // Notify if there was an error.
                System.out.println("Service (MariaDB) couldn't be stopped.");
            }
        }

        try {
            // Create a new Connection by using the SQL DriverManager and the MariaDB Java Driver and notify if successful.
            connection = DriverManager.getConnection("jdbc:mariadb://" + databaseServerIP + ":" + databaseServerPort + "/" + databaseName + "?autoReconnect=true", databaseUser, databasePassword);
            System.out.println("Service (MariaDB) has been started. Connection was successful.");
        } catch (Exception ignore) {
            // Notify if there was an error.
            System.out.println("Service (MariaDB) couldn't be started. Connection was unsuccessful.");
        }
    }

    /**
     * Create Tables in the Database if they aren't already set.
     */
    public void createTables() {

        // Check if there is an open Connection if not, skip.
        if (!IsConnected()) return;

        // Create Settings Table in the Database.
        try (PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS Settings (GID VARCHAR(40), NAME VARCHAR(40), VALUE VARCHAR(50))")) {
            ps.executeUpdate();
        } catch (SQLException ignore) {
            System.out.println("Couldn't create Settings Table.");
        }

        // Create CommandStats Table in the Database.
        try (PreparedStatement ps = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS CommandStats (COMMAND VARCHAR(40), USES VARCHAR(50))")) {
            ps.executeUpdate();
        } catch (SQLException ignore) {
            System.out.println("Couldn't create CommandStats Table.");
        }

        // Create GuildStats Table in the Database.
        try (PreparedStatement ps = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS GuildStats (GID VARCHAR(40), COMMAND VARCHAR(40), USES VARCHAR(50))")) {
            ps.executeUpdate();
        } catch (SQLException ignore) {
            System.out.println("Couldn't create GuildStats Table.");
        }

        // Create Webinterface Table in the Database.
        try (PreparedStatement ps = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS Webinterface (GID VARCHAR(40), AUTH VARCHAR(50))")) {
            ps.executeUpdate();
        } catch (SQLException ignore) {
            System.out.println("Couldn't create Webinterface Table.");
        }

        // Create Twitch Notify Table in the Database.
        try (PreparedStatement ps = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS TwitchNotify (GID VARCHAR(40), NAME VARCHAR(40), CID VARCHAR(40), TOKEN VARCHAR(68))")) {
            ps.executeUpdate();
        } catch (SQLException ignore) {
            System.out.println("Couldn't create TwitchNotify Table.");
        }

        // Create Log Webhook Table in the Database.
        try (PreparedStatement ps = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS LogWebhooks (GID VARCHAR(40), CID VARCHAR(40), TOKEN VARCHAR(68))")) {
            ps.executeUpdate();
        } catch (SQLException ignore) {
            System.out.println("Couldn't create LogWebhooks Table.");
        }

        // Create Welcome Webhook Table in the Database.
        try (PreparedStatement ps = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS WelcomeWebhooks (GID VARCHAR(40), CID VARCHAR(40), TOKEN VARCHAR(68))")) {
            ps.executeUpdate();
        } catch (SQLException ignore) {
            System.out.println("Couldn't create WelcomeWebhook Table.");
        }

        // Create News Webhook Table in the Database.
        try (PreparedStatement ps = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS NewsWebhooks (GID VARCHAR(40), CID VARCHAR(40), TOKEN VARCHAR(68))")) {
            ps.executeUpdate();
        } catch (SQLException ignore) {
            System.out.println("Couldn't create NewsWebhook Table.");
        }

        // Create Rainbow Six Siege Webhook Table in the Database.
        try (PreparedStatement ps = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS RainbowWebhooks (GID VARCHAR(40), CID VARCHAR(40), TOKEN VARCHAR(68))")) {
            ps.executeUpdate();
        } catch (SQLException ignore) {
            System.out.println("Couldn't create RainbowWebhook Table.");
        }

        // Create join Message Table in the Database.
        try (PreparedStatement ps = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS JoinMessage (GID VARCHAR(40), MSG VARCHAR(250))")) {
            ps.executeUpdate();
        } catch (SQLException ignore) {
            System.out.println("Couldn't create JoinMessage Table.");
        }

        // Create Mute Roles Table in the Database.
        try (PreparedStatement ps = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS MuteRoles (GID VARCHAR(40), RID VARCHAR(40))")) {
            ps.executeUpdate();
        } catch (SQLException ignore) {
            System.out.println("Couldn't create MuteRoles Table.");
        }

        // Create ChatProtector Table in the Database.
        try (PreparedStatement ps = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS ChatProtector (GID VARCHAR(40), WORD VARCHAR(40))")) {
            ps.executeUpdate();
        } catch (SQLException ignore) {
            System.out.println("Couldn't create ChatProtector Table.");
        }

        // Create auto Roles Table in the Database.
        try (PreparedStatement ps = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS AutoRoles (GID VARCHAR(40), RID VARCHAR(40))")) {
            ps.executeUpdate();
        } catch (SQLException ignore) {
            System.out.println("Couldn't create AutoRoles Table.");
        }

        // Create Invites Table in the Database.
        try (PreparedStatement ps = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS Invites (GID VARCHAR(40), UID VARCHAR(40), USES VARCHAR(40), CODE VARCHAR(40))")) {
            ps.executeUpdate();
        } catch (SQLException ignore) {
            System.out.println("Couldn't create Invites Table.");
        }

        // Create ChatLevel Table in the Database.
        try (PreparedStatement ps = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS Level (GID VARCHAR(40), UID VARCHAR(40), XP VARCHAR(500))")) {
            ps.executeUpdate();
        } catch (SQLException ignore) {
            System.out.println("Couldn't create Level Table.");
        }

        // Create VoiceLevel Table in the Database.
        try (PreparedStatement ps = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS VCLevel (GID VARCHAR(40), UID VARCHAR(40), XP VARCHAR(500))")) {
            ps.executeUpdate();
        } catch (SQLException ignore) {
            System.out.println("Couldn't create VCLevel Table.");
        }

        // Create VoiceLevel auto Roles Table in the Database.
        try (PreparedStatement ps = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS VCLevelAutoRoles (GID VARCHAR(40), RID VARCHAR(40), LVL VARCHAR(500))")) {
            ps.executeUpdate();
        } catch (SQLException ignore) {
            System.out.println("Couldn't create VCLevelAutoRoles Table.");
        }

        // Create ChatLevel auto Roles Table in the Database.
        try (PreparedStatement ps = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS ChatLevelAutoRoles (GID VARCHAR(40), RID VARCHAR(40), LVL VARCHAR(500))")) {
            ps.executeUpdate();
        } catch (SQLException ignore) {
            System.out.println("Couldn't create ChatLevelAutoRoles Table.");
        }
    }

    /**
     * Check if there is an open connection to the Database Server.
     * @return boolean If the connection is opened.
     */
    public boolean IsConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (Exception ignore) {}

        return false;
    }

    /**
     * Call to close the current Connection.
     */
    public void close() {
        // Check if there is already an open Connection.
        if (IsConnected()) {
            try {
                // Close if there is and notify.
                connection.close();
                System.out.println("Service (MariaDB) has been stopped.");
            } catch (Exception ignore) {
                // Notify if there was an error.
                System.out.println("Service (MariaDB) couldn't be stopped.");
            }
        }
    }

    /**
     * Retrieve an Instance of the SQL-Connection.
     * @return Connection Instance of te SQL-Connection.
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Retrieve an Instance of the SQL-Worker to work with the Data.
     * @return {@link SQLWorker} the Instance saved in this SQL-Connector.
     */
    public SQLWorker getSqlWorker() {
        return sqlWorker;
    }
}
