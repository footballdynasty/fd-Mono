import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { User, Team, AuthContextType, LoginRequest, AuthResponse } from '../types';
import { authApi } from '../services/api';

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [selectedTeam, setSelectedTeam] = useState<Team | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Check for stored auth token on app start
    const token = localStorage.getItem('auth_token');
    const storedUser = localStorage.getItem('user');
    const storedTeam = localStorage.getItem('selected_team');

    if (token && storedUser) {
      try {
        const userData = JSON.parse(storedUser);
        setUser(userData);
        
        if (storedTeam) {
          const teamData = JSON.parse(storedTeam);
          setSelectedTeam(teamData);
        }
      } catch (error) {
        console.error('Error parsing stored auth data:', error);
        localStorage.removeItem('auth_token');
        localStorage.removeItem('user');
        localStorage.removeItem('selected_team');
      }
    }
    
    setIsLoading(false);
  }, []);

  const login = async (username: string, password: string): Promise<void> => {
    try {
      setIsLoading(true);
      const response = await authApi.login({ username, password });
      const { user: userData, token, selectedTeam: userTeam } = response.data;

      // Store auth data
      localStorage.setItem('auth_token', token);
      localStorage.setItem('user', JSON.stringify(userData));
      
      setUser(userData);
      
      if (userTeam) {
        localStorage.setItem('selected_team', JSON.stringify(userTeam));
        setSelectedTeam(userTeam);
      }
    } catch (error) {
      console.error('Login failed:', error);
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  const register = async (username: string, password: string, email?: string): Promise<void> => {
    try {
      setIsLoading(true);
      const response = await authApi.register({ username, password, email });
      const { user: userData, token, selectedTeam: userTeam } = response.data;

      // Store auth data
      localStorage.setItem('auth_token', token);
      localStorage.setItem('user', JSON.stringify(userData));
      
      setUser(userData);
      
      if (userTeam) {
        localStorage.setItem('selected_team', JSON.stringify(userTeam));
        setSelectedTeam(userTeam);
      }
    } catch (error) {
      console.error('Registration failed:', error);
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  const logout = (): void => {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('user');
    localStorage.removeItem('selected_team');
    setUser(null);
    setSelectedTeam(null);
  };

  const selectTeam = (team: Team): void => {
    localStorage.setItem('selected_team', JSON.stringify(team));
    setSelectedTeam(team);
    
    // Update user with selected team ID
    if (user) {
      const updatedUser = { ...user, selectedTeamId: team.id };
      localStorage.setItem('user', JSON.stringify(updatedUser));
      setUser(updatedUser);
    }
  };

  const value: AuthContextType = {
    user,
    selectedTeam,
    isAuthenticated: !!user,
    isLoading,
    login,
    register,
    logout,
    selectTeam,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};