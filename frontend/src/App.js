// App.js (JavaScript + JSX)
import React, { useState, useEffect, useCallback } from 'react';
import axios from 'axios';
import {
    Table, TableHead, TableBody, TableRow, TableCell, TablePagination, TableSortLabel,
    TextField, InputAdornment, Button, Dialog, DialogTitle, DialogContent, DialogActions,
    MenuItem, Select, InputLabel, FormControl, CircularProgress, Box, Typography, Grid,
    Chip, IconButton, FormControlLabel, Checkbox, Fab
} from '@mui/material';
import {
    Search as SearchIcon, Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon,
    Business as OrgIcon, Person as PersonIcon
} from '@mui/icons-material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import dayjs from 'dayjs';

const API_BASE = 'http://localhost:8081/is-1-1.0-SNAPSHOT/api'; // ← поменяйте, если нужно
const POLLING_INTERVAL = 5000; // 5 секунд

function App() {
    const [workers, setWorkers] = useState([]);
    const [organizations, setOrganizations] = useState([]);
    const [page, setPage] = useState(0);
    const [rowsPerPage, setRowsPerPage] = useState(20);
    const [total, setTotal] = useState(0);
    const [sort, setSort] = useState('id');
    const [dir, setDir] = useState('asc');
    const [filter, setFilter] = useState('');
    const [loading, setLoading] = useState(false);

    // Worker CRUD modal
    const [workerModalOpen, setWorkerModalOpen] = useState(false);
    const [editingWorker, setEditingWorker] = useState(null);
    const [workerForm, setWorkerForm] = useState({});

    // Organization creation modal (inside worker modal)
    const [orgModalOpen, setOrgModalOpen] = useState(false);
    const [newOrgForm, setNewOrgForm] = useState({
        fullName: '',
        annualTurnover: 0,
        rating: 0,
        employeesCount: 0,
        officialAddress: { street: '', town: { x: 0, y: 0, z: 0, name: '' } }
    });

    // Person toggle
    const [hasPerson, setHasPerson] = useState(false);

    // Special searches
    const [searchNameContains, setSearchNameContains] = useState('');
    const [searchNameStarts, setSearchNameStarts] = useState('');
    const [searchRatingLess, setSearchRatingLess] = useState('');
    const [specialResults, setSpecialResults] = useState([]);

    const loadOrganizations = async () => {
        try {
            const res = await axios.get(`${API_BASE}/organizations`);
            setOrganizations(res.data || []);
        } catch (e) {
            console.error(e);
        }
    };


const loadWorkers = useCallback(async () => {
    setLoading(true);
    try {
        const params = new URLSearchParams({
            page: page.toString(),
            size: rowsPerPage.toString(),
            sort,
            dir,
            ...(filter && { filter }),
        });
        const res = await axios.get(`${API_BASE}/workers?${params}`);
        setWorkers(res.data.content || []);
        setTotal(res.data.totalElements || 0);
    } catch (e) {
        console.error(e);
    }
    setLoading(false);
}, [page, rowsPerPage, sort, dir, filter]);

// Initial + polling
useEffect(() => {
    loadWorkers();
    loadOrganizations();

    const interval = setInterval(loadWorkers, POLLING_INTERVAL);
    return () => clearInterval(interval);
}, [loadWorkers]);

const openWorkerModal = (worker = null) => {
    setEditingWorker(worker);
    const hasPerson = !!worker?.person;
    setHasPerson(hasPerson);
    setWorkerForm(worker || {
        name: '',
        coordinates: { x: 0, y: 0 },
        salary: 0,
        rating: 0,
        startDate: dayjs().format('YYYY-MM-DD'),
        endDate: null,
        position: '',
        organization: organizations[0] || null,
        person: hasPerson ? worker.person : null
    });
    setWorkerModalOpen(true);
};

const createOrganization = async () => {
    try {
        const res = await axios.post(`${API_BASE}/organizations`, newOrgForm);
        await loadOrganizations(); // обновляем список
        setWorkerForm({ ...workerForm, organization: res.data });
        setOrgModalOpen(false);
        setNewOrgForm({
            fullName: '',
            annualTurnover: 0,
            rating: 0,
            employeesCount: 0,
            officialAddress: { street: '', town: { x: 0, y: 0, z: 0, name: '' } }
        });
    } catch (e) {
        console.error(e);
    }
};

const saveWorker = async () => {
    try {
        // Если Person не нужен — ставим null
        const payload = {
            ...workerForm,
            person: hasPerson ? workerForm.person : null
        };

        if (editingWorker) {
            await axios.put(`${API_BASE}/workers/update?id=${editingWorker.id}`, payload);
        } else {
            await axios.post(`${API_BASE}/workers`, payload);
        }
        setWorkerModalOpen(false);
        loadWorkers();
    } catch (e) {
        console.error(e);
    }
};

const deleteWorker = async (id) => {
    if (window.confirm('Удалить работника?')) {
        await axios.delete(`${API_BASE}/workers/delete?id=${id}`);
        loadWorkers();
    }
};

const runSpecialSearch = async (type) => {
    let url = '';
    if (type === 'contains') url = `${API_BASE}/workers/search/name-contains?q=${encodeURIComponent(searchNameContains)}`;
    if (type === 'starts') url = `${API_BASE}/workers/search/name-starts?q=${encodeURIComponent(searchNameStarts)}`;
    if (type === 'rating') url = `${API_BASE}/workers/search/rating-less?value=${searchRatingLess}`;

    if (!url) return;
    try {
        const res = await axios.get(url);
        setSpecialResults(res.data);
    } catch (e) {
        console.error(e);
    }
};

const columns = [ /* тот же массив, что и раньше */ ];

return (
    <LocalizationProvider dateAdapter={AdapterDayjs}>
        <Box p={4}>
            <Typography variant="h4" gutterBottom>Управление работниками</Typography>

            {/* Глобальный поиск */}
            <TextField
                fullWidth
                margin="normal"
                label="Поиск"
                value={filter}
                onChange={e => { setFilter(e.target.value); setPage(0); }}
                InputProps={{ startAdornment: <InputAdornment position="start"><SearchIcon /></InputAdornment> }}
            />

            <Button variant="contained" startIcon={<AddIcon />} onClick={() => openWorkerModal()} sx={{ mb: 2 }}>
                Добавить работника
            </Button>

            {/* Таблица и пагинация — без изменений */}

            {/* Специальные операции — без изменений */}

            {/* Модальное окно Worker */}
            <Dialog open={workerModalOpen} onClose={() => setWorkerModalOpen(false)} maxWidth="md" fullWidth>
                <DialogTitle>{editingWorker ? 'Редактировать' : 'Создать'} работника</DialogTitle>
                <DialogContent dividers>
                    <Grid container spacing={2}>
                        {/* Основные поля Worker */}
                        <Grid item xs={6}><TextField fullWidth label="Имя" value={workerForm.name || ''} onChange={e => setWorkerForm({ ...workerForm, name: e.target.value })} /></Grid>

                        <Grid item xs={6}>
                            <FormControl fullWidth>
                                <InputLabel>Организация</InputLabel>
                                <Select
                                    value={workerForm.organization?.id || ''}
                                    onChange={e => setWorkerForm({ ...workerForm, organization: organizations.find(o => o.id === e.target.value) || null })}
                                >
                                    {organizations.map(o => (
                                        <MenuItem key={o.id} value={o.id}>{o.fullName || `ID ${o.id}`}</MenuItem>
                                    ))}
                                </Select>
                            </FormControl>
                            <Button size="small" startIcon={<AddIcon />} onClick={() => setOrgModalOpen(true)} sx={{ mt: 1 }}>
                                Новая организация
                            </Button>
                        </Grid>

                        {/* Остальные поля Worker (salary, rating, dates, coordinates, position) — аналогично предыдущему коду */}

                        {/* Person toggle */}
                        <Grid item xs={12}>
                            <FormControlLabel
                                control={<Checkbox checked={hasPerson} onChange={e => setHasPerson(e.target.checked)} />}
                                label="Добавить персональные данные"
                            />
                        </Grid>

                        {hasPerson && (
                            <>
                                <Grid item xs={6}><TextField fullWidth label="Passport ID" value={workerForm.person?.passportID || ''} onChange={e => setWorkerForm({ ...workerForm, person: { ...workerForm.person || {}, passportID: e.target.value } })} /></Grid>
                                <Grid item xs={6}><TextField fullWidth label="Height" type="number" value={workerForm.person?.height || ''} onChange={e => setWorkerForm({ ...workerForm, person: { ...workerForm.person || {}, height: +e.target.value } })} /></Grid>
                                {/* Добавьте остальные поля Person по аналогии */}
                            </>
                        )}
                    </Grid>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setWorkerModalOpen(false)}>Отмена</Button>
                    <Button variant="contained" onClick={saveWorker}>Сохранить</Button>
                </DialogActions>
            </Dialog>

            {/* Модальное окно создания Organization */}
            <Dialog open={orgModalOpen} onClose={() => setOrgModalOpen(false)}>
                <DialogTitle>Новая организация</DialogTitle>
                <DialogContent dividers>
                    <Grid container spacing={2}>
                        <Grid item xs={12}><TextField fullWidth label="Полное название" value={newOrgForm.fullName} onChange={e => setNewOrgForm({ ...newOrgForm, fullName: e.target.value })} /></Grid>
                        <Grid item xs={6}><TextField fullWidth label="Годовой оборот" type="number" value={newOrgForm.annualTurnover} onChange={e => setNewOrgForm({ ...newOrgForm, annualTurnover: +e.target.value })} /></Grid>
                        <Grid item xs={6}><TextField fullWidth label="Рейтинг" type="number" step="0.01" value={newOrgForm.rating} onChange={e => setNewOrgForm({ ...newOrgForm, rating: +e.target.value })} /></Grid>
                        <Grid item xs={12}><TextField fullWidth label="Улица" value={newOrgForm.officialAddress.street} onChange={e => setNewOrgForm({ ...newOrgForm, officialAddress: { ...newOrgForm.officialAddress, street: e.target.value } })} /></Grid>
                        <Grid item xs={12}><TextField fullWidth label="Город" value={newOrgForm.officialAddress.town.name || ''} onChange={e => setNewOrgForm({ ...newOrgForm, officialAddress: { ...newOrgForm.officialAddress, town: { ...newOrgForm.officialAddress.town, name: e.target.value } } })} /></Grid>
                        {/* Координаты города — добавьте по желанию */}
                    </Grid>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setOrgModalOpen(false)}>Отмена</Button>
                    <Button variant="contained" onClick={createOrganization}>Создать и выбрать</Button>
                </DialogActions>
            </Dialog>
        </Box>
    </LocalizationProvider>
);
}

export default App;