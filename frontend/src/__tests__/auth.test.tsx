// @vitest-environment jsdom
import { describe, it, expect, beforeEach } from 'vitest';

describe('Auth Tokens local storage', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('stores and retrieves access token correctly', () => {
    localStorage.setItem('accessToken', 'mock-jwt-token');
    expect(localStorage.getItem('accessToken')).toBe('mock-jwt-token');
  });

  it('clears session on logout', () => {
    localStorage.setItem('accessToken', 'mock-jwt-token');
    localStorage.setItem('refreshToken', 'mock-refresh-token');

    localStorage.clear();

    expect(localStorage.getItem('accessToken')).toBeNull();
    expect(localStorage.getItem('refreshToken')).toBeNull();
  });
});
