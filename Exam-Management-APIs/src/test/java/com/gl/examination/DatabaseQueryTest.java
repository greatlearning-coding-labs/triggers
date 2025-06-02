package com.gl.examination;
import org.junit.jupiter.api.*;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabaseQueryTest {

    private static Connection connection;

    @BeforeAll
    static void connect() throws SQLException {
        connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/stocks_db", "root", "");
    }

    @Test
    @Order(1)
    void testProductsTableExists() throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        ResultSet rs = meta.getTables(null, null, "products", new String[]{"TABLE"});
        assertTrue(rs.next(), "Table 'products' should exist");
    }

    @Test
    @Order(2)
    void testInventoryTableExists() throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        ResultSet rs = meta.getTables(null, null, "inventory", new String[]{"TABLE"});
        assertTrue(rs.next(), "Table 'inventory' should exist");
    }

    @Test
    @Order(3)
    void testTriggerUpdateStockExists() throws SQLException {
    	String triggerCheckQuery = "SELECT trigger_name FROM information_schema.triggers " +
                "WHERE trigger_name = 'UpdateStock' AND trigger_schema = 'stocks_db'";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(triggerCheckQuery)) {
            assertTrue(rs.next(), "Trigger 'UpdateStock' should exist in the database");
        }
    }

    @Test
    @Order(4)
    void testInsertProductAndVerifyInventoryEntry() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("DELETE FROM inventory");
            stmt.executeUpdate("DELETE FROM products");

            stmt.executeUpdate("INSERT INTO products (product_id, product_name, price) VALUES (1, 'Test Product', 19.99)");

            ResultSet rs = stmt.executeQuery("SELECT stock_quantity FROM inventory WHERE product_id = 1");
            assertTrue(rs.next(), "Inventory entry for product_id = 1 should exist");
            assertEquals(0, rs.getInt("stock_quantity"), "Stock quantity should be 0 for new product");
        }
    }

    @AfterAll
    static void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
