const CustomerAccountState = {
  me: null,
  orders: [],
  activeOrders: []
};

const accountEls = {};

document.addEventListener("DOMContentLoaded", () => {
  cacheCustomerAccountElements();
  bindCustomerAccountEvents();
  bootstrapCustomerAccount();
});

function cacheCustomerAccountElements() {
  [
    "customerAccountContent",
    "myOrdersCard",
    "myOrdersList"
  ].forEach((id) => {
    accountEls[id] = document.getElementById(id);
  });
}

function bindCustomerAccountEvents() {
  accountEls.customerAccountContent?.addEventListener("click", async (event) => {
    const actionEl = event.target.closest("[data-account-action]");
    if (!actionEl) return;

    const action = actionEl.getAttribute("data-account-action");

    if (action === "switch-tab") {
      renderGuestCustomerState(actionEl.getAttribute("data-tab") || "login");
      return;
    }

    if (action === "open-tracking") {
      if (typeof window.openTrackingDrawer === "function") {
        window.openTrackingDrawer();
      }
      return;
    }

    if (action === "logout") {
      await CustomerAuth.logout();
      CustomerAccountState.me = null;
      CustomerAccountState.orders = [];
      CustomerAccountState.activeOrders = [];
      renderGuestCustomerState("login");
      if (accountEls.myOrdersCard) {
        accountEls.myOrdersCard.classList.add("hidden");
      }
      if (typeof window.showToastMessage === "function") {
        window.showToastMessage("Logged out successfully", "success");
      }
      return;
    }
  });

  accountEls.customerAccountContent?.addEventListener("submit", async (event) => {
    const form = event.target.closest("form[data-account-form]");
    if (!form) return;

    event.preventDefault();
    const formType = form.getAttribute("data-account-form");

    if (formType === "login") {
      await submitLoginForm(form);
      return;
    }

    if (formType === "signup") {
      await submitSignupForm(form);
    }
  });

  accountEls.myOrdersList?.addEventListener("click", async (event) => {
    const orderCard = event.target.closest("[data-order-id]");
    if (!orderCard) return;

    const orderId = Number(orderCard.getAttribute("data-order-id"));
    if (!orderId) return;

    await openCustomerOrder(orderId, false);
  });
}

async function bootstrapCustomerAccount() {
  if (!CustomerAuth.hasSession()) {
    renderGuestCustomerState("login");
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
    renderGuestCustomerState("login");
  }
}

async function refreshCustomerAccountData() {
  if (!CustomerAuth.hasSession()) {
    renderGuestCustomerState("login");
    return;
  }

  try {
    const [ordersResponse, activeResponse] = await Promise.all([
      CustomerApi.get("/orders", { baseUrl: window.CUSTOMER_APP_CONFIG.CUSTOMER_BASE_URL }),
      CustomerApi.get("/orders/active", { baseUrl: window.CUSTOMER_APP_CONFIG.CUSTOMER_BASE_URL })
    ]);

    CustomerAccountState.orders = Array.isArray(ordersResponse.data) ? ordersResponse.data : [];
    CustomerAccountState.activeOrders = Array.isArray(activeResponse.data) ? activeResponse.data : [];

    renderLoggedInCustomerState(
      CustomerAccountState.me,
      CustomerAccountState.orders.length,
      CustomerAccountState.activeOrders.length
    );
    renderCustomerOrders(CustomerAccountState.orders);

    if (CustomerAccountState.activeOrders.length > 0) {
      await openCustomerOrder(CustomerAccountState.activeOrders[0].orderId, true);
    }
  } catch (error) {
    console.error("Failed to load customer account data", error);
  }
}

window.refreshCustomerAccountData = refreshCustomerAccountData;

function renderGuestCustomerState(activeTab = "login") {
  if (!accountEls.customerAccountContent) return;

  const isSignup = activeTab === "signup";

  accountEls.customerAccountContent.innerHTML = `
    <div class="account-shell">
      <div class="account-summary-header">
        <span class="account-mode-pill">Guest mode</span>
        <h3>Login or create account only when needed</h3>
        <p class="muted">Keep the main screen clean. Open this panel to login, sign up, see saved orders later, and reopen tracking faster.</p>
      </div>

      <div class="auth-benefit-row">
        <span class="account-benefit">Saved orders</span>
        <span class="account-benefit">Quick tracking</span>
        <span class="account-benefit">Faster checkout</span>
      </div>

      <div class="account-auth-tabs">
        <button type="button" class="form-tab-btn ${!isSignup ? "active" : ""}" data-account-action="switch-tab" data-tab="login">Login</button>
        <button type="button" class="form-tab-btn ${isSignup ? "active" : ""}" data-account-action="switch-tab" data-tab="signup">Create Account</button>
      </div>

      ${isSignup ? renderSignupShell() : renderLoginShell()}
    </div>
  `;

  if (accountEls.myOrdersCard) {
    accountEls.myOrdersCard.classList.add("hidden");
  }
}

function renderLoginShell() {
  return `
    <div class="auth-form-shell">
      <p class="auth-form-note">Login to fill checkout details automatically and reopen active orders in one tap.</p>
      <form class="auth-form" data-account-form="login">
        <div class="field-group">
          <label for="drawerLoginEmail">Email</label>
          <input id="drawerLoginEmail" name="email" type="email" required />
        </div>
        <div class="field-group">
          <label for="drawerLoginPassword">Password</label>
          <input id="drawerLoginPassword" name="password" type="password" required />
        </div>
        <button type="submit" class="primary-btn btn-full">Login</button>
      </form>
      <div id="accountInlineMessage" class="tracking-state"></div>
    </div>
  `;
}

function renderSignupShell() {
  return `
    <div class="auth-form-shell">
      <p class="auth-form-note">Use the same phone number as older guest orders so those orders can appear inside your account later.</p>
      <form class="auth-form" data-account-form="signup">
        <div class="field-group">
          <label for="drawerSignupName">Full name</label>
          <input id="drawerSignupName" name="fullName" type="text" required />
        </div>
        <div class="field-group">
          <label for="drawerSignupEmail">Email</label>
          <input id="drawerSignupEmail" name="email" type="email" required />
        </div>
        <div class="field-group">
          <label for="drawerSignupPhone">Phone number</label>
          <input id="drawerSignupPhone" name="phone" type="text" required />
        </div>
        <div class="field-group">
          <label for="drawerSignupPassword">Password</label>
          <input id="drawerSignupPassword" name="password" type="password" required />
        </div>
        <div class="field-group">
          <label for="drawerSignupConfirmPassword">Confirm password</label>
          <input id="drawerSignupConfirmPassword" name="confirmPassword" type="password" required />
        </div>
        <button type="submit" class="primary-btn btn-full">Create Account</button>
      </form>
      <div id="accountInlineMessage" class="tracking-state"></div>
    </div>
  `;
}

function renderLoggedInCustomerState(me, orderCount = 0, activeOrderCount = 0) {
  if (!accountEls.customerAccountContent) return;

  const initials = getInitialsSafe(me?.fullName || me?.email || "Customer");

  accountEls.customerAccountContent.innerHTML = `
    <div class="account-shell">
      <div class="account-split-row">
        <div class="account-user-stack">
          <div class="account-avatar">${escapeHtmlSafe(initials)}</div>
          <div class="account-user-meta">
            <strong>${escapeHtmlSafe(me?.fullName || "Customer")}</strong>
            <span class="muted">${escapeHtmlSafe(me?.email || "")}</span>
            <span class="muted">${escapeHtmlSafe(me?.phone || "")}</span>
          </div>
        </div>
        <button type="button" class="ghost-btn compact-btn" data-account-action="logout">Logout</button>
      </div>

      <div class="account-stats-grid">
        <div class="account-stat-card">
          <span>Saved orders</span>
          <strong>${escapeHtmlSafe(orderCount)}</strong>
        </div>
        <div class="account-stat-card">
          <span>Active now</span>
          <strong>${escapeHtmlSafe(activeOrderCount)}</strong>
        </div>
      </div>

      <div class="account-quick-actions">
        <button type="button" data-account-action="open-tracking">Open tracking</button>
      </div>
    </div>
  `;

  if (accountEls.myOrdersCard) {
    accountEls.myOrdersCard.classList.remove("hidden");
  }
}

async function submitLoginForm(form) {
  const messageEl = document.getElementById("accountInlineMessage");
  if (messageEl) {
    messageEl.textContent = "";
  }

  const submitButton = form.querySelector("button[type='submit']");
  const originalLabel = submitButton?.textContent || "Login";

  try {
    if (submitButton) {
      submitButton.disabled = true;
      submitButton.textContent = "Logging in...";
    }

    const email = String(new FormData(form).get("email") || "").trim();
    const password = String(new FormData(form).get("password") || "");

    await CustomerAuth.login(email, password);
    CustomerAccountState.me = await CustomerAuth.getCurrentUser();
    renderLoggedInCustomerState(CustomerAccountState.me, 0, 0);
    prefillCheckoutFields(CustomerAccountState.me);
    await refreshCustomerAccountData();

    if (typeof window.showToastMessage === "function") {
      window.showToastMessage("Logged in successfully", "success");
    }
  } catch (error) {
    if (messageEl) {
      messageEl.textContent = error.message || "Login failed";
    }
  } finally {
    if (submitButton) {
      submitButton.disabled = false;
      submitButton.textContent = originalLabel;
    }
  }
}

async function submitSignupForm(form) {
  const messageEl = document.getElementById("accountInlineMessage");
  if (messageEl) {
    messageEl.textContent = "";
  }

  const submitButton = form.querySelector("button[type='submit']");
  const originalLabel = submitButton?.textContent || "Create Account";

  try {
    if (submitButton) {
      submitButton.disabled = true;
      submitButton.textContent = "Creating account...";
    }

    const formData = new FormData(form);
    const payload = {
      fullName: String(formData.get("fullName") || "").trim(),
      email: String(formData.get("email") || "").trim(),
      phone: String(formData.get("phone") || "").trim(),
      password: String(formData.get("password") || ""),
      confirmPassword: String(formData.get("confirmPassword") || "")
    };

    await CustomerAuth.signup(payload);
    CustomerAccountState.me = await CustomerAuth.getCurrentUser();
    renderLoggedInCustomerState(CustomerAccountState.me, 0, 0);
    prefillCheckoutFields(CustomerAccountState.me);
    await refreshCustomerAccountData();

    if (typeof window.showToastMessage === "function") {
      window.showToastMessage("Account created successfully", "success");
    }
  } catch (error) {
    if (messageEl) {
      messageEl.textContent = error.message || "Signup failed";
    }
  } finally {
    if (submitButton) {
      submitButton.disabled = false;
      submitButton.textContent = originalLabel;
    }
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
    accountEls.myOrdersList.innerHTML = `<div class="account-empty-row">No previous orders found.</div>`;
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
}

async function openCustomerOrder(orderId, silent) {
  try {
    const response = await CustomerApi.get(`/orders/${orderId}`, {
      baseUrl: window.CUSTOMER_APP_CONFIG.CUSTOMER_BASE_URL
    });
    const detail = response.data;

    const trackingOrderNumber = document.getElementById("trackingOrderNumber");
    const trackingPhone = document.getElementById("trackingPhone");

    if (trackingOrderNumber) {
      trackingOrderNumber.value = detail.orderNumber || "";
    }
    if (trackingPhone) {
      trackingPhone.value = detail.customerPhone || CustomerAccountState.me?.phone || "";
    }

    if (typeof window.renderTrackingPanel === "function") {
      window.renderTrackingPanel(detail);
    }
    if (typeof window.renderTrackingState === "function") {
      window.renderTrackingState("Showing your saved order details.");
    }

    if (!silent && typeof window.openTrackingDrawer === "function") {
      window.openTrackingDrawer();
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
