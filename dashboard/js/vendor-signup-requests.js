requireAuth();

document.addEventListener("DOMContentLoaded", async () => {
  await renderSidebar("signup-requests");
  await loadRequests();
});

async function loadRequests() {
  try {
    const response = await Api.get("/admin/vendor-signup-requests");
    const rows = response.data || [];
    const body = document.getElementById("signupRequestsBody");
    body.innerHTML = "";

    rows.forEach(r => {
      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td>${r.id}</td>
        <td>${r.restaurantName}</td>
        <td>${r.contactPersonName}</td>
        <td>${r.contactEmail}</td>
        <td>${r.status}</td>
        <td class="actions">
          ${r.status === 'PENDING' ? `
            <button class="btn-primary" onclick="approveRequest(${r.id}, '${r.restaurantName.replace(/'/g, "") }')">Approve</button>
            <button class="btn-danger" onclick="rejectRequest(${r.id})">Reject</button>
          ` : '-'}
        </td>
      `;
      body.appendChild(tr);
    });
  } catch (err) {
    Utils.showMessage(err.message, "error");
  }
}

async function approveRequest(requestId, restaurantName) {
  const vendorCode = prompt("Enter vendor code", restaurantName.toUpperCase().replace(/\s+/g, "_").slice(0, 20));
  if (!vendorCode) return;

  const initialPassword = prompt("Enter initial password for vendor manager", "Vendor@123");
  if (!initialPassword) return;

  try {
    await Api.post(`/admin/vendor-signup-requests/${requestId}/approve`, {
      vendorCode,
      roleCode: "VENDOR_MANAGER",
      initialPassword
    });
    Utils.showMessage("Signup request approved");
    loadRequests();
  } catch (err) {
    Utils.showMessage(err.message, "error");
  }
}

async function rejectRequest(requestId) {
  const rejectionReason = prompt("Rejection reason");
  if (!rejectionReason) return;

  try {
    await Api.post(`/admin/vendor-signup-requests/${requestId}/reject`, {
      rejectionReason
    });
    Utils.showMessage("Signup request rejected");
    loadRequests();
  } catch (err) {
    Utils.showMessage(err.message, "error");
  }
}