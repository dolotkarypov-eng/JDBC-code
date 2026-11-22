public class Clothing extends Product implements Taxable, Discountable {
    private String size;
    private String material;

    public Clothing(String id, String name, double price, int quantity, String size, String material, double weight, String country, Supplier supplier) {
        super(id, name, "Apparel", price, quantity, weight, country, supplier);
        this.size = size;
        this.material = material;
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
        return getPrice() * 0.05;
    }

    @Override
    public void applyPromoDiscount() {
        setPrice(getPrice() * 0.85);
    }
}