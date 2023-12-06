import java.sql.*;
import java.util.Scanner;

public class bookstore {

    private static final String DATABASE_URL = "jdbc:postgresql://localhost:5433/test_database";
    private static final String DB_USER = "turalhasanov";
    private static final String DB_PASSWORD = "";

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in);
             Connection conn = DriverManager.getConnection(DATABASE_URL, DB_USER, DB_PASSWORD)) {

            System.out.println("Please keep in mind to create an author and then a book, also a customer and then an order.\nBecause those are interrelated, and have foreign keys pointing to each other.\nIn case you ignore it, you will have errors while performing operations on the database");
            System.out.println("Choose an action: \n1. Create Book\n2. Create Author\n3. Create Customer\n4. Create Order\n5. Retrieve\n6. Update\n7. Delete\n8 - Get Metadata for individual table\n");
            String userInput = scanner.nextLine();

            if (userInput.equals("1")) {
                addBook(scanner, conn);
            } else if (userInput.equals("2")) {
                addAuthor(scanner, conn);
            } else if (userInput.equals("3")) {
                addCustomer(scanner, conn);
            } else if (userInput.equals("4")) {
                addOrder(scanner, conn);
            } else if (userInput.equals("5")) {
                retrieve(conn);
            } else if (userInput.equals("6")) {
                updateBook(scanner, conn);
            } else if (userInput.equals("7")) {
                deleteBook(scanner, conn);
            } else if (userInput.equals("8")) {
                System.out.print("Which table are you looking for: ");
                String name_of_table = scanner.nextLine();
                accessMetadata(conn, name_of_table);
            } else {
                System.out.println("Invalid choice.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
private static void addBook(Scanner scanner, Connection connection) throws SQLException {
    System.out.println("Inserting a new book.");
    System.out.print("Enter book ID: ");
    int bookId = Integer.parseInt(scanner.nextLine());
    System.out.print("Enter book name: ");
    String bookName = scanner.nextLine();
    System.out.print("Enter quantity: ");
    int bookQuantity = Integer.parseInt(scanner.nextLine());
    System.out.print("Enter author ID: ");
    int authorId = Integer.parseInt(scanner.nextLine());

    String insertBookSql = "INSERT INTO Books (book_id, name, quantity, author_id) VALUES (?, ?, ?, ?)";
    try (PreparedStatement insertBookStmt = connection.prepareStatement(insertBookSql)) {
        insertBookStmt.setInt(1, bookId);
        insertBookStmt.setString(2, bookName);
        insertBookStmt.setInt(3, bookQuantity);
        insertBookStmt.setInt(4, authorId);
        insertBookStmt.executeUpdate();
        System.out.println("Book added successfully.");
    }
}



private static void addAuthor(Scanner scanner, Connection connection) throws SQLException {
    System.out.println("Inserting a new author.");
    System.out.print("Enter author ID: ");
    int authorId = Integer.parseInt(scanner.nextLine());
    System.out.print("Enter author's name: ");
    String authorName = scanner.nextLine();

    String insertAuthorSql = "INSERT INTO Authors (author_id, name) VALUES (?, ?)";
    try (PreparedStatement insertAuthorStmt = connection.prepareStatement(insertAuthorSql)) {
        insertAuthorStmt.setInt(1, authorId);
        insertAuthorStmt.setString(2, authorName);
        insertAuthorStmt.executeUpdate();
        System.out.println("Author added successfully.");
    }
}



private static void addCustomer(Scanner scanner, Connection connection) throws SQLException {
    System.out.println("Inserting a new customer.");
    System.out.print("Enter customer ID: ");
    int customerId = Integer.parseInt(scanner.nextLine());
    System.out.print("Enter customer's name: ");
    String customerName = scanner.nextLine();
    System.out.print("Enter customer's age: ");
    int customerAge = Integer.parseInt(scanner.nextLine());

    String insertCustomerSql = "INSERT INTO Customers (customer_id, name, age) VALUES (?, ?, ?)";
    try (PreparedStatement insertCustomerStmt = connection.prepareStatement(insertCustomerSql)) {
        insertCustomerStmt.setInt(1, customerId);
        insertCustomerStmt.setString(2, customerName);
        insertCustomerStmt.setInt(3, customerAge);
        insertCustomerStmt.executeUpdate();
        System.out.println("Customer added successfully.");
    }
}




    private static void addOrder(Scanner scanner, Connection connection) throws SQLException {
        System.out.println("Inserting a new order.");
        System.out.print("Enter order ID: ");
        int orderId = Integer.parseInt(scanner.nextLine());
        System.out.print("Enter payment amount: ");
        int payment = Integer.parseInt(scanner.nextLine());
        System.out.print("Enter customer ID: ");
        int customerId = Integer.parseInt(scanner.nextLine());
        System.out.print("Enter book ID: ");
        int bookId = Integer.parseInt(scanner.nextLine());
        System.out.print("Enter order quantity: ");
        int orderQuantity = Integer.parseInt(scanner.nextLine());

        String insertOrderSql = "INSERT INTO Orders (order_id, payment, customer_id, book_id, orders_quantity) VALUES (?, ?, ?, ?, ?)";
        String checkStockSql = "SELECT quantity FROM Books WHERE book_id = ?";
        String updateQuantitySql = "UPDATE Books SET quantity = quantity - ? WHERE book_id = ?";

        try (PreparedStatement checkStockStmt = connection.prepareStatement(checkStockSql)) {

            checkStockStmt.setInt(1, bookId);
            ResultSet resultSet = checkStockStmt.executeQuery();

            if (!resultSet.next() || resultSet.getInt("quantity") < orderQuantity) {
                throw new SQLException("There is not enough stock for this book");
            }
        }

        try (PreparedStatement insertOrderStmt = connection.prepareStatement(insertOrderSql)) {
            insertOrderStmt.setInt(1, orderId);
            insertOrderStmt.setInt(2, payment);
            insertOrderStmt.setInt(3, customerId);
            insertOrderStmt.setInt(4, bookId);
            insertOrderStmt.setInt(5, orderQuantity);
            insertOrderStmt.executeUpdate();
            System.out.println("Order added successfully.");
        }

        try (PreparedStatement updateStockStmt = connection.prepareStatement(updateQuantitySql)) {
            updateStockStmt.setInt(1, orderQuantity);
            updateStockStmt.setInt(2, bookId);
            updateStockStmt.executeUpdate();
        }
    }


    private static void retrieve(Connection conn) throws SQLException {
        String sql = "SELECT b.name, b.quantity, a.name AS author_name, " +
                "COALESCE(SUM(o.order_quantity), 0) AS total_orders_quantity " +
                "FROM Books b " +
                "LEFT JOIN Authors a ON b.author_id = a.author_id " +
                "LEFT JOIN Orders o ON b.book_id = o.book_id " +
                "GROUP BY b.name, a.name, b.quantity";


        System.out.println("Displaying all books with associated orders and authors:");
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String bookName = rs.getString("name");
                String authorName = rs.getString("author_name");
                String quantity = rs.getString("quantity");
                String orderInfo = rs.getString("total_orders_quantity");
                System.out.println("Book: " + bookName + ", Quantity " + quantity + ", Author: " + authorName + ", Order Quantity: " + orderInfo);
            }
        }
    }



private static void updateBook(Scanner scanner, Connection connection) throws SQLException {
    System.out.println("Updating a book.");
    System.out.print("Enter the ID of the book to update: ");
    int bookId = Integer.parseInt(scanner.nextLine());
    System.out.print("Enter new name (press enter to skip): ");
    String newName = scanner.nextLine();
    System.out.print("Enter new quantity (enter 0 to skip): ");
    int newQuantity = Integer.parseInt(scanner.nextLine());

    String updateQuery = "UPDATE Books SET name = COALESCE(NULLIF(?, ''), name), " +
            "quantity = COALESCE(NULLIF(?, 0), quantity) WHERE book_id = ?";
    try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
        preparedStatement.setString(1, newName);
        preparedStatement.setInt(2, newQuantity);
        preparedStatement.setInt(3, bookId);
        int rowsAffected = preparedStatement.executeUpdate();
        System.out.println(rowsAffected + " book(s) updated.");
    }
}



private static void deleteBook(Scanner scanner, Connection connection) throws SQLException {
    System.out.println("Deleting a book.");
    System.out.print("Enter the ID of the book to delete: ");
    int bookId = Integer.parseInt(scanner.nextLine());

    String deleteQuery = "DELETE FROM Books WHERE book_id = ?";
    try (PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {
        preparedStatement.setInt(1, bookId);
        int rowsAffected = preparedStatement.executeUpdate();
        System.out.println(rowsAffected + " book(s) deleted.");
    }
}


private static void accessMetadata(Connection connection, String tableName) {
    try {
        retrieveColumnDetails(connection, tableName);
        retrievePrimaryKeys(connection, tableName);
        retrieveForeignKeys(connection, tableName);
    } catch (SQLException exception) {
        System.out.println("An error occurred while accessing metadata for table: " + tableName);
        exception.printStackTrace();
    }
}


private static void retrieveColumnDetails(Connection connection, String tableName) throws SQLException {
    DatabaseMetaData metadata = connection.getMetaData();

    System.out.println("Details of columns in table " + tableName + ":");
    try (ResultSet columnsResultSet = metadata.getColumns(null, null, tableName, null)) {
        while (columnsResultSet.next()) {
            System.out.println("Column Name: " + columnsResultSet.getString("COLUMN_NAME") +
                    ", Data Type: " + columnsResultSet.getString("TYPE_NAME") +
                    ", Width: " + columnsResultSet.getInt("COLUMN_SIZE"));
        }
    }
}




    private static void retrievePrimaryKeys(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();

        System.out.println("Listing primary keys in table: " + tableName);
        try (ResultSet primaryKeysResultSet = metadata.getPrimaryKeys(null, null, tableName)) {
            while (primaryKeysResultSet.next()) {
                System.out.println("Primary Key Column: " + primaryKeysResultSet.getString("COLUMN_NAME") +
                        ", Key Name: " + primaryKeysResultSet.getString("PK_NAME"));
            }
        }
    }



    private static void retrieveForeignKeys(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();

        System.out.println("Foreign keys associated with table " + tableName + ":");
        try (ResultSet foreignKeysResultSet = metadata.getImportedKeys(null, null, tableName)) {
            while (foreignKeysResultSet.next()) {
                System.out.println("Foreign Key Name: " + foreignKeysResultSet.getString("FK_NAME") +
                        ", Associated Column: " + foreignKeysResultSet.getString("FKCOLUMN_NAME") +
                        ", Reference Table: " + foreignKeysResultSet.getString("PKTABLE_NAME") +
                        ", Reference Column: " + foreignKeysResultSet.getString("PKCOLUMN_NAME"));
            }
        }
    }
}



