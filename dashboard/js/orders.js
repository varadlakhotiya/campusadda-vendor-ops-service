requireAuth();

let ordersRefreshTimer = null;
let selectedOrderId = null;
let portalContext = null;

document.addEventListener("DOMContentLoaded", async () => {
  try {
    portalContext = await Portal.getContext();
    await renderSidebar("orders");
    bindOrderEvents();

    await VendorScope.initVendorSelect("vendorSelect", async () => {
      selectedOrderId = null;
      clearOrderDetail();
      clearStockPreview();
      await loadOrderBoard();
      startAutoRefresh();
    });
  } catch (err) {
    Utils.showMessage(err.message || "Failed to load orders page", "error");
  }
});

function bindOrderEvents() {
  const refreshBtn = document.getElementById("refreshOrdersBtn");
  if (refreshBtn) {
    refreshBtn.addEventListener("click", async () => {
      await loadOrderBoard(false);
      if (selectedOrderId) {
        await viewOrderDetails(selectedOrderId, false);
      }
    });
  }
}

function canManageOrders() {
  return Boolean(portalContext?.vendorUser) && !Boolean(portalContext?.adminUser);
}

function getActionsForColumn(columnKey) {
  if (!canManageOrders()) {
    return [];
  }

  switch (columnKey) {
    case "created":
      return ["ACCEPTED", "CANCELLED"];
    case "accepted":
      return ["PREPARING", "CANCELLED"];
    case "preparing":
      return ["READY", "CANCELLED"];
    case "ready":
      return ["COMPLETED"];
    default:
      return [];
  }
}

async function getSelectedVendorId() {
  let vendorId = await VendorScope.getVendorId("vendorSelect");

  if (!vendorId) {
    const ctx = portalContext || await Portal.getContext();
    vendorId = ctx?.primaryVendorId ? Number(ctx.primaryVendorId) : null;
  }

  return vendorId;
}

function startAutoRefresh() {
  if (ordersRefreshTimer) {
    clearInterval(ordersRefreshTimer);
  }

  ordersRefreshTimer = setInterval(async () => {
    try {
      await loadOrderBoard(false);
      if (selectedOrderId) {
        await viewOrderDetails(selectedOrderId, false);
      }
    } catch (_) {
      // ignore background refresh errors
    }
  }, 20000);
}

async function loadOrderBoard(showToastOnError = true) {
  const vendorId = await getSelectedVendorId();

  if (!vendorId) {
    renderColumn("createdOrders", [], []);
    renderColumn("acceptedOrders", [], []);
    renderColumn("preparingOrders", [], []);
    renderColumn("readyOrders", [], []);
    renderColumn("completedOrders", [], []);
    clearOrderDetail();
    clearStockPreview();
    return;
  }

  try {
    const response = await Api.get(`/vendors/${vendorId}/orders/board`);
    const board = response.data || {};

    renderColumn("createdOrders", board.created || [], getActionsForColumn("created"));
    renderColumn("acceptedOrders", board.accepted || [], getActionsForColumn("accepted"));
    renderColumn("preparingOrders", board.preparing || [], getActionsForColumn("preparing"));
    renderColumn("readyOrders", board.ready || [], getActionsForColumn("ready"));
    renderColumn("completedOrders", board.completed || [], []);

    const allOrders = [
      ...(board.created || []),
      ...(board.accepted || []),
      ...(board.preparing || []),
      ...(board.ready || []),
      ...(board.completed || [])
    ];

    if (selectedOrderId && allOrders.some((order) => Number(order.id) === Number(selectedOrderId))) {
      return;
    }

    if (!selectedOrderId && allOrders.length > 0) {
      await viewOrderDetails(allOrders[0].id, false);
    } else if (allOrders.length === 0) {
      clearOrderDetail();
      clearStockPreview();
    }
  } catch (err) {
    if (showToastOnError) {
      Utils.showMessage(err.message || "Failed to load order board", "error");
    }
  }
}

function renderColumn(containerId, orders, actions) {
  const container = document.getElementById(containerId);
  if (!container) return;

  container.innerHTML = "";

  if (!orders || orders.length === 0) {
    const empty = document.createElement("div");
    empty.className = "order-card";
    empty.textContent = "No orders";
    container.appendChild(empty);
    return;
  }

  orders.forEach((order) => {
    const card = document.createElement("div");
    card.className = `order-card ${Number(selectedOrderId) === Number(order.id) ? "is-selected" : ""}`;
    card.dataset.orderId = String(order.id);

    let buttons = `
      <button onclick="viewOrderDetails(${order.id})" class="btn-secondary">View Details</button>
      <button onclick="previewStock(${order.id})" class="btn-secondary">Preview Stock</button>
    `;

    if (actions.length > 0) {
      actions.forEach((action) => {
        if (action === "CANCELLED") {
          buttons += ` <button onclick="cancelOrder(${order.id})" class="btn-danger">${action}</button>`;
        } else {
          buttons += ` <button onclick="updateOrderStatus(${order.id}, '${action}')" class="btn-primary">${action}</button>`;
        }
      });
    } else if (portalContext?.adminUser) {
      buttons += ` <span class="muted">Read-only admin view</span>`;
    }

    card.innerHTML = `
      <div class="order-card-head">
        <strong>${escapeHtml(order.orderNumber ?? "-")}</strong>
        <span class="status-chip status-${String(order.status || "").toLowerCase()}">
          ${escapeHtml(order.status ?? "-")}
        </span>
      </div>

      <div class="order-card-grid">
        <div class="order-kv">
          <span>Customer</span>
          <strong>${escapeHtml(order.customerName ?? "-")}</strong>
        </div>
        <div class="order-kv">
          <span>Phone</span>
          <strong>${escapeHtml(order.customerPhone ?? "-")}</strong>
        </div>
        <div class="order-kv">
          <span>Total</span>
          <strong>${Utils.formatCurrency(order.totalAmount ?? 0)}</strong>
        </div>
        <div class="order-kv">
          <span>Source</span>
          <strong>${escapeHtml(order.orderSource ?? "-")}</strong>
        </div>
      </div>

      <div class="order-kv">
        <span>Placed at</span>
        <strong>${order.placedAt ? Utils.formatDateTime(order.placedAt) : "-"}</strong>
      </div>

      ${
        order.notes
          ? `<div class="order-notes"><span>Notes</span><p>${escapeHtml(order.notes)}</p></div>`
          : ""
      }

      <div class="actions order-actions">${buttons}</div>
    `;

    container.appendChild(card);
  });
}

async function viewOrderDetails(orderId, scrollIntoView = true) {
  try {
    const response = await Api.get(`/orders/${orderId}`);
    const detail = response.data || {};
    selectedOrderId = orderId;
    renderOrderDetail(detail);
    highlightSelectedOrder();

    if (scrollIntoView) {
      document.getElementById("selectedOrderTitle")?.scrollIntoView({
        behavior: "smooth",
        block: "start"
      });
    }
  } catch (err) {
    Utils.showMessage(err.message || "Failed to load order details", "error");
  }
}

function renderOrderDetail(detail) {
  const order = detail?.order || {};
  const items = Array.isArray(detail?.items) ? detail.items : [];
  const history = Array.isArray(detail?.statusHistory) ? detail.statusHistory : [];

  const title = document.getElementById("selectedOrderTitle");
  const status = document.getElementById("selectedOrderStatus");
  const summary = document.getElementById("selectedOrderSummary");
  const itemsBox = document.getElementById("selectedOrderItems");
  const historyBox = document.getElementById("selectedOrderHistory");

  if (title) {
    title.textContent = order.orderNumber || "Order Details";
  }

  if (status) {
    status.textContent = order.status || "Details";
    status.className = `badge status-chip status-${String(order.status || "").toLowerCase()}`;
  }

  if (summary) {
    summary.className = "detail-summary";
    summary.innerHTML = `
      <div class="detail-summary-grid">
        <div class="order-kv">
          <span>Customer Name</span>
          <strong>${escapeHtml(order.customerName ?? "-")}</strong>
        </div>
        <div class="order-kv">
          <span>Phone Number</span>
          <strong>${escapeHtml(order.customerPhone ?? "-")}</strong>
        </div>
        <div class="order-kv">
          <span>Total Amount</span>
          <strong>${Utils.formatCurrency(order.totalAmount ?? 0)}</strong>
        </div>
        <div class="order-kv">
          <span>Order Source</span>
          <strong>${escapeHtml(order.orderSource ?? "-")}</strong>
        </div>
        <div class="order-kv">
          <span>Placed At</span>
          <strong>${order.placedAt ? Utils.formatDateTime(order.placedAt) : "-"}</strong>
        </div>
        <div class="order-kv">
          <span>Payment Status</span>
          <strong>${escapeHtml(order.paymentStatus ?? "-")}</strong>
        </div>
      </div>
      <div class="order-notes detail-notes">
        <span>Customer Notes</span>
        <p>${escapeHtml(order.notes ?? "No notes added by customer.")}</p>
      </div>
    `;
  }

  if (itemsBox) {
    if (!items.length) {
      itemsBox.className = "detail-list empty-detail";
      itemsBox.textContent = "No items to show.";
    } else {
      itemsBox.className = "detail-list";
      itemsBox.innerHTML = items
        .map((item) => `
          <div class="detail-row">
            <div>
              <strong>${escapeHtml(item.itemNameSnapshot ?? item.itemName ?? "-")}</strong>
              <div class="muted">
                Qty ${escapeHtml(item.quantity ?? "-")} • ${Utils.formatCurrency(item.unitPrice ?? 0)} each
              </div>
              ${
                item.specialInstructions
                  ? `<div class="detail-note">Instructions: ${escapeHtml(item.specialInstructions)}</div>`
                  : ""
              }
            </div>
            <strong>${Utils.formatCurrency(item.lineTotal ?? 0)}</strong>
          </div>
        `)
        .join("");
    }
  }

  if (historyBox) {
    if (!history.length) {
      historyBox.className = "history-list empty-detail";
      historyBox.textContent = "No status history to show.";
    } else {
      historyBox.className = "history-list";
      historyBox.innerHTML = history
        .map((entry) => `
          <div class="history-row">
            <div class="history-dot"></div>
            <div class="history-content">
              <strong>${escapeHtml(entry.toStatus ?? "-")}</strong>
              <div class="muted">${escapeHtml(entry.remarks ?? "Status updated")}</div>
              <div class="muted">
                ${entry.changedAt ? Utils.formatDateTime(entry.changedAt) : "-"}
              </div>
            </div>
          </div>
        `)
        .join("");
    }
  }
}

function highlightSelectedOrder() {
  document.querySelectorAll(".order-card[data-order-id]").forEach((card) => {
    card.classList.toggle(
      "is-selected",
      Number(card.dataset.orderId) === Number(selectedOrderId)
    );
  });
}

function clearOrderDetail() {
  const title = document.getElementById("selectedOrderTitle");
  const status = document.getElementById("selectedOrderStatus");
  const summary = document.getElementById("selectedOrderSummary");
  const itemsBox = document.getElementById("selectedOrderItems");
  const historyBox = document.getElementById("selectedOrderHistory");

  if (title) {
    title.textContent = "No order selected";
  }

  if (status) {
    status.textContent = "Details";
    status.className = "badge";
  }

  if (summary) {
    summary.className = "detail-summary empty-detail";
    summary.textContent = "Select any order card to view customer details, notes, items, and status history.";
  }

  if (itemsBox) {
    itemsBox.className = "detail-list empty-detail";
    itemsBox.textContent = "No items to show.";
  }

  if (historyBox) {
    historyBox.className = "history-list empty-detail";
    historyBox.textContent = "No status history to show.";
  }
}

function clearStockPreview() {
  const box = document.getElementById("orderInsightBox");
  if (box) {
    box.textContent = 'Click "Preview Stock" on an order card to inspect stock impact.';
  }
}

async function updateOrderStatus(orderId, status) {
  if (!canManageOrders()) {
    Utils.showMessage("Admin has read-only access for vendor orders.", "error");
    return;
  }

  try {
    await Api.patch(`/orders/${orderId}/status`, {
      status,
      remarks: `Updated to ${status} from dashboard`
    });

    Utils.showMessage("Order status updated");
    await loadOrderBoard(false);
    await viewOrderDetails(orderId, false);
  } catch (err) {
    Utils.showMessage(err.message || "Failed to update order", "error");
  }
}

async function cancelOrder(orderId) {
  if (!canManageOrders()) {
    Utils.showMessage("Admin has read-only access for vendor orders.", "error");
    return;
  }

  const confirmed = window.confirm("Cancel this order?");
  if (!confirmed) return;

  try {
    await Api.post(`/orders/${orderId}/cancel`, {
      reason: "Cancelled from dashboard"
    });

    Utils.showMessage("Order cancelled");
    await loadOrderBoard(false);

    if (Number(selectedOrderId) === Number(orderId)) {
      clearOrderDetail();
      clearStockPreview();
      selectedOrderId = null;
    }
  } catch (err) {
    Utils.showMessage(err.message || "Failed to cancel order", "error");
  }
}

async function previewStock(orderId) {
  const box = document.getElementById("orderInsightBox");
  if (box) {
    box.textContent = "Loading stock preview.";
  }

  try {
    if (Number(selectedOrderId) !== Number(orderId)) {
      await viewOrderDetails(orderId, false);
    }

    const response = await Api.get(`/orders/${orderId}/stock-consumption-preview`);
    if (box) {
      box.textContent = JSON.stringify(response.data || {}, null, 2);
    }
  } catch (err) {
    if (box) {
      box.textContent = "Failed to load stock preview";
    }
    Utils.showMessage(err.message || "Failed to load stock preview", "error");
  }
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}