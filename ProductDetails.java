public class ProductDetails {
    private double weightKg;
    private String countryOfOrigin;

    public ProductDetails(double weightKg, String countryOfOrigin) {
        this.weightKg = weightKg;
        this.countryOfOrigin = countryOfOrigin;
    }

    public void printDetails() {
        System.out.println("Specs        : " + weightKg + " kg, Made in " + countryOfOrigin);
    }
}