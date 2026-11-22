import java.util.Objects;

public abstract class Product implements Comparable<Product> {
    private String id; // Уникальный бизнес-ключ (SKU)
    private String name;
    private String category;
    private double price;
    private int quantity;
    private boolean inStock;

    private ProductDetails details; // Composition
    private Supplier supplier;       // Aggregation

    public Product(String id, String name, String category, double price, int quantity, double weight, String country, Supplier supplier) {
        this.id = id;
        this.name = name;
        this.category = category;
        setPrice(price);
        setQuantity(quantity);
        this.details = new ProductDetails(weight, country);
        this.supplier = supplier;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getPrice() { return price; }
    public void setPrice(double price) {
        if (price >= 0) this.price = price;
        else this.price = 0.0;
    }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) {
        if (quantity >= 0) {
            this.quantity = quantity;
            this.inStock = (quantity > 0);
        } else {
            this.quantity = 0;
            this.inStock = false;
        }
    }
    public boolean isInStock() { return inStock; }
    public Supplier getSupplier() { return supplier; }

    public void displayInfo() {
        System.out.println("----------------------------------------");
        System.out.println("ID           : " + id);
        System.out.println("Product Name : " + name);
        System.out.println("Category     : " + category);
        System.out.printf("Base Price   : $%.2f\n", price);
        System.out.println("Quantity     : " + quantity);
        System.out.println("In Stock     : " + (inStock ? "Yes" : "No"));
        details.printDetails();
        if (supplier != null) {
            System.out.println("Supplier     : " + supplier.getCompanyName() + " (" + supplier.getContactEmail() + ")");
        }
    }

    public abstract double getFinalPrice();
    public abstract void adjustInventory(int amount);

    // Block A Contract: Уникальность по ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Block C Contract: Естественная сортировка по названию
    @Override
    public int compareTo(Product o) {
        return this.name.compareToIgnoreCase(o.name);
    }
}