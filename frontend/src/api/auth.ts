import { api } from './client';
import type { AuthResponse, LoginRequest, SignupRequest } from '../types';

export function login(body: LoginRequest): Promise<AuthResponse> {
  return api.post<AuthResponse>('/auth/login', body);
}

export function signup(body: SignupRequest): Promise<AuthResponse> {
  return api.post<AuthResponse>('/auth/signup', body);
}
