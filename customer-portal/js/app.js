const TRACKING_STORAGE_KEY = "campusadda_customer_tracking";
const TRACKING_POLL_INTERVAL_MS = 15000;

const state = {
  vendors: [],
  vendorMenusById: {},
  vendorSearchIndexById: {},
  selectedVendor: null,
  menu: null,
  selectedCategoryId: null,
  cart: [],
  globalQuery: "",
  itemQuery: "",
  ui: {
    activeLayer: null
  },
  tracking: {
    orderNumber: "",
    phone: "",
    data: null,
    autoRefreshTimer: null
  }
};

const els = {};

const LAYERS = {
  vendor: "vendorModal",
  cart: "cartDrawer",
  account: "accountDrawer",
  tracking: "trackingDrawer"
};

document.addEventListener("DOMContentLoaded", async () => {
  cacheElements();
  bindStaticEvents();
  bindCheckout();
  bindTracking();

  try {
    renderVendorSkeletons();
    renderMenuSkeletons();
    renderCart();
    renderTrackingState("Enter your order number and phone number to see live status updates.");
    renderTrackingPanel(null);
    await loadVendors();
    restoreTrackingSession();
  } catch (error) {
    showToast(error.message || "Unable to load vendors.", "error");
    renderMenuEmpty("We could not load the menu right now. Please try again.");
  }
});

function cacheElements() {
  [
    "globalSearchInput",
    "heroVendorCount",
    "heroItemCount",
    "heroAvgPrep",
    "heroFeaturedMeta",
    "heroFeaturedVendorName",
    "heroFeaturedDescription",
    "heroFeaturedItems",
    "heroFeaturedTags",
    "heroFeaturedImage",
    "vendorsCountLabel",
    "vendorsList",
    "vendorsCarousel",
    "prevVendorsBtn",
    "nextVendorsBtn",
    "heroTrackBtn",
    "footerTrackLink",
    "footerAccountLink",
    "footerCartLink",
    "footerSavedOrdersLink",
    "scrollToCartBtn",
    "openAccountBtn",
    "openTrackingBtn",
    "backdrop",
    "vendorModal",
    "closeVendorModalBtn",
    "vendorHeroImage",
    "vendorHeroStatus",
    "vendorHeroLocation",
    "vendorTitle",
    "vendorSubtitle",
    "vendorDescription",
    "selectedVendorMeta",
    "selectedVendorItemsCount",
    "selectedVendorCategoriesCount",
    "selectedVendorAvgPrep",
    "categoriesRow",
    "itemSearchInput",
    "clearItemSearchBtn",
    "resultCountLabel",
    "menuGrid",
    "cartDrawer",
    "closeCartBtn",
    "cartItems",
    "cartVendorInfo",
    "cartItemsBadge",
    "cartCountBadge",
    "cartSummaryMeta",
    "cartTotal",
    "checkoutForm",
    "customerName",
    "customerPhone",
    "customerNotes",
    "orderResult",
    "accountDrawer",
    "closeAccountBtn",
    "trackingDrawer",
    "closeTrackingBtn",
    "refreshTrackingBtn",
    "trackingForm",
    "trackingOrderNumber",
    "trackingPhone",
    "trackOrderBtn",
    "trackingState",
    "trackingPanel",
    "trackingVendorName",
    "trackingOrderMeta",
    "trackingStatusBadge",
    "trackingSteps",
    "trackingItems",
    "trackingHistory",
    "trackingLastUpdated",
    "toastContainer"
  ].forEach((id) => {
    els[id] = document.getElementById(id);
  });
}

function bindStaticEvents() {
  els.globalSearchInput?.addEventListener("input", (event) => {
    state.globalQuery = normalizeText(event.target.value);
    renderVendors();
    syncSelectedVendorWithFilters();
    renderHeroPreview();
  });

  els.itemSearchInput?.addEventListener("input", (event) => {
    state.itemQuery = normalizeText(event.target.value);
    renderMenuItems();
  });

  els.clearItemSearchBtn?.addEventListener("click", () => {
    if (els.itemSearchInput) {
      els.itemSearchInput.value = "";
    }
    state.itemQuery = "";
    renderMenuItems();
  });

  els.prevVendorsBtn?.addEventListener("click", () => scrollVendorCarousel(-1));
  els.nextVendorsBtn?.addEventListener("click", () => scrollVendorCarousel(1));

  els.heroTrackBtn?.addEventListener("click", () => openLayer("tracking"));
  els.footerTrackLink?.addEventListener("click", () => openLayer("tracking"));
  els.openTrackingBtn?.addEventListener("click", () => openLayer("tracking"));
  els.openAccountBtn?.addEventListener("click", () => openLayer("account"));
  els.footerAccountLink?.addEventListener("click", () => openLayer("account"));
  els.scrollToCartBtn?.addEventListener("click", () => openLayer("cart"));
  els.footerCartLink?.addEventListener("click", () => openLayer("cart"));
  els.footerSavedOrdersLink?.addEventListener("click", () => {
    openLayer("account");
    document.getElementById("myOrdersCard")?.scrollIntoView({ behavior: "smooth", block: "start" });
  });

  els.closeVendorModalBtn?.addEventListener("click", closeActiveLayer);
  els.closeCartBtn?.addEventListener("click", closeActiveLayer);
  els.closeAccountBtn?.addEventListener("click", closeActiveLayer);
  els.closeTrackingBtn?.addEventListener("click", closeActiveLayer);
  els.backdrop?.addEventListener("click", closeActiveLayer);

  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape") {
      closeActiveLayer();
    }
  });

  els.vendorsList?.addEventListener("click", async (event) => {
    const card = event.target.closest("[data-vendor-id]");
    if (!card) return;

    const vendorId = Number(card.getAttribute("data-vendor-id"));
    if (!vendorId) return;

    try {
      await selectVendor(vendorId);
      openLayer("vendor");
    } catch (error) {
      showToast(error.message || "Unable to open vendor.", "error");
    }
  });

  els.categoriesRow?.addEventListener("click", (event) => {
    const chip = event.target.closest("[data-category-id]");
    if (!chip) return;

    const rawValue = chip.getAttribute("data-category-id");
    state.selectedCategoryId = rawValue === "all" ? null : Number(rawValue);
    renderCategories();
    renderMenuItems();
  });

  els.menuGrid?.addEventListener("click", (event) => {
    const button = event.target.closest("[data-action]");
    if (!button) return;

    const action = button.getAttribute("data-action");
    const itemId = Number(button.getAttribute("data-item-id"));
    if (!itemId) return;

    if (action === "qty-inc") {
      updateMenuQuantityInput(itemId, 1);
      return;
    }

    if (action === "qty-dec") {
      updateMenuQuantityInput(itemId, -1);
      return;
    }

    if (action === "add-to-cart") {
      addToCart(itemId);
    }
  });

  els.cartItems?.addEventListener("click", (event) => {
    const button = event.target.closest("[data-cart-action]");
    if (!button) return;

    const action = button.getAttribute("data-cart-action");
    const index = Number(button.getAttribute("data-index"));
    if (Number.isNaN(index)) return;

    if (action === "inc") {
      updateCartQuantity(index, 1);
      return;
    }

    if (action === "dec") {
      updateCartQuantity(index, -1);
      return;
    }

    if (action === "remove") {
      removeFromCart(index);
    }
  });
}

function scrollVendorCarousel(direction) {
  const track = els.vendorsList || els.vendorsCarousel;
  if (!track) return;
  const delta = Math.max(track.clientWidth * 0.72, 320) * direction;
  track.scrollBy({ left: delta, behavior: "smooth" });
}

function setLayerState(layerName, isOpen) {
  const id = LAYERS[layerName];
  const panel = id ? els[id] : null;
  if (!panel) return;

  panel.classList.toggle("hidden", !isOpen);
  panel.classList.toggle("active", isOpen);
  panel.setAttribute("aria-hidden", isOpen ? "false" : "true");
}

function openLayer(layerName) {
  if (!LAYERS[layerName]) return;

  Object.keys(LAYERS).forEach((name) => setLayerState(name, name === layerName));
  state.ui.activeLayer = layerName;
  els.backdrop?.classList.remove("hidden");
  document.body.classList.add("ui-open", "drawer-open");
}

function closeActiveLayer() {
  Object.keys(LAYERS).forEach((name) => setLayerState(name, false));
  state.ui.activeLayer = null;
  els.backdrop?.classList.add("hidden");
  document.body.classList.remove("ui-open", "drawer-open");
}

async function loadVendors() {
  const response = await CustomerApi.get("/vendors");
  state.vendors = Array.isArray(response.data) ? response.data : [];
  state.vendorMenusById = {};
  state.vendorSearchIndexById = {};
  state.selectedVendor = null;
  state.menu = null;
  state.selectedCategoryId = null;

  if (state.vendors.length > 0) {
    await warmVendorSearchIndex();
  }

  updateHeroStats();
  renderVendors();
  renderVendorHeader();
  renderCategories();
  renderMenuItems();
  renderHeroPreview();

  if (state.vendors.length === 0) {
    renderMenuEmpty("No vendors are available right now.");
  }
}

async function warmVendorSearchIndex() {
  await Promise.all(
    state.vendors.map(async (vendor) => {
      const vendorId = Number(vendor.id);

      try {
        const response = await CustomerApi.get(`/vendors/${vendorId}/menu`);
        const menu = response.data || { items: [], categories: [] };
        state.vendorMenusById[vendorId] = menu;
        state.vendorSearchIndexById[vendorId] = buildVendorSearchIndex(vendor, menu);
      } catch (_) {
        state.vendorSearchIndexById[vendorId] = buildVendorSearchIndex(vendor, null);
      }
    })
  );
}

async function selectVendor(vendorId) {
  const numericVendorId = Number(vendorId);
  if (!numericVendorId) {
    state.selectedVendor = null;
    state.menu = null;
    state.selectedCategoryId = null;
    renderVendorHeader();
    renderCategories();
    renderMenuItems();
    renderHeroPreview();
    return;
  }

  renderMenuSkeletons();

  let menu = state.vendorMenusById[numericVendorId];
  if (!menu) {
    const response = await CustomerApi.get(`/vendors/${numericVendorId}/menu`);
    menu = response.data || { items: [], categories: [] };
    state.vendorMenusById[numericVendorId] = menu;
  }

  const vendor = state.vendors.find((entry) => Number(entry.id) === numericVendorId) || null;
  if (vendor) {
    state.vendorSearchIndexById[numericVendorId] = buildVendorSearchIndex(vendor, menu);
  }

  state.selectedVendor = vendor;
  state.menu = menu;
  state.selectedCategoryId = null;
  state.itemQuery = "";

  if (els.itemSearchInput) {
    els.itemSearchInput.value = "";
  }

  renderVendors();
  renderVendorHeader();
  renderCategories();
  renderMenuItems();
  renderHeroPreview();
  renderCart();
}

function renderVendors() {
  const vendors = getFilteredVendors();
  if (!els.vendorsList) return;

  els.vendorsList.innerHTML = "";
  if (els.vendorsCountLabel) {
    els.vendorsCountLabel.textContent = `${vendors.length} vendor${vendors.length === 1 ? "" : "s"}`;
  }

  if (vendors.length === 0) {
    els.vendorsList.innerHTML = `<div class="empty-state-card"><strong>No vendors match your search.</strong><div class="muted">Try a different keyword.</div></div>`;
    return;
  }

  vendors.forEach((vendor, index) => {
    const isActive = Number(vendor.id) === Number(state.selectedVendor?.id);
    const menu = state.vendorMenusById[Number(vendor.id)] || { items: [], categories: [] };
    const itemCount = menu.items?.length || 0;
    const categoryCount = menu.categories?.length || 0;
    const avgPrep = calculateAveragePrep(menu.items || []);
    const coverImage = getVendorCoverImage(vendor, menu);
    const location = [vendor.locationLabel, vendor.campusArea].filter(Boolean).join(" • ") || "Campus pickup available";
    const rating = getVendorDisplayRating(vendor, menu);
    const description = vendor.description || "Fresh campus menu with pickup-friendly ordering.";

    const card = document.createElement("button");
    card.type = "button";
    card.className = `vendor-card reveal-up ${isActive ? "active" : ""}`;
    card.style.animationDelay = `${Math.min(index * 40, 200)}ms`;
    card.setAttribute("data-vendor-id", String(vendor.id));

    card.innerHTML = `
      <div class="vendor-card-cover">
        <img src="${escapeAttribute(coverImage)}" alt="${escapeAttribute(vendor.name || "Vendor")}" loading="lazy" />
        <div class="vendor-card-topline">
          <span class="vendor-card-badge">${isActive ? "Selected" : "Live menu"}</span>
          <span class="vendor-card-rating">★ ${escapeHtml(rating)}</span>
        </div>
      </div>
      <div class="vendor-card-content">
        <div class="vendor-card-copy">
          <h3>${escapeHtml(vendor.name || "Vendor")}</h3>
          <div class="vendor-location-line">${escapeHtml(location)}</div>
          <p>${escapeHtml(description)}</p>
        </div>
        <div class="vendor-card-meta">
          <span class="vendor-stat">${escapeHtml(`${itemCount} dishes`)}</span>
          <span class="vendor-stat">${escapeHtml(`${categoryCount} categories`)}</span>
          <span class="vendor-stat">${escapeHtml(avgPrep ? `${avgPrep} min prep` : "Pickup ready")}</span>
        </div>
        <div class="vendor-card-tags">
          <span class="vendor-tag">Campus ready</span>
          <span class="vendor-tag">Quick browse</span>
          <span class="vendor-tag">Student favourite</span>
        </div>
      </div>
    `;

    els.vendorsList.appendChild(card);
  });
}

function renderVendorHeader() {
  if (!state.selectedVendor || !state.menu) {
    if (els.vendorHeroImage) {
      els.vendorHeroImage.src = "https://placehold.co/1400x900/ffedd5/9a3412?text=Vendor+Menu";
    }
    if (els.vendorHeroStatus) {
      els.vendorHeroStatus.textContent = "Campus pickup";
    }
    if (els.vendorHeroLocation) {
      els.vendorHeroLocation.textContent = "Choose a vendor to begin";
    }
    if (els.vendorTitle) {
      els.vendorTitle.textContent = "Select a vendor";
    }
    if (els.vendorSubtitle) {
      els.vendorSubtitle.textContent = "";
    }
    if (els.vendorDescription) {
      els.vendorDescription.textContent = "Pick a vendor card to open a full menu experience.";
    }
    if (els.selectedVendorItemsCount) {
      els.selectedVendorItemsCount.textContent = "0";
    }
    if (els.selectedVendorCategoriesCount) {
      els.selectedVendorCategoriesCount.textContent = "0";
    }
    if (els.selectedVendorAvgPrep) {
      els.selectedVendorAvgPrep.textContent = "0 min";
    }
    if (els.selectedVendorMeta) {
      els.selectedVendorMeta.innerHTML = "";
    }
    return;
  }

  const items = state.menu.items || [];
  const categories = state.menu.categories || [];
  const avgPrep = calculateAveragePrep(items);
  const subtitle = [state.menu.locationLabel, state.menu.campusArea].filter(Boolean).join(" • ");
  const heroLocation = subtitle || [state.selectedVendor.locationLabel, state.selectedVendor.campusArea].filter(Boolean).join(" • ") || "Campus pickup";

  if (els.vendorHeroImage) {
    els.vendorHeroImage.src = getVendorCoverImage(state.selectedVendor, state.menu);
  }
  if (els.vendorHeroStatus) {
    els.vendorHeroStatus.textContent = items.length ? `${items.length} dishes available` : "Live menu";
  }
  if (els.vendorHeroLocation) {
    els.vendorHeroLocation.textContent = heroLocation;
  }
  if (els.vendorTitle) {
    els.vendorTitle.textContent = state.menu.vendorName || state.selectedVendor.name || "Vendor";
  }
  if (els.vendorSubtitle) {
    els.vendorSubtitle.textContent = heroLocation;
  }
  if (els.vendorDescription) {
    els.vendorDescription.textContent = state.selectedVendor.description || "Browse the full menu in a cleaner, more premium ordering experience.";
  }
  if (els.selectedVendorItemsCount) {
    els.selectedVendorItemsCount.textContent = String(items.length);
  }
  if (els.selectedVendorCategoriesCount) {
    els.selectedVendorCategoriesCount.textContent = String(categories.length);
  }
  if (els.selectedVendorAvgPrep) {
    els.selectedVendorAvgPrep.textContent = `${avgPrep} min`;
  }

  if (els.selectedVendorMeta) {
    els.selectedVendorMeta.innerHTML = "";
    [
      heroLocation,
      `${items.length} dishes available`,
      `${categories.length} categories`,
      avgPrep ? `Avg prep ${avgPrep} min` : "Freshly prepared"
    ].filter(Boolean).forEach((label) => {
      const span = document.createElement("span");
      span.className = "meta-pill";
      span.textContent = label;
      els.selectedVendorMeta.appendChild(span);
    });
  }
}

function renderCategories() {
  if (!els.categoriesRow) return;

  if (!state.selectedVendor || !state.menu) {
    els.categoriesRow.innerHTML = "";
    return;
  }

  const categories = state.menu.categories || [];
  els.categoriesRow.innerHTML = "";

  const allChip = document.createElement("button");
  allChip.type = "button";
  allChip.className = `category-chip ${state.selectedCategoryId === null ? "active" : ""}`;
  allChip.setAttribute("data-category-id", "all");
  allChip.textContent = "All";
  els.categoriesRow.appendChild(allChip);

  categories.forEach((category) => {
    const chip = document.createElement("button");
    chip.type = "button";
    chip.className = `category-chip ${Number(state.selectedCategoryId) === Number(category.id) ? "active" : ""}`;
    chip.setAttribute("data-category-id", String(category.id));
    chip.textContent = category.categoryName || "Category";
    els.categoriesRow.appendChild(chip);
  });
}

function renderMenuItems() {
  if (!els.menuGrid) return;

  const items = getFilteredMenuItems();
  const totalItems = state.menu?.items?.length || 0;
  els.menuGrid.innerHTML = "";

  if (!state.selectedVendor || !state.menu) {
    renderMenuEmpty("Select a vendor to see the menu.");
    if (els.resultCountLabel) {
      els.resultCountLabel.textContent = "No vendor selected.";
    }
    return;
  }

  if (els.resultCountLabel) {
    els.resultCountLabel.textContent = items.length === totalItems
      ? `Showing all ${items.length} dishes`
      : `Showing ${items.length} of ${totalItems} dishes`;
  }

  if (items.length === 0) {
    renderMenuEmpty("No menu items match your search or selected category.");
    return;
  }

  items.forEach((item, index) => {
    const isUnavailable = item.isAvailable === false;
    const imageUrl = item.primaryImageUrl || getVendorCoverImage(state.selectedVendor, state.menu, item.itemName || "Dish");
    const foodBadge = item.isVeg ? "Veg" : "Non-Veg";
    const prepLabel = item.prepTimeMinutes ? `${item.prepTimeMinutes} min prep` : "Freshly prepared";
    const category = getCategoryName(item.categoryId);

    const card = document.createElement("article");
    card.className = `menu-card reveal-up ${isUnavailable ? "unavailable" : ""}`;
    card.style.animationDelay = `${Math.min(index * 35, 180)}ms`;

    card.innerHTML = `
      <div class="menu-card-media">
        <img src="${escapeAttribute(imageUrl)}" alt="${escapeAttribute(item.itemName || "Dish")}" loading="lazy" />
        <div class="menu-card-badge-row">
          <span class="item-type-badge">${escapeHtml(foodBadge)}</span>
          <span class="availability-badge">${escapeHtml(isUnavailable ? "Unavailable" : "Pickup ready")}</span>
        </div>
      </div>
      <div class="menu-card-body">
        <div class="menu-card-head">
          <div>
            <div class="menu-card-kicker">${escapeHtml(category)}</div>
            <h3>${escapeHtml(item.itemName || "Menu Item")}</h3>
          </div>
          <div class="menu-price">${formatCurrency(item.price)}</div>
        </div>
        <p class="menu-description">${escapeHtml(item.description || "A student-friendly menu option prepared on order.")}</p>
        <div class="menu-meta-row">
          <span class="vendor-tag">${escapeHtml(prepLabel)}</span>
          <span class="vendor-tag">${escapeHtml(foodBadge)}</span>
          <span class="vendor-tag">${escapeHtml(item.trackInventory === false ? "Always available" : "Live availability")}</span>
        </div>
        <div class="menu-card-actions">
          <div class="quantity-stepper">
            <button type="button" class="qty-button" data-action="qty-dec" data-item-id="${item.id}" ${isUnavailable ? "disabled" : ""}>−</button>
            <input id="menuQty-${item.id}" type="number" min="1" value="1" ${isUnavailable ? "disabled" : ""} />
            <button type="button" class="qty-button" data-action="qty-inc" data-item-id="${item.id}" ${isUnavailable ? "disabled" : ""}>+</button>
          </div>
          <button type="button" class="primary-btn btn-full" data-action="add-to-cart" data-item-id="${item.id}" ${isUnavailable ? "disabled" : ""}>${isUnavailable ? "Unavailable" : "Add to cart"}</button>
        </div>
      </div>
    `;

    els.menuGrid.appendChild(card);
  });
}

function renderCart() {
  if (!els.cartItems) return;

  const cart = state.cart;
  els.cartItems.innerHTML = "";

  if (!cart.length) {
    if (els.cartVendorInfo) {
      els.cartVendorInfo.textContent = "Your cart is empty. Add a few dishes to get started.";
    }
    els.cartItems.innerHTML = `<div class="empty-state">No dishes added yet.</div>`;
    if (els.cartTotal) {
      els.cartTotal.textContent = "₹0.00";
    }
    if (els.cartItemsBadge) {
      els.cartItemsBadge.textContent = "0 items";
    }
    if (els.cartCountBadge) {
      els.cartCountBadge.textContent = "0";
    }
    if (els.cartSummaryMeta) {
      els.cartSummaryMeta.innerHTML = "";
    }
    return;
  }

  const itemCount = cart.reduce((sum, item) => sum + item.quantity, 0);
  const total = cart.reduce((sum, item) => sum + item.unitPrice * item.quantity, 0);
  const estimatedPrep = cart.reduce((sum, item) => sum + (Number(item.prepTimeMinutes) || 0), 0) || calculateAveragePrep(state.menu?.items || []);

  if (els.cartVendorInfo) {
    els.cartVendorInfo.textContent = `Ordering from ${cart[0].vendorName}`;
  }
  if (els.cartItemsBadge) {
    els.cartItemsBadge.textContent = `${itemCount} item${itemCount === 1 ? "" : "s"}`;
  }
  if (els.cartCountBadge) {
    els.cartCountBadge.textContent = String(itemCount);
  }
  if (els.cartTotal) {
    els.cartTotal.textContent = formatCurrency(total);
  }
  if (els.cartSummaryMeta) {
    els.cartSummaryMeta.innerHTML = `
      <span class="summary-pill">${itemCount} item${itemCount === 1 ? "" : "s"}</span>
      <span class="summary-pill">Estimated prep ${estimatedPrep} min</span>
    `;
  }

  cart.forEach((item, index) => {
    const row = document.createElement("div");
    row.className = "cart-item reveal-up";
    row.style.animationDelay = `${Math.min(index * 35, 160)}ms`;
    row.innerHTML = `
      <div class="cart-item-header">
        <div>
          <strong>${escapeHtml(item.itemName)}</strong>
          <div class="cart-item-meta">${formatCurrency(item.unitPrice)} each</div>
        </div>
        <div class="cart-item-price">${formatCurrency(item.unitPrice * item.quantity)}</div>
      </div>
      <div class="cart-item-footer">
        <div class="cart-item-qty">
          <button type="button" class="qty-button" data-cart-action="dec" data-index="${index}">−</button>
          <span>${item.quantity}</span>
          <button type="button" class="qty-button" data-cart-action="inc" data-index="${index}">+</button>
        </div>
        <button type="button" class="remove-link" data-cart-action="remove" data-index="${index}">Remove</button>
      </div>
    `;
    els.cartItems.appendChild(row);
  });
}

function renderVendorSkeletons() {
  if (!els.vendorsList) return;
  els.vendorsList.innerHTML = Array.from({ length: 4 }, (_, index) => `
    <div class="empty-state-card reveal-up" style="animation-delay:${index * 40}ms">
      <strong>Loading vendors...</strong>
      <div class="muted">Fetching live vendor menus and images.</div>
    </div>
  `).join("");
}

function renderMenuSkeletons() {
  if (!els.menuGrid) return;
  els.menuGrid.innerHTML = Array.from({ length: 4 }, (_, index) => `
    <div class="empty-state-card reveal-up" style="animation-delay:${index * 30}ms">
      <strong>Loading menu...</strong>
      <div class="muted">Preparing a premium vendor menu view.</div>
    </div>
  `).join("");
}

function renderMenuEmpty(message) {
  if (!els.menuGrid) return;
  els.menuGrid.innerHTML = `<div class="empty-state-card"><strong>${escapeHtml(message)}</strong><div class="muted">Choose another vendor or search differently.</div></div>`;
}

function updateHeroStats() {
  const vendorCount = state.vendors.length;
  const allMenus = Object.values(state.vendorMenusById);
  const menuItemCount = allMenus.reduce((sum, menu) => sum + (menu.items?.length || 0), 0);
  const prepValues = allMenus.flatMap((menu) => (menu.items || []).map((item) => Number(item.prepTimeMinutes)).filter((value) => Number.isFinite(value) && value > 0));
  const averagePrep = prepValues.length ? Math.round(prepValues.reduce((sum, value) => sum + value, 0) / prepValues.length) : 0;

  if (els.heroVendorCount) {
    els.heroVendorCount.textContent = String(vendorCount);
  }
  if (els.heroItemCount) {
    els.heroItemCount.textContent = String(menuItemCount);
  }
  if (els.heroAvgPrep) {
    els.heroAvgPrep.textContent = `${averagePrep} min`;
  }
}

function renderHeroPreview() {
  const filteredVendors = getFilteredVendors();
  const vendor = state.selectedVendor || null;
  const menu = vendor ? state.vendorMenusById[Number(vendor.id)] || state.menu || { items: [], categories: [] } : null;
  const items = (menu?.items || []).slice(0, 3);
  const categories = menu?.categories || [];

  if (els.heroFeaturedVendorName) {
    els.heroFeaturedVendorName.textContent = vendor?.name || "CampusAdda Preview";
  }
  if (els.heroFeaturedMeta) {
    els.heroFeaturedMeta.textContent = vendor
      ? ([vendor.locationLabel, vendor.campusArea].filter(Boolean).join(" • ") || "Campus pickup available")
      : "Pick any vendor card to preview its dishes here";
  }
  if (els.heroFeaturedDescription) {
    els.heroFeaturedDescription.textContent = vendor?.description || "A warmer food-app style with cleaner hierarchy, quieter drawers, and a smoother ordering flow for students.";
  }
  if (els.heroFeaturedImage) {
    els.heroFeaturedImage.src = vendor ? getVendorCoverImage(vendor, menu) : "https://placehold.co/1200x800/ffedd5/9a3412?text=CampusAdda+Preview";
  }

  if (els.heroFeaturedTags) {
    els.heroFeaturedTags.innerHTML = "";
    (vendor
      ? [
          `${menu?.items?.length || 0} dishes`,
          `${categories.length} categories`,
          `${calculateAveragePrep(menu?.items || []) || 0} min avg prep`
        ]
      : ["Warm palette", "Clean drawers", "Better cart flow"])
      .forEach((tag) => {
        const span = document.createElement("span");
        span.className = "meta-pill";
        span.textContent = tag;
        els.heroFeaturedTags.appendChild(span);
      });
  }

  if (els.heroFeaturedItems) {
    els.heroFeaturedItems.innerHTML = "";

    if (!items.length) {
      els.heroFeaturedItems.innerHTML = `
        <div class="hero-preview-item">
          <div>
            <strong>Start exploring</strong>
            <div class="muted">Select a vendor to preview dishes here.</div>
          </div>
          <div class="hero-preview-price">Live</div>
        </div>
      `;
      return;
    }

    items.forEach((item) => {
      const card = document.createElement("div");
      card.className = "hero-preview-item";
      card.innerHTML = `
        <div>
          <strong>${escapeHtml(item.itemName || "Menu Item")}</strong>
          <div class="muted">${escapeHtml(getCategoryNameFromMenu(item.categoryId, menu))} • ${escapeHtml(item.prepTimeMinutes ? `${item.prepTimeMinutes} min prep` : "Freshly prepared")}</div>
        </div>
        <div class="hero-preview-price">${formatCurrency(item.price)}</div>
      `;
      els.heroFeaturedItems.appendChild(card);
    });
  }
}

function getFilteredVendors() {
  const queryParts = state.globalQuery.split(" ").filter(Boolean);
  if (!queryParts.length) {
    return state.vendors;
  }

  return state.vendors.filter((vendor) => {
    const vendorId = Number(vendor.id);
    const haystack = state.vendorSearchIndexById[vendorId] || buildVendorSearchIndex(vendor, state.vendorMenusById[vendorId]);
    return queryParts.every((word) => haystack.includes(word));
  });
}

function buildVendorSearchIndex(vendor, menu) {
  const categoryNames = (menu?.categories || []).map((entry) => entry.categoryName);
  const itemText = (menu?.items || []).flatMap((item) => [item.itemName, item.description]);

  return normalizeText([
    vendor?.name,
    vendor?.locationLabel,
    vendor?.campusArea,
    vendor?.description,
    ...categoryNames,
    ...itemText
  ].filter(Boolean).join(" "));
}

function syncSelectedVendorWithFilters() {
  if (!state.selectedVendor) return;

  const visibleVendorIds = new Set(getFilteredVendors().map((vendor) => Number(vendor.id)));
  if (visibleVendorIds.has(Number(state.selectedVendor.id))) {
    return;
  }

  state.selectedVendor = null;
  state.menu = null;
  state.selectedCategoryId = null;
  state.itemQuery = "";
  if (els.itemSearchInput) {
    els.itemSearchInput.value = "";
  }

  renderVendorHeader();
  renderCategories();
  renderMenuItems();
}

function getFilteredMenuItems() {
  const menu = state.menu;
  if (!menu) return [];

  const queryParts = state.itemQuery.split(" ").filter(Boolean);

  return (menu.items || []).filter((item) => {
    const matchesCategory = state.selectedCategoryId === null || Number(item.categoryId) === Number(state.selectedCategoryId);
    if (!matchesCategory) {
      return false;
    }

    if (!queryParts.length) {
      return true;
    }

    const haystack = normalizeText([
      item.itemName,
      item.description,
      getCategoryName(item.categoryId)
    ].filter(Boolean).join(" "));

    return queryParts.every((word) => haystack.includes(word));
  });
}

function getCategoryName(categoryId) {
  return getCategoryNameFromMenu(categoryId, state.menu);
}

function getCategoryNameFromMenu(categoryId, menu) {
  const category = (menu?.categories || []).find((entry) => Number(entry.id) === Number(categoryId));
  return category?.categoryName || "Menu";
}

function updateMenuQuantityInput(itemId, delta) {
  const input = document.getElementById(`menuQty-${itemId}`);
  if (!input) return;
  const current = Math.max(1, Number(input.value) || 1);
  input.value = String(Math.max(1, current + delta));
}

function addToCart(menuItemId) {
  hideOrderResult();
  const item = (state.menu?.items || []).find((entry) => Number(entry.id) === Number(menuItemId));
  if (!item || !state.menu) return;

  const qtyInput = document.getElementById(`menuQty-${menuItemId}`);
  const quantity = Math.max(1, Number(qtyInput?.value || 1));

  if (state.cart.length > 0 && Number(state.cart[0].vendorId) !== Number(state.menu.vendorId)) {
    showToast("You can only order from one vendor at a time. Clear the cart or stay with the same vendor.", "info");
    return;
  }

  const existing = state.cart.find((row) => Number(row.menuItemId) === Number(menuItemId));
  if (existing) {
    existing.quantity += quantity;
  } else {
    state.cart.push({
      vendorId: state.menu.vendorId,
      vendorName: state.menu.vendorName,
      menuItemId: item.id,
      itemName: item.itemName,
      unitPrice: Number(item.price) || 0,
      quantity,
      prepTimeMinutes: Number(item.prepTimeMinutes) || 0
    });
  }

  if (qtyInput) {
    qtyInput.value = "1";
  }

  renderCart();
  showToast(`${item.itemName} added to cart`, "success");
}

function updateCartQuantity(index, delta) {
  hideOrderResult();
  const item = state.cart[index];
  if (!item) return;

  item.quantity += delta;
  if (item.quantity <= 0) {
    state.cart.splice(index, 1);
  }

  renderCart();
}

function removeFromCart(index) {
  hideOrderResult();
  const removed = state.cart[index];
  state.cart.splice(index, 1);
  renderCart();

  if (removed) {
    showToast(`${removed.itemName} removed`, "info");
  }
}

function bindCheckout() {
  els.checkoutForm?.addEventListener("submit", async (event) => {
    event.preventDefault();

    if (!state.cart.length) {
      showToast("Add at least one dish before placing the order.", "info");
      return;
    }

    const payload = {
      vendorId: state.cart[0].vendorId,
      customerName: els.customerName?.value.trim() || "",
      customerPhone: els.customerPhone?.value.trim() || "",
      notes: els.customerNotes?.value.trim() || "",
      items: state.cart.map((item) => ({
        menuItemId: item.menuItemId,
        quantity: item.quantity,
        specialInstructions: null
      }))
    };

    if (!payload.customerName || !payload.customerPhone) {
      showToast("Name and phone number are required.", "info");
      return;
    }

    const submitButton = els.checkoutForm.querySelector("button[type='submit']");
    const originalLabel = submitButton?.textContent || "Place Order";

    try {
      if (submitButton) {
        submitButton.disabled = true;
        submitButton.textContent = "Placing order...";
      }

      const response = await CustomerApi.post("/orders", payload);
      const order = response.data || {};
      const totalAmount = order.totalAmount || state.cart.reduce((sum, item) => sum + item.unitPrice * item.quantity, 0);

      if (els.orderResult) {
        els.orderResult.classList.remove("hidden");
        els.orderResult.innerHTML = `
          <strong>Order placed successfully.</strong><br>
          Order Number: ${escapeHtml(order.orderNumber || "-")}<br>
          Vendor: ${escapeHtml(order.vendorName || state.cart[0]?.vendorName || "-")}<br>
          Status: ${escapeHtml(order.status || "CREATED")}<br>
          Total: ${formatCurrency(totalAmount)}<br><br>
          <span class="muted">Tracking is ready in the tracking drawer.</span>
        `;
      }

      if (typeof window.refreshCustomerAccountData === "function") {
        await window.refreshCustomerAccountData();
      }

      const trackingPhone = payload.customerPhone;
      const trackingOrderNumber = order.orderNumber || "";

      if (els.trackingOrderNumber) {
        els.trackingOrderNumber.value = trackingOrderNumber;
      }
      if (els.trackingPhone) {
        els.trackingPhone.value = trackingPhone;
      }

      persistTrackingSession(trackingOrderNumber, trackingPhone);

      state.cart = [];
      renderCart();
      els.checkoutForm.reset();

      if (typeof CustomerAuth !== "undefined" && typeof CustomerAuth.getStoredUser === "function") {
        const storedUser = CustomerAuth.getStoredUser();
        if (storedUser) {
          if (els.customerName) els.customerName.value = storedUser.fullName || "";
          if (els.customerPhone) els.customerPhone.value = storedUser.phone || "";
        }
      }

      await trackOrder({
        orderNumber: trackingOrderNumber,
        phone: trackingPhone,
        showToastOnSuccess: false,
        showToastOnError: true,
        startPolling: true
      });

      openLayer("tracking");
      showToast("Order placed successfully", "success");
    } catch (error) {
      showToast(error.message || "Failed to place order.", "error");
    } finally {
      if (submitButton) {
        submitButton.disabled = false;
        submitButton.textContent = originalLabel;
      }
    }
  });
}

function bindTracking() {
  els.trackingForm?.addEventListener("submit", async (event) => {
    event.preventDefault();

    await trackOrder({
      orderNumber: els.trackingOrderNumber?.value || "",
      phone: els.trackingPhone?.value || "",
      showToastOnSuccess: true,
      showToastOnError: true,
      startPolling: true
    });
  });

  els.refreshTrackingBtn?.addEventListener("click", async () => {
    const orderNumber = els.trackingOrderNumber?.value || state.tracking.orderNumber;
    const phone = els.trackingPhone?.value || state.tracking.phone;

    if (!String(orderNumber).trim() || !String(phone).trim()) {
      showToast("Enter order number and phone number first.", "info");
      return;
    }

    await trackOrder({
      orderNumber,
      phone,
      showToastOnSuccess: true,
      showToastOnError: true,
      startPolling: true
    });
  });
}

function restoreTrackingSession() {
  stopTrackingAutoRefresh();

  let saved = null;
  try {
    const raw = window.localStorage.getItem(TRACKING_STORAGE_KEY);
    saved = raw ? JSON.parse(raw) : null;
  } catch (_) {
    saved = null;
  }

  if (!saved?.orderNumber || !saved?.phone) {
    state.tracking.orderNumber = "";
    state.tracking.phone = "";
    state.tracking.data = null;
    if (els.trackingOrderNumber) els.trackingOrderNumber.value = "";
    if (els.trackingPhone) els.trackingPhone.value = "";
    renderTrackingPanel(null);
    renderTrackingState("Enter your order number and phone number to see live status updates.");
    return;
  }

  state.tracking.orderNumber = saved.orderNumber;
  state.tracking.phone = saved.phone;

  if (els.trackingOrderNumber) els.trackingOrderNumber.value = saved.orderNumber;
  if (els.trackingPhone) els.trackingPhone.value = saved.phone;

  trackOrder({
    orderNumber: saved.orderNumber,
    phone: saved.phone,
    showToastOnSuccess: false,
    showToastOnError: false,
    startPolling: true
  });
}

async function trackOrder({
  orderNumber,
  phone,
  showToastOnSuccess = false,
  showToastOnError = true,
  startPolling = true
}) {
  const normalizedOrderNumber = String(orderNumber || "").trim();
  const normalizedPhone = String(phone || "").trim();

  if (!normalizedOrderNumber || !normalizedPhone) {
    renderTrackingState("Enter your order number and phone number to see live status updates.");
    renderTrackingPanel(null);
    stopTrackingAutoRefresh();
    return;
  }

  const originalButtonLabel = els.trackOrderBtn?.textContent || "Track Order";

  try {
    if (els.trackOrderBtn) {
      els.trackOrderBtn.disabled = true;
      els.trackOrderBtn.textContent = "Tracking...";
    }

    renderTrackingState("Fetching latest order status...");

    const path = `/orders/track?orderNumber=${encodeURIComponent(normalizedOrderNumber)}&phone=${encodeURIComponent(normalizedPhone)}`;
    const response = await CustomerApi.get(path);
    const trackingData = response.data || null;

    state.tracking.orderNumber = normalizedOrderNumber;
    state.tracking.phone = normalizedPhone;
    state.tracking.data = trackingData;

    persistTrackingSession(normalizedOrderNumber, normalizedPhone);
    renderTrackingPanel(trackingData);

    if (startPolling) {
      startTrackingAutoRefresh();
    }

    if (showToastOnSuccess) {
      showToast("Tracking refreshed", "success");
    }
  } catch (error) {
    state.tracking.data = null;
    renderTrackingPanel(null);
    renderTrackingState(error.message || "Failed to fetch order status.");
    stopTrackingAutoRefresh();

    if (showToastOnError) {
      showToast(error.message || "Failed to fetch order status.", "error");
    }
  } finally {
    if (els.trackOrderBtn) {
      els.trackOrderBtn.disabled = false;
      els.trackOrderBtn.textContent = originalButtonLabel;
    }
  }
}

function startTrackingAutoRefresh() {
  stopTrackingAutoRefresh();

  if (!state.tracking.orderNumber || !state.tracking.phone) {
    return;
  }

  const terminalStatuses = ["COMPLETED", "CANCELLED"];
  if (terminalStatuses.includes(String(state.tracking.data?.status || "").toUpperCase())) {
    return;
  }

  state.tracking.autoRefreshTimer = window.setInterval(async () => {
    await trackOrder({
      orderNumber: state.tracking.orderNumber,
      phone: state.tracking.phone,
      showToastOnSuccess: false,
      showToastOnError: false,
      startPolling: false
    });
  }, TRACKING_POLL_INTERVAL_MS);
}

function stopTrackingAutoRefresh() {
  if (!state.tracking.autoRefreshTimer) return;
  window.clearInterval(state.tracking.autoRefreshTimer);
  state.tracking.autoRefreshTimer = null;
}

function persistTrackingSession(orderNumber, phone) {
  try {
    if (!orderNumber || !phone) {
      window.localStorage.removeItem(TRACKING_STORAGE_KEY);
      return;
    }
    window.localStorage.setItem(TRACKING_STORAGE_KEY, JSON.stringify({ orderNumber, phone }));
  } catch (_) {
    // ignore storage issues
  }
}

function renderTrackingState(message) {
  if (!els.trackingState) return;
  els.trackingState.textContent = message || "";
}

function renderTrackingPanel(order) {
  if (!els.trackingPanel) return;

  if (!order) {
    els.trackingPanel.classList.add("hidden");
    if (els.trackingVendorName) els.trackingVendorName.textContent = "Vendor";
    if (els.trackingOrderMeta) els.trackingOrderMeta.innerHTML = "";
    if (els.trackingStatusBadge) {
      els.trackingStatusBadge.textContent = "TRACKING";
      els.trackingStatusBadge.className = "tracking-status-badge";
    }
    if (els.trackingSteps) els.trackingSteps.innerHTML = "";
    if (els.trackingItems) els.trackingItems.innerHTML = "";
    if (els.trackingHistory) els.trackingHistory.innerHTML = "";
    if (els.trackingLastUpdated) els.trackingLastUpdated.textContent = "";
    return;
  }

  els.trackingPanel.classList.remove("hidden");
  renderTrackingState("Live tracking active. Status refreshes automatically.");

  if (els.trackingVendorName) {
    els.trackingVendorName.textContent = order.vendorName || "Vendor";
  }
  if (els.trackingOrderMeta) {
    const metaParts = [
      `Order: ${escapeHtml(order.orderNumber || "-")}`,
      `Total: ${formatCurrency(order.totalAmount || 0)}`,
      order.placedAt ? `Placed: ${escapeHtml(formatDateTimeSafe(order.placedAt))}` : ""
    ].filter(Boolean);

    els.trackingOrderMeta.innerHTML = metaParts.map((part) => `<span class="tracking-meta-pill">${part}</span>`).join("");
  }
  if (els.trackingStatusBadge) {
    const normalizedStatus = String(order.status || "CREATED").toUpperCase();
    els.trackingStatusBadge.textContent = statusLabel(normalizedStatus);
    els.trackingStatusBadge.className = `tracking-status-badge status-${normalizedStatus.toLowerCase()}`;
  }

  renderTrackingSteps(order);
  renderTrackingItems(order.items || []);
  renderTrackingHistory(order.statusHistory || []);

  if (els.trackingLastUpdated) {
    els.trackingLastUpdated.textContent = `Last updated: ${formatDateTimeSafe(new Date().toISOString())}`;
  }

  const terminalStatuses = ["COMPLETED", "CANCELLED"];
  if (terminalStatuses.includes(String(order.status || "").toUpperCase())) {
    stopTrackingAutoRefresh();
  }
}

function renderTrackingSteps(order) {
  if (!els.trackingSteps) return;

  const currentStatus = String(order.status || "CREATED").toUpperCase();
  const flow = ["CREATED", "ACCEPTED", "PREPARING", "READY", "COMPLETED"];

  if (currentStatus === "CANCELLED") {
    els.trackingSteps.innerHTML = `
      <div class="tracking-step done">
        <span class="tracking-step-dot"></span>
        <span class="tracking-step-label">Created</span>
      </div>
      <div class="tracking-step cancelled active">
        <span class="tracking-step-dot"></span>
        <span class="tracking-step-label">Cancelled</span>
      </div>
    `;
    return;
  }

  const currentIndex = flow.indexOf(currentStatus);
  els.trackingSteps.innerHTML = flow.map((status, index) => {
    let className = "tracking-step";
    if (index < currentIndex) className += " done";
    if (index === currentIndex) className += " active";

    return `
      <div class="${className}">
        <span class="tracking-step-dot"></span>
        <span class="tracking-step-label">${statusLabel(status)}</span>
      </div>
    `;
  }).join("");
}

function renderTrackingItems(items) {
  if (!els.trackingItems) return;

  if (!items.length) {
    els.trackingItems.innerHTML = `<div class="tracking-empty-row">No order items found.</div>`;
    return;
  }

  els.trackingItems.innerHTML = items.map((item) => `
    <div class="tracking-item-card">
      <div>
        <strong>${escapeHtml(item.itemName || "-")}</strong>
        <div class="muted">Qty ${escapeHtml(item.quantity ?? "-")} • ${formatCurrency(item.unitPrice || 0)} each</div>
        ${item.specialInstructions ? `<div class="tracking-item-note">Note: ${escapeHtml(item.specialInstructions)}</div>` : ""}
      </div>
      <div class="tracking-item-total">${formatCurrency(item.lineTotal || 0)}</div>
    </div>
  `).join("");
}

function renderTrackingHistory(history) {
  if (!els.trackingHistory) return;

  if (!history.length) {
    els.trackingHistory.innerHTML = `<div class="tracking-empty-row">No tracking history yet.</div>`;
    return;
  }

  els.trackingHistory.innerHTML = history.map((entry) => `
    <div class="tracking-history-row">
      <div class="tracking-history-dot"></div>
      <div class="tracking-history-content">
        <strong>${escapeHtml(statusLabel(entry.toStatus || "-"))}</strong>
        <div class="muted">${escapeHtml(entry.remarks || "Status updated")}</div>
        <div class="tracking-last-updated">${escapeHtml(formatDateTimeSafe(entry.changedAt))}</div>
      </div>
    </div>
  `).join("");
}

function statusLabel(status) {
  const value = String(status || "").toUpperCase();

  switch (value) {
    case "CREATED":
      return "Created";
    case "ACCEPTED":
      return "Accepted";
    case "PREPARING":
      return "Preparing";
    case "READY":
      return "Ready";
    case "COMPLETED":
      return "Completed";
    case "CANCELLED":
      return "Cancelled";
    default:
      return value || "-";
  }
}

function formatDateTimeSafe(value) {
  if (!value) return "-";
  const date = value instanceof Date ? value : new Date(value);
  if (Number.isNaN(date.getTime())) return String(value);

  return date.toLocaleString("en-IN", {
    day: "numeric",
    month: "short",
    year: "numeric",
    hour: "numeric",
    minute: "2-digit"
  });
}

function hideOrderResult() {
  els.orderResult?.classList.add("hidden");
}

function showToast(message, type = "info") {
  if (!els.toastContainer) return;
  const toast = document.createElement("div");
  toast.className = `toast ${type}`;
  toast.textContent = message;
  els.toastContainer.appendChild(toast);

  window.setTimeout(() => {
    toast.style.opacity = "0";
    toast.style.transform = "translateY(-8px)";
    window.setTimeout(() => toast.remove(), 220);
  }, 2800);
}

function calculateAveragePrep(items) {
  const values = (items || []).map((item) => Number(item.prepTimeMinutes)).filter((value) => Number.isFinite(value) && value > 0);
  if (!values.length) return 0;
  return Math.round(values.reduce((sum, value) => sum + value, 0) / values.length);
}

function formatCurrency(value) {
  const amount = Number(value) || 0;
  return `₹${amount.toFixed(2)}`;
}

function normalizeText(value) {
  return String(value || "").toLowerCase().trim().replace(/\s+/g, " ");
}

function escapeHtml(value) {
  return String(value ?? "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/\"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

function escapeAttribute(value) {
  return escapeHtml(value).replace(/`/g, "&#96;");
}

function getVendorDisplayRating(vendor, menu) {
  const itemCount = menu?.items?.length || 0;
  const categoryCount = menu?.categories?.length || 0;
  const base = 3.9 + Math.min((itemCount * 0.03) + (categoryCount * 0.05), 0.9);
  return base.toFixed(1);
}

function getVendorCoverImage(vendor, menu, fallbackText = "CampusAdda") {
  const directImage = vendor?.coverImageUrl || vendor?.imageUrl || vendor?.primaryImageUrl || vendor?.bannerImageUrl || vendor?.photoUrl || vendor?.logoUrl;
  if (directImage) return directImage;

  const firstMenuImage = (menu?.items || []).map((item) => item.primaryImageUrl).find(Boolean);
  if (firstMenuImage) return firstMenuImage;

  const text = vendor?.name || fallbackText;
  return `https://placehold.co/1200x800/ffedd5/9a3412?text=${encodeURIComponent(text)}`;
}

window.openCartDrawer = () => openLayer("cart");
window.openAccountDrawer = () => openLayer("account");
window.openTrackingDrawer = () => openLayer("tracking");
window.closeUiLayers = closeActiveLayer;
window.showToastMessage = showToast;
window.renderTrackingPanel = renderTrackingPanel;
window.renderTrackingState = renderTrackingState;
window.trackCustomerOrder = trackOrder;
window.statusLabel = statusLabel;
