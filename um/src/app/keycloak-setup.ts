import {
  createInterceptorCondition, IncludeBearerTokenCondition,
} from 'keycloak-angular';

export const urlCondition = createInterceptorCondition<IncludeBearerTokenCondition>({
  urlPattern: /^(http:\/\/localhost:8080)(\/.*)?$/i,
  bearerPrefix: 'Bearer'
});

export async function fetchAuthConfig() {
  try {
    const response = await fetch('http://localhost:8080/api/auth/config');
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    const result = await response.json();
    if (!result || !result.data) {
      throw new Error('Failed to load auth config');
    }
    return result.data as {
      authServerUrl: string;
      realm: string;
      clientId: string;
    };
  } catch (error) {
    console.error('Failed to fetch auth config:', error);
    throw error;
  }
}
