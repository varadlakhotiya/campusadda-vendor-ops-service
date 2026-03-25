requireAuth();
renderSidebar("alerts");

document.addEventListener("DOMContentLoaded", async () => {
  await loadVendors();
  document.getElementById("vendorSelect").addEventListener("change", loadAlerts);
});

async function loadVendors() {
  const response = await Api.get("/vendors");
  Utils.populateSelect("vendorSelect", response.data || [], "id", "name", "Select Vendor");
}

async function loadAlerts() {
  const vendorId = document.getElementById("vendorSelect").value;
  if (!vendorId) return;

  const response = await Api.get(`/vendors/${vendorId}/alerts`);
  const alerts = response.data || [];
  const body = document.getElementById("alertsBody");
  body.innerHTML = "";

  alerts.forEach(alert => {
    const row = document.createElement("tr");
    row.innerHTML = `
      <td>${alert.alertType}</td>
      <td>${alert.title}</td>
      <td>${alert.status}</td>
      <td>${alert.severity}</td>
      <td>${Utils.formatDateTime(alert.triggeredAt)}</td>
      <td class="actions">
        <button class="btn-secondary" onclick="ackAlert(${vendorId}, ${alert.id})">Acknowledge</button>
        <button class="btn-primary" onclick="resolveAlert(${vendorId}, ${alert.id})">Resolve</button>
      </td>
    `;
    body.appendChild(row);
  });
}

async function ackAlert(vendorId, alertId) {
  try {
    await Api.patch(`/vendors/${vendorId}/alerts/${alertId}/acknowledge`, {});
    Utils.showMessage("Alert acknowledged");
    loadAlerts();
  } catch (err) {
    Utils.showMessage(err.message, "error");
  }
}

async function resolveAlert(vendorId, alertId) {
  try {
    await Api.patch(`/vendors/${vendorId}/alerts/${alertId}/resolve`, {});
    Utils.showMessage("Alert resolved");
    loadAlerts();
  } catch (err) {
    Utils.showMessage(err.message, "error");
  }
}