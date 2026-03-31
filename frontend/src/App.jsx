import { useEffect, useState } from 'react';
import {
  createAccount,
  fetchAccounts,
  debitAccount,
  creditAccount,
  performTransaction,
  fetchAudits
} from './api.js';

const tabs = ['Accounts', 'Transactions', 'Audits'];

function App() {
  const [activeTab, setActiveTab] = useState('Accounts');

  return (
    <div className="app-shell">
      <header className="topbar">
        <div>
          <h1>Banking Microservices Dashboard</h1>
          <p>React frontend for Spring Boot microservices via API Gateway</p>
        </div>
      </header>

      <nav className="tabs">
        {tabs.map((tab) => (
          <button
            key={tab}
            type="button"
            className={tab === activeTab ? 'tab active' : 'tab'}
            onClick={() => setActiveTab(tab)}
          >
            {tab}
          </button>
        ))}
      </nav>

      <main className="content">
        {activeTab === 'Accounts' && <AccountsPanel />}
        {activeTab === 'Transactions' && <TransactionsPanel />}
        {activeTab === 'Audits' && <AuditsPanel />}
      </main>
    </div>
  );
}

function AccountsPanel() {
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [status, setStatus] = useState('');
  const [form, setForm] = useState({ accountId: '', owner: '', balance: '0' });
  const [amounts, setAmounts] = useState({});

  const loadAccounts = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await fetchAccounts();
      setAccounts(data);
    } catch (err) {
      setError(`Unable to load accounts: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAccounts();
  }, []);

  const handleCreate = async (event) => {
    event.preventDefault();
    setStatus('');
    setError('');

    if (!form.accountId || !form.owner) {
      setError('Please enter account ID and owner name.');
      return;
    }

    try {
      await createAccount({
        accountId: form.accountId,
        owner: form.owner,
        balance: Number(form.balance || 0)
      });
      setStatus('Account created successfully.');
      setForm({ accountId: '', owner: '', balance: '0' });
      await loadAccounts();
    } catch (err) {
      setError(`Create failed: ${err.message}`);
    }
  };

  const handleAmountChange = (accountId, value) => {
    setAmounts((current) => ({ ...current, [accountId]: value }));
  };

  const handleAccountAction = async (accountId, type) => {
    const amount = Number(amounts[accountId] || 0);
    if (amount <= 0) {
      setError('Enter an amount greater than zero for debit or credit.');
      return;
    }

    try {
      setError('');
      setStatus('Processing account update...');
      if (type === 'debit') {
        await debitAccount(accountId, amount);
      } else {
        await creditAccount(accountId, amount);
      }
      setStatus(`Account ${type} successful.`);
      await loadAccounts();
    } catch (err) {
      setError(`Account ${type} failed: ${err.message}`);
    }
  };

  return (
    <section>
      <div className="panel-heading">
        <h2>Accounts</h2>
        <p>View accounts, create new accounts, and post debit/credit operations.</p>
      </div>

      <div className="panel-grid">
        <div className="panel-card">
          <h3>Create Account</h3>
          <form onSubmit={handleCreate} className="form-grid">
            <label>
              Account ID
              <input
                value={form.accountId}
                onChange={(e) => setForm({ ...form, accountId: e.target.value })}
                placeholder="acc-1001"
              />
            </label>
            <label>
              Owner
              <input
                value={form.owner}
                onChange={(e) => setForm({ ...form, owner: e.target.value })}
                placeholder="John Doe"
              />
            </label>
            <label>
              Opening Balance
              <input
                type="number"
                min="0"
                value={form.balance}
                onChange={(e) => setForm({ ...form, balance: e.target.value })}
              />
            </label>
            <button type="submit" className="primary-button">Create Account</button>
          </form>
        </div>

        <div className="panel-card wide-card">
          <h3>Account List</h3>
          {loading ? (
            <p>Loading accounts…</p>
          ) : accounts.length === 0 ? (
            <p>No accounts available yet.</p>
          ) : (
            <div className="table-scroll">
              <table>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Owner</th>
                    <th>Balance</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {accounts.map((account) => (
                    <tr key={account.accountId}>
                      <td>{account.accountId}</td>
                      <td>{account.owner}</td>
                      <td>{account.balance}</td>
                      <td>
                        <div className="account-actions">
                          <input
                            type="number"
                            min="0"
                            value={amounts[account.accountId] || ''}
                            onChange={(e) => handleAmountChange(account.accountId, e.target.value)}
                            placeholder="amount"
                          />
                          <button type="button" onClick={() => handleAccountAction(account.accountId, 'debit')}>
                            Debit
                          </button>
                          <button type="button" onClick={() => handleAccountAction(account.accountId, 'credit')}>
                            Credit
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      {(status || error) && (
        <div className={`message-box ${error ? 'error' : 'success'}`}>
          {error || status}
        </div>
      )}
    </section>
  );
}

function TransactionsPanel() {
  const [form, setForm] = useState({ fromAccountId: '', toAccountId: '', amount: '' });
  const [status, setStatus] = useState('');
  const [error, setError] = useState('');
  const [transaction, setTransaction] = useState(null);

  const submitTransaction = async (event) => {
    event.preventDefault();
    setError('');
    setStatus('');
    setTransaction(null);

    if (!form.fromAccountId || !form.toAccountId || !form.amount) {
      setError('Complete all fields to perform a transaction.');
      return;
    }

    try {
      const result = await performTransaction({
        fromAccountId: form.fromAccountId,
        toAccountId: form.toAccountId,
        amount: Number(form.amount)
      });
      setTransaction(result);
      setStatus('Transaction submitted successfully.');
      setForm({ fromAccountId: '', toAccountId: '', amount: '' });
    } catch (err) {
      setError(`Transaction failed: ${err.message}`);
    }
  };

  return (
    <section>
      <div className="panel-heading">
        <h2>Transactions</h2>
        <p>Submit a transfer between accounts and view the response.</p>
      </div>

      <div className="panel-card wide-card">
        <form onSubmit={submitTransaction} className="form-grid">
          <label>
            From Account ID
            <input
              value={form.fromAccountId}
              onChange={(e) => setForm({ ...form, fromAccountId: e.target.value })}
              placeholder="acc-1001"
            />
          </label>
          <label>
            To Account ID
            <input
              value={form.toAccountId}
              onChange={(e) => setForm({ ...form, toAccountId: e.target.value })}
              placeholder="acc-1002"
            />
          </label>
          <label>
            Amount
            <input
              type="number"
              min="0"
              value={form.amount}
              onChange={(e) => setForm({ ...form, amount: e.target.value })}
            />
          </label>
          <button type="submit" className="primary-button">Submit Transaction</button>
        </form>

        {status && <p className="success">{status}</p>}
        {error && <p className="error">{error}</p>}

        {transaction && (
          <div className="result-card">
            <h4>Transaction Result</h4>
            <pre>{JSON.stringify(transaction, null, 2)}</pre>
          </div>
        )}
      </div>
    </section>
  );
}

function LedgerPanel() {
  const [entries, setEntries] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      setError('');
      try {
        setEntries(await fetchLedger());
      } catch (err) {
        setError(`Unable to load ledger entries: ${err.message}`);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  return (
    <section>
      <div className="panel-heading">
        <h2>Ledger</h2>
        <p>Review ledger entries for account activity.</p>
      </div>

      <div className="panel-card wide-card">
        {loading ? (
          <p>Loading ledger entries…</p>
        ) : error ? (
          <p className="error">{error}</p>
        ) : entries.length === 0 ? (
          <p>No ledger entries found.</p>
        ) : (
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Account</th>
                  <th>Type</th>
                  <th>Amount</th>
                  <th>Description</th>
                  <th>Created At</th>
                </tr>
              </thead>
              <tbody>
                {entries.map((entry) => (
                  <tr key={entry.id}>
                    <td>{entry.id}</td>
                    <td>{entry.accountId}</td>
                    <td>{entry.type}</td>
                    <td>{entry.amount}</td>
                    <td>{entry.description}</td>
                    <td>{new Date(entry.createdAt).toLocaleString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </section>
  );
}

function AuditsPanel() {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      setError('');
      try {
        setEvents(await fetchAudits());
      } catch (err) {
        setError(`Unable to load audit events: ${err.message}`);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  return (
    <section>
      <div className="panel-heading">
        <h2>Audits</h2>
        <p>Browse audit events produced by transactions and service actions.</p>
      </div>

      <div className="panel-card wide-card">
        {loading ? (
          <p>Loading audits…</p>
        ) : error ? (
          <p className="error">{error}</p>
        ) : events.length === 0 ? (
          <p>No audit records found.</p>
        ) : (
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Event Type</th>
                  <th>Payload</th>
                  <th>Created At</th>
                </tr>
              </thead>
              <tbody>
                {events.map((event) => (
                  <tr key={event.id}>
                    <td>{event.id}</td>
                    <td>{event.eventType}</td>
                    <td>{event.payload}</td>
                    <td>{new Date(event.createdAt).toLocaleString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </section>
  );
}

export default App;
