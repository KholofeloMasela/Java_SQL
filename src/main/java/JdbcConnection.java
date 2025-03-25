import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;
import java.util.Scanner;

public class JdbcConnection {
    // Database connection parameters
    private String url;
    private String username;
    private String password;
    private String driverClass;

    // Database resources
    private Connection connection;
    private Scanner scanner;


    public JdbcConnection() {
        this.scanner = new Scanner(System.in);
    }


    public JdbcConnection(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }


    public void loadDatabaseProperties(String propertiesFile) throws IOException, ClassNotFoundException {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(propertiesFile)) {
            properties.load(fis);

            this.url = properties.getProperty("db.url");
            this.username = properties.getProperty("db.username");
            this.password = properties.getProperty("db.password");
            this.driverClass = properties.getProperty("db.driver");

            Class.forName(this.driverClass);
        }
    }


    public void connectToDatabase() throws SQLException {
        if (this.connection == null) {
            this.connection = DriverManager.getConnection(url, username, password);
        }
    }


    public ResultSet getClientRecords(int patientId) throws SQLException {
        String sql = "SELECT * FROM Clients_records WHERE Patient_ID = ?";
        PreparedStatement stmt = connection.prepareStatement(sql,
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY);
        stmt.setInt(1, patientId);
        return stmt.executeQuery();
    }


    public void displayClientRecords(ResultSet rs) throws SQLException {
        if (rs.next()) {
            System.out.println("Client Records: ");
            System.out.println("ID: " + rs.getInt("Patient_ID"));
            System.out.println("Name: " + rs.getString("First_name"));
            System.out.println("Lastname: " + rs.getString("Last_name"));
            System.out.println("Street name: " + rs.getString("Street"));
            System.out.println("*******************************************************************************");
        } else {
            System.out.println("No client records found with that Patient ID.");
        }
    }


    public ResultSet getAccountDetails(int accountId) throws SQLException {
        String clientQuery = "SELECT * FROM account_details WHERE Account_ID = ? AND account_type = 'CHECKING'";
        PreparedStatement stmt = connection.prepareStatement(clientQuery);
        stmt.setInt(1, accountId);
        return stmt.executeQuery();
    }


    public void displayAccountDetails(ResultSet results) throws SQLException {
        if (results.next()) {
            System.out.println("Account Details:");
            System.out.println("Account ID: " + results.getInt("Account_ID"));
            System.out.println("Account Number: " + results.getLong("Account_Number"));
            System.out.println("Balance: " + results.getInt("Current_Balance"));
            System.out.println("Account Type: " + results.getString("Account_Type"));
            System.out.println("************************************************************************************");
        } else {
            System.out.println("No account details found.");
        }
    }


    public ResultSet getTransactionHistory(int patientId) throws SQLException {
        String transactionQuery =
                "SELECT * FROM transactions t JOIN account_details a ON t.Account_Number = a.Account_Number " +
                        "WHERE a.Account_ID = ? AND a.account_type = 'CHECKING'";
        PreparedStatement stmt = connection.prepareStatement(transactionQuery);
        stmt.setInt(1, patientId);
        return stmt.executeQuery();
    }


    public void displayTransactionHistory(ResultSet results) throws SQLException {
        System.out.println("Transaction History: ");
        boolean hasTransactions = false;

        while (results.next()) {
            hasTransactions = true;
            System.out.println("Transaction ID: " + results.getInt("Transaction_id"));
            System.out.println("Account number: " + results.getLong("Account_Number"));
            System.out.println("Transaction Date: " + results.getString("Transaction_Date"));
            System.out.println("Type of Transaction: " + results.getString("Type_of_transaction"));
            System.out.println("Amount: " + results.getInt("Amount"));
            System.out.println(" ");
        }

        if (!hasTransactions) {
            System.out.println("No transactions found.");
        }

        System.out.println("*********************************************************************");
    }


    public void closeResources(ResultSet rs) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException e) {
            System.out.println("Error closing ResultSet: " + e.getMessage());
        }
    }


    public void cleanup() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            if (scanner != null) {
                scanner.close();
            }
        } catch (SQLException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }


    public void run() {
        try {

            System.out.println("Enter Patient ID 12, 13, 14, 15 to view client records: ");
            int patientId = scanner.nextInt();
            System.out.println("========================================================================================");


            ResultSet clientRecords = getClientRecords(patientId);
            displayClientRecords(clientRecords);


            if (!clientRecords.first()) {
                System.out.println("No Patient found with that ID: " + patientId);
                closeResources(clientRecords);
                return;
            }


            clientRecords.beforeFirst();
            displayClientRecords(clientRecords);


            System.out.println("Enter Patient ID 12, 13, 14, 15 to view Account details");
            int accountId = scanner.nextInt();
            System.out.println("========================================================================================");


            ResultSet accountDetails = getAccountDetails(accountId);
            displayAccountDetails(accountDetails);


            System.out.println("Enter Patient ID 12, 13, 14, 15 Transaction:  ");
            int transactionId = scanner.nextInt();
            System.out.println("========================================================================================");


            ResultSet transactionHistory = getTransactionHistory(transactionId);
            displayTransactionHistory(transactionHistory);


            closeResources(clientRecords);
            closeResources(accountDetails);
            closeResources(transactionHistory);

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    public static void main(String[] args) {
        JdbcConnection app = new JdbcConnection();
        String propertiesFile = "src/main/resources/db.properties";

        try {
            app.loadDatabaseProperties(propertiesFile);
            app.connectToDatabase();
            app.run();
        } catch (IOException e) {
            System.out.println("Error loading properties: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("Database driver not found: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            app.cleanup();
        }
    }
}
