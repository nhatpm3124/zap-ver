import axios from 'axios';

const API_URL = 'http://localhost:8080/api/auth/';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface SignupRequest {
  username: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  id: number;
  username: string;
  email: string;
}

class AuthService {
  login(username: string, password: string): Promise<AuthResponse> {
    return axios
      .post(API_URL + 'signin', {
        username,
        password
      })
      .then(response => {
        if (response.data.accessToken) {
          localStorage.setItem('user', JSON.stringify(response.data));
        }
        return response.data;
      });
  }

  logout(): void {
    localStorage.removeItem('user');
  }

  register(username: string, email: string, password: string): Promise<any> {
    return axios.post(API_URL + 'signup', {
      username,
      email,
      password
    });
  }

  getCurrentUser(): AuthResponse | null {
    const userStr = localStorage.getItem('user');
    if (userStr) return JSON.parse(userStr);
    return null;
  }
}

export default new AuthService(); 