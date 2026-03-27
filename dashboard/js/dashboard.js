requireAuth();
renderSidebar("dashboard");

document.addEventListener("DOMContentLoaded", async () => {
  try {
    const vendorsResponse = await Api.get("/vendors");
    const vendors = vendorsResponse.data || [];
    Utils.populateSelect("vendorSelect", vendors, "id", "name", "Select Vendor");

    document.getElementById("vendorSelect").addEventListener("change", loadDashboardData);
  } catch (err) {
    Utils.showMessage(err.message, "error");
  }
});

async function loadDashboardData() {
  const vendorId = document.getElementById("vendorSelect").value;
  if (!vendorId) return;

  try {
    const summaryResponse = await Api.get(`/vendors/${vendorId}/summary`);
    const summary = summaryResponse.data;

    Utils.setText("assignedUserCount", summary.assignedUserCount);
    Utils.setText("activeMenuItemCount", summary.activeMenuItemCount);
    Utils.setText("lowStockItemCount", summary.lowStockItemCount);
    Utils.setText("todayOrderCount", summary.todayOrderCount);

    const alertsResponse = await Api.get(`/vendors/${vendorId}/alerts`);
    renderAlerts(alertsResponse.data || []);
  } catch (err) {
    Utils.showMessage(err.message, "error");
  }
}

function renderAlerts(alerts) {
  const body = document.getElementById("recentAlertsBody");
  body.innerHTML = "";

  alerts.slice(0, 5).forEach(alert => {
    const row = document.createElement("tr");
    row.innerHTML = `
      <td>${alert.alertType}</td>
      <td>${alert.title}</td>
      <td>${alert.status}</td>
    `;
    body.appendChild(row);
  });
}