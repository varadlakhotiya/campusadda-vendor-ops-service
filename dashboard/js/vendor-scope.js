const VendorScope = {
  async initVendorSelect(selectId, loadFn) {
    const select = document.getElementById(selectId);
    if (!select) return null;

    const ctx = await Portal.getContext();

    if (ctx.adminUser) {
      const vendorsResponse = await Api.get("/vendors");
      const vendors = vendorsResponse.data || [];

      Utils.populateSelect(selectId, vendors, "id", "name", "Select Vendor");
      select.disabled = false;
      select.parentElement.style.display = "";

      if (loadFn) {
        select.addEventListener("change", loadFn);

        if (vendors.length === 1) {
          select.value = vendors[0].id;
          await loadFn();
        }
      }

      return null;
    }

    if (ctx.vendorUser && ctx.primaryVendorId) {
      select.innerHTML = `<option value="${ctx.primaryVendorId}">${ctx.primaryVendorName}</option>`;
      select.value = String(ctx.primaryVendorId);
      select.disabled = true;
      select.parentElement.style.display = "none";

      if (loadFn) {
        await loadFn();
      }

      return ctx.primaryVendorId;
    }

    select.innerHTML = `<option value="">No vendor assigned</option>`;
    select.disabled = true;
    select.parentElement.style.display = "none";
    return null;
  },

  async getVendorId(selectId = "vendorSelect") {
    const select = document.getElementById(selectId);
    if (!select) return null;
    return select.value ? Number(select.value) : null;
  }
};