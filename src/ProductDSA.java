public class ProductDSA {

    Product[] products = new Product[200];
    int productCount = 0;
    int nextId = 1;

    // Add product: Array insertion
    public boolean addProduct(String name, double price, int quantity) {

        if (productCount >= products.length) {
            return false;
        }

        products[productCount] =
                new Product(nextId++, name, price, quantity);

        productCount++;
        return true;
    }

    // View products: Array traversal
    public Product[] getProducts() {
        return products;
    }

    public int getProductCount() {
        return productCount;
    }

    // Access product using ID
    public Product findById(int id) {

        for (int i = 0; i < productCount; i++) {
            if (products[i].id == id) {
                return products[i];
            }
        }

        return null;
    }

    // Update product
    public boolean updateProduct(int id, String name,
                                 double price, int quantity) {

        Product product = findById(id);

        if (product == null) {
            return false;
        }

        product.name = name;
        product.price = price;
        product.quantity = quantity;

        return true;
    }

    // Delete product: Shift array elements
    public boolean deleteProduct(int id) {

        int index = -1;

        for (int i = 0; i < productCount; i++) {
            if (products[i].id == id) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            return false;
        }

        for (int i = index; i < productCount - 1; i++) {
            products[i] = products[i + 1];
        }

        products[productCount - 1] = null;
        productCount--;

        return true;
    }