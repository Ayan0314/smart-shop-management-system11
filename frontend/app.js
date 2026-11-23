const $ = (id) => document.getElementById(id);

let allProducts = [];


// API CONNECTION

async function api(url, options = {}) {

    const response = await fetch(url, options);

    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.error || "Something went wrong.");
    }

    return data;
}


function post(url, values) {

    return api(url, {

        method: "POST",

        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },

        body: new URLSearchParams(values)

    });

}


// FORMAT PRICE

function money(value) {

    return "Rs. " + Number(value).toLocaleString(
        "en-PK",
        { maximumFractionDigits: 2 }
    );

}


// NOTIFICATION

function toast(message) {

    $("toast").textContent = message;

    $("toast").classList.add("show");

    setTimeout(() => {

        $("toast").classList.remove("show");

    }, 2600);

}


// PROTECT HTML OUTPUT

function safe(text) {

    return String(text).replace(/[&<>"']/g, c => ({

        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"

    }[c]));

}


// NAVIGATION

function showView(name) {

    document.querySelectorAll(".view").forEach(v => {

        v.classList.toggle("hidden", v.id !== name);

    });


    document.querySelectorAll(".nav").forEach(b => {

        b.classList.toggle(
            "active",
            b.dataset.view === name
        );

    });


    $("pageTitle").textContent =
        name === "sales"
            ? "Sales & Invoices"
            : name[0].toUpperCase() + name.slice(1);


    if (name === "sales") {

        loadSales();

    }

}


document.querySelectorAll(".nav").forEach(b => {

    b.addEventListener("click", () => {

        showView(b.dataset.view);

    });

});


document.querySelectorAll("[data-go]").forEach(b => {

    b.addEventListener("click", () => {

        showView(b.dataset.go);

    });

});


// LOAD PRODUCTS FROM JAVA

async function loadProducts() {

    allProducts = await api("/api/products");

    renderProducts(allProducts);

    renderDashboard();

    renderSaleOptions();

}


// DISPLAY PRODUCTS

function renderProducts(items) {

    $("productRows").innerHTML = items.length

        ? items.map(p => `

            <tr>

                <td class="product-name">
                    ${safe(p.name)}
                </td>

                <td class="id-tag">
                    #${p.id}
                </td>

                <td>
                    ${money(p.price)}
                </td>

                <td>
                    ${p.quantity}
                </td>

                <td>

                    <div class="actions">

                        <button
                            class="action"
                            data-edit="${p.id}"
                        >
                            Edit
                        </button>

                        <button
                            class="action delete"
                            data-delete="${p.id}"
                        >
                            Delete
                        </button>

                    </div>

                </td>

            </tr>

        `).join("")

        : `

            <tr>
                <td colspan="5" class="empty">
                    No products found.
                </td>
            </tr>

        `;


    $("productFoot").textContent =
        `${items.length} product(s) displayed`;


    // EDIT BUTTONS

    document.querySelectorAll("[data-edit]").forEach(b => {

        b.addEventListener("click", () => {

            editProduct(Number(b.dataset.edit));

        });

    });


    // DELETE BUTTONS

    document.querySelectorAll("[data-delete]").forEach(b => {

        b.addEventListener("click", () => {

            deleteProduct(Number(b.dataset.delete));

        });

    });

}


// DASHBOARD

function renderDashboard() {

    $("productCount").textContent = allProducts.length;


    const totalStock = allProducts.reduce(
        (sum, p) => sum + p.quantity,
        0
    );

    $("stockCount").textContent = totalStock;


    $("dashboardProducts").innerHTML =

        allProducts.slice(0, 5).map(p => `

            <tr>

                <td class="product-name">
                    ${safe(p.name)}
                </td>

                <td>
                    #${p.id}
                </td>

                <td>
                    ${money(p.price)}
                </td>

                <td>
                    ${p.quantity}
                </td>

                <td>

                    <span class="stock ${
                        p.quantity <= 5 ? "low" : ""
                    }">

                        ${
                            p.quantity <= 5
                                ? "Low stock"
                                : "In stock"
                        }

                    </span>

                </td>

            </tr>

        `).join("")

        || `

            <tr>
                <td colspan="5" class="empty">
                    No products available.
                </td>
            </tr>

        `;


    loadSalesSummary();

}


// PRODUCT OPTIONS FOR SALES

function renderSaleOptions() {

    $("saleProduct").innerHTML =

        allProducts

            .filter(p => p.quantity > 0)

            .map(p => `

                <option value="${p.id}">

                    ${safe(p.name)}
                    — ${p.quantity} available

                </option>

            `).join("");


    if (!$("saleProduct").options.length) {

        $("saleProduct").innerHTML = `

            <option value="">
                No stock available
            </option>

        `;

    }

}


// SALES SUMMARY

async function loadSalesSummary() {

    const sales = await api("/api/sales");

    $("salesCount").textContent = sales.length;


    const revenue = sales.reduce(
        (sum, s) => sum + s.total,
        0
    );

    $("revenue").textContent = money(revenue);

}


// LOAD SALES

async function loadSales() {

    const sales = await api("/api/sales");


    $("salesRows").innerHTML = sales.length

        ? [...sales].reverse().map(s => `

            <tr>

                <td>
                    #${s.invoiceNo}
                </td>

                <td class="product-name">
                    ${safe(s.productName)}
                </td>

                <td>
                    ${s.quantity}
                </td>

                <td>
                    ${money(s.unitPrice)}
                </td>

                <td>
                    <strong>
                        ${money(s.total)}
                    </strong>
                </td>

                <td>
                    ${safe(s.dateTime)}
                </td>

            </tr>

        `).join("")

        : `

            <tr>
                <td colspan="6" class="empty">
                    No sales recorded yet.
                </td>
            </tr>

        `;


    loadSalesSummary();

}


// SHOW ADD PRODUCT FORM

$("showAdd").addEventListener("click", () => {

    $("addPanel").classList.remove("hidden");

});


$("closeAdd").addEventListener("click", () => {

    $("addPanel").classList.add("hidden");

});


// ADD PRODUCT

$("addForm").addEventListener("submit", async e => {

    e.preventDefault();

    try {

        const result = await post(
            "/api/add",
            Object.fromEntries(new FormData(e.target))
        );

        e.target.reset();

        $("addPanel").classList.add("hidden");

        await loadProducts();

        toast(result.message);

    }

    catch (err) {

        toast(err.message);

    }

});


// UPDATE PRODUCT

async function editProduct(id) {

    const p = allProducts.find(x => x.id === id);

    if (!p) return;


    const name = prompt("Product name:", p.name);

    if (name === null) return;


    const price = prompt("Price (Rs.):", p.price);

    if (price === null) return;


    const quantity = prompt("Quantity:", p.quantity);

    if (quantity === null) return;


    try {

        const result = await post("/api/update", {

            id,
            name,
            price,
            quantity

        });


        await loadProducts();

        toast(result.message);

    }

    catch (err) {

        toast(err.message);

    }

}


// DELETE PRODUCT

async function deleteProduct(id) {

    const p = allProducts.find(x => x.id === id);

    if (!p || !confirm(`Delete ${p.name}?`)) return;


    try {

        const result = await post("/api/delete", { id });

        await loadProducts();

        toast(result.message);

    }

    catch (err) {

        toast(err.message);

    }

}


// SEARCH PRODUCT

$("searchInput").addEventListener("input", async e => {

    try {

        const result = await api(
            "/api/search?q=" + encodeURIComponent(e.target.value)
        );

        renderProducts(result);

    }

    catch (err) {

        toast(err.message);

    }

});


// SORT PRODUCTS

$("sortButton").addEventListener("click", async () => {

    try {

        const by = $("sortBy").value;

        const order = $("sortOrder").value;


        allProducts = await api(
            `/api/sort?by=${by}&order=${order}`
        );


        renderProducts(allProducts);

        renderDashboard();

        renderSaleOptions();


        toast(`Sorted by ${by} (${order}).`);

    }

    catch (err) {

        toast(err.message);

    }

});


// RESET SEARCH

$("resetSearch").addEventListener("click", async () => {

    $("searchInput").value = "";

    await loadProducts();

});


// RECORD SALE

$("saleForm").addEventListener("submit", async e => {

    e.preventDefault();

    try {

        const result = await post(
            "/api/sell",
            Object.fromEntries(new FormData(e.target))
        );


        e.target.reset();

        await loadProducts();

        await loadSales();


        toast(result.message);

    }

    catch (err) {

        toast(err.message);

    }

});


// INITIAL LOAD

loadProducts().catch(err => {

    toast(
        "Could not connect to Java server: " + err.message
    );

});