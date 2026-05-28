requireAuth();

let currentVendorId = null;

document.addEventListener("DOMContentLoaded", async () => {
  try {
    await renderSidebar("alerts");
    await VendorScope.initVendorSelect("vendorSelect", async () => {
      currentVendorId = await getSelectedVendorId();
      await loadAlerts();
    });
  } catch (err) {
    Utils.showMessage(err.message || "Failed to load alerts page", "error");
  }
});

async function getSelectedVendorId() {
  let vendorId = await VendorScope.getVendorId("vendorSelect");
  if (!vendorId) {
    const ctx = await Portal.getContext();
    vendorId = ctx?.primaryVendorId ? Number(ctx.primaryVendorId) : null;
  }
  return vendorId;
}

async function loadAlerts() {
  const vendorId = currentVendorId || await getSelectedVendorId();
  const body = document.getElementById("alertsBody");
  const meta = document.getElementById("alertFeedMeta");
  if (!body) return;
  if (!vendorId) { body.innerHTML = `<tr><td colspan="7">Select a vendor first</td></tr>`; return; }

  try {
    const [alertResponse, reorderResponse, anomalyResponse, inventoryResponse] = await Promise.all([
      Api.get(`/vendors/${vendorId}/alerts`),
      Api.get(`/vendors/${vendorId}/reorder-recommendations`),
      Api.get(`/vendors/${vendorId}/anomalies`),
      Api.get(`/vendors/${vendorId}/inventory-items`)
    ]);

    const alerts = alertResponse.data || [];
    const reorders = reorderResponse.data || [];
    const anomalies = anomalyResponse.data || [];
    const inventoryItems = inventoryResponse.data || [];

    const inventoryNameMap = new Map((inventoryItems || []).map((item) => [Number(item.id), item.itemName]));
    const systemRows = alerts.map((alert) => ({ source: "SYSTEM", id: alert.id, alertType: alert.alertType ?? "-", title: alert.title ?? "-", status: alert.status ?? "-", severity: alert.severity ?? "-", triggeredAt: alert.triggeredAt, actionType: "system" }));
    const reorderRows = reorders.filter((row) => Number(row.suggestedReorderQty || 0) > 0).map((row) => ({ source: "ML", id: `reorder-${row.id}`, alertType: "REORDER", title: `${inventoryNameMap.get(Number(row.inventoryItemId)) || `Inventory #${row.inventoryItemId}`} needs replenishment`, status: row.recommendationStatus || "OPEN", severity: Number(row.suggestedReorderQty || 0) >= Number(row.reorderPointQty || 0) * 0.5 ? "HIGH" : "WARNING", triggeredAt: row.recommendationDate, actionType: "reorder" }));
    const anomalyRows = anomalies.filter((row) => String(row.status || "").toUpperCase() !== "RESOLVED").map((row) => ({ source: "ML", id: `anomaly-${row.id}`, alertType: row.anomalyType || "ANOMALY", title: `${row.menuItemName || `Item #${row.menuItemId}`} shows unusual demand`, status: row.status || "OPEN", severity: row.severity || "WARNING", triggeredAt: row.anomalyDate, actionType: "anomaly", anomalyId: row.id }));

    const combined = [...reorderRows, ...anomalyRows, ...systemRows].sort((a, b) => String(b.triggeredAt || "").localeCompare(String(a.triggeredAt || "")));
    if (meta) meta.textContent = combined.length ? `${combined.length} visible signal(s)` : "System + ML signals";
    if (!combined.length) { body.innerHTML = `<tr><td colspan="7">No alerts found</td></tr>`; return; }

    body.innerHTML = combined.map((row) => `
      <tr>
        <td><span class="pill ${row.source === "ML" ? "pill-info" : "pill-neutral"}">${escapeHtml(row.source)}</span></td>
        <td>${escapeHtml(row.alertType)}</td>
        <td>${escapeHtml(row.title)}</td>
        <td><span class="pill pill-neutral">${escapeHtml(row.status)}</span></td>
        <td><span class="pill ${severityClass(row.severity)}">${escapeHtml(row.severity)}</span></td>
        <td>${row.triggeredAt ? formatFlexibleDate(row.triggeredAt) : "-"}</td>
        <td class="actions">${buildActions(vendorId, row)}</td>
      </tr>`).join("");
  } catch (err) {
    Utils.showMessage(err.message || "Failed to load alerts", "error");
  }
}

function buildActions(vendorId, row) {
  if (row.actionType === "system") {
    return `<button class="btn-secondary" onclick="ackAlert(${vendorId}, ${row.id})">Acknowledge</button><button class="btn-primary" onclick="resolveAlert(${vendorId}, ${row.id})">Resolve</button>`;
  }
  if (row.actionType === "anomaly") {
    return `<button class="btn-primary" onclick="resolveAnomaly(${vendorId}, ${row.anomalyId})">Resolve anomaly</button>`;
  }
  return `<span class="muted-text">Inventory action</span>`;
}

async function ackAlert(vendorId, alertId) {
  try { await Api.patch(`/vendors/${vendorId}/alerts/${alertId}/acknowledge`, {}); Utils.showMessage("Alert acknowledged"); await loadAlerts(); }
  catch (err) { Utils.showMessage(err.message || "Failed to acknowledge alert", "error"); }
}
async function resolveAlert(vendorId, alertId) {
  try { await Api.patch(`/vendors/${vendorId}/alerts/${alertId}/resolve`, {}); Utils.showMessage("Alert resolved"); await loadAlerts(); }
  catch (err) { Utils.showMessage(err.message || "Failed to resolve alert", "error"); }
}
async function resolveAnomaly(vendorId, anomalyId) {
  try { await Api.patch(`/vendors/${vendorId}/anomalies/${anomalyId}/resolve`, {}); Utils.showMessage("Anomaly resolved"); await loadAlerts(); }
  catch (err) { Utils.showMessage(err.message || "Failed to resolve anomaly", "error"); }
}
function severityClass(value) {
  const normalized = String(value || "").toUpperCase();
  if (normalized === "CRITICAL" || normalized === "HIGH") return "pill-danger";
  if (normalized === "WARNING" || normalized === "MEDIUM") return "pill-warning";
  if (normalized === "INFO") return "pill-info";
  return "pill-neutral";
}
function formatFlexibleDate(value) {
  if (!value) return "-";
  if (/^\d{4}-\d{2}-\d{2}$/.test(String(value))) return Utils.formatDateShort(value);
  return Utils.formatDateTime(value);
}
function escapeHtml(value) {
  return String(value).replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll('"', "&quot;").replaceAll("'", "&#39;");
}
