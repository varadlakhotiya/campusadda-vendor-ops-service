requireAuth();
renderSidebar("vendors");

document.addEventListener("DOMContentLoaded", () => {
  loadVendors();

  document.getElementById("vendorForm").addEventListener("submit", async (e) => {
    e.preventDefault();

    try {
      const payload = {
        vendorCode: document.getElementById("vendorCode").value,
        name: document.getElementById("name").value,
        contactName: document.getElementById("contactName").value,
        contactPhone: document.getElementById("contactPhone").value,
        contactEmail: document.getElementById("contactEmail").value,
        locationLabel: document.getElementById("locationLabel").value,
        campusArea: document.getElementById("campusArea").value,
        status: document.getElementById("status").value,
        sourceSystem: document.getElementById("sourceSystem").value,
        externalVendorId: document.getElementById("externalVendorId").value,
        description: document.getElementById("description").value
      };

      await Api.post("/vendors", payload);
      Utils.showMessage("Vendor created successfully");
      e.target.reset();
      loadVendors();
    } catch (err) {
      Utils.showMessage(err.message, "error");
    }
  });
});

async function loadVendors() {
  try {
    const response = await Api.get("/vendors");
    const vendors = response.data || [];
    const body = document.getElementById("vendorsTableBody");
    body.innerHTML = "";

    vendors.forEach(vendor => {
      const row = document.createElement("tr");
      row.innerHTML = `
        <td>${vendor.id}</td>
        <td>${vendor.vendorCode}</td>
        <td>${vendor.name}</td>
        <td>${vendor.status}</td>
        <td>${vendor.campusArea || "-"}</td>
      `;
      body.appendChild(row);
    });
  } catch (err) {
    Utils.showMessage(err.message, "error");
  }
}