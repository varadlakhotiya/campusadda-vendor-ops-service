function renderSidebar(activePage) {
  const sidebar = document.getElementById("sidebar");
  if (!sidebar) return;

  sidebar.innerHTML = `
    <div class="sidebar-brand">CampusAdda Vendor Ops</div>
    <a class="${activePage === 'dashboard' ? 'active' : ''}" href="dashboard.html">Dashboard</a>
    <a class="${activePage === 'vendors' ? 'active' : ''}" href="vendors.html">Vendors</a>
    <a class="${activePage === 'menu-categories' ? 'active' : ''}" href="menu-categories.html">Menu Categories</a>
    <a class="${activePage === 'menu-items' ? 'active' : ''}" href="menu-items.html">Menu Items</a>
    <a class="${activePage === 'recipe' ? 'active' : ''}" href="recipe.html">Recipe</a>
    <a class="${activePage === 'inventory' ? 'active' : ''}" href="inventory.html">Inventory</a>
    <a class="${activePage === 'stock-movements' ? 'active' : ''}" href="stock-movements.html">Stock Movements</a>
    <a class="${activePage === 'orders' ? 'active' : ''}" href="orders.html">Orders</a>
    <a class="${activePage === 'alerts' ? 'active' : ''}" href="alerts.html">Alerts</a>
    <button id="logoutBtn" class="logout-btn">Logout</button>
  `;

  document.getElementById("logoutBtn")?.addEventListener("click", () => {
    Auth.logout();
  });
}