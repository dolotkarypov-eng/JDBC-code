import java.sql.*;
import java.util.*;

public class ProductService {
    private List<Product> productList = new ArrayList<>();
    private Set<Product> uniqueProducts = new HashSet<>();
    private Map<String, Product> productMap = new HashMap<>();
    private Map<String, List<Product>> categoryMap = new HashMap<>();
    private Queue<Product> orderQueue = new ArrayDeque<>();

    // Подключение к вашей БД H2 (убедитесь, что путь совпадает с настройками siw1)
    private static final String DB_URL = "jdbc:h2:C:/Users/ukuev/Downloads/siw1;AUTO_SERVER=TRUE";
    private static final String DB_USER = "sa";
    private static final String DB_PASS = "";

    public ProductService() {
        initializeDatabase();
        loadFromDatabase();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    // 1. Создание таблицы (DDL)
    public void initializeDatabase() {
        String sql = """
            CREATE TABLE IF NOT EXISTS products (
                id VARCHAR(20) PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                category VARCHAR(50) NOT NULL,
                price DECIMAL(10, 2) NOT NULL,
                quantity INT NOT NULL,
                in_stock BOOLEAN NOT NULL,
                supplier_name VARCHAR(100) NOT NULL
            )
            """;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(" [!] DB Init Error: " + e.getMessage());
        }
    }

    // 2. CREATE (Добавление товара через PreparedStatement)
    public boolean addProduct(Product p) {
        if (productMap.containsKey(p.getId())) {
            System.out.println(" [!] Error: Product with ID '" + p.getId() + "' already exists!");
            return false;
        }

        String sql = "INSERT INTO products (id, name, category, price, quantity, in_stock, supplier_name) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, p.getId());
            pstmt.setString(2, p.getName());
            pstmt.setString(3, p.getCategory());
            pstmt.setDouble(4, p.getPrice());
            pstmt.setInt(5, p.getQuantity());
            pstmt.setBoolean(6, p.isInStock());
            pstmt.setString(7, p.getSupplier() != null ? p.getSupplier().getCompanyName() : "Unknown");
            pstmt.executeUpdate();

            productList.add(p);
            uniqueProducts.add(p);
            productMap.put(p.getId(), p);
            categoryMap.computeIfAbsent(p.getCategory(), k -> new ArrayList<>()).add(p);

            System.out.println(" [+] Product added to DB: " + p.getName() + " (ID: " + p.getId() + ")");
            return true;
        } catch (SQLException e) {
            System.out.println(" [!] DB Insert Error: " + e.getMessage());
            return false;
        }
    }

    // 3. DELETE (Удаление товара из БД и коллекций)
    public boolean deleteProduct(String id) {
        Product p = productMap.get(id);
        if (p == null) {
            System.out.println(" [!] Product ID '" + id + "' not found!");
            return false;
        }

        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();

            productMap.remove(id);
            productList.remove(p);
            uniqueProducts.remove(p);
            if (categoryMap.containsKey(p.getCategory())) {
                categoryMap.get(p.getCategory()).remove(p);
            }

            System.out.println(" [-] Product ID '" + id + "' deleted from DB and collections.");
            return true;
        } catch (SQLException e) {
            System.out.println(" [!] DB Delete Error: " + e.getMessage());
            return false;
        }
    }

    // 4. UPDATE (Обновление количества товара в БД)
    public boolean adjustStock(String id, int amount) {
        Product p = productMap.get(id);
        if (p == null) {
            System.out.println(" [!] Product ID not found!");
            return false;
        }

        p.adjustInventory(amount);
        String sql = "UPDATE products SET quantity = ?, in_stock = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, p.getQuantity());
            pstmt.setBoolean(2, p.isInStock());
            pstmt.setString(3, id);
            pstmt.executeUpdate();

            System.out.println(" [*] Stock updated in DB for '" + p.getName() + "'. New quantity: " + p.getQuantity());
            return true;
        } catch (SQLException e) {
            System.out.println(" [!] DB Update Error: " + e.getMessage());
            return false;
        }
    }

    // 5. READ ALL (Загрузка товаров из БД при старте)
    public void loadFromDatabase() {
        String sql = "SELECT * FROM products ORDER BY id";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Product p = mapRowToProduct(rs);
                productList.add(p);
                uniqueProducts.add(p);
                productMap.put(p.getId(), p);
                categoryMap.computeIfAbsent(p.getCategory(), k -> new ArrayList<>()).add(p);
            }
        } catch (SQLException e) {
            System.out.println(" [!] DB Load Error: " + e.getMessage());
        }
    }

    // 6. READ BY ID (Поиск товара в БД через PreparedStatement)
    public Product findById(String id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToProduct(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println(" [!] DB Search Error: " + e.getMessage());
        }
        return null;
    }

    // 7. AGGREGATE FUNCTIONS (COUNT, SUM, AVG, MIN, MAX)
    public void displayAggregateStats() {
        String sql = """
            SELECT
                COUNT(*) AS total_products,
                SUM(quantity) AS total_units,
                SUM(price * quantity) AS total_value,
                AVG(price) AS avg_price,
                MIN(price) AS min_price,
                MAX(price) AS max_price
            FROM products
            """;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                System.out.println("\n================ SQL AGGREGATE STATISTICS ================");
                System.out.println("Total Products (COUNT) : " + rs.getInt("total_products"));
                System.out.println("Total Units    (SUM)   : " + rs.getInt("total_units") + " pcs");
                System.out.printf("Total Value    (SUM)   : $%.2f\n", rs.getDouble("total_value"));
                System.out.printf("Average Price  (AVG)   : $%.2f\n", rs.getDouble("avg_price"));
                System.out.printf("Minimum Price  (MIN)   : $%.2f\n", rs.getDouble("min_price"));
                System.out.printf("Maximum Price  (MAX)   : $%.2f\n", rs.getDouble("max_price"));
                System.out.println("==========================================================");
            }
        } catch (SQLException e) {
            System.out.println(" [!] DB Aggregate Error: " + e.getMessage());
        }
    }

    private Product mapRowToProduct(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String name = rs.getString("name");
        String category = rs.getString("category");
        double price = rs.getDouble("price");
        int qty = rs.getInt("quantity");
        String suppName = rs.getString("supplier_name");

        String cleanName = suppName.toLowerCase().replaceAll("[^a-z0-9]", "");
        Supplier supp = new Supplier(suppName, "contact@" + cleanName + ".com");

        if ("Food & Beverage".equalsIgnoreCase(category)) {
            return new FoodItem(id, name, price, qty, "2026-12-31", true, 1.0, "Kyrgyzstan", supp);
        } else if ("Apparel".equalsIgnoreCase(category)) {
            return new Clothing(id, name, price, qty, "L", "Cotton", 0.5, "Imported", supp);
        } else {
            return new Electronics(id, name, price, qty, 12, "Brand", 1.0, "Imported", supp);
        }
    }

    public void displayUserStorefront() {
        System.out.println("\n=======================================================");
        System.out.println("                ONLINE STORE CATALOG                   ");
        System.out.println("=======================================================");
        for (Product p : productList) {
            System.out.printf(" ID: %-6s | Item: %-18s | Category: %-15s | Price: $%-8.2f | Status: %s\n",
                    p.getId(), p.getName(), p.getCategory(), p.getFinalPrice(),
                    (p.isInStock() ? "In Stock (" + p.getQuantity() + " available)" : "OUT OF STOCK"));
        }
        System.out.println("-------------------------------------------------------");
    }

    public void displayAdminWarehouseDetails() {
        System.out.println("\n=======================================================");
        System.out.println("            FULL ADMIN WAREHOUSE AUDIT                 ");
        System.out.println("=======================================================");
        for (Product p : productList) {
            p.displayInfo();
        }
        displayAggregateStats();
        System.out.println("-------------------------------------------------------");
    }

    public List<Product> getByCategory(String category) { return categoryMap.getOrDefault(category, Collections.emptyList()); }

    public void enqueueOrder(Product p) {
        orderQueue.add(p);
        System.out.println(" [>] Order for " + p.getName() + " queued for processing!");
    }

    public void processNextOrder(InvoiceGenerator invoiceGen) {
        Product p = orderQueue.poll();
        if (p != null) {
            System.out.println("\n [Admin] Processing queued request via poll():");
            invoiceGen.generateInvoice(p);
        } else {
            System.out.println(" [i] Order queue is currently empty.");
        }
    }

    public List<Product> getProductsSorted(Comparator<Product> comparator) {
        List<Product> sortedList = new ArrayList<>(productList);
        if (comparator == null) Collections.sort(sortedList);
        else sortedList.sort(comparator);
        return sortedList;
    }

    public List<Product> getAllProducts() { return productList; }
    public int getQueueSize() { return orderQueue.size(); }
}