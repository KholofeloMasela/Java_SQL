import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Epic("Get reports")
@Feature("Extract data from SQL")
@ExtendWith(MockitoExtension.class)
public class JdbcConnectionTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    private JdbcConnection jdbcConnection;
    private AutoCloseable closeable;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public void setUp() throws Exception {
        closeable = MockitoAnnotations.openMocks(this);


        System.setOut(new PrintStream(outContent));


        Scanner scanner = new Scanner(new ByteArrayInputStream("12\n".getBytes()));


        jdbcConnection = new JdbcConnection(mockConnection, scanner);
    }

    @AfterEach
    public void tearDown() throws Exception {
        System.setOut(originalOut);
        closeable.close();
    }


    @Test
    @Story("Client records")
    @Description("Given a customer ID, address data is displayed")
    public void testDisplayClientRecords_WithData() throws Exception {

        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("Patient_ID")).thenReturn(12);
        when(mockResultSet.getString("First_name")).thenReturn("John");
        when(mockResultSet.getString("Last_name")).thenReturn("Doe");
        when(mockResultSet.getString("Street")).thenReturn("123 Main St");


        jdbcConnection.displayClientRecords(mockResultSet);


        String output = outContent.toString();
        assertTrue(output.contains("Client Records:"));
        assertTrue(output.contains("ID: 12"));
        assertTrue(output.contains("Name: John"));
        assertTrue(output.contains("Lastname: Doe"));
        assertTrue(output.contains("Street name: 123 Main St"));
    }

    @Test
    @Story("Client records")
    @Description("Given a customer ID that does not exist, address data is not displayed")
    public void testDisplayClientRecords_NoData() throws Exception {

        when(mockResultSet.next()).thenReturn(false);


        jdbcConnection.displayClientRecords(mockResultSet);


        String output = outContent.toString();
        assertTrue(output.contains("No client records found"));
    }



    @Test
    @Story("Client records")
    @Description("Given a customer ID, the total balance for all their accounts is displayed")
    public void testDisplayAccountDetails_WithData() throws Exception {

        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("Account_ID")).thenReturn(12);
        when(mockResultSet.getLong("Account_Number")).thenReturn(123456789L);
        when(mockResultSet.getInt("Current_Balance")).thenReturn(1000);
        when(mockResultSet.getString("Account_Type")).thenReturn("CHECKING");


        jdbcConnection.displayAccountDetails(mockResultSet);


        String output = outContent.toString();
        assertTrue(output.contains("Account Details:"));
        assertTrue(output.contains("Account ID: 12"));
        assertTrue(output.contains("Account Number: 123456789"));
        assertTrue(output.contains("Balance: 1000"));
        assertTrue(output.contains("Account Type: CHECKING"));
    }


    @Test
    @Story("Client records")
    @Description("Given a customer ID, all transactions for all their CHECKING is displayed ")
    public void testDisplayTransactionHistory_WithData() throws Exception {

        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("Transaction_id")).thenReturn(101, 102);
        when(mockResultSet.getLong("Account_Number")).thenReturn(123456789L);
        when(mockResultSet.getString("Transaction_Date")).thenReturn("2023-01-01", "2023-01-02");
        when(mockResultSet.getString("Type_of_transaction")).thenReturn("DEPOSIT", "WITHDRAWAL");
        when(mockResultSet.getInt("Amount")).thenReturn(500, 200);


        jdbcConnection.displayTransactionHistory(mockResultSet);


        String output = outContent.toString();
        assertTrue(output.contains("Transaction History:"));
        assertTrue(output.contains("Transaction ID: 101"));
        assertTrue(output.contains("Transaction ID: 102"));
        assertTrue(output.contains("Type of Transaction: DEPOSIT"));
        assertTrue(output.contains("Type of Transaction: WITHDRAWAL"));
    }

    @Test
    @Story("Client records")
    @Description("Given a customer ID that does not exist, all transactions for all their CHECKING is not displayed")
    public void testDisplayTransactionHistory_NoData() throws Exception {

        when(mockResultSet.next()).thenReturn(false);

        jdbcConnection.displayTransactionHistory(mockResultSet);

        String output = outContent.toString();
        assertTrue(output.contains("Transaction History:"));
        assertTrue(output.contains("No transactions found."));
    }

    @Test
    public void testCloseResources() throws Exception {

        jdbcConnection.closeResources(mockResultSet);


        verify(mockResultSet).close();
    }
}
