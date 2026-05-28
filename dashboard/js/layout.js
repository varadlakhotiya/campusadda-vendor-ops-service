async function renderSidebar(activePage) {
  const sidebar = document.getElementById("sidebar");
  if (!sidebar) return;

  const ctx = await Portal.getContext();

  const adminLinks = [
    ["dashboard", "dashboard.html", "Dashboard"],
    ["vendors", "vendors.html", "Vendors"],
    ["signup-requests", "vendor-signup-requests.html", "Vendor Signup Requests"],
    ["menu-categories", "menu-categories.html", "Menu Categories"],
    ["menu-items", "menu-items.html", "Menu Items"],
    ["recipe", "recipe.html", "Recipe"],
    ["inventory", "inventory.html", "Inventory"],
    ["stock-movements", "stock-movements.html", "Stock Movements"],
    ["orders", "orders.html", "Orders"],
    ["alerts", "alerts.html", "Alerts"]
  ];

  const vendorLinks = [
    ["dashboard", "dashboard.html", "My Dashboard"],
    ["menu-categories", "menu-categories.html", "My Menu Categories"],
    ["menu-items", "menu-items.html", "My Menu Items"],
    ["recipe", "recipe.html", "My Recipes"],
    ["inventory", "inventory.html", "My Inventory"],
    ["stock-movements", "stock-movements.html", "My Stock Movements"],
    ["orders", "orders.html", "My Orders"],
    ["alerts", "alerts.html", "My Alerts"]
  ];

  const links = ctx.adminUser ? adminLinks : vendorLinks;

  const vendorBadge = ctx.vendorUser && ctx.primaryVendorName
    ? `<div class="badge" style="margin-bottom:16px;">${ctx.primaryVendorName}</div>`
    : "";

  sidebar.innerHTML = `
    <div class="sidebar-brand">CampusAdda Vendor Ops</div>
    <div style="margin-bottom:8px; font-size:13px; opacity:0.9;">${ctx.fullName}</div>
    ${vendorBadge}
    ${links.map(([key, href, label]) => `
      <a class="${activePage === key ? 'active' : ''}" href="${href}">${label}</a>
    `).join('')}
    <button id="logoutBtn" class="logout-btn">Logout</button>
  `;

  document.getElementById("logoutBtn")?.addEventListener("click", () => {
    Auth.logout();
  });
}