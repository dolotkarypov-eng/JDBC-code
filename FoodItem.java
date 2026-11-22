public class FoodItem extends Product implements Discountable {
    private String expirationDate;
    private boolean isOrganic;

    public FoodItem(String id, String name, double price, int quantity, String expirationDate, boolean isOrganic, double weight, String country, Supplier supplier) {
        super(id, name, "Food & Beverage", price, quantity, weight, country, supplier);
        this.expirationDate = expirationDate;
        this.isOrganic = isOrganic;
    }

    @Override
    public double getFinalPrice() {
        return getPrice();
    }

    @Override
    public void adjustInventory(int amount) {
        setQuantity(getQuantity() + amount);
    }

    @Override
    public void applyPromoDiscount() {
        if (isOrganic) {
            setPrice(getPrice() * 0.95);
        }
    }
}