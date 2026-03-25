requireAuth();
renderSidebar("orders");

document.addEventListener("DOMContentLoaded", async () => {
  await loadVendors();
  document.getElementById("vendorSelect").addEventListener("change", loadOrderBoard);
});

async function loadVendors() {
  const response = await Api.get("/vendors");
  Utils.populateSelect("vendorSelect", response.data || [], "id", "name", "Select Vendor");
}

async function loadOrderBoard() {
  const vendorId = document.getElementById("vendorSelect").value;
  if (!vendorId) return;

  const response = await Api.get(`/vendors/${vendorId}/orders/board`);
  const board = response.data;

  renderColumn("createdOrders", board.created, ["ACCEPTED", "CANCELLED"]);
  renderColumn("acceptedOrders", board.accepted, ["PREPARING", "CANCELLED"]);
  renderColumn("preparingOrders", board.preparing, ["READY", "CANCELLED"]);
  renderColumn("readyOrders", board.ready, ["COMPLETED"]);
  renderColumn("completedOrders", board.completed, []);
}

function renderColumn(containerId, orders, actions) {
  const container = document.getElementById(containerId);
  container.innerHTML = "";

  (orders || []).forEach(order => {
    const card = document.createElement("div");
    card.className = "order-card";

    let buttons = "";
    actions.forEach(action => {
      if (action === "CANCELLED") {
        buttons += `<button onclick="cancelOrder(${order.id})" class="btn-danger">${action}</button>`;
      } else {
        buttons += `<button onclick="updateOrderStatus(${order.id}, '${action}')" class="btn-primary">${action}</button>`;
      }
    });

    card.innerHTML = `
      <strong>${order.orderNumber}</strong>
      <div>${order.customerName || "-"}</div>
      <div>${Utils.formatCurrency(order.totalAmount)}</div>
      <div class="actions" style="margin-top:8px;">${buttons}</div>
    `;

    container.appendChild(card);
  });
}

async function updateOrderStatus(orderId, status) {
  try {
    await Api.patch(`/orders/${orderId}/status`, {
      status,
      remarks: `Updated to ${status}`
    });
    Utils.showMessage("Order status updated");
    loadOrderBoard();
  } catch (err) {
    Utils.showMessage(err.message, "error");
  }
}

async function cancelOrder(orderId) {
  try {
    await Api.post(`/orders/${orderId}/cancel`, {
      reason: "Cancelled from dashboard"
    });
    Utils.showMessage("Order cancelled");
    loadOrderBoard();
  } catch (err) {
    Utils.showMessage(err.message, "error");
  }
}