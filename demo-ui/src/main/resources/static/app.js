// ---------- helpers ----------

function esc(value) {
    const div = document.createElement('div');
    div.textContent = value ?? '';
    return div.innerHTML;
}

async function fetchJson(url, options) {
    const res = await fetch(url, options);
    if (!res.ok) {
        let message = `HTTP ${res.status}`;
        try {
            const body = await res.json();
            if (body.error) message = body.error;
            if (body.detail) message += ` — ${body.detail}`;
        } catch (ignored) { /* non-JSON error body */ }
        if (res.status === 428) message = 'BETA credentials not configured — see Settings';
        throw new Error(message);
    }
    if (res.status === 204) return null;
    return res.json();
}

// Options arrive as a single string; BETA delivers them newline-delimited
// (verified the hard way in ZAF #7). Comma is a fallback only.
function parseOptions(options) {
    if (!options) return [];
    const byNewline = options.split('\n').map(s => s.trim()).filter(Boolean);
    if (byNewline.length > 1) return byNewline;
    return options.split(',').map(s => s.trim()).filter(Boolean);
}

function chip(text, extraClass) {
    return `<span class="chip ${extraClass ?? ''}">${esc(text)}</span>`;
}

function statusChip(status) {
    return chip(status ?? '?', 'st-' + String(status ?? '').toLowerCase());
}

// ---------- state ----------

const state = {
    me: null,                 // UserContextDto once credentials work
    requests: [],
    selectedPartner: null,
    formTemplate: null,
    currentCaseToken: null,
    currentWebhookId: null,
};

// ---------- tabs ----------

document.querySelectorAll('.tab').forEach(tab => {
    tab.addEventListener('click', () => showView(tab.dataset.view));
});

function showView(name) {
    document.querySelectorAll('.view').forEach(v => v.classList.add('hidden'));
    document.getElementById(`view-${name}`).classList.remove('hidden');
    document.querySelectorAll('.tab').forEach(t =>
        t.classList.toggle('active', t.dataset.view === name));
    if (name === 'dashboard') loadRequests();
    if (name === 'webhooks') loadWebhooks();
}

// ---------- identity ----------

async function refreshIdentity() {
    const badge = document.getElementById('auth-badge');
    try {
        const settings = await fetchJson('/api/settings');
        if (!settings.configured) {
            state.me = null;
            badge.textContent = 'Not configured';
            badge.className = 'badge not-configured';
            return;
        }
        badge.textContent = 'Connecting...';
        badge.className = 'badge';
        const me = await fetchJson('/api/me');
        state.me = me;
        badge.textContent = `${me.companyName ?? 'Unknown company'} — ${me.email ?? me.username ?? ''}`;
        badge.className = 'badge configured';
    } catch (err) {
        state.me = null;
        badge.textContent = `Auth failed: ${err.message}`;
        badge.className = 'badge not-configured';
    }
}

function directionOf(request) {
    if (!state.me?.companyId) return null;
    if (request.submitCompanyId === state.me.companyId) return 'outbound';
    if (request.receiveCompanyId === state.me.companyId) return 'inbound';
    return null;
}

function partnerNameOf(request) {
    const dir = directionOf(request);
    if (dir === 'outbound') return request.receiveCompanyName;
    if (dir === 'inbound') return request.submitCompanyName;
    return `${request.submitCompanyName ?? '?'} → ${request.receiveCompanyName ?? '?'}`;
}

// ---------- dashboard ----------

async function loadRequests() {
    const tbody = document.querySelector('#requests-table tbody');
    tbody.innerHTML = '<tr><td colspan="6">Loading...</td></tr>';
    try {
        if (!state.me) await refreshIdentity();
        state.requests = await fetchJson('/api/requests');
        renderRequests();
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="6">Failed to load: ${esc(err.message)}</td></tr>`;
    }
}

function renderRequests() {
    const tbody = document.querySelector('#requests-table tbody');
    const filter = document.getElementById('direction-filter').value;
    const rows = state.requests.filter(r => {
        if (filter === 'all') return true;
        return directionOf(r) === filter;
    });
    if (rows.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6">No cases found.</td></tr>';
        return;
    }
    tbody.innerHTML = '';
    for (const r of rows) {
        const dir = directionOf(r);
        const tr = document.createElement('tr');
        tr.className = 'clickable';
        tr.innerHTML = `
            <td>${esc(r.id)}</td>
            <td>${dir ? chip(dir, dir) : ''}</td>
            <td>${statusChip(r.status)}</td>
            <td>${esc(r.summary)}</td>
            <td>${esc(partnerNameOf(r))}</td>
            <td>${esc(r.updatedAt)}</td>
        `;
        tr.addEventListener('click', () => openCase(r.token));
        tbody.appendChild(tr);
    }
}

document.getElementById('refresh-btn').addEventListener('click', loadRequests);
document.getElementById('direction-filter').addEventListener('change', renderRequests);

// ---------- case detail ----------

document.getElementById('back-to-dashboard').addEventListener('click', () => showView('dashboard'));

async function openCase(token) {
    state.currentCaseToken = token;
    document.querySelectorAll('.view').forEach(v => v.classList.add('hidden'));
    document.getElementById('view-case').classList.remove('hidden');
    document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
    document.getElementById('case-title').textContent = 'Loading case...';
    document.getElementById('case-summary').innerHTML = '';
    document.getElementById('case-notes').innerHTML = '';
    document.getElementById('case-responses').innerHTML = '';
    document.getElementById('case-actions').innerHTML = '';
    document.getElementById('action-form-host').innerHTML = '';
    document.getElementById('case-action-status').textContent = '';
    document.getElementById('attach-config').textContent = '';
    document.getElementById('attach-form').classList.add('hidden');
    document.getElementById('attach-status').textContent = '';
    try {
        const detail = await fetchJson(`/api/requests/${encodeURIComponent(token)}`);
        renderCase(detail);
    } catch (err) {
        document.getElementById('case-title').textContent = 'Failed to load case';
        document.getElementById('case-summary').innerHTML = `<p class="status">${esc(err.message)}</p>`;
    }
}

function kv(key, value, full) {
    return `<div class="kv ${full ? 'full' : ''}"><div class="k">${esc(key)}</div><div class="v">${value}</div></div>`;
}

function renderCase(detail) {
    const s = detail.status;
    const dir = directionOf(s);
    document.getElementById('case-title').textContent = `Case #${s.id ?? '?'} — ${s.summary ?? ''}`;
    document.getElementById('case-summary').innerHTML = [
        kv('Status', statusChip(s.status)),
        kv('Direction', dir ? chip(dir, dir) : '—'),
        kv('Submitted by', esc(s.submitCompanyName)),
        kv('Received by', esc(s.receiveCompanyName)),
        kv('Created', esc(s.createdAt)),
        kv('Updated', esc(s.updatedAt)),
        kv('Case token', `<code>${esc(s.token)}</code>`, true),
    ].join('');

    renderCaseActions(dir);
    renderTimeline('case-notes', detail.notes.map(n => ({
        title: n.summary,
        meta: `${n.creatorName ?? n.creatorUsername ?? '?'} (${n.companyName ?? '?'}) · ${n.priority ?? ''} · ${n.createdAt ?? ''}`,
        body: n.description,
    })), 'No notes yet.');
    renderTimeline('case-responses', detail.responses.map(r => ({
        title: r.type,
        meta: `${r.engineerName ?? ''} ${r.engineerEmail ? '<' + r.engineerEmail + '>' : ''} · ${r.createdAt ?? ''}`,
        body: r.nextSteps ?? r.caseNumber ?? '',
    })), 'No responses yet.');
}

function renderTimeline(hostId, items, emptyText) {
    const host = document.getElementById(hostId);
    if (!items.length) {
        host.innerHTML = `<p class="muted">${esc(emptyText)}</p>`;
        return;
    }
    host.innerHTML = items.map(i => `
        <div class="timeline-item">
            <div class="t-head"><span class="t-title">${esc(i.title)}</span><span>${esc(i.meta)}</span></div>
            <div class="t-body">${esc(i.body)}</div>
        </div>
    `).join('');
}

// Receiver-side actions (approve/reject/request-info) vs submitter-side
// (respond-info/close). The API enforces which are valid for the case's
// current state — the demo shows the relevant set by direction and lets
// upstream validation speak for itself.
function renderCaseActions(dir) {
    const host = document.getElementById('case-actions');
    host.innerHTML = '';
    const actions = [];
    if (dir === 'inbound' || dir === null) {
        actions.push(['Approve', () => showActionForm('approve')]);
        actions.push(['Reject', () => showActionForm('reject')]);
        actions.push(['Request Info', () => showActionForm('request-info')]);
    }
    if (dir === 'outbound' || dir === null) {
        actions.push(['Respond to Info Request', () => showActionForm('respond-info')]);
        actions.push(['Close Case', () => runClose()]);
    }
    actions.push(['Add Note', () => showActionForm('note')]);
    for (const [label, handler] of actions) {
        const btn = document.createElement('button');
        btn.textContent = label;
        if (label === 'Reject') btn.className = 'danger';
        if (label === 'Close Case' || label === 'Add Note') btn.className = 'secondary';
        btn.addEventListener('click', handler);
        host.appendChild(btn);
    }
}

const ACTION_FORMS = {
    'approve': {
        title: 'Approve collaboration request',
        fields: [
            ['caseNumber', 'Your internal case number', 'text'],
            ['engineerName', 'Engineer name', 'text'],
            ['engineerEmail', 'Engineer email', 'email'],
            ['engineerPhone', 'Engineer phone', 'text'],
            ['text', 'Next steps', 'textarea'],
        ],
    },
    'reject': {
        title: 'Reject collaboration request',
        fields: [
            ['engineerName', 'Engineer name', 'text'],
            ['engineerEmail', 'Engineer email', 'email'],
            ['engineerPhone', 'Engineer phone', 'text'],
            ['text', 'Rejection reason', 'textarea'],
        ],
    },
    'request-info': {
        title: 'Request additional information',
        fields: [
            ['engineerName', 'Engineer name', 'text'],
            ['engineerEmail', 'Engineer email', 'email'],
            ['engineerPhone', 'Engineer phone', 'text'],
            ['text', 'Information requested', 'textarea'],
        ],
    },
    'respond-info': {
        title: 'Respond to information request',
        fields: [
            ['text', 'Response', 'textarea'],
        ],
    },
    'note': {
        title: 'Add note',
        fields: [
            ['summary', 'Summary', 'text'],
            ['description', 'Description', 'textarea'],
            ['priority', 'Priority', 'select', ['LOW', 'MEDIUM', 'HIGH']],
        ],
    },
};

function showActionForm(action) {
    const spec = ACTION_FORMS[action];
    const host = document.getElementById('action-form-host');
    const fieldsHtml = spec.fields.map(([name, label, type, options]) => {
        if (type === 'textarea') {
            return `<label>${esc(label)}<textarea name="${name}" rows="3" required></textarea></label>`;
        }
        if (type === 'select') {
            const opts = options.map(o => `<option>${esc(o)}</option>`).join('');
            return `<label>${esc(label)}<select name="${name}">${opts}</select></label>`;
        }
        return `<label>${esc(label)}<input type="${type}" name="${name}" required></label>`;
    }).join('');
    host.innerHTML = `
        <form class="action-form" id="current-action-form">
            <h3>${esc(spec.title)}</h3>
            ${fieldsHtml}
            <div class="button-row">
                <button type="submit">Submit</button>
                <button type="button" class="secondary" id="cancel-action">Cancel</button>
            </div>
        </form>
    `;
    document.getElementById('cancel-action').addEventListener('click', () => host.innerHTML = '');
    document.getElementById('current-action-form').addEventListener('submit', async (event) => {
        event.preventDefault();
        const form = event.target;
        const body = {};
        for (const [name] of spec.fields) body[name] = form[name].value;
        const status = document.getElementById('case-action-status');
        status.textContent = 'Submitting...';
        try {
            const url = action === 'note'
                ? `/api/requests/${encodeURIComponent(state.currentCaseToken)}/notes`
                : `/api/requests/${encodeURIComponent(state.currentCaseToken)}/${action}`;
            await fetchJson(url, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(body),
            });
            status.textContent = `${spec.title}: done.`;
            host.innerHTML = '';
            await openCase(state.currentCaseToken);
        } catch (err) {
            status.textContent = `Failed: ${err.message}`;
        }
    });
}

async function runClose() {
    const status = document.getElementById('case-action-status');
    status.textContent = 'Closing...';
    try {
        await fetchJson(`/api/requests/${encodeURIComponent(state.currentCaseToken)}/close`, {method: 'POST'});
        status.textContent = 'Case closed.';
        await openCase(state.currentCaseToken);
    } catch (err) {
        status.textContent = `Failed: ${err.message}`;
    }
}

// ---------- attachments ----------

document.getElementById('load-attach-config').addEventListener('click', async () => {
    const host = document.getElementById('attach-config');
    host.textContent = 'Loading attachment config...';
    try {
        const config = await fetchJson(
            `/api/requests/${encodeURIComponent(state.currentCaseToken)}/attachments/config`);
        host.innerHTML = `<pre>${esc(JSON.stringify(config, null, 2))}</pre>`;
        document.getElementById('attach-form').classList.remove('hidden');
    } catch (err) {
        host.textContent = `Failed: ${err.message}`;
    }
});

document.getElementById('attach-form').addEventListener('submit', async (event) => {
    event.preventDefault();
    const form = event.target;
    const status = document.getElementById('attach-status');
    const data = new FormData();
    data.append('description', form.description.value);
    for (const file of form.files.files) data.append('files', file);
    status.textContent = 'Forwarding...';
    try {
        const results = await fetchJson(
            `/api/requests/${encodeURIComponent(state.currentCaseToken)}/attachments`,
            {method: 'POST', body: data});
        status.textContent = `Forwarded ${results.length} attachment(s).`;
        form.reset();
    } catch (err) {
        status.textContent = `Failed: ${err.message}`;
    }
});

// ---------- new collaboration ----------

document.getElementById('partner-search-form').addEventListener('submit', async (event) => {
    event.preventDefault();
    const host = document.getElementById('partner-results');
    host.innerHTML = '<p class="muted">Searching...</p>';
    try {
        const term = document.getElementById('partner-search-input').value;
        const partners = await fetchJson(`/api/partners?q=${encodeURIComponent(term)}`);
        if (!partners.length) {
            host.innerHTML = '<p class="muted">No partners matched.</p>';
            return;
        }
        host.innerHTML = '';
        for (const p of partners) {
            const card = document.createElement('div');
            card.className = 'partner-card';
            card.innerHTML = `
                <div>
                    <div class="p-name">${esc(p.companyName ?? p.label)}</div>
                    <div class="p-dept">${esc(p.departmentName ?? '')} ${p.documentId ? `· form #${esc(p.documentId)}` : ''}</div>
                </div>
            `;
            const btn = document.createElement('button');
            btn.textContent = 'Select';
            btn.addEventListener('click', () => selectPartner(p));
            card.appendChild(btn);
            host.appendChild(card);
        }
    } catch (err) {
        host.innerHTML = `<p class="status">Failed: ${esc(err.message)}</p>`;
    }
});

async function selectPartner(partner) {
    state.selectedPartner = partner;
    const panel = document.getElementById('form-panel');
    const label = document.getElementById('selected-partner');
    const customHost = document.getElementById('custom-fields');
    panel.classList.remove('hidden');
    label.textContent = `Loading process form for ${partner.companyName ?? partner.label}...`;
    customHost.innerHTML = '';
    try {
        const params = new URLSearchParams();
        if (partner.documentId != null) params.set('documentId', partner.documentId);
        else if (partner.departmentId != null) params.set('departmentId', partner.departmentId);
        else params.set('companyId', partner.companyId);
        const template = await fetchJson(`/api/partners/form?${params}`);
        state.formTemplate = template;
        label.textContent =
            `${partner.companyName ?? partner.label}` +
            (partner.departmentName ? ` / ${partner.departmentName}` : '') +
            ` — ${template.fields?.length ?? 0} partner-defined field(s)`;
        renderCustomFields(template);
        panel.scrollIntoView({behavior: 'smooth'});
    } catch (err) {
        label.textContent = `Failed to load form: ${err.message}`;
        state.formTemplate = null;
    }
}

function renderCustomFields(template) {
    const host = document.getElementById('custom-fields');
    const fields = [...(template.fields ?? [])]
        .sort((a, b) => (a.displayOrder ?? 0) - (b.displayOrder ?? 0));
    if (!fields.length) {
        host.innerHTML = '<p class="muted">This partner has no custom fields on their process form.</p>';
        return;
    }
    let currentSection = null;
    let html = '';
    for (const f of fields) {
        if (f.section && f.section !== currentSection) {
            currentSection = f.section;
            html += `<h3>${esc(currentSection)}</h3>`;
        }
        const req = f.required ? 'required' : '';
        const name = `custom-${f.fieldId}`;
        const type = String(f.type ?? '').toLowerCase();
        const options = parseOptions(f.options);
        if (options.length && (type.includes('select') || type.includes('drop') || type.includes('option') || type.includes('list'))) {
            const opts = ['<option value="">-- select --</option>',
                ...options.map(o => `<option>${esc(o)}</option>`)].join('');
            html += `<label>${esc(f.label)}${f.required ? ' *' : ''}<select name="${name}" ${req}>${opts}</select></label>`;
        } else if (type.includes('textarea') || type.includes('multi')) {
            html += `<label>${esc(f.label)}${f.required ? ' *' : ''}<textarea name="${name}" rows="3" ${req}></textarea></label>`;
        } else if (type.includes('date')) {
            html += `<label>${esc(f.label)}${f.required ? ' *' : ''}<input type="date" name="${name}" ${req}></label>`;
        } else if (type.includes('number') || type.includes('int')) {
            html += `<label>${esc(f.label)}${f.required ? ' *' : ''}<input type="number" name="${name}" ${req}></label>`;
        } else {
            html += `<label>${esc(f.label)}${f.required ? ' *' : ''}<input type="text" name="${name}" ${req}></label>`;
        }
    }
    host.innerHTML = html;
}

document.getElementById('collab-form').addEventListener('submit', async (event) => {
    event.preventDefault();
    const form = event.target;
    const status = document.getElementById('collab-status');
    if (!state.formTemplate) {
        status.textContent = 'Select a partner first.';
        return;
    }
    const customFieldValues = {};
    for (const f of state.formTemplate.fields ?? []) {
        const input = form[`custom-${f.fieldId}`];
        if (input && input.value !== '') customFieldValues[f.fieldId] = input.value;
    }
    status.textContent = 'Submitting collaboration request...';
    try {
        const created = await fetchJson('/api/requests', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({
                formTemplate: state.formTemplate,
                caseNumber: form.caseNumber.value,
                summary: form.summary.value,
                description: form.description.value,
                customFieldValues,
            }),
        });
        status.innerHTML = `Created case #${esc(created.id)} (${statusChip(created.status)}). `;
        const link = document.createElement('a');
        link.href = '#';
        link.textContent = 'Open case →';
        link.addEventListener('click', (e) => {
            e.preventDefault();
            openCase(created.token);
        });
        status.appendChild(link);
        form.reset();
    } catch (err) {
        status.textContent = `Failed: ${err.message}`;
    }
});

// ---------- webhooks ----------

async function loadWebhooks() {
    const tbody = document.querySelector('#webhooks-table tbody');
    tbody.innerHTML = '<tr><td colspan="5">Loading...</td></tr>';
    document.getElementById('webhook-deliveries').classList.add('hidden');
    try {
        const subs = await fetchJson('/api/webhooks');
        if (!subs.length) {
            tbody.innerHTML = '<tr><td colspan="5">No subscriptions.</td></tr>';
            return;
        }
        tbody.innerHTML = '';
        for (const s of subs) {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${esc(s.id)}</td>
                <td>${esc(s.callbackUrl)}</td>
                <td>${esc(s.eventTypes)}</td>
                <td>${s.active ? chip('active', 'st-approved') : chip('inactive', 'st-closed')}</td>
            `;
            const td = document.createElement('td');
            const view = document.createElement('button');
            view.textContent = 'Deliveries';
            view.className = 'secondary';
            view.addEventListener('click', () => loadDeliveries(s.id));
            const del = document.createElement('button');
            del.textContent = 'Delete';
            del.className = 'danger';
            del.style.marginLeft = '0.5rem';
            del.addEventListener('click', async () => {
                if (!confirm(`Delete webhook subscription #${s.id}?`)) return;
                try {
                    await fetchJson(`/api/webhooks/${s.id}`, {method: 'DELETE'});
                    await loadWebhooks();
                } catch (err) {
                    alert(`Failed: ${err.message}`);
                }
            });
            td.append(view, del);
            tr.appendChild(td);
            tbody.appendChild(tr);
        }
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="5">Failed to load: ${esc(err.message)}</td></tr>`;
    }
}

async function loadDeliveries(subscriptionId) {
    const wrap = document.getElementById('webhook-deliveries');
    const tbody = document.querySelector('#deliveries-table tbody');
    wrap.classList.remove('hidden');
    document.getElementById('deliveries-sub').textContent = `— subscription #${subscriptionId}`;
    tbody.innerHTML = '<tr><td colspan="5">Loading...</td></tr>';
    try {
        const page = await fetchJson(`/api/webhooks/${subscriptionId}/deliveries?page=0&size=20`);
        const items = page.content ?? page.items ?? [];
        if (!items.length) {
            tbody.innerHTML = '<tr><td colspan="5">No deliveries recorded.</td></tr>';
            return;
        }
        tbody.innerHTML = items.map(d => `
            <tr>
                <td>${esc(d.eventType)}</td>
                <td>${esc(d.httpStatus)}</td>
                <td>${esc(d.attemptNumber)}</td>
                <td>${d.success ? chip('yes', 'st-approved') : chip('no', 'st-rejected')}</td>
                <td>${esc(d.createdAt)}</td>
            </tr>
        `).join('');
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="5">Failed: ${esc(err.message)}</td></tr>`;
    }
}

document.getElementById('refresh-webhooks').addEventListener('click', loadWebhooks);

document.getElementById('webhook-form').addEventListener('submit', async (event) => {
    event.preventDefault();
    const form = event.target;
    const status = document.getElementById('webhook-status');
    status.textContent = 'Creating...';
    try {
        const eventTypes = form.eventTypes.value.split(',').map(s => s.trim()).filter(Boolean);
        await fetchJson('/api/webhooks', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({callbackUrl: form.callbackUrl.value, eventTypes}),
        });
        status.textContent = 'Subscription created.';
        form.reset();
        await loadWebhooks();
    } catch (err) {
        status.textContent = `Failed: ${err.message}`;
    }
});

// ---------- settings ----------

document.getElementById('settings-form').addEventListener('submit', async (event) => {
    event.preventDefault();
    const status = document.getElementById('settings-status');
    const form = event.target;
    status.textContent = 'Saving...';
    try {
        await fetchJson('/api/settings', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({username: form.username.value, password: form.password.value}),
        });
        form.reset();
        status.textContent = 'Credentials saved.';
        await refreshIdentity();
    } catch (err) {
        status.textContent = `Failed: ${err.message}`;
    }
});

document.getElementById('clear-settings-btn').addEventListener('click', async () => {
    const status = document.getElementById('settings-status');
    status.textContent = 'Clearing...';
    try {
        await fetchJson('/api/settings', {method: 'DELETE'});
        status.textContent = 'Credentials cleared.';
        await refreshIdentity();
    } catch (err) {
        status.textContent = `Failed: ${err.message}`;
    }
});

// ---------- init ----------

refreshIdentity().then(loadRequests);
