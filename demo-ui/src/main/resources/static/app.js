async function loadRequests() {
    const tbody = document.querySelector('#requests-table tbody');
    tbody.innerHTML = '<tr><td colspan="6">Loading...</td></tr>';
    try {
        const res = await fetch('/api/requests');
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const requests = await res.json();
        tbody.innerHTML = '';
        if (requests.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6">No requests found.</td></tr>';
            return;
        }
        for (const r of requests) {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${r.id ?? ''}</td>
                <td>${r.status ?? ''}</td>
                <td>${r.summary ?? ''}</td>
                <td>${r.submitCompanyName ?? ''}</td>
                <td>${r.receiveCompanyName ?? ''}</td>
                <td>${r.updatedAt ?? ''}</td>
            `;
            tbody.appendChild(row);
        }
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="6">Failed to load: ${err.message}</td></tr>`;
    }
}

document.getElementById('refresh-btn').addEventListener('click', loadRequests);

document.getElementById('create-form').addEventListener('submit', async (event) => {
    event.preventDefault();
    const status = document.getElementById('create-status');
    const form = event.target;
    const body = {
        receiverCompanyId: Number(form.receiverCompanyId.value),
        caseNumber: form.caseNumber.value,
        summary: form.summary.value,
        description: form.description.value,
    };
    status.textContent = 'Submitting...';
    try {
        const res = await fetch('/api/requests', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(body),
        });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        status.textContent = 'Request created.';
        form.reset();
        await loadRequests();
    } catch (err) {
        status.textContent = `Failed: ${err.message}`;
    }
});

loadRequests();
