package de.presti.ree6.webinterface.sql;

import java.sql.Statement;

/**
 * A Class to actually handle the SQL data.
 * Used to provide Data from the Database and to save Data into the Database.
 */
public class SQLWorker {

    // Instance of the SQL Connector to actually access the SQL Database.
    private final SQLConnector sqlConnector;

    /**
     * Constructor to create a new Instance of the SQLWorker with a ref to the SQL-Connector.
     * @param sqlConnector an Instance of the SQL-Connector to retrieve the data from.
     */
    public SQLWorker (SQLConnector sqlConnector) {
        this.sqlConnector = sqlConnector;
    }

    /**
     * Send an SQL-Query to SQL-Server.
     * @param sqlQuery the SQL-Query.
     */
    public void querySQL(String sqlQuery) {
        if (!sqlConnector.IsConnected()) return;

        try (Statement statement = sqlConnector.getConnection().createStatement()) {
            statement.executeUpdate(sqlQuery);
        } catch (Exception ignore) {
            System.out.println("Couldn't send Query to SQL-Server");
        }
    }

}
