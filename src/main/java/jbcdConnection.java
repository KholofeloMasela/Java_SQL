import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class jbcdConnection {


    public static void main(String[] args) {
        String propertiesFile = "src/main/resources/db.properties";

        String url = null;
        String username = null;
        String password = null;
        String driverClass = null;


        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        Properties properties = new Properties();

        try(FileInputStream fis = new FileInputStream(propertiesFile)) {
            properties.load(fis);

            url = properties.getProperty("db.url");
            username = properties.getProperty("db.username");
            password = properties.getProperty("db.password");
            driverClass = properties.getProperty("db.driver");
            Class.forName(driverClass);

            conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connected to the database!");


            stmt = conn.createStatement();


            String sql = "SELECT * FROM Clients_records";  // Example query to get all users from the Users table
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                int userId = rs.getInt("Patient_ID");
                String firstName = rs.getString("First_name");
                String lastName = rs.getString("Last_name");
                String email = rs.getString("Street");

                System.out.println("Patient ID: " + userId + ", Name: " + firstName + " " + lastName + ", Street: " + email);
            }
            rs.close();
            stmt.close();
            conn.close();

        } catch (IOException | SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
