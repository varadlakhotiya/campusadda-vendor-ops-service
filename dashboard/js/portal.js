const Portal = {
  context: null,

  async loadContext() {
    if (this.context) return this.context;

    const response = await Api.get("/portal/context");
    this.context = response.data;
    return this.context;
  },

  async getContext() {
    return this.loadContext();
  },

  async isAdmin() {
    const ctx = await this.loadContext();
    return !!ctx.adminUser;
  },

  async isVendorUser() {
    const ctx = await this.loadContext();
    return !!ctx.vendorUser && !ctx.adminUser;
  },

  async getPrimaryVendorId() {
    const ctx = await this.loadContext();
    return ctx.primaryVendorId;
  },

  async getPrimaryVendorName() {
    const ctx = await this.loadContext();
    return ctx.primaryVendorName;
  },

  clear() {
    this.context = null;
  }
};