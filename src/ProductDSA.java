
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

    // Linear Search
    public Product[] searchProducts(String keyword) {

        Product[] results = new Product[productCount];
        int count = 0;

        for (int i = 0; i < productCount; i++) {

            Product p = products[i];

            if (p.name.toLowerCase().contains(keyword.toLowerCase())
                    || String.valueOf(p.id).equals(keyword)) {

                results[count] = p;
                count++;
            }
        }

        Product[] finalResults = new Product[count];

        for (int i = 0; i < count; i++) {
            finalResults[i] = results[i];
        }

        return finalResults;
    }

    // Bubble Sort: Sort products by name
    public void bubbleSort(String order) {

        for (int i = 0; i < productCount - 1; i++) {

            for (int j = 0; j < productCount - i - 1; j++) {

                int comparison = products[j].name.compareToIgnoreCase(
                        products[j + 1].name);

                boolean swap = order.equals("asc")
                        ? comparison > 0
                        : comparison < 0;

                if (swap) {
                    Product temp = products[j];
                    products[j] = products[j + 1];
                    products[j + 1] = temp;
                }
            }
        }
    }

    // Selection Sort: Sort by price or quantity
    public void selectionSort(String by, String order) {

        for (int i = 0; i < productCount - 1; i++) {

            int selected = i;

            for (int j = i + 1; j < productCount; j++) {

                double current;
                double selectedValue;

                if (by.equals("price")) {
                    current = products[j].price;
                    selectedValue = products[selected].price;
                } else {
                    current = products[j].quantity;
                    selectedValue = products[selected].quantity;
                }

                boolean shouldSelect = order.equals("asc")
                        ? current < selectedValue
                        : current > selectedValue;

                if (shouldSelect) {
                    selected = j;
                }
            }

            Product temp = products[i];
            products[i] = products[selected];
            products[selected] = temp;
        }
    }

    // Load sample shop products
    public void loadSampleProducts() {

        String[] names = {
            "Milk", "Bread", "Eggs", "Sugar", "Rice",
            "Flour", "Cooking Oil", "Tea", "Coffee", "Salt",
            "Butter", "Cheese", "Yogurt", "Cream", "Juice",
            "Water Bottle", "Soft Drink", "Biscuits", "Chips", "Chocolate",
            "Soap", "Shampoo", "Conditioner", "Toothpaste", "Toothbrush",
            "Washing Powder", "Dishwashing Liquid", "Hand Wash", "Tissue Box", "Toilet Paper",
            "Detergent", "Floor Cleaner", "Air Freshener", "Mosquito Spray", "Shaving Cream",
            "Razor", "Hair Oil", "Face Wash", "Body Lotion", "Sunscreen",
            "Lentils", "Chickpeas", "Red Beans", "Black Pepper", "Red Chilli",
            "Turmeric", "Cumin", "Coriander Powder", "Garlic", "Ginger",
            "Onions", "Potatoes", "Tomatoes", "Carrots", "Cucumbers",
            "Apples", "Bananas", "Oranges", "Mangoes", "Grapes",
            "Chicken", "Beef", "Fish", "Frozen Peas", "Frozen Fries",
            "Jam", "Honey", "Peanut Butter", "Mayonnaise", "Ketchup",
            "Vinegar", "Soy Sauce", "Noodles", "Pasta", "Macaroni",
            "Cornflakes", "Oats", "Cereal", "Cake", "Rusk",
            "Mineral Water", "Energy Drink", "Green Tea", "Milk Powder", "Custard Powder",
            "Baking Powder", "Baking Soda", "Corn Flour", "Vermicelli", "Pudding Mix",
            "Baby Diapers", "Baby Wipes", "Sanitary Pads", "Cotton", "Bandages",
            "Batteries", "Light Bulb", "Matchbox", "Garbage Bags", "Aluminium Foil"
        };

        for (int i = 0; i < names.length; i++) {

            double price = 30 + (i * 17 % 970);
            int quantity = 10 + (i * 7 % 90);

            addProduct(names[i], price, quantity);
        }
    }
}
