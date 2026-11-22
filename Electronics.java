public class Electronics extends Product implements Taxable {
    private int warrantyMonths;
    private String brand;

    public Electronics(String id, String name, double price, int quantity, int warrantyMonths, String brand, double weight, String country, Supplier supplier) {
        super(id, name, "Electronics", price, quantity, weight, country, supplier);
        this.warrantyMonths = warrantyMonths;
        this.brand = brand;
    }

    @Override
    public double getFinalPrice() {
        return getPrice() + calculateTax();
    }

    @Override
    public void adjustInventory(int amount) {
        setQuantity(getQuantity() + amount);
    }

    @Override
    public double calculateTax() {
        return getPrice() * 0.12;
    }
}