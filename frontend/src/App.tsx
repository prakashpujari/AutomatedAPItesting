import React, { useState } from 'react';
import { runOrchestrator } from './api';

function App() {
  const [apiSources, setApiSources] = useState('[]');
  const [result, setResult] = useState<Record<string, unknown> | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const data = await runOrchestrator({ apiSources });
      setResult(data);
    } catch (err) {
      setError('Failed to run orchestrator: ' + (err as Error).message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: '2rem', fontFamily: 'sans-serif' }}>
      <h1>API Testing Platform</h1>
      <form onSubmit={handleSubmit}>
        <label>
          API Sources (JSON array):
          <br />
          <textarea
            value={apiSources}
            onChange={(e) => setApiSources(e.target.value)}
            rows={5}
            cols={60}
            placeholder='[{"url": "https://api.example.com/openapi.json"}]'
          />
        </label>
        <br />
        <br />
        <button type="submit" disabled={loading}>
          {loading ? 'Running...' : 'Run Tests'}
        </button>
      </form>
      {error && <p style={{ color: 'red' }}>{error}</p>}
      {result && (
        <div style={{ marginTop: '2rem' }}>
          <h2>Results</h2>
          <pre>{JSON.stringify(result, null, 2)}</pre>
        </div>
      )}
    </div>
  );
}

export default App;