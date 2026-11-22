public class InvoiceGenerator {
    public void generateInvoice(Product product) {
        System.out.println("========== INVOICE ==========");
        System.out.println("Item ID: " + product.getId() + " | Name: " + product.getName());
        System.out.printf("Total Due: $%.2f\n", product.getFinalPrice());
        System.out.println("=============================");
    }
}