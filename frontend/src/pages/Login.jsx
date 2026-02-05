import React, { useState } from 'react';
import {
    Box, Container, Paper, TextField, Button, Typography,
    Alert, Link, CircularProgress
} from '@mui/material';
import axios from 'axios';

const API_BASE = 'http://localhost:8081/is-1-1.0-SNAPSHOT/api';

function Login({ onLogin, onSwitchToRegister }) {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            const res = await axios.post(`${API_BASE}/auth/login`, {
                username,
                password
            });

            const { token, userId, username: user, role } = res.data;
            localStorage.setItem('token', token);
            localStorage.setItem('user', JSON.stringify({ userId, username: user, role }));

            onLogin({ userId, username: user, role, token });
        } catch (err) {
            setError(err.response?.data?.error || 'Ошибка входа');
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container maxWidth="sm">
            <Box sx={{ mt: 8, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                <Paper elevation={3} sx={{ p: 4, width: '100%' }}>
                    <Typography component="h1" variant="h5" align="center" gutterBottom>
                        Вход в систему
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
                            autoComplete="current-password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                        />
                        <Button
                            type="submit"
                            fullWidth
                            variant="contained"
                            sx={{ mt: 3, mb: 2 }}
                            disabled={loading}
                        >
                            {loading ? <CircularProgress size={24} /> : 'Войти'}
                        </Button>
                        <Box sx={{ textAlign: 'center' }}>
                            <Link
                                component="button"
                                variant="body2"
                                onClick={onSwitchToRegister}
                                type="button"
                            >
                                Нет аккаунта? Зарегистрируйтесь
                            </Link>
                        </Box>
                    </Box>
                </Paper>
            </Box>
        </Container>
    );
}

export default Login;

