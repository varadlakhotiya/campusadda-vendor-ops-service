requireAuth();

document.addEventListener("DOMContentLoaded", async () => {
  try {
    await renderSidebar("dashboard");
    await VendorScope.initVendorSelect("vendorSelect", loadDashboardData);
  } catch (err) {
    Utils.showMessage(err.message || "Failed to load dashboard", "error");
  }
});

async function loadDashboardData() {
  const vendorId = document.getElementById("vendorSelect")?.value;
  if (!vendorId) return;

  try {
    const summaryResponse = await Api.get(`/vendors/${vendorId}/summary`);
    const summary = summaryResponse.data || {};

    Utils.setText("assignedUserCount", summary.assignedUserCount ?? 0);
    Utils.setText("activeMenuItemCount", summary.activeMenuItemCount ?? 0);
    Utils.setText("lowStockItemCount", summary.lowStockItemCount ?? 0);
    Utils.setText("todayOrderCount", summary.todayOrderCount ?? 0);

    const alertsResponse = await Api.get(`/vendors/${vendorId}/alerts`);
    renderAlerts(alertsResponse.data || []);
  } catch (err) {
    Utils.showMessage(err.message || "Failed to load dashboard data", "error");
  }
}

function renderAlerts(alerts) {
  const body = document.getElementById("recentAlertsBody");
  if (!body) return;

  body.innerHTML = "";

  alerts.slice(0, 5).forEach((alert) => {
    const row = document.createElement("tr");
    row.innerHTML = `
      <td>${escapeHtml(alert.alertType ?? "-")}</td>
      <td>${escapeHtml(alert.title ?? "-")}</td>
      <td>${escapeHtml(alert.status ?? "-")}</td>
    `;
    body.appendChild(row);
  });

  if (alerts.length === 0) {
    const row = document.createElement("tr");
    row.innerHTML = `<td colspan="3">No recent alerts</td>`;
    body.appendChild(row);
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