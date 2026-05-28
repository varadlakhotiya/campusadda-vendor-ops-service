requireAuth();

document.addEventListener("DOMContentLoaded", async () => {
  try {
    await renderSidebar("vendors");
    bindVendorEvents();
    await loadVendorsPage();
  } catch (err) {
    Utils.showMessage(err.message || "Failed to load vendors page", "error");
  }
});

function bindVendorEvents() {
  document.getElementById("refreshVendorIntelBtn")?.addEventListener("click", loadVendorIntelligence);
  document.getElementById("vendorForm")?.addEventListener("submit", handleCreateVendor);
}

async function loadVendorsPage() {
  const isAdmin = await Portal.isAdmin();
  const createSection = document.getElementById("createVendorSection");

  if (createSection && !isAdmin) {
    createSection.style.display = "none";
  }

  await loadVendors();
  if (isAdmin) {
    await loadVendorIntelligence();
  } else {
    document.getElementById("vendorIntelBody").innerHTML = `<tr><td colspan="9">Vendor intelligence is available only in admin view.</td></tr>`;
  }
}

async function handleCreateVendor(e) {
  e.preventDefault();

  try {
    const payload = {
      vendorCode: document.getElementById("vendorCode").value.trim(),
      name: document.getElementById("name").value.trim(),
      contactName: document.getElementById("contactName").value.trim(),
      contactPhone: document.getElementById("contactPhone").value.trim(),
      contactEmail: document.getElementById("contactEmail").value.trim(),
      locationLabel: document.getElementById("locationLabel").value.trim(),
      campusArea: document.getElementById("campusArea").value.trim(),
      status: document.getElementById("status").value.trim() || "ACTIVE",
      sourceSystem: document.getElementById("sourceSystem").value.trim() || "VENDOR_OPS",
      externalVendorId: document.getElementById("externalVendorId").value.trim() || null,
      description: document.getElementById("description").value.trim()
    };

    await Api.post("/vendors", payload);
    Utils.showMessage("Vendor created successfully");
    e.target.reset();
    await loadVendorsPage();
  } catch (err) {
    Utils.showMessage(err.message || "Failed to create vendor", "error");
  }
}

async function loadVendors() {
  try {
    const response = await Api.get("/vendors");
    const vendors = response.data || [];
    const body = document.getElementById("vendorsTableBody");

    Utils.setText("totalVendorCount", vendors.length);
    Utils.setText("activeVendorCount", vendors.filter((vendor) => String(vendor.status || "").toUpperCase() === "ACTIVE").length);

    body.innerHTML = "";

    if (!vendors.length) {
      body.innerHTML = `<tr><td colspan="5">No vendors found</td></tr>`;
      return vendors;
    }

    vendors.forEach((vendor) => {
      const row = document.createElement("tr");
      row.innerHTML = `
        <td>${vendor.id ?? "-"}</td>
        <td>${Utils.escapeHtml(vendor.vendorCode ?? "-")}</td>
        <td>${Utils.escapeHtml(vendor.name ?? "-")}</td>
        <td><span class="pill pill-neutral">${Utils.escapeHtml(vendor.status ?? "-")}</span></td>
        <td>${Utils.escapeHtml(vendor.campusArea ?? "-")}</td>
      `;
      body.appendChild(row);
    });

    return vendors;
  } catch (err) {
    Utils.showMessage(err.message || "Failed to load vendors", "error");
    return [];
  }
}

async function loadVendorIntelligence() {
  const body = document.getElementById("vendorIntelBody");
  if (!body) return;

  body.innerHTML = `<tr><td colspan="9">Loading vendor intelligence...</td></tr>`;

  try {
    const vendorsResponse = await Api.get("/vendors");
    const vendors = vendorsResponse.data || [];

    if (!vendors.length) {
      body.innerHTML = `<tr><td colspan="9">No vendors found</td></tr>`;
      return;
    }

    const { fromDate, toDate } = getDateRange(30);

    const rows = await Promise.all(vendors.map(async (vendor) => {
      const [summaryRes, analyticsRes, reorderRes, anomalyRes] = await Promise.allSettled([
        Api.get(`/vendors/${vendor.id}/summary`),
        Api.get(`/vendors/${vendor.id}/analytics/dashboard?fromDate=${fromDate}&toDate=${toDate}`),
        Api.get(`/vendors/${vendor.id}/reorder-recommendations`),
        Api.get(`/vendors/${vendor.id}/anomalies`)
      ]);

      const summary = summaryRes.status === "fulfilled" ? (summaryRes.value.data || {}) : {};
      const analytics = analyticsRes.status === "fulfilled" ? (analyticsRes.value.data || {}) : {};
      const reorders = reorderRes.status === "fulfilled" ? (reorderRes.value.data || []) : [];
      const anomalies = anomalyRes.status === "fulfilled" ? (anomalyRes.value.data || []) : [];

      const openReorders = reorders.filter((item) => Number(item.suggestedReorderQty || 0) > 0).length;
      const openAnomalies = anomalies.filter((item) => String(item.status || "").toUpperCase() !== "RESOLVED").length;

      return {
        vendor,
        summary,
        analytics,
        openReorders,
        openAnomalies
      };
    }));

    Utils.setText("vendorsWithReorders", rows.filter((row) => row.openReorders > 0).length);
    Utils.setText("vendorsWithAnomalies", rows.filter((row) => row.openAnomalies > 0).length);

    body.innerHTML = rows.map(({ vendor, summary, analytics, openReorders, openAnomalies }) => `
      <tr>
        <td>${Utils.escapeHtml(vendor.name ?? "-")}</td>
        <td>${Utils.escapeHtml(vendor.campusArea ?? "-")}</td>
        <td>${Utils.formatNumber(analytics.totalOrders ?? 0)}</td>
        <td>${Utils.formatCurrency(analytics.grossRevenue ?? 0)}</td>
        <td>${Utils.formatNumber(summary.lowStockItemCount ?? analytics.lowStockCount ?? 0)}</td>
        <td>${Utils.formatNumber(openReorders)}</td>
        <td>${Utils.formatNumber(openAnomalies)}</td>
        <td>${Utils.escapeHtml(analytics.topItemName ?? "-")}</td>
        <td><span class="pill ${signalClass(openReorders, openAnomalies, summary.lowStockItemCount ?? analytics.lowStockCount ?? 0)}">${signalLabel(openReorders, openAnomalies, summary.lowStockItemCount ?? analytics.lowStockCount ?? 0)}</span></td>
      </tr>
    `).join("");
  } catch (err) {
    body.innerHTML = `<tr><td colspan="9">Failed to load vendor intelligence</td></tr>`;
    Utils.showMessage(err.message || "Failed to load vendor intelligence", "error");
  }
}

function getDateRange(days = 30) {
  const to = new Date();
  const from = new Date();
  from.setDate(to.getDate() - Math.max(days - 1, 0));
  return {
    fromDate: Utils.formatDateInput(from),
    toDate: Utils.formatDateInput(to)
  };
}

function signalLabel(openReorders, openAnomalies, lowStockCount) {
  if (openAnomalies > 0) return "Investigate anomalies";
  if (openReorders > 0) return "Review reorders";
  if (Number(lowStockCount || 0) > 0) return "Watch stock";
  return "Healthy";
}

function signalClass(openReorders, openAnomalies, lowStockCount) {
  if (openAnomalies > 0) return "pill-danger";
  if (openReorders > 0 || Number(lowStockCount || 0) > 0) return "pill-warning";
  return "pill-success";
}
