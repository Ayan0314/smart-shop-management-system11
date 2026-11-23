import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;

public class Main {

    private static final Product[] products = new Product[200];
    private static final Sale[] sales = new Sale[500];

    private static int productCount = 0;
    private static int salesCount = 0;

    private static int nextProductId = 1;
    private static int nextInvoiceNo = 1001;

    private static final Path FRONTEND = Paths.get("frontend");

    public static void main(String[] args) throws Exception {

        // Sample products from project report
        addSeed("Milk", 180, 20);
        addSeed("Bread", 120, 15);
        addSeed("Eggs", 300, 30);
        addSeed("Juice", 250, 10);

        HttpServer server = HttpServer.create(
            new InetSocketAddress(8080), 0
        );

        server.createContext("/", Main::handle);

        server.setExecutor(Executors.newCachedThreadPool());

        server.start();

        System.out.println(
            "Smart Shop is running at http://localhost:8080"
        );
    }

    private static void addSeed(
        String name, double price, int quantity
    ) {

        products[productCount++] =
            new Product(nextProductId++, name, price, quantity);
    }

    private static void handle(HttpExchange ex)
            throws IOException {

        String path = ex.getRequestURI().getPath();
        String method = ex.getRequestMethod();

        try {

            if (path.startsWith("/api/")) {

                if (path.equals("/api/products")
                        && method.equals("GET")) {

                    sendJson(ex, 200, productsJson());

                }

                else if (path.equals("/api/sales")
                        && method.equals("GET")) {

                    sendJson(ex, 200, salesJson());

                }

                else if (path.equals("/api/add")
                        && method.equals("POST")) {

                    addProduct(ex);

                }

                else if (path.equals("/api/update")
                        && method.equals("POST")) {

                    updateProduct(ex);

                }

                else if (path.equals("/api/delete")
                        && method.equals("POST")) {

                    deleteProduct(ex);

                }

                else if (path.equals("/api/sell")
                        && method.equals("POST")) {

                    sellProduct(ex);

                }

                else if (path.equals("/api/search")
                        && method.equals("GET")) {

                    searchProducts(ex);

                }

                else if (path.equals("/api/sort")
                        && method.equals("GET")) {

                    sortProducts(ex);

                }

                else {

                    sendJson(
                        ex, 404,
                        "{\"error\":\"API route not found\"}"
                    );

                }

            }

            else {

                serveFrontend(ex, path);

            }

        }

        catch (Exception e) {

            sendJson(
                ex,
                400,
                "{\"error\":\""
                + escape(
                    e.getMessage() == null
                    ? "Invalid request"
                    : e.getMessage()
                )
                + "\"}"
            );

        }
    }

    // INSERTION: ADD PRODUCT TO ARRAY

    private static void addProduct(HttpExchange ex)
            throws IOException {

        var p = readForm(ex);

        String name = required(p, "name").trim();

        double price =
            Double.parseDouble(required(p, "price"));

        int quantity =
            Integer.parseInt(required(p, "quantity"));

        if (name.isEmpty() || price < 0 || quantity < 0) {

            sendJson(
                ex, 400,
                "{\"error\":\"Enter a valid name, price and quantity.\"}"
            );

            return;
        }

        if (productCount == products.length) {

            sendJson(
                ex, 400,
                "{\"error\":\"Product array is full.\"}"
            );

            return;
        }

        products[productCount++] = new Product(
            nextProductId++, name, price, quantity
        );

        sendJson(
            ex, 200,
            "{\"message\":\"Product added successfully.\"}"
        );
    }

    // UPDATE PRODUCT

    private static void updateProduct(HttpExchange ex)
            throws IOException {

        var p = readForm(ex);

        int id = Integer.parseInt(required(p, "id"));

        Product product = findById(id);

        if (product == null) {

            sendJson(
                ex, 404,
                "{\"error\":\"Product not found.\"}"
            );

            return;
        }

        String name = required(p, "name").trim();

        double price =
            Double.parseDouble(required(p, "price"));

        int quantity =
            Integer.parseInt(required(p, "quantity"));

        if (name.isEmpty() || price < 0 || quantity < 0) {

            sendJson(
                ex, 400,
                "{\"error\":\"Enter valid product details.\"}"
            );

            return;
        }

        product.name = name;
        product.price = price;
        product.quantity = quantity;

        sendJson(
            ex, 200,
            "{\"message\":\"Product updated successfully.\"}"
        );
    }

    // DELETION: SHIFT ARRAY ELEMENTS TO LEFT

    private static void deleteProduct(HttpExchange ex)
            throws IOException {

        var p = readForm(ex);

        int id = Integer.parseInt(required(p, "id"));

        int index = indexOf(id);

        if (index == -1) {

            sendJson(
                ex, 404,
                "{\"error\":\"Product not found.\"}"
            );

            return;
        }

        for (int i = index; i < productCount - 1; i++) {

            products[i] = products[i + 1];

        }

        products[--productCount] = null;

        sendJson(
            ex, 200,
            "{\"message\":\"Product deleted successfully.\"}"
        );
    }

    // SALES AND STOCK UPDATE

    private static void sellProduct(HttpExchange ex)
            throws IOException {

        var p = readForm(ex);

        int id = Integer.parseInt(required(p, "id"));

        int quantity =
            Integer.parseInt(required(p, "quantity"));

        Product product = findById(id);

        if (product == null || quantity <= 0) {

            sendJson(
                ex, 400,
                "{\"error\":\"Select a valid product and sale quantity.\"}"
            );

            return;
        }

        if (quantity > product.quantity) {

            sendJson(
                ex, 400,
                "{\"error\":\"Not enough stock available.\"}"
            );

            return;
        }

        if (salesCount == sales.length) {

            sendJson(
                ex, 400,
                "{\"error\":\"Sales record array is full.\"}"
            );

            return;
        }

        product.quantity -= quantity;

        double total = product.price * quantity;

        String time = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
        );

        sales[salesCount++] = new Sale(
            nextInvoiceNo++,
            product.name,
            quantity,
            product.price,
            total,
            time
        );

        sendJson(
            ex,
            200,
            "{\"message\":\"Sale recorded. Invoice total: Rs. "
            + String.format("%.2f", total)
            + "\"}"
        );
    }

    // LINEAR SEARCH

    private static void searchProducts(HttpExchange ex)
            throws IOException {

        String q = queryParams(ex)
            .getOrDefault("q", "")
            .toLowerCase();

        StringBuilder out = new StringBuilder("[");

        boolean first = true;

        for (int i = 0; i < productCount; i++) {

            Product p = products[i];

            if (
                p.name.toLowerCase().contains(q)
                || String.valueOf(p.id).equals(q)
            ) {

                if (!first) {
                    out.append(",");
                }

                out.append(productJson(p));

                first = false;
            }
        }

        out.append("]");

        sendJson(ex, 200, out.toString());
    }

    // BUBBLE SORT AND SELECTION SORT

    private static void sortProducts(HttpExchange ex)
            throws IOException {

        var params = queryParams(ex);

        String by = params.getOrDefault("by", "name");

        boolean descending =
            "desc".equalsIgnoreCase(
                params.getOrDefault("order", "asc")
            );

        // BUBBLE SORT: PRODUCT NAME

        if (by.equals("name")) {

            for (int i = 0; i < productCount - 1; i++) {

                for (int j = 0; j < productCount - i - 1; j++) {

                    int cmp = products[j].name.compareToIgnoreCase(
                        products[j + 1].name
                    );

                    if (
                        descending ? cmp < 0 : cmp > 0
                    ) {

                        swap(j, j + 1);

                    }
                }
            }
        }

        // SELECTION SORT: PRICE OR QUANTITY

        else if (
            by.equals("price") || by.equals("quantity")
        ) {

            for (int i = 0; i < productCount - 1; i++) {

                int selected = i;

                for (int j = i + 1; j < productCount; j++) {

                    double a = by.equals("price")
                        ? products[j].price
                        : products[j].quantity;

                    double b = by.equals("price")
                        ? products[selected].price
                        : products[selected].quantity;

                    if (
                        descending ? a > b : a < b
                    ) {

                        selected = j;

                    }
                }

                swap(i, selected);
            }
        }

        else {

            sendJson(
                ex, 400,
                "{\"error\":\"Sort field must be name, price or quantity.\"}"
            );

            return;
        }

        sendJson(ex, 200, productsJson());
    }

    // SWAP TWO ARRAY ELEMENTS

    private static void swap(int a, int b) {

        Product temp = products[a];

        products[a] = products[b];

        products[b] = temp;
    }

    // FIND PRODUCT BY ID

    private static Product findById(int id) {

        int index = indexOf(id);

        return index == -1 ? null : products[index];
    }

    private static int indexOf(int id) {

        for (int i = 0; i < productCount; i++) {

            if (products[i].id == id) {

                return i;

            }
        }

        return -1;
    }

    // CONVERT PRODUCTS ARRAY INTO JSON

    private static String productsJson() {

        StringBuilder out = new StringBuilder("[");

        for (int i = 0; i < productCount; i++) {

            if (i > 0) {
                out.append(",");
            }

            out.append(productJson(products[i]));
        }

        return out.append("]").toString();
    }

    private static String productJson(Product p) {

        return "{\"id\":" + p.id
            + ",\"name\":\"" + escape(p.name)
            + "\",\"price\":" + p.price
            + ",\"quantity\":" + p.quantity
            + "}";
    }

    // CONVERT SALES ARRAY INTO JSON

    private static String salesJson() {

        StringBuilder out = new StringBuilder("[");

        for (int i = 0; i < salesCount; i++) {

            Sale s = sales[i];

            if (i > 0) {
                out.append(",");
            }

            out.append("{\"invoiceNo\":")
                .append(s.invoiceNo)

                .append(",\"productName\":\"")
                .append(escape(s.productName))

                .append("\",\"quantity\":")
                .append(s.quantity)

                .append(",\"unitPrice\":")
                .append(s.unitPrice)

                .append(",\"total\":")
                .append(s.total)

                .append(",\"dateTime\":\"")
                .append(escape(s.dateTime))
                .append("\"}");
        }

        return out.append("]").toString();
    }

    // READ FORM DATA

    private static java.util.Map<String, String> readForm(
        HttpExchange ex
    ) throws IOException {

        String body = new String(
            ex.getRequestBody().readAllBytes(),
            StandardCharsets.UTF_8
        );

        return parseParams(body);
    }

    // READ URL PARAMETERS

    private static java.util.Map<String, String> queryParams(
        HttpExchange ex
    ) {

        String query = ex.getRequestURI().getRawQuery();

        return parseParams(
            query == null ? "" : query
        );
    }

    private static java.util.Map<String, String> parseParams(
        String input
    ) {

        java.util.Map<String, String> map =
            new java.util.HashMap<>();

        for (String pair : input.split("&")) {

            if (pair.isEmpty()) {
                continue;
            }

            String[] parts = pair.split("=", 2);

            map.put(
                decode(parts[0]),
                parts.length > 1
                    ? decode(parts[1])
                    : ""
            );
        }

        return map;
    }

    private static String decode(String value) {

        try {

            return URLDecoder.decode(
                value,
                StandardCharsets.UTF_8
            );

        }

        catch (Exception e) {

            return value;

        }
    }

    private static String required(
        java.util.Map<String, String> map,
        String key
    ) {

        String value = map.get(key);

        if (value == null) {

            throw new IllegalArgumentException(
                "Missing field: " + key
            );

        }

        return value;
    }

    // ESCAPE SPECIAL JSON CHARACTERS

    private static String escape(String s) {

        return s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r");
    }

    // SEND JSON RESPONSE

    private static void sendJson(
        HttpExchange ex,
        int status,
        String json
    ) throws IOException {

        byte[] bytes = json.getBytes(
            StandardCharsets.UTF_8
        );

        ex.getResponseHeaders().set(
            "Content-Type",
            "application/json; charset=UTF-8"
        );

        ex.sendResponseHeaders(status, bytes.length);

        try (OutputStream os = ex.getResponseBody()) {

            os.write(bytes);

        }
    }

    // SERVE HTML, CSS AND JAVASCRIPT

    private static void serveFrontend(
        HttpExchange ex,
        String requestPath
    ) throws IOException {

        String relative = requestPath.equals("/")
            ? "index.html"
            : requestPath.substring(1);

        Path file = FRONTEND.resolve(relative).normalize();

        if (
            !file.startsWith(FRONTEND)
            || !Files.exists(file)
            || Files.isDirectory(file)
        ) {

            byte[] notFound =
                "Not found".getBytes(StandardCharsets.UTF_8);

            ex.sendResponseHeaders(404, notFound.length);

            try (OutputStream os = ex.getResponseBody()) {

                os.write(notFound);

            }

            return;
        }

        String type = relative.endsWith(".css")
            ? "text/css; charset=UTF-8"
            : relative.endsWith(".js")
                ? "application/javascript; charset=UTF-8"
                : "text/html; charset=UTF-8";

        byte[] bytes = Files.readAllBytes(file);

        ex.getResponseHeaders().set(
            "Content-Type", type
        );

        ex.sendResponseHeaders(200, bytes.length);

        try (OutputStream os = ex.getResponseBody()) {

            os.write(bytes);

        }
    }
}