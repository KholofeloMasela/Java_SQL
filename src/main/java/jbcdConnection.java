import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;
import java.util.Scanner;

public class jbcdConnection {


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String propertiesFile = "src/main/resources/db.properties";

        String url = null;
        String username = null;
        String password = null;
        String driverClass = null;


        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        Properties properties = new Properties();
        System.out.println("Enter Patient ID 12, 13, 14, 15");
        int PatientID= scanner.nextInt();

        try(FileInputStream fis = new FileInputStream(propertiesFile)) {
            properties.load(fis);

            url = properties.getProperty("db.url");
            username = properties.getProperty("db.username");
            password = properties.getProperty("db.password");
            driverClass = properties.getProperty("db.driver");
            Class.forName(driverClass);

            conn = DriverManager.getConnection(url, username, password);
            System.out.println("========================================================================================");


//            stmt = conn.createStatement();



            String sql = "SELECT * FROM Clients_records WHERE Patient_ID = ?";
            PreparedStatement customerStmt = conn.prepareStatement(sql);
            customerStmt.setInt(1, PatientID);

            rs = customerStmt.executeQuery();

            if (rs.next()) {

                System.out.println("Client Records: ");
                int userId = rs.getInt("Patient_ID");
                String firstName = rs.getString("First_name");
                String lastName = rs.getString("Last_name");
                String streetName = rs.getString("Street");


                System.out.println("ID: " + userId);
                System.out.println("Name: " + firstName);
                System.out.println("Lastname: " + lastName);
                System.out.println("Street name: " + streetName);

                System.out.println("*******************************************************************************");


                String clientQuary = "SELECT * FROM account_details WHERE Account_ID = ?";
                PreparedStatement clientStmt = conn.prepareStatement(clientQuary);
                clientStmt.setInt(1, PatientID);
                ResultSet clientResults = clientStmt.executeQuery();



//                System.out.println("Patient ID: " + userId + ", Name: " + firstName + " " + lastName + ", Street: " + email);

                if(clientResults.next()){



                    int accountId = clientResults.getInt("Account_ID");
                    long accountNumber = clientResults.getLong("Account_Number");
//                    enum accountType = clientResults.getString("Account_Type");
                    double CurrentBalance = clientResults.getInt("Current_Balance");


                    System.out.println("Account Details:");
                    System.out.println("Account ID: " + accountId);
                    System.out.println("Account Type: " + accountNumber);
                    System.out.println("Balance: " + CurrentBalance);
                    System.out.println("************************************************************************************");

                    String transactionQuery = "SELECT * FROM transactions WHERE Transaction_id = ?";
                    PreparedStatement transactionStmt = conn.prepareStatement(transactionQuery);
                    transactionStmt.setInt(1, accountId);
                    ResultSet transactionResult = transactionStmt.executeQuery();

                    System.out.println("Transaction History: ");
                    if(transactionResult.next()){
                        int TransactionId = transactionResult.getInt("Transaction_id");
                        long AccountNumber = transactionResult.getLong("Account_Number");
                        String transactionDate = transactionResult.getString("Transaction_Date");
//                        enum TypeOfTransaction = transactionResult.getString("Type_of_transaction");
                        double amount = transactionResult.getInt("Amount");


//                        Print details
                        System.out.println("Transaction ID: " + TransactionId);
                        System.out.println("Account number: " + AccountNumber);
                        System.out.println("Transaction Date: " + transactionDate);
                        System.out.println("Amount: "+ amount);
                        System.out.println("*********************************************************************");
                    }
                    transactionResult.close();

                }
                clientResults.close();
            }
            else {
                System.out.println("No Patient found with that ID: " + PatientID);
            }
            rs.close();

        } catch (IOException | SQLException | ClassNotFoundException e) {
            System.out.println("Error fetching Report: " + e.getMessage());
            e.printStackTrace();
        }

        finally {
            scanner.close();
        }
    }

}
