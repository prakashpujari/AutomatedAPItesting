import axios from 'axios';

/**
 * Simple wrapper around the backend orchestrator endpoint.
 * Vite's dev server proxies `/api/**` to the Spring Boot backend (http://localhost:8080).
 */
export const runOrchestrator = async (payload: Record<string, unknown>) => {
  const response = await axios.post<Record<string, unknown>>('/api/orchestrate', payload);
  return response.data;
};
