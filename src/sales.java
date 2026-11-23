public class Sale {

    int invoiceNo;
    String productName;
    int quantity;
    double unitPrice;
    double total;
    String dateTime;

    public Sale(int invoiceNo, String productName, int quantity,
                double unitPrice, double total, String dateTime) {

        this.invoiceNo = invoiceNo;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.total = total;
        this.dateTime = dateTime;
    }
}
