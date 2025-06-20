import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  IconButton,
  Box,
  Typography,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Switch,
  FormControlLabel,
  Button,
  Alert,
  useTheme,
  alpha,
  Avatar,
  Grid,
} from '@mui/material';
import {
  Close,
  Save,
  Add,
  Edit,
  Delete,
  CloudUpload,
} from '@mui/icons-material';
import { motion } from 'framer-motion';
import { Team } from '../../types';
import GradientButton from '../ui/GradientButton';

export type TeamManagementMode = 'create' | 'edit' | 'delete';

interface TeamManagementModalProps {
  open: boolean;
  mode: TeamManagementMode;
  team?: Team | null;
  conferences: string[];
  onClose: () => void;
  onSave: (teamData: Partial<Team>) => Promise<void>;
  onDelete?: (teamId: string) => Promise<void>;
  loading?: boolean;
}

interface TeamFormData {
  name: string;
  coach: string;
  username: string;
  conference: string;
  isHuman: boolean;
  imageUrl: string;
}

const TeamManagementModal: React.FC<TeamManagementModalProps> = ({
  open,
  mode,
  team,
  conferences,
  onClose,
  onSave,
  onDelete,
  loading = false,
}) => {
  const theme = useTheme();
  const [formData, setFormData] = useState<TeamFormData>({
    name: '',
    coach: '',
    username: '',
    conference: '',
    isHuman: true,
    imageUrl: '',
  });
  const [error, setError] = useState<string | null>(null);

  // Initialize form data when team changes
  useEffect(() => {
    if (team && mode === 'edit') {
      setFormData({
        name: team.name || '',
        coach: team.coach || '',
        username: team.username || '',
        conference: team.conference || '',
        isHuman: team.isHuman ?? true,
        imageUrl: team.imageUrl || '',
      });
    } else if (mode === 'create') {
      setFormData({
        name: '',
        coach: '',
        username: '',
        conference: '',
        isHuman: true,
        imageUrl: '',
      });
    }
    setError(null);
  }, [team, mode, open]);

  const handleInputChange = (field: keyof TeamFormData) => (
    event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement> | { target: { value: unknown } }
  ) => {
    const value = event.target.value;
    setFormData(prev => ({
      ...prev,
      [field]: value,
    }));
    setError(null);
  };

  const handleSwitchChange = (field: keyof TeamFormData) => (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    setFormData(prev => ({
      ...prev,
      [field]: event.target.checked,
    }));
  };

  const validateForm = (): boolean => {
    if (!formData.name.trim()) {
      setError('Team name is required');
      return false;
    }
    if (formData.isHuman && !formData.coach.trim()) {
      setError('Coach name is required for human teams');
      return false;
    }
    if (formData.isHuman && !formData.username.trim()) {
      setError('Username is required for human teams');
      return false;
    }
    return true;
  };

  const handleSave = async () => {
    if (!validateForm()) return;

    try {
      await onSave(formData);
      handleClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save team');
    }
  };

  const handleDelete = async () => {
    if (!team || !onDelete) return;

    try {
      await onDelete(team.id);
      handleClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete team');
    }
  };

  const handleClose = () => {
    setError(null);
    onClose();
  };

  const getTitle = () => {
    switch (mode) {
      case 'create':
        return 'Create New Team';
      case 'edit':
        return 'Edit Team';
      case 'delete':
        return 'Delete Team';
      default:
        return 'Team Management';
    }
  };

  const getIcon = () => {
    switch (mode) {
      case 'create':
        return <Add />;
      case 'edit':
        return <Edit />;
      case 'delete':
        return <Delete />;
      default:
        return <Edit />;
    }
  };

  // Generate team initials for avatar fallback
  const getTeamInitials = (name: string) => {
    return name
      .split(' ')
      .map(word => word.charAt(0))
      .join('')
      .toUpperCase()
      .substring(0, 2);
  };

  return (
    <Dialog
      open={open}
      onClose={handleClose}
      maxWidth="md"
      fullWidth
      PaperProps={{
        sx: {
          background: 'linear-gradient(145deg, rgba(26,29,53,0.95) 0%, rgba(36,39,68,0.95) 100%)',
          backdropFilter: 'blur(20px)',
          border: '1px solid rgba(255,255,255,0.1)',
          borderRadius: '16px',
          boxShadow: '0 24px 48px rgba(0,0,0,0.3)',
        },
      }}
    >
      <DialogTitle sx={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center',
        pb: 1,
        borderBottom: `1px solid ${alpha('#ffffff', 0.1)}`,
      }}>
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          {getIcon()}
          <Typography variant="h5" sx={{ fontWeight: 600, ml: 1 }}>
            {getTitle()}
          </Typography>
        </Box>
        <IconButton
          onClick={handleClose}
          sx={{
            color: 'white',
            '&:hover': {
              background: alpha('#ffffff', 0.1),
            },
          }}
        >
          <Close />
        </IconButton>
      </DialogTitle>

      <DialogContent sx={{ p: 3 }}>
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.3 }}
        >
          {error && (
            <Alert severity="error" sx={{ mb: 3 }}>
              {error}
            </Alert>
          )}

          {mode === 'delete' ? (
            <Box sx={{ textAlign: 'center', py: 3 }}>
              <Avatar
                src={team?.imageUrl}
                sx={{
                  width: 80,
                  height: 80,
                  mx: 'auto',
                  mb: 2,
                  border: `2px solid ${alpha('#f44336', 0.5)}`,
                  background: 'linear-gradient(135deg, #1e88e5 0%, #42a5f5 100%)',
                }}
              >
                {team?.name ? getTeamInitials(team.name) : 'T'}
              </Avatar>
              <Typography variant="h6" sx={{ mb: 2 }}>
                Are you sure you want to delete "{team?.name}"?
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
                This action cannot be undone. All team data, games, and statistics will be permanently removed.
              </Typography>
            </Box>
          ) : (
            <Grid container spacing={3}>
              {/* Team Avatar Preview */}
              <Grid item xs={12} sx={{ textAlign: 'center', mb: 2 }}>
                <Avatar
                  src={formData.imageUrl || ''}
                  sx={{
                    width: 100,
                    height: 100,
                    mx: 'auto',
                    mb: 2,
                    border: `2px solid ${alpha('#ffffff', 0.2)}`,
                    background: 'linear-gradient(135deg, #1e88e5 0%, #42a5f5 100%)',
                  }}
                >
                  {formData.name ? getTeamInitials(formData.name) : 'T'}
                </Avatar>
                <Typography variant="body2" color="text.secondary">
                  Team Avatar Preview
                </Typography>
              </Grid>

              {/* Basic Information */}
              <Grid item xs={12}>
                <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>
                  Basic Information
                </Typography>
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Team Name"
                  value={formData.name}
                  onChange={handleInputChange('name')}
                  required
                  variant="outlined"
                  sx={{
                    '& .MuiOutlinedInput-root': {
                      backgroundColor: alpha('#ffffff', 0.05),
                      '&:hover': {
                        backgroundColor: alpha('#ffffff', 0.08),
                      },
                    },
                  }}
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <FormControl fullWidth>
                  <InputLabel>Conference</InputLabel>
                  <Select
                    value={formData.conference}
                    onChange={handleInputChange('conference')}
                    label="Conference"
                    sx={{
                      backgroundColor: alpha('#ffffff', 0.05),
                      '&:hover': {
                        backgroundColor: alpha('#ffffff', 0.08),
                      },
                    }}
                  >
                    <MenuItem value="">
                      <em>Independent</em>
                    </MenuItem>
                    {conferences.map((conference) => (
                      <MenuItem key={conference} value={conference}>
                        {conference}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>

              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Team Logo URL"
                  value={formData.imageUrl}
                  onChange={handleInputChange('imageUrl')}
                  variant="outlined"
                  placeholder="https://example.com/team-logo.png"
                  sx={{
                    '& .MuiOutlinedInput-root': {
                      backgroundColor: alpha('#ffffff', 0.05),
                      '&:hover': {
                        backgroundColor: alpha('#ffffff', 0.08),
                      },
                    },
                  }}
                />
              </Grid>

              {/* Team Management */}
              <Grid item xs={12}>
                <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>
                  Team Management
                </Typography>
              </Grid>

              <Grid item xs={12}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={formData.isHuman}
                      onChange={handleSwitchChange('isHuman')}
                      color="primary"
                    />
                  }
                  label={
                    <Box>
                      <Typography variant="body1">
                        Human-Controlled Team
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Enable if this team is managed by a human player
                      </Typography>
                    </Box>
                  }
                />
              </Grid>

              {formData.isHuman && (
                <>
                  <Grid item xs={12} sm={6}>
                    <TextField
                      fullWidth
                      label="Head Coach"
                      value={formData.coach}
                      onChange={handleInputChange('coach')}
                      required={formData.isHuman}
                      variant="outlined"
                      sx={{
                        '& .MuiOutlinedInput-root': {
                          backgroundColor: alpha('#ffffff', 0.05),
                          '&:hover': {
                            backgroundColor: alpha('#ffffff', 0.08),
                          },
                        },
                      }}
                    />
                  </Grid>

                  <Grid item xs={12} sm={6}>
                    <TextField
                      fullWidth
                      label="Username"
                      value={formData.username}
                      onChange={handleInputChange('username')}
                      required={formData.isHuman}
                      variant="outlined"
                      sx={{
                        '& .MuiOutlinedInput-root': {
                          backgroundColor: alpha('#ffffff', 0.05),
                          '&:hover': {
                            backgroundColor: alpha('#ffffff', 0.08),
                          },
                        },
                      }}
                    />
                  </Grid>
                </>
              )}
            </Grid>
          )}
        </motion.div>
      </DialogContent>

      <DialogActions sx={{ p: 3, borderTop: `1px solid ${alpha('#ffffff', 0.1)}` }}>
        <Button
          onClick={handleClose}
          variant="outlined"
          sx={{
            borderColor: alpha('#ffffff', 0.3),
            color: 'white',
            '&:hover': {
              borderColor: alpha('#ffffff', 0.5),
              backgroundColor: alpha('#ffffff', 0.05),
            },
          }}
        >
          Cancel
        </Button>
        
        {mode === 'delete' ? (
          <GradientButton
            onClick={handleDelete}
            disabled={loading}
            startIcon={<Delete />}
            sx={{
              background: 'linear-gradient(135deg, #f44336 0%, #d32f2f 100%)',
              '&:hover': {
                background: 'linear-gradient(135deg, #d32f2f 0%, #c62828 100%)',
              },
            }}
          >
            {loading ? 'Deleting...' : 'Delete Team'}
          </GradientButton>
        ) : (
          <GradientButton
            onClick={handleSave}
            disabled={loading}
            startIcon={mode === 'create' ? <Add /> : <Save />}
          >
            {loading 
              ? (mode === 'create' ? 'Creating...' : 'Saving...') 
              : (mode === 'create' ? 'Create Team' : 'Save Changes')
            }
          </GradientButton>
        )}
      </DialogActions>
    </Dialog>
  );
};

export default TeamManagementModal;