const API_BASE = '/api';

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options
  });

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || response.statusText);
  }

  return response.json();
}

export function fetchAccounts() {
  return request('/accounts');
}

export function createAccount(account) {
  return request('/accounts', {
    method: 'POST',
    body: JSON.stringify(account)
  });
}

export function debitAccount(accountId, amount) {
  return request(`/accounts/${encodeURIComponent(accountId)}/debit`, {
    method: 'POST',
    body: JSON.stringify({ amount })
  });
}

export function creditAccount(accountId, amount) {
  return request(`/accounts/${encodeURIComponent(accountId)}/credit`, {
    method: 'POST',
    body: JSON.stringify({ amount })
  });
}

export function performTransaction(transactionRequest) {
  return request('/transactions', {
    method: 'POST',
    body: JSON.stringify(transactionRequest)
  });
}

export function fetchAudits() {
  return request('/audits');
}
