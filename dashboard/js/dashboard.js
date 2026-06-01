requireAuth();

const DashboardPage = { rangeValue: "30" };

document.addEventListener("DOMContentLoaded", async () => {
  try {
    await renderSidebar("dashboard");
    bindDashboardEvents();
    await VendorScope.initVendorSelect("vendorSelect", loadDashboardData);
  } catch (err) {
    Utils.showMessage(err.message || "Failed to load dashboard", "error");
  }
});

function bindDashboardEvents() {
  document.getElementById("rangeDaysSelect")?.addEventListener("change", async (e) => {
    DashboardPage.rangeValue = String(e.target.value || "30");
    toggleCustomRange();
    await loadDashboardData();
  });
  document.getElementById("fromDateInput")?.addEventListener("change", async () => {
    if (DashboardPage.rangeValue === "custom") await loadDashboardData();
  });
  document.getElementById("toDateInput")?.addEventListener("change", async () => {
    if (DashboardPage.rangeValue === "custom") await loadDashboardData();
  });
  document.getElementById("refreshDashboardBtn")?.addEventListener("click", loadDashboardData);
  document.getElementById("generateRecommendationsBtn")?.addEventListener("click", generateRecommendations);
  document.getElementById("scanAnomaliesBtn")?.addEventListener("click", scanAnomalies);
}

function toggleCustomRange() {
  const group = document.getElementById("customRangeGroup");
  if (!group) return;
  group.style.display = DashboardPage.rangeValue === "custom" ? "flex" : "none";
  if (DashboardPage.rangeValue === "custom") {
    const { fromDate, toDate } = getDateRange(30);
    const fromInput = document.getElementById("fromDateInput");
    const toInput = document.getElementById("toDateInput");
    if (fromInput && !fromInput.value) fromInput.value = fromDate;
    if (toInput && !toInput.value) toInput.value = toDate;
  }
}

async function getSelectedVendorId() {
  let vendorId = await VendorScope.getVendorId("vendorSelect");
  if (!vendorId) {
    const ctx = await Portal.getContext();
    vendorId = ctx?.primaryVendorId ? Number(ctx.primaryVendorId) : null;
  }
  return vendorId;
}

function getDateRange(days = 30) {
  const to = new Date();
  const from = new Date();
  from.setDate(to.getDate() - Math.max(days - 1, 0));
  return { fromDate: Utils.formatDateInput(from), toDate: Utils.formatDateInput(to) };
}

function getSelectedDateRange() {
  if (DashboardPage.rangeValue === "custom") {
    const fromDate = document.getElementById("fromDateInput")?.value;
    const toDate = document.getElementById("toDateInput")?.value;
    if (fromDate && toDate) return { fromDate, toDate, label: `${fromDate} → ${toDate}` };
  }
  const days = Number(DashboardPage.rangeValue || 30);
  const { fromDate, toDate } = getDateRange(days);
  return { fromDate, toDate, label: `${days} day range` };
}

async function loadDashboardData() {
  const vendorId = await getSelectedVendorId();
  if (!vendorId) { renderDashboardEmptyState(); return; }

  const { fromDate, toDate, label } = getSelectedDateRange();
  Utils.setText("analyticsRangeLabel", label);

  try {
    const [summaryResponse, analyticsResponse, dailySalesResponse, topItemsResponse, alertsResponse, reorderResponse, anomalyResponse, inventoryResponse, forecastRunsResponse] =
      await Promise.all([
        Api.get(`/vendors/${vendorId}/summary`),
        Api.get(`/vendors/${vendorId}/analytics/dashboard?fromDate=${fromDate}&toDate=${toDate}`),
        Api.get(`/vendors/${vendorId}/analytics/daily-sales?fromDate=${fromDate}&toDate=${toDate}`),
        Api.get(`/vendors/${vendorId}/analytics/top-items?fromDate=${fromDate}&toDate=${toDate}`),
        Api.get(`/vendors/${vendorId}/alerts`),
        Api.get(`/vendors/${vendorId}/reorder-recommendations`),
        Api.get(`/vendors/${vendorId}/anomalies`),
        Api.get(`/vendors/${vendorId}/inventory-items`),
        Api.get(`/vendors/${vendorId}/forecast-runs`)
      ]);

    const summary = summaryResponse.data || {};
    const analytics = analyticsResponse.data || {};
    const dailySales = dailySalesResponse.data || [];
    const topItems = topItemsResponse.data || [];
    const alerts = alertsResponse.data || [];
    const reorders = reorderResponse.data || [];
    const anomalies = anomalyResponse.data || [];
    const inventoryItems = inventoryResponse.data || [];
    const forecastRuns = forecastRunsResponse.data || [];

    Utils.setText("assignedUserCount", summary.assignedUserCount ?? 0);
    Utils.setText("activeMenuItemCount", summary.activeMenuItemCount ?? 0);
    Utils.setText("lowStockItemCount", summary.lowStockItemCount ?? analytics.lowStockCount ?? 0);
    Utils.setText("todayOrderCount", summary.todayOrderCount ?? 0);

    Utils.setText("analyticsTotalOrders", analytics.totalOrders ?? 0);
    Utils.setText("grossRevenue", Utils.formatCurrency(analytics.grossRevenue ?? 0));
    Utils.setText("avgOrderValue", Utils.formatCurrency(analytics.avgOrderValue ?? 0));
    Utils.setText("openReorderCount", (reorders || []).filter((item) => Number(item.suggestedReorderQty || 0) > 0).length);

    renderDailySales(dailySales);
    await renderForecastCards(vendorId, topItems, forecastRuns);
    renderReorders(reorders, inventoryItems);
    renderAnomalies(anomalies);
    renderActionCenter(
    reorders,
    anomalies,
    inventoryItems
);

renderBusinessAdvisor(
    analytics,
    reorders,
    anomalies
);
    renderAlerts(alerts, reorders, anomalies, inventoryItems);
  } catch (err) {
    Utils.showMessage(err.message || "Failed to load dashboard data", "error");
  }
}

function renderDashboardEmptyState() {
  Utils.setText("assignedUserCount", 0);
  Utils.setText("activeMenuItemCount", 0);
  Utils.setText("lowStockItemCount", 0);
  Utils.setText("todayOrderCount", 0);
  Utils.setText("analyticsTotalOrders", 0);
  Utils.setText("grossRevenue", Utils.formatCurrency(0));
  Utils.setText("avgOrderValue", Utils.formatCurrency(0));
  Utils.setText("openReorderCount", 0);
  document.getElementById("dailySalesChart").innerHTML = `<div class="empty-state">Select a vendor to load sales guidance.</div>`;
  document.getElementById("forecastCards").innerHTML = `<div class="empty-state">Sales guidance will appear here after loading data.</div>`;
  document.getElementById("dashboardReorderBody").innerHTML = `<tr><td colspan="5">No buy list to show.</td></tr>`;
  document.getElementById("dashboardAnomalyBody").innerHTML = `<tr><td colspan="5">No demand risks to show.</td></tr>`;
  document.getElementById("recentAlertsBody").innerHTML = `<tr><td colspan="5">No action alerts to show.</td></tr>`;
  Utils.setText("dailySalesMeta", "No data yet");
  Utils.setText("forecastMeta", "Sales guidance");
  Utils.setText("reorderMeta", "Buy list");
  Utils.setText("anomalyMeta", "Demand risk");
  Utils.setText("alertsMeta", "Action signals");

  const actionCenter =
 document.getElementById(
   "actionCenterCards"
 );

if(actionCenter){
 actionCenter.innerHTML =
 `
 <div class="empty-state">
   No actions available.
 </div>
 `;
}

const advisor =
 document.getElementById(
   "businessAdvisorCards"
 );

if(advisor){
 advisor.innerHTML =
 `
 <div class="empty-state">
   Recommendations will appear here.
 </div>
 `;
}


}

function renderAlerts(alerts, reorders, anomalies, inventoryItems) {
  const body = document.getElementById("recentAlertsBody");
  const meta = document.getElementById("alertsMeta");
  if (!body) return;

  const inventoryNameMap = new Map((inventoryItems || []).map((item) => [Number(item.id), item.itemName]));

  const reorderAlerts = (reorders || [])
    .filter((rec) => Number(rec.suggestedReorderQty || 0) > 0)
    .slice(0, 6)
    .map((rec) => ({
      source: "OPS",
      alertType: "RESTOCK",
      title: `${inventoryNameMap.get(Number(rec.inventoryItemId)) || `Inventory #${rec.inventoryItemId}`} needs restocking`,
      status: rec.recommendationStatus || "OPEN",
      severity: Number(rec.suggestedReorderQty || 0) >= Number(rec.reorderPointQty || 0) * 0.5 ? "HIGH" : "WARNING"
    }));

  const anomalyAlerts = (anomalies || [])
    .filter((item) => String(item.status || "").toUpperCase() !== "RESOLVED")
    .slice(0, 6)
    .map((item) => ({
      source: "OPS",
      alertType: item.anomalyType || "REVIEW",
      title: `${item.menuItemName || `Item #${item.menuItemId}`} needs review`,
      status: item.status || "OPEN",
      severity: item.severity || "WARNING"
    }));

  const systemAlerts = (alerts || [])
    .slice(0, 8)
    .map((alert) => ({
      source: "SYSTEM",
      alertType: alert.alertType ?? "-",
      title: alert.title ?? "-",
      status: alert.status ?? "-",
      severity: alert.severity ?? "-"
    }));

  const combined = [...reorderAlerts, ...anomalyAlerts, ...systemAlerts].slice(0, 8);
  if (meta) meta.textContent = combined.length ? `${combined.length} action alert(s)` : "Action signals";

  if (!combined.length) {
    body.innerHTML = `<tr><td colspan="5">No action alerts found</td></tr>`;
    return;
  }

  body.innerHTML = combined.map((item) => `
    <tr>
      <td><span class="pill ${item.source === "SYSTEM" ? "pill-neutral" : "pill-info"}">${businessSourceLabel(item.source)}</span></td>
      <td>${Utils.escapeHtml(item.alertType)}</td>
      <td>${Utils.escapeHtml(item.title)}</td>
      <td><span class="pill pill-neutral">${Utils.escapeHtml(item.status)}</span></td>
      <td><span class="pill ${severityClass(item.severity)}">${Utils.escapeHtml(item.severity)}</span></td>
    </tr>`).join("");
}

function renderDailySales(dailySales) {
  const container = document.getElementById("dailySalesChart");
  const meta = document.getElementById("dailySalesMeta");
  if (!container) return;
  if (!dailySales.length) {
    if (meta) meta.textContent = "No sales rows found";
    container.innerHTML = `<div class="empty-state">No daily sales available for the selected range.</div>`;
    return;
  }
  const maxRevenue = Math.max(...dailySales.map((row) => Number(row.grossRevenue || 0)), 1);
  const totalRevenue = dailySales.reduce((sum, row) => sum + Number(row.grossRevenue || 0), 0);
  const totalOrders = dailySales.reduce((sum, row) => sum + Number(row.totalOrders || 0), 0);
  if (meta) meta.textContent = `${dailySales.length} day(s) • ${totalOrders} orders • ${Utils.formatCurrency(totalRevenue)}`;

  container.innerHTML = dailySales.map((row) => {
    const revenue = Number(row.grossRevenue || 0);
    const width = Math.max(6, Math.round((revenue / maxRevenue) * 100));
    return `<div class="chart-row"><div class="chart-label"><strong>${Utils.formatDateShort(row.salesDate)}</strong><span>${row.totalOrders ?? 0} orders</span></div><div class="chart-bar-track"><div class="chart-bar-fill" style="width:${width}%"></div></div><div class="chart-value">${Utils.formatCurrency(revenue)}</div></div>`;
  }).join("");
}

async function renderForecastCards(vendorId, topItems, forecastRuns) {
  const container = document.getElementById("forecastCards");
  const meta = document.getElementById("forecastMeta");
  if (!container) return;

  const topItemMap = new Map((topItems || []).map((item) => [Number(item.menuItemId), item]));
  const latestRuns = [];
  const seen = new Set();

  (forecastRuns || [])
    .filter((run) => String(run.status || "").toUpperCase() === "SUCCESS")
    .sort((a, b) => new Date(b.startedAt || 0) - new Date(a.startedAt || 0))
    .forEach((run) => {
      const menuItemId = Number(run.menuItemId);
      if (!seen.has(menuItemId)) {
        seen.add(menuItemId);
        latestRuns.push(run);
      }
    });

  const prioritized = latestRuns
    .sort((a, b) => {
      const aTop = topItemMap.has(Number(a.menuItemId)) ? 1 : 0;
      const bTop = topItemMap.has(Number(b.menuItemId)) ? 1 : 0;
      if (aTop !== bTop) return bTop - aTop;
      return Number(topItemMap.get(Number(b.menuItemId))?.quantitySold || 0) - Number(topItemMap.get(Number(a.menuItemId))?.quantitySold || 0);
    })
    .slice(0, 6);

  if (meta) meta.textContent = prioritized.length ? `${prioritized.length} item(s) to prepare for` : "Sales guidance";
  if (!prioritized.length) {
    container.innerHTML = `<div class="empty-state">No sales guidance available for this vendor.</div>`;
    return;
  }

  const cards = await Promise.all(
    prioritized.map(async (run) => {
      try {
        const response = await Api.get(`/vendors/${vendorId}/forecast-runs/${run.id}/values`);
        const values = response.data || [];
        const next7Days = values.reduce((sum, point) => sum + Number(point.predictedQuantity || 0), 0);
        const tomorrow = Number(values[0]?.predictedQuantity || 0);
        const top = topItemMap.get(Number(run.menuItemId));

        return {
          itemName: top?.itemName || `Menu Item #${run.menuItemId}`,
          quantitySold: Number(top?.quantitySold || 0),
          next7Days,
          tomorrow,
          action: businessForecastAction(tomorrow, next7Days)
        };
      } catch (_) {
        const top = topItemMap.get(Number(run.menuItemId));
        return {
          itemName: top?.itemName || `Menu Item #${run.menuItemId}`,
          quantitySold: Number(top?.quantitySold || 0),
          next7Days: null,
          tomorrow: null,
          action: "Monitor today"
        };
      }
    })
  );

  container.innerHTML = cards.map((item) => `
    <div class="mini-card">
      <div class="mini-card-head">
        <span class="badge subtle-badge">Sell More</span>
        <span class="mini-card-model">${Utils.escapeHtml(item.action)}</span>
      </div>
      <h4>${Utils.escapeHtml(item.itemName)}</h4>
      <div class="mini-metrics">
        <div>
          <span class="mini-label">Sold in range</span>
          <strong>${Utils.formatNumber(item.quantitySold ?? 0)}</strong>
        </div>
        <div>
          <span class="mini-label">Tomorrow</span>
          <strong>${item.tomorrow == null ? "—" : Utils.formatNumber(item.tomorrow)}</strong>
        </div>
      </div>
      <div class="mini-metrics">
        <div>
          <span class="mini-label">Next 7 days</span>
          <strong>${item.next7Days == null ? "—" : Utils.formatNumber(item.next7Days)}</strong>
        </div>
        <div>
          <span class="mini-label">Action</span>
          <strong>${Utils.escapeHtml(item.action)}</strong>
        </div>
      </div>
    </div>
  `).join("");
}

function renderReorders(reorders, inventoryItems) {
  const body = document.getElementById("dashboardReorderBody");
  const meta = document.getElementById("reorderMeta");
  if (!body) return;

  const inventoryNameMap = new Map((inventoryItems || []).map((item) => [Number(item.id), item.itemName]));
  const actionable = (reorders || [])
    .filter((item) => Number(item.suggestedReorderQty || 0) > 0)
    .sort((a, b) => Number(b.suggestedReorderQty || 0) - Number(a.suggestedReorderQty || 0))
    .slice(0, 8);

  if (meta) meta.textContent = actionable.length ? `${actionable.length} item(s) need buying` : "Stock-out risk";
  if (!actionable.length) {
    body.innerHTML = `<tr><td colspan="5">No items need buying right now</td></tr>`;
    return;
  }

  body.innerHTML = actionable.map((rec) => {
    const priority = businessReorderAction(rec.suggestedReorderQty);
    const pillClass = priority === "Buy now" ? "pill-danger" : priority === "Buy soon" ? "pill-warning" : "pill-neutral";

    return `
      <tr>
        <td>${Utils.escapeHtml(inventoryNameMap.get(Number(rec.inventoryItemId)) || `Inventory #${rec.inventoryItemId}`)}</td>
        <td>${Utils.formatNumber(rec.currentStockQty ?? 0)}</td>
        <td>${Utils.formatNumber(rec.reorderPointQty ?? 0)}</td>
        <td>${Utils.formatNumber(rec.suggestedReorderQty ?? 0)}</td>
        <td><span class="pill ${pillClass}">${Utils.escapeHtml(priority)}</span></td>
      </tr>`;
  }).join("");
}

function renderAnomalies(anomalies) {
  const body = document.getElementById("dashboardAnomalyBody");
  const meta = document.getElementById("anomalyMeta");
  if (!body) return;

  const openAnomalies = (anomalies || [])
    .filter((item) => String(item.status || "").toUpperCase() !== "RESOLVED")
    .slice(0, 8);

  if (meta) meta.textContent = openAnomalies.length ? `${openAnomalies.length} item(s) need review` : "Demand risk";
  if (!openAnomalies.length) {
    body.innerHTML = `<tr><td colspan="5">No demand risks found</td></tr>`;
    return;
  }

  body.innerHTML = openAnomalies.map((item) => `
    <tr>
      <td>${Utils.escapeHtml(item.menuItemName ?? `Item #${item.menuItemId ?? "-"}`)}</td>
      <td>${Utils.escapeHtml(item.anomalyType ?? "-")}</td>
      <td>${Utils.formatNumber(item.observedValue ?? 0)}</td>
      <td>${Utils.formatNumber(item.expectedValue ?? 0)}</td>
      <td><span class="pill ${severityClass(item.severity)}">${Utils.escapeHtml(item.severity ?? "-")}</span></td>
    </tr>`).join("");
}

async function generateRecommendations() {
  const vendorId = await getSelectedVendorId();
  if (!vendorId) { Utils.showMessage("Select a vendor first", "error"); return; }
  const button = document.getElementById("generateRecommendationsBtn");
  try {
    Utils.setButtonLoading(button, true, "Generating.");
    const response = await Api.post(`/vendors/${vendorId}/reorder-recommendations/generate`, {});
    const count = Array.isArray(response?.data) ? response.data.filter((row) => Number(row.suggestedReorderQty || 0) > 0).length : 0;
    Utils.showMessage(count ? `Generated ${count} reorder recommendation(s)` : "Generation completed. No positive reorder candidates yet.");
    await loadDashboardData();
  } catch (err) {
    Utils.showMessage(err.message || "Failed to generate recommendations", "error");
  } finally {
    Utils.setButtonLoading(button, false);
  }
}

async function scanAnomalies() {
  const vendorId = await getSelectedVendorId();
  if (!vendorId) { Utils.showMessage("Select a vendor first", "error"); return; }
  const button = document.getElementById("scanAnomaliesBtn");
  try {
    Utils.setButtonLoading(button, true, "Scanning.");
    const response = await Api.post(`/vendors/${vendorId}/anomalies/scan`, {});
    const count = Array.isArray(response?.data) ? response.data.length : 0;
    Utils.showMessage(count ? `Detected ${count} anomaly signal(s)` : "Scan completed. No strong anomalies crossed the threshold.");
    await loadDashboardData();
  } catch (err) {
    Utils.showMessage(err.message || "Failed to scan anomalies", "error");
  } finally {
    Utils.setButtonLoading(button, false);
  }
}

function severityClass(value) {
  const normalized = String(value || "").toUpperCase();
  if (normalized === "CRITICAL" || normalized === "HIGH") return "pill-danger";
  if (normalized === "WARNING" || normalized === "MEDIUM") return "pill-warning";
  if (normalized === "INFO") return "pill-info";
  return "pill-neutral";
}

function businessForecastAction(tomorrow, next7Days) {
  const t = Number(tomorrow || 0);
  const w = Number(next7Days || 0);

  if (t >= 2 || w >= 10) return "Prepare more today";
  if (t <= 0.5 && w <= 3) return "Reduce prep";
  return "Monitor today";
}

function businessReorderAction(qty) {
  const q = Number(qty || 0);

  if (q >= 10) return "Buy now";
  if (q > 0) return "Buy soon";
  return "OK";
}

function businessSourceLabel(source) {
  return String(source || "").toUpperCase() === "SYSTEM" ? "System" : "Ops";
}


function renderActionCenter(reorders, anomalies, inventoryItems) {
  const container = document.getElementById("actionCenterCards");
  if (!container) return;

  const actions = [];
  const inventoryMap = new Map((inventoryItems || []).map((i) => [Number(i.id), i.itemName]));

  (reorders || [])
    .filter((r) => Number(r.suggestedReorderQty || 0) > 0)
    .slice(0, 5)
    .forEach((r) => {
      actions.push({
        priority: "high",
        title: "Buy today",
        description: `${inventoryMap.get(Number(r.inventoryItemId)) || `Inventory #${r.inventoryItemId}`} — buy ${Utils.formatNumber(r.suggestedReorderQty)} units`
      });
    });

  (anomalies || [])
    .filter((a) => String(a.status || "").toUpperCase() !== "RESOLVED")
    .slice(0, 3)
    .forEach((a) => {
      actions.push({
        priority: "medium",
        title: "Review demand",
        description: `${a.menuItemName || `Item #${a.menuItemId}`} is moving away from normal`
      });
    });

  if (!actions.length) {
    container.innerHTML = `<div class="advisor-card">No urgent actions for today. Keep prep tight to reduce waste.</div>`;
    return;
  }

  container.innerHTML = actions.map((a) => `
    <div class="action-card action-priority-${a.priority}">
      <h4>${Utils.escapeHtml(a.title)}</h4>
      <div>${Utils.escapeHtml(a.description)}</div>
    </div>
  `).join("");
}


function renderBusinessAdvisor(analytics, reorders, anomalies) {
  const container = document.getElementById("businessAdvisorCards");
  if (!container) return;

  const insights = [];
  const actionableReorders = (reorders || []).filter((r) => Number(r.suggestedReorderQty || 0) > 0);
  const openAnomalies = (anomalies || []).filter((a) => String(a.status || "").toUpperCase() !== "RESOLVED");

  if (Number(analytics.grossRevenue || 0) > 0) {
    insights.push(`Sales in the selected range were ${Utils.formatCurrency(analytics.grossRevenue || 0)}.`);
  }

  if (actionableReorders.length) {
    insights.push(`${actionableReorders.length} item(s) need buying today to avoid stock-outs.`);
  }

  if (openAnomalies.length) {
    insights.push(`${openAnomalies.length} item(s) need review because demand is not normal.`);
  }

  if (!insights.length) {
    insights.push("No urgent action required. Keep prep tight to reduce waste.");
  }

  container.innerHTML = insights.map((i) => `
    <div class="advisor-card">
      <strong>Today</strong>
      ${Utils.escapeHtml(i)}
    </div>
  `).join("");
}