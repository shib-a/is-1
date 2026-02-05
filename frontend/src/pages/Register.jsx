import React, { useState } from 'react';
import {
    Box, Container, Paper, TextField, Button, Typography,
    Alert, Link, CircularProgress
} from '@mui/material';
import axios from 'axios';

const API_BASE = 'http://localhost:8081/is-1-1.0-SNAPSHOT/api';

function Register({ onRegister, onSwitchToLogin }) {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        if (password !== confirmPassword) {
            setError('Пароли не совпадают');
            return;
        }

        if (password.length < 4) {
            setError('Пароль должен быть не менее 4 символов');
            return;
        }

        setLoading(true);

        try {
            const res = await axios.post(`${API_BASE}/auth/register`, {
                username,
                password
            });

            const { token, userId, username: user, role } = res.data;
            localStorage.setItem('token', token);
            localStorage.setItem('user', JSON.stringify({ userId, username: user, role }));

            onRegister({ userId, username: user, role, token });
        } catch (err) {
            setError(err.response?.data?.error || 'Ошибка регистрации');
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container maxWidth="sm">
            <Box sx={{ mt: 8, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                <Paper elevation={3} sx={{ p: 4, width: '100%' }}>
                    <Typography component="h1" variant="h5" align="center" gutterBottom>
                        Регистрация
                    </Typography>

                    {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

                    <Box component="form" onSubmit={handleSubmit} sx={{ mt: 1 }}>
                        <TextField
                            margin="normal"
                            required
                            fullWidth
                            id="username"
                            label="Имя пользователя"
                            name="username"
                            autoComplete="username"
                            autoFocus
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                        />
                        <TextField
                            margin="normal"
                            required
                            fullWidth
                            name="password"
                            label="Пароль"
                            type="password"
                            id="password"
                            autoComplete="new-password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            helperText="Минимум 4 символа"
                        />
                        <TextField
                            margin="normal"
                            required
                            fullWidth
                            name="confirmPassword"
                            label="Подтвердите пароль"
                            type="password"
                            id="confirmPassword"
                            autoComplete="new-password"
                            value={confirmPassword}
                            onChange={(e) => setConfirmPassword(e.target.value)}
                        />
                        <Button
                            type="submit"
                            fullWidth
                            variant="contained"
                            sx={{ mt: 3, mb: 2 }}
                            disabled={loading}
                        >
                            {loading ? <CircularProgress size={24} /> : 'Зарегистрироваться'}
                        </Button>
                        <Box sx={{ textAlign: 'center' }}>
                            <Link
                                component="button"
                                variant="body2"
                                onClick={onSwitchToLogin}
                                type="button"
                            >
                                Уже есть аккаунт? Войдите
                            </Link>
                        </Box>
                    </Box>
                </Paper>
            </Box>
        </Container>
    );
}

export default Register;

