requireAuth();

document.addEventListener("DOMContentLoaded", async () => {
  try {
    await renderSidebar("alerts");
    await VendorScope.initVendorSelect("vendorSelect", loadAlerts);
  } catch (err) {
    Utils.showMessage(err.message || "Failed to load alerts page", "error");
  }
});

async function loadAlerts() {
  const vendorId = document.getElementById("vendorSelect")?.value;
  if (!vendorId) return;

  try {
    const response = await Api.get(`/vendors/${vendorId}/alerts`);
    const alerts = response.data || [];
    const body = document.getElementById("alertsBody");
    if (!body) return;

    body.innerHTML = "";

    if (alerts.length === 0) {
      const row = document.createElement("tr");
      row.innerHTML = `<td colspan="6">No alerts found</td>`;
      body.appendChild(row);
      return;
    }

    alerts.forEach((alert) => {
      const row = document.createElement("tr");
      row.innerHTML = `
        <td>${escapeHtml(alert.alertType ?? "-")}</td>
        <td>${escapeHtml(alert.title ?? "-")}</td>
        <td>${escapeHtml(alert.status ?? "-")}</td>
        <td>${escapeHtml(alert.severity ?? "-")}</td>
        <td>${alert.triggeredAt ? Utils.formatDateTime(alert.triggeredAt) : "-"}</td>
        <td class="actions">
          <button class="btn-secondary" onclick="ackAlert(${vendorId}, ${alert.id})">Acknowledge</button>
          <button class="btn-primary" onclick="resolveAlert(${vendorId}, ${alert.id})">Resolve</button>
        </td>
      `;
      body.appendChild(row);
    });
  } catch (err) {
    Utils.showMessage(err.message || "Failed to load alerts", "error");
  }
}

async function ackAlert(vendorId, alertId) {
  try {
    await Api.patch(`/vendors/${vendorId}/alerts/${alertId}/acknowledge`, {});
    Utils.showMessage("Alert acknowledged");
    await loadAlerts();
  } catch (err) {
    Utils.showMessage(err.message || "Failed to acknowledge alert", "error");
  }
}

async function resolveAlert(vendorId, alertId) {
  try {
    await Api.patch(`/vendors/${vendorId}/alerts/${alertId}/resolve`, {});
    Utils.showMessage("Alert resolved");
    await loadAlerts();
  } catch (err) {
    Utils.showMessage(err.message || "Failed to resolve alert", "error");
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