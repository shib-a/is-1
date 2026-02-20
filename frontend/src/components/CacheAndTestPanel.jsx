import React, { useState } from 'react';
import {
    Box, Button, Card, CardContent, Typography, CircularProgress,
    Dialog, DialogTitle, DialogContent, DialogActions, Alert, Grid
} from '@mui/material';
import {
    Storage as StorageIcon, Settings as SettingsIcon,
    Speed as SpeedIcon, CheckCircle as CheckCircleIcon,
    Error as ErrorIcon, Settings as TestIcon
} from '@mui/icons-material';
import axios from 'axios';

const API_BASE = 'http://localhost:25203/is-1-1.0-SNAPSHOT/api';

function CacheAndTestPanel() {
    const [cacheStats, setCacheStats] = useState(null);
    const [loggingEnabled, setLoggingEnabled] = useState(false);
    const [loading, setLoading] = useState(false);
    const [testResults, setTestResults] = useState(null);
    const [openDialog, setOpenDialog] = useState(false);
    const [selectedTest, setSelectedTest] = useState(null);

    const getCacheStats = async () => {
        setLoading(true);
        try {
            const response = await axios.get(`${API_BASE}/cache/stats`);
            setCacheStats(response.data);
        } catch (error) {
            console.error('Failed to fetch cache stats:', error);
            setCacheStats({ error: 'Failed to fetch cache statistics' });
        }
        setLoading(false);
    };

    const toggleCacheLogging = async () => {
        setLoading(true);
        try {
            const endpoint = loggingEnabled
                ? `${API_BASE}/cache/logging/disable`
                : `${API_BASE}/cache/logging/enable`;
            const response = await axios.post(endpoint);
            setLoggingEnabled(!loggingEnabled);
            setCacheStats(response.data);
        } catch (error) {
            console.error('Failed to toggle logging:', error);
        }
        setLoading(false);
    };

    const clearCache = async () => {
        setLoading(true);
        try {
            await axios.post(`${API_BASE}/cache/clear`);
            setCacheStats(null);
            alert('Cache cleared successfully');
        } catch (error) {
            console.error('Failed to clear cache:', error);
            alert('Failed to clear cache');
        }
        setLoading(false);
    };

    const resetCacheStats = async () => {
        setLoading(true);
        try {
            await axios.post(`${API_BASE}/cache/stats/reset`);
            setCacheStats(null);
            alert('Cache statistics reset');
        } catch (error) {
            console.error('Failed to reset stats:', error);
            alert('Failed to reset statistics');
        }
        setLoading(false);
    };

    const runTest = async (testType) => {
        setLoading(true);
        try {
            const endpoint = testType === 'db-failure'
                ? `${API_BASE}/test/2pc/test-db-failure`
                : `${API_BASE}/test/2pc/test-business-logic-failure`;
            const response = await axios.post(endpoint);
            setTestResults(response.data);
            setOpenDialog(true);
        } catch (error) {
            setTestResults({
                status: 'ERROR',
                message: error.message,
                testPassed: false
            });
            setOpenDialog(true);
        }
        setLoading(false);
    };

    return (
        <Box sx={{ padding: 2 }}>
            <Grid container spacing={3}>
                {/* Cache Management Card */}
                <Grid item xs={12} md={6}>
                    <Card>
                        <CardContent>
                            <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                <StorageIcon /> L2 Cache Management
                            </Typography>

                            <Box sx={{ marginY: 2, display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                                <Button
                                    variant="contained"
                                    startIcon={loading ? <CircularProgress size={20} /> : <SpeedIcon />}
                                    onClick={getCacheStats}
                                    disabled={loading}
                                >
                                    Get Cache Stats
                                </Button>
                                <Button
                                    variant="contained"
                                    color={loggingEnabled ? 'error' : 'success'}
                                    startIcon={<SettingsIcon />}
                                    onClick={toggleCacheLogging}
                                    disabled={loading}
                                >
                                    {loggingEnabled ? 'Disable' : 'Enable'} Logging
                                </Button>
                            </Box>

                            <Box sx={{ marginY: 2, display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                                <Button
                                    variant="outlined"
                                    color="warning"
                                    onClick={clearCache}
                                    disabled={loading}
                                >
                                    Clear Cache
                                </Button>
                                <Button
                                    variant="outlined"
                                    color="info"
                                    onClick={resetCacheStats}
                                    disabled={loading}
                                >
                                    Reset Stats
                                </Button>
                            </Box>

                            {cacheStats && (
                                <Box sx={{ marginTop: 2, padding: 1, backgroundColor: '#f5f5f5', borderRadius: 1 }}>
                                    <Typography variant="body2">
                                        <strong>Cache Hits:</strong> {cacheStats.hits || 0}
                                    </Typography>
                                    <Typography variant="body2">
                                        <strong>Cache Misses:</strong> {cacheStats.misses || 0}
                                    </Typography>
                                    <Typography variant="body2">
                                        <strong>Logging Enabled:</strong> {cacheStats.loggingEnabled ? 'Yes' : 'No'}
                                    </Typography>
                                </Box>
                            )}
                        </CardContent>
                    </Card>
                </Grid>

                {/* 2PC Test Card */}
                <Grid item xs={12} md={6}>
                    <Card>
                        <CardContent>
                            <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                <TestIcon /> Two-Phase Commit Tests
                            </Typography>

                            <Typography variant="body2" color="textSecondary" gutterBottom>
                                Test distributed transaction handling between PostgreSQL and MinIO
                            </Typography>

                            <Box sx={{ marginY: 2, display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                                <Button
                                    variant="contained"
                                    color="error"
                                    startIcon={loading ? <CircularProgress size={20} /> : <ErrorIcon />}
                                    onClick={() => runTest('db-failure')}
                                    disabled={loading}
                                >
                                    Test DB Failure
                                </Button>
                                <Button
                                    variant="contained"
                                    color="warning"
                                    startIcon={loading ? <CircularProgress size={20} /> : <ErrorIcon />}
                                    onClick={() => runTest('business-logic-failure')}
                                    disabled={loading}
                                >
                                    Test Logic Failure
                                </Button>
                            </Box>

                            <Typography variant="caption" color="textSecondary" sx={{ display: 'block', marginTop: 1 }}>
                                • DB Failure: Tests rollback when database fails<br />
                                • Logic Failure: Tests rollback when business logic fails
                            </Typography>
                        </CardContent>
                    </Card>
                </Grid>
            </Grid>

            {/* Test Results Dialog */}
            <Dialog open={openDialog} onClose={() => setOpenDialog(false)} maxWidth="sm" fullWidth>
                <DialogTitle sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    {testResults?.testPassed ? (
                        <>
                            <CheckCircleIcon color="success" />
                            Test Passed
                        </>
                    ) : (
                        <>
                            <ErrorIcon color="error" />
                            Test Failed
                        </>
                    )}
                </DialogTitle>
                <DialogContent>
                    {testResults && (
                        <Box sx={{ marginTop: 2 }}>
                            <Alert severity={testResults.testPassed ? 'success' : 'error'} sx={{ marginBottom: 2 }}>
                                {testResults.message}
                            </Alert>
                            <Typography variant="body2">
                                <strong>Status:</strong> {testResults.status}
                            </Typography>
                            <Typography variant="body2">
                                <strong>Test File:</strong> {testResults.testFileName}
                            </Typography>
                            <Typography variant="body2">
                                <strong>MinIO File Created:</strong> {testResults.minioFileCreated ? 'Yes' : 'No'}
                            </Typography>
                        </Box>
                    )}
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setOpenDialog(false)} variant="contained">
                        Close
                    </Button>
                </DialogActions>
            </Dialog>
        </Box>
    );
}

export default CacheAndTestPanel;

