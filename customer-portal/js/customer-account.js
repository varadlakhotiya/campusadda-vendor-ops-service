const CustomerAccountState = {
  me: null,
  orders: []
};

const accountEls = {};

document.addEventListener("DOMContentLoaded", () => {
  cacheCustomerAccountElements();
  bindCustomerAccountEvents();
  bootstrapCustomerAccount();
});

function cacheCustomerAccountElements() {
  [
    "customerAccountCard",
    "customerAccountContent",
    "customerLoginBtn",
    "customerSignupBtn",
    "customerLogoutBtn",
    "myOrdersCard",
    "myOrdersList",
    "myOrdersEmpty"
  ].forEach((id) => {
    accountEls[id] = document.getElementById(id);
  });
}

function bindCustomerAccountEvents() {
  accountEls.customerLogoutBtn?.addEventListener("click", async () => {
    await CustomerAuth.logout();
    window.location.reload();
  });
}

async function bootstrapCustomerAccount() {
  if (!CustomerAuth.hasSession()) {
    renderGuestCustomerState();
    return;
  }

  try {
    CustomerAccountState.me = await CustomerAuth.getCurrentUser();
    renderLoggedInCustomerState(CustomerAccountState.me, 0, 0);
    prefillCheckoutFields(CustomerAccountState.me);
    await refreshCustomerAccountData();
  } catch (_) {
    CustomerAuth.clearSession();
    CustomerAccountState.me = null;
    renderGuestCustomerState();
  }
}

async function refreshCustomerAccountData() {
  if (!CustomerAuth.hasSession()) {
    renderGuestCustomerState();
    return;
  }

  try {
    const [ordersResponse, activeResponse] = await Promise.all([
      CustomerApi.get("/orders", { baseUrl: window.CUSTOMER_APP_CONFIG.CUSTOMER_BASE_URL }),
      CustomerApi.get("/orders/active", { baseUrl: window.CUSTOMER_APP_CONFIG.CUSTOMER_BASE_URL })
    ]);

    CustomerAccountState.orders = Array.isArray(ordersResponse.data) ? ordersResponse.data : [];
    const activeOrders = Array.isArray(activeResponse.data) ? activeResponse.data : [];

    renderLoggedInCustomerState(CustomerAccountState.me, CustomerAccountState.orders.length, activeOrders.length);
    renderCustomerOrders(CustomerAccountState.orders);

    if (activeOrders.length > 0) {
      await openCustomerOrder(activeOrders[0].orderId, true);
    }
  } catch (error) {
    console.error("Failed to load customer account data", error);
  }
}

window.refreshCustomerAccountData = refreshCustomerAccountData;

function renderGuestCustomerState() {
  accountEls.customerAccountContent.innerHTML = `
    <div class="account-summary-shell">
      <span class="account-mode-pill">Guest mode</span>
      <div>
        <strong>Order faster after login.</strong>
        <p class="muted">Save older orders, fill checkout details automatically, and reopen tracking in one tap.</p>
      </div>
      <div class="account-benefits">
        <span class="account-benefit">Saved orders</span>
        <span class="account-benefit">Quick tracking</span>
        <span class="account-benefit">Faster checkout</span>
      </div>
    </div>
    <div class="account-actions">
      <a class="btn-secondary btn-full" href="login.html">Login</a>
      <a class="btn-primary btn-full" href="signup.html">Create Account</a>
    </div>
  `;

  if (accountEls.myOrdersCard) {
    accountEls.myOrdersCard.classList.add("hidden");
  }
}

function renderLoggedInCustomerState(me, orderCount = 0, activeOrderCount = 0) {
  const initials = getInitialsSafe(me?.fullName || me?.email || "Customer");

  accountEls.customerAccountContent.innerHTML = `
    <div class="account-summary-shell">
      <div class="account-profile">
        <div class="account-user-row">
          <div class="account-avatar">${escapeHtmlSafe(initials)}</div>
          <div class="account-profile-meta">
            <strong>${escapeHtmlSafe(me?.fullName || "Customer")}</strong>
            <span class="muted">${escapeHtmlSafe(me?.email || "")}</span>
            <span class="muted">${escapeHtmlSafe(me?.phone || "")}</span>
          </div>
        </div>
        <button id="customerLogoutBtn" type="button" class="ghost-chip">Logout</button>
      </div>

      <div class="account-mini-stats">
        <div class="account-mini-stat">
          <span>Saved orders</span>
          <strong>${escapeHtmlSafe(orderCount)}</strong>
        </div>
        <div class="account-mini-stat">
          <span>Active now</span>
          <strong>${escapeHtmlSafe(activeOrderCount)}</strong>
        </div>
      </div>
    </div>
  `;

  document.getElementById("customerLogoutBtn")?.addEventListener("click", async () => {
    await CustomerAuth.logout();
    window.location.reload();
  });

  if (accountEls.myOrdersCard) {
    accountEls.myOrdersCard.classList.remove("hidden");
  }
}

function prefillCheckoutFields(me) {
  const nameInput = document.getElementById("customerName");
  const phoneInput = document.getElementById("customerPhone");

  if (nameInput && me?.fullName) {
    nameInput.value = me.fullName;
  }

  if (phoneInput && me?.phone) {
    phoneInput.value = me.phone;
  }
}

function renderCustomerOrders(orders) {
  if (!accountEls.myOrdersList) return;

  if (!orders.length) {
    accountEls.myOrdersList.innerHTML = `<div class="tracking-empty-row">No previous orders found.</div>`;
    return;
  }

  accountEls.myOrdersList.innerHTML = orders
    .map((order) => `
      <button type="button" class="my-order-card" data-order-id="${order.orderId}">
        <div class="my-order-card-top">
          <strong>${escapeHtmlSafe(order.vendorName || "Vendor")}</strong>
          <span class="tracking-status-badge status-${String(order.status || "").toLowerCase()}">
            ${escapeHtmlSafe(order.status || "-")}
          </span>
        </div>
        <div class="my-order-meta-row">
          <span>${escapeHtmlSafe(formatDateSafe(order.placedAt))}</span>
          <span>${formatCurrencySafe(order.totalAmount || 0)}</span>
        </div>
        <div class="my-order-items">
          ${(order.items || []).map((item) => `${escapeHtmlSafe(item.itemName || "-")} × ${escapeHtmlSafe(item.quantity ?? "-")}`).join(", ")}
        </div>
      </button>
    `)
    .join("");

  accountEls.myOrdersList.querySelectorAll("[data-order-id]").forEach((button) => {
    button.addEventListener("click", async () => {
      const orderId = Number(button.getAttribute("data-order-id"));
      if (!orderId) return;
      await openCustomerOrder(orderId, false);
    });
  });
}

async function openCustomerOrder(orderId, silent) {
  try {
    const response = await CustomerApi.get(
      `/orders/${orderId}`,
      { baseUrl: window.CUSTOMER_APP_CONFIG.CUSTOMER_BASE_URL }
    );
    const detail = response.data;

    const trackingOrderNumber = document.getElementById("trackingOrderNumber");
    const trackingPhone = document.getElementById("trackingPhone");

    if (trackingOrderNumber) {
      trackingOrderNumber.value = detail.orderNumber || "";
    }
    if (trackingPhone) {
      trackingPhone.value = detail.customerPhone || CustomerAccountState.me?.phone || "";
    }

    if (typeof renderTrackingPanel === "function") {
      renderTrackingPanel(detail);
    }

    if (typeof renderTrackingState === "function") {
      renderTrackingState("Showing your saved order details.");
    }

    if (!silent) {
      document.getElementById("trackingPanel")?.scrollIntoView({
        behavior: "smooth",
        block: "start"
      });
    }
  } catch (error) {
    console.error("Failed to open customer order", error);
  }
}


function getInitialsSafe(value) {
  return String(value || "CA")
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase() || "")
    .join("") || "CA";
}

function escapeHtmlSafe(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

function formatDateSafe(value) {
  if (!value) return "-";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return String(value);
  return date.toLocaleString("en-IN", {
    day: "numeric",
    month: "short",
    year: "numeric",
    hour: "numeric",
    minute: "2-digit"
  });
}

function formatCurrencySafe(value) {
  const amount = Number(value || 0);
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 2
  }).format(amount);
}