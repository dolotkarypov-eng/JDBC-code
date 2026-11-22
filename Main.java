import java.util.*;

public class Main {
    private static ProductService service = new ProductService();
    private static InvoiceGenerator invoiceGen = new InvoiceGenerator();
    private static Scanner scanner = new Scanner(System.in);

    // Скрытые данные для входа админа
    private static final String ADMIN_LOGIN = "admin";
    private static final String ADMIN_PASS = "admin123";

    // Личная корзина покупателя: Товар -> Количество в заказе
    private static Map<Product, Integer> userCart = new HashMap<>();

    public static void main(String[] args) {
        if (service.getAllProducts().isEmpty()) {
            initDefaultData();
        }

        while (true) {
            System.out.println("\n==========================================");
            System.out.println("           WELCOME TO OUR STORE           ");
            System.out.println("==========================================");
            System.out.println("1. Enter Customer Storefront");
            System.out.println("0. Exit Application");
            System.out.print("Select action (or type hidden command): ");

            String input = scanner.nextLine().trim();

            if (input.equals("1")) {
                userStorefrontMenu();
            } else if (input.equals("0")) {
                System.out.println("Thank you for visiting! Goodbye.");
                return;
            } else if (input.equalsIgnoreCase("admin")) {
                adminLogin();
            } else {
                System.out.println("Invalid choice! Try again.");
            }
        }
    }

    private static void initDefaultData() {
        Supplier techSupplier = new Supplier("Global Tech Inc.", "orders@globaltech.com");
        Supplier agroSupplier = new Supplier("Organic Farms Co.", "contact@organicfarms.com");
        Supplier fashionSupplier = new Supplier("Fashion World Ltd.", "contact@fashionworld.com");

        service.addProduct(new Electronics("E-101", "Gaming Laptop", 1200.00, 5, 24, "ASUS", 2.4, "Taiwan", techSupplier));
        service.addProduct(new Electronics("E-102", "Smartphone", 800.00, 12, 12, "Apple", 0.2, "China", techSupplier));
        service.addProduct(new FoodItem("F-201", "Organic Milk", 3.50, 50, "2026-10-15", true, 1.0, "Kyrgyzstan", agroSupplier));
        service.addProduct(new Clothing("A-301", "Cotton T-Shirt", 25.00, 30, "L", "Cotton", 0.3, "Turkey", fashionSupplier));
    }

    // ==========================================
    // ИНТЕРФЕЙС ПОКУПАТЕЛЯ (USER STOREFRONT)
    // ==========================================
    private static void userStorefrontMenu() {
        while (true) {
            System.out.println("\n--- CUSTOMER STOREFRONT MENU ---");
            System.out.println("1. View Store Catalog");
            System.out.println("2. Search Product by ID (O(1) Map)");
            System.out.println("3. View Products Sorted by Price");
            System.out.println("4. Open My Shopping Cart (" + userCart.size() + " items)");
            System.out.println("0. Back to Main Entrance");
            System.out.print("Select choice: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    service.displayUserStorefront();
                    break;
                case "2":
                    System.out.print("Enter Product ID (e.g. E-101): ");
                    String id = scanner.nextLine();
                    Product found = service.findById(id);
                    if (found != null) {
                        System.out.printf("\nFound: %s | Price: $%.2f | Available: %d pcs\n",
                                found.getName(), found.getFinalPrice(), found.getQuantity());
                    } else {
                        System.out.println("Product not found!");
                    }
                    break;
                case "3":
                    System.out.println("\n--- Products Sorted by Price ---");
                    for (Product p : service.getProductsSorted(new PriceComparator())) {
                        System.out.printf(" - %s [%s]: $%.2f\n", p.getName(), p.getId(), p.getFinalPrice());
                    }
                    break;
                case "4":
                    manageUserCart();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    private static void manageUserCart() {
        while (true) {
            System.out.println("\n--- MY SHOPPING CART ---");
            if (userCart.isEmpty()) {
                System.out.println(" Your cart is currently empty.");
            } else {
                double total = 0;
                for (Map.Entry<Product, Integer> entry : userCart.entrySet()) {
                    Product p = entry.getKey();
                    int qty = entry.getValue();
                    double subtotal = p.getFinalPrice() * qty;
                    total += subtotal;
                    System.out.printf(" - [%s] %s | Qty: %d | Price/ea: $%.2f | Subtotal: $%.2f\n",
                            p.getId(), p.getName(), qty, p.getFinalPrice(), subtotal);
                }
                System.out.printf(" Total Estimated Price: $%.2f\n", total);
            }

            System.out.println("\nCart Actions:");
            System.out.println("1. Add item to cart");
            System.out.println("2. Increase item quantity (+1)");
            System.out.println("3. Decrease item quantity (-1)");
            System.out.println("4. Checkout & Place Order (Send to Queue)");
            System.out.println("0. Back to Store Catalog");
            System.out.print("Select action: ");

            String action = scanner.nextLine();
            switch (action) {
                case "1":
                    System.out.print("Enter Product ID to add: ");
                    String addId = scanner.nextLine();
                    Product pAdd = service.findById(addId);
                    if (pAdd != null && pAdd.isInStock()) {
                        userCart.put(pAdd, userCart.getOrDefault(pAdd, 0) + 1);
                        System.out.println(" [+] Added 1 unit of " + pAdd.getName() + " to your cart.");
                    } else {
                        System.out.println(" Product unavailable or not found!");
                    }
                    break;
                case "2":
                    System.out.print("Enter Product ID to increase (+1): ");
                    String incId = scanner.nextLine();
                    Product pInc = service.findById(incId);
                    if (pInc != null && userCart.containsKey(pInc)) {
                        if (userCart.get(pInc) < pInc.getQuantity()) {
                            userCart.put(pInc, userCart.get(pInc) + 1);
                            System.out.println(" [+] Quantity increased for " + pInc.getName());
                        } else {
                            System.out.println(" Cannot exceed available warehouse stock!");
                        }
                    } else {
                        System.out.println(" Item not in cart!");
                    }
                    break;
                case "3":
                    System.out.print("Enter Product ID to decrease (-1): ");
                    String decId = scanner.nextLine();
                    Product pDec = service.findById(decId);
                    if (pDec != null && userCart.containsKey(pDec)) {
                        int currentQty = userCart.get(pDec);
                        if (currentQty > 1) {
                            userCart.put(pDec, currentQty - 1);
                            System.out.println(" [-] Quantity decreased for " + pDec.getName());
                        } else {
                            userCart.remove(pDec);
                            System.out.println(" [-] Item removed from cart.");
                        }
                    } else {
                        System.out.println(" Item not in cart!");
                    }
                    break;
                case "4":
                    if (userCart.isEmpty()) {
                        System.out.println(" Cannot checkout an empty cart!");
                    } else {
                        for (Map.Entry<Product, Integer> entry : userCart.entrySet()) {
                            Product item = entry.getKey();
                            service.enqueueOrder(item);
                        }
                        userCart.clear();
                        System.out.println(" Order successfully placed and submitted to processing Queue!");
                        return;
                    }
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    // ==========================================
    // ⚙️ СКРЫТЫЙ ИНТЕРФЕЙС АДМИНИСТРАТОРА
    // ==========================================
    private static void adminLogin() {
        System.out.println("\n--- RESTRICTED ADMIN ACCESS ---");
        System.out.print("Enter Admin Username: ");
        String login = scanner.nextLine();
        System.out.print("Enter Admin Password: ");
        String pass = scanner.nextLine();

        if (ADMIN_LOGIN.equals(login) && ADMIN_PASS.equals(pass)) {
            System.out.println("\n Authentication successful! Welcome, Administrator.");
            adminControlPanel();
        } else {
            System.out.println(" Error: Access Denied! Invalid credentials.");
        }
    }

    private static void adminControlPanel() {
        while (true) {
            System.out.println("\n--- ADMINISTRATIVE CONTROL PANEL ---");
            System.out.println("1. Full Warehouse Audit (View all details & stock)");
            System.out.println("2. Adjust Warehouse Stock (+ / - items)");
            System.out.println("3. Add New Product Position");
            System.out.println("4. Delete Product by ID (Synchronous Delete)");
            System.out.println("5. View Incoming Orders Queue (Pending: " + service.getQueueSize() + ")");
            System.out.println("6. Process Next Order from Queue (poll())");
            System.out.println("0. Exit Admin Control Panel");
            System.out.print("Select administrative action: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    service.displayAdminWarehouseDetails();
                    break;
                case "2":
                    System.out.print("Enter Product ID (e.g. E-101): ");
                    String adjId = scanner.nextLine();
                    System.out.print("Enter stock adjustment amount (e.g., 10 or -5): ");
                    try {
                        int amount = Integer.parseInt(scanner.nextLine());
                        service.adjustStock(adjId, amount);
                    } catch (Exception e) {
                        System.out.println("Invalid number!");
                    }
                    break;
                case "3":
                    try {
                        System.out.println("\nSelect Product Category:");
                        System.out.println("1. Electronics");
                        System.out.println("2. Food & Beverage");
                        System.out.println("3. Apparel (Clothing)");
                        System.out.print("Choice: ");
                        String catChoice = scanner.nextLine();

                        System.out.print("Enter ID (e.g. A-302): ");
                        String id = scanner.nextLine();
                        System.out.print("Enter Name: ");
                        String name = scanner.nextLine();
                        System.out.print("Enter Price: ");
                        double price = Double.parseDouble(scanner.nextLine());
                        System.out.print("Enter Quantity: ");
                        int qty = Integer.parseInt(scanner.nextLine());

                        Supplier supp = new Supplier("Admin Supplier Co.", "admin@supplier.com");
                        Product newProd;

                        if (catChoice.equals("2")) {
                            newProd = new FoodItem(id, name, price, qty, "2026-12-31", true, 0.5, "Local", supp);
                        } else if (catChoice.equals("3")) {
                            newProd = new Clothing(id, name, price, qty, "M", "Cotton", 0.4, "Imported", supp);
                        } else {
                            newProd = new Electronics(id, name, price, qty, 12, "Generic", 1.0, "China", supp);
                        }

                        service.addProduct(newProd);
                    } catch (Exception e) {
                        System.out.println("Input error!");
                    }
                    break;
                case "4":
                    System.out.print("Enter Product ID to delete: ");
                    String delId = scanner.nextLine();
                    service.deleteProduct(delId);
                    break;
                case "5":
                    System.out.println("Pending customer orders in queue: " + service.getQueueSize());
                    break;
                case "6":
                    service.processNextOrder(invoiceGen);
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }
}