const state = {
    customers: [],
    tiers: [],
    purchases: []
};

const $ = (selector) => document.querySelector(selector);
const money = (value) => `₹${Number(value || 0).toFixed(2)}`;
const number = (value) => Number(value || 0).toLocaleString('en-IN');

async function apiRequest(url, options = {}) {
    const response = await fetch(url, {
        headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
        ...options
    });
    const contentType = response.headers.get('content-type') || '';
    const body = contentType.includes('application/json') ? await response.json() : await response.text();
    if (!response.ok) {
        throw new Error(body.message || body || 'The request could not be completed.');
    }
    return body;
}

function showMessage(element, text, isError = false) {
    const target = typeof element === 'string' ? $(element) : element;
    target.textContent = text;
    target.classList.toggle('error', isError);
}

function customerName(customerId) {
    const customer = state.customers.find((item) => item.id === Number(customerId));
    return customer ? customer.name : 'Customer';
}

function renderTier(tier) {
    return `<article class="tier-item"><h3>${escapeHtml(tier.name)}</h3><div class="tier-detail"><span>Minimum Points</span><strong>${number(tier.minimumPoints)}</strong></div><div class="tier-detail"><span>Discount</span><strong class="discount-text">${tier.discountPercentage}%</strong></div></article>`;
}

function renderCustomerOptions() {
    const selects = ['#purchase-customer', '#redeem-customer', '#history-customer'];
    selects.forEach((selector) => {
        const select = $(selector);
        const selected = select.value;
        select.innerHTML = '<option value="">Select customer</option>' + state.customers
            .map((customer) => `<option value="${customer.id}">${escapeHtml(customer.name)} (${escapeHtml(customer.email)})</option>`).join('');
        if (state.customers.some((customer) => String(customer.id) === selected)) select.value = selected;
    });
}

function renderCustomers() {
    const table = $('#customers-table');
    $('#customer-count').textContent = `${state.customers.length} customer${state.customers.length === 1 ? '' : 's'}`;
    $('#total-customers').textContent = state.customers.length;
    if (!state.customers.length) {
        table.innerHTML = '<tr><td colspan="5" class="muted">No customers have been created yet.</td></tr>';
        return;
    }
    table.innerHTML = state.customers.map((customer) => `<tr><td>${escapeHtml(customer.name)}</td><td>${escapeHtml(customer.email)}</td><td>${number(customer.availablePoints)}</td><td>${number(customer.lifetimePoints)}</td><td>${escapeHtml(customer.tier.name)}</td></tr>`).join('');
}

function renderTiers() {
    $('#total-tiers').textContent = state.tiers.length;
    const cards = state.tiers.map(renderTier).join('');
    $('#dashboard-tiers').innerHTML = cards || '<p class="muted">No tiers available.</p>';
    $('#tiers-list').innerHTML = cards || '<p class="muted">No tiers available.</p>';
}

async function loadCustomers() {
    state.customers = await apiRequest('/api/customers');
    renderCustomers();
    renderCustomerOptions();
}

async function loadTiers() {
    state.tiers = await apiRequest('/api/tiers');
    renderTiers();
}

async function loadDashboardPurchases() {
    // Purchase records have no list endpoint, so this count represents purchases recorded in this page session.
    $('#total-purchases').textContent = state.purchases.length;
    $('#total-points').textContent = number(state.purchases.reduce((total, purchase) => total + Number(purchase.pointsEarned || 0), 0));
}

function showSelectedCustomerInfo(customerId, targetSelector) {
    const target = $(targetSelector);
    const customer = state.customers.find((item) => item.id === Number(customerId));
    if (!customer) {
        target.className = targetSelector === '#purchase-customer-info' ? 'info-box muted' : 'info-box';
        target.innerHTML = targetSelector === '#purchase-customer-info' ? 'Select a customer to view their current tier.' : 'Available Points: <strong>--</strong>';
        return;
    }
    if (targetSelector === '#purchase-customer-info') {
        target.innerHTML = `Current Tier: <strong>${escapeHtml(customer.tier.name)}</strong><br><span class="discount-text">Tier Discount: ${customer.tier.discountPercentage}%</span><br>Available Points: ${number(customer.availablePoints)}`;
    } else {
        target.innerHTML = `Available Points: <strong>${number(customer.availablePoints)}</strong>`;
    }
}

async function createCustomer(event) {
    event.preventDefault();
    showMessage('#customer-message', 'Creating...');
    try {
        const customer = await apiRequest('/api/customers', { method: 'POST', body: JSON.stringify({ name: $('#customer-name').value.trim(), email: $('#customer-email').value.trim() }) });
        state.customers.push(customer);
        renderCustomers();
        renderCustomerOptions();
        event.target.reset();
        showMessage('#customer-message', 'Customer created successfully.');
    } catch (error) {
        showMessage('#customer-message', error.message, true);
    }
}

async function makePurchase(event) {
    event.preventDefault();
    const customerId = $('#purchase-customer').value;
    showMessage('#purchase-message', 'Saving purchase...');
    try {
        const purchase = await apiRequest(`/api/purchases/${customerId}`, { method: 'POST', body: JSON.stringify({ purchaseAmount: Number($('#purchase-amount').value) }) });
        state.purchases.push(purchase);
        $('#result-purchase').textContent = money(purchase.purchaseAmount);
        $('#result-discount').textContent = money(purchase.discountAmount);
        $('#result-final').textContent = money(purchase.finalAmount);
        $('#result-points').textContent = number(purchase.pointsEarned);
        $('#purchase-result').classList.remove('hidden');
        showMessage('#purchase-message', 'Purchase saved successfully.');
        await loadCustomers();
        $('#purchase-customer').value = customerId;
        showSelectedCustomerInfo(customerId, '#purchase-customer-info');
        await loadDashboardPurchases();
    } catch (error) {
        showMessage('#purchase-message', error.message, true);
    }
}

async function redeemPoints(event) {
    event.preventDefault();
    const customerId = $('#redeem-customer').value;
    showMessage('#redeem-message', 'Redeeming...');
    try {
        await apiRequest(`/api/points/redeem/${customerId}`, { method: 'POST', body: JSON.stringify({ points: Number($('#redeem-points').value) }) });
        showMessage('#redeem-message', 'Points redeemed successfully.');
        $('#redeem-points').value = '';
        await loadCustomers();
        $('#redeem-customer').value = customerId;
        showSelectedCustomerInfo(customerId, '#redeem-available');
    } catch (error) {
        showMessage('#redeem-message', error.message.toLowerCase().includes('insufficient') ? 'Insufficient points.' : error.message, true);
    }
}

async function loadPointsHistory(customerId) {
    const table = $('#history-table');
    if (!customerId) {
        table.innerHTML = '<tr><td colspan="5" class="muted">Select a customer to load history.</td></tr>';
        $('#history-status').textContent = 'No customer selected';
        return;
    }
    table.innerHTML = '<tr><td colspan="5" class="loading">Loading...</td></tr>';
    try {
        const history = await apiRequest(`/api/points/${customerId}/history`);
        $('#history-status').textContent = `${history.length} transaction${history.length === 1 ? '' : 's'}`;
        table.innerHTML = history.length ? history.map((entry) => `<tr><td>${formatDate(entry.createdAt)}</td><td>${escapeHtml(customerName(customerId))}</td><td>${escapeHtml(entry.type)}</td><td>${number(entry.points)}</td><td>${escapeHtml(entry.description || '')}</td></tr>`).join('') : '<tr><td colspan="5" class="muted">No point transactions found.</td></tr>';
    } catch (error) {
        table.innerHTML = `<tr><td colspan="5" class="error">${escapeHtml(error.message)}</td></tr>`;
    }
}

function formatDate(value) {
    return value ? new Date(value).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' }) : '-';
}

function escapeHtml(value) {
    return String(value ?? '').replace(/[&<>'"]/g, (character) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' }[character]));
}

function setupNavigation() {
    document.querySelectorAll('.nav-link').forEach((link) => link.addEventListener('click', () => {
        document.querySelectorAll('.nav-link').forEach((item) => item.classList.remove('active'));
        document.querySelectorAll('.page-section').forEach((section) => section.classList.remove('active-section'));
        link.classList.add('active');
        $(`#${link.dataset.section}`).classList.add('active-section');
    }));
}

function setupEvents() {
    setupNavigation();
    $('#show-customer-form').addEventListener('click', () => $('#customer-form-panel').classList.remove('hidden'));
    $('#hide-customer-form').addEventListener('click', () => $('#customer-form-panel').classList.add('hidden'));
    $('#customer-form').addEventListener('submit', createCustomer);
    $('#purchase-form').addEventListener('submit', makePurchase);
    $('#redeem-form').addEventListener('submit', redeemPoints);
    $('#purchase-customer').addEventListener('change', (event) => showSelectedCustomerInfo(event.target.value, '#purchase-customer-info'));
    $('#redeem-customer').addEventListener('change', (event) => showSelectedCustomerInfo(event.target.value, '#redeem-available'));
    $('#history-customer').addEventListener('change', (event) => loadPointsHistory(event.target.value));
}

async function loadApplication() {
    try {
        await Promise.all([loadCustomers(), loadTiers()]);
        await loadDashboardPurchases();
    } catch (error) {
        showMessage('#global-message', `Could not load application data: ${error.message}`, true);
    }
}

setupEvents();
loadApplication();
