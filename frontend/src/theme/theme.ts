import { createTheme, alpha } from '@mui/material/styles';

declare module '@mui/material/styles' {
  interface Palette {
    gradient: {
      primary: string;
      secondary: string;
      success: string;
      warning: string;
      error: string;
    };
    glass: {
      light: string;
      medium: string;
      dark: string;
    };
    surface: {
      main: string;
      light: string;
      dark: string;
    };
  }

  interface PaletteOptions {
    gradient?: {
      primary?: string;
      secondary?: string;
      success?: string;
      warning?: string;
      error?: string;
    };
    glass?: {
      light?: string;
      medium?: string;
      dark?: string;
    };
    surface?: {
      main?: string;
      light?: string;
      dark?: string;
    };
  }
}

const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#1e88e5',
      light: '#42a5f5',
      dark: '#1565c0',
      contrastText: '#ffffff',
    },
    secondary: {
      main: '#26a69a',
      light: '#4db6ac',
      dark: '#00695c',
      contrastText: '#ffffff',
    },
    background: {
      default: '#0a0e27',
      paper: '#1a1d35',
    },
    surface: {
      main: '#242744',
      light: '#2d3357',
      dark: '#1a1d35',
    },
    gradient: {
      primary: 'linear-gradient(135deg, #1e88e5 0%, #42a5f5 100%)',
      secondary: 'linear-gradient(135deg, #26a69a 0%, #4db6ac 100%)',
      success: 'linear-gradient(135deg, #4caf50 0%, #66bb6a 100%)',
      warning: 'linear-gradient(135deg, #ff9800 0%, #ffb74d 100%)',
      error: 'linear-gradient(135deg, #f44336 0%, #e57373 100%)',
    },
    glass: {
      light: alpha('#ffffff', 0.1),
      medium: alpha('#ffffff', 0.05),
      dark: alpha('#000000', 0.3),
    },
    text: {
      primary: '#ffffff',
      secondary: alpha('#ffffff', 0.7),
    },
  },
  typography: {
    fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
    h1: {
      fontSize: '3rem',
      fontWeight: 700,
      letterSpacing: '-0.02em',
    },
    h2: {
      fontSize: '2.25rem',
      fontWeight: 600,
      letterSpacing: '-0.01em',
    },
    h3: {
      fontSize: '1.875rem',
      fontWeight: 600,
    },
    h4: {
      fontSize: '1.5rem',
      fontWeight: 600,
    },
    h5: {
      fontSize: '1.25rem',
      fontWeight: 600,
    },
    h6: {
      fontSize: '1.125rem',
      fontWeight: 600,
    },
    body1: {
      fontSize: '1rem',
      lineHeight: 1.5,
    },
    body2: {
      fontSize: '0.875rem',
      lineHeight: 1.43,
    },
  },
  shape: {
    borderRadius: 16,
  },
  components: {
    MuiCard: {
      styleOverrides: {
        root: {
          background: 'linear-gradient(145deg, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0.05) 100%)',
          backdropFilter: 'blur(20px)',
          border: '1px solid rgba(255,255,255,0.1)',
          borderRadius: 20,
          boxShadow: '0 8px 32px rgba(0,0,0,0.3)',
        },
      },
    },
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 12,
          textTransform: 'none',
          fontWeight: 600,
          padding: '12px 24px',
          boxShadow: 'none',
          '&:hover': {
            boxShadow: '0 8px 24px rgba(0,0,0,0.2)',
            transform: 'translateY(-2px)',
          },
          transition: 'all 0.3s ease',
        },
        contained: {
          background: 'linear-gradient(135deg, #1e88e5 0%, #42a5f5 100%)',
          '&:hover': {
            background: 'linear-gradient(135deg, #1565c0 0%, #1e88e5 100%)',
          },
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          background: 'linear-gradient(145deg, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0.05) 100%)',
          backdropFilter: 'blur(20px)',
          border: '1px solid rgba(255,255,255,0.1)',
        },
      },
    },
    MuiTextField: {
      styleOverrides: {
        root: {
          '& .MuiOutlinedInput-root': {
            borderRadius: 12,
            background: alpha('#ffffff', 0.05),
            '& fieldset': {
              borderColor: alpha('#ffffff', 0.2),
            },
            '&:hover fieldset': {
              borderColor: alpha('#ffffff', 0.3),
            },
            '&.Mui-focused fieldset': {
              borderColor: '#42a5f5',
            },
          },
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          fontWeight: 600,
        },
        filled: {
          background: 'linear-gradient(135deg, rgba(255,255,255,0.2) 0%, rgba(255,255,255,0.1) 100%)',
          backdropFilter: 'blur(10px)',
        },
      },
    },
    MuiTableContainer: {
      styleOverrides: {
        root: {
          borderRadius: 16,
          background: 'linear-gradient(145deg, rgba(255,255,255,0.08) 0%, rgba(255,255,255,0.04) 100%)',
          backdropFilter: 'blur(20px)',
          border: '1px solid rgba(255,255,255,0.1)',
        },
      },
    },
    MuiTableHead: {
      styleOverrides: {
        root: {
          background: 'linear-gradient(135deg, rgba(255,255,255,0.15) 0%, rgba(255,255,255,0.1) 100%)',
        },
      },
    },
  },
});

export default theme;