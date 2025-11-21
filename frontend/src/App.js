import React, { useState, useEffect, useCallback } from 'react';
import axios from 'axios';
import {
    Table, TableHead, TableBody, TableRow, TableCell, TablePagination, TableSortLabel,
    TextField, InputAdornment, Button, Dialog, DialogTitle, DialogContent, DialogActions,
    MenuItem, Select, InputLabel, FormControl, CircularProgress, Box, Typography, Grid,
    Chip, IconButton, FormControlLabel, Checkbox
} from '@mui/material';
import {
    Search as SearchIcon, Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon
} from '@mui/icons-material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import dayjs from 'dayjs';

const API_BASE = 'http://localhost:8081/is-1-1.0-SNAPSHOT/api';
const POLLING_INTERVAL = 5000;

function App() {
    const [workers, setWorkers] = useState([]);
    const [organizations, setOrganizations] = useState([]);
    const [addresses, setAddresses] = useState([]);
    const [locations, setLocations] = useState([]);
    const [coordinatesList, setCoordinatesList] = useState([]);
    const [persons, setPersons] = useState([]);

    const [page, setPage] = useState(0);
    const [rowsPerPage, setRowsPerPage] = useState(20);
    const [total, setTotal] = useState(0);
    const [sort, setSort] = useState('id');
    const [dir, setDir] = useState('asc');
    const [filter, setFilter] = useState('');
    const [loading, setLoading] = useState(false);

    // Modals
    const [workerModalOpen, setWorkerModalOpen] = useState(false);
    const [editingWorker, setEditingWorker] = useState(null);
    const [workerForm, setWorkerForm] = useState({});

    const [orgModalOpen, setOrgModalOpen] = useState(false);
    const [addressModalOpen, setAddressModalOpen] = useState(false);
    const [locationModalOpen, setLocationModalOpen] = useState(false);
    const [coordsModalOpen, setCoordsModalOpen] = useState(false);
    const [personModalOpen, setPersonModalOpen] = useState(false);

    const [newOrg, setNewOrg] = useState({
        fullName: '',
        annualTurnover: 0,
        rating: 0,
        employeesCount: 1,
        officialAddress: null
    });

    const [newAddress, setNewAddress] = useState({ street: '', town: null });
    const [newLocation, setNewLocation] = useState({ x: 0, y: 0, z: 0, name: '' });
    const [newCoords, setNewCoords] = useState({ x: 0, y: 0 });
    const [newPerson, setNewPerson] = useState({
        passportID: '',
        height: 170,
        eyeColor: 'BLACK',
        hairColor: 'BLACK',
        nationality: null,
        location: null
    });

    const [hasPerson, setHasPerson] = useState(false);

    // Special searches
    const [searchNameContains, setSearchNameContains] = useState('');
    const [searchNameStarts, setSearchNameStarts] = useState('');
    const [searchRatingLess, setSearchRatingLess] = useState('');
    const [specialResults, setSpecialResults] = useState([]);

    const loadAllData = async () => {
        try {
            const [org, addr, loc, coord, person] = await Promise.all([
                axios.get(`${API_BASE}/organizations/recent`),
                axios.get(`${API_BASE}/addresses/recent`),
                axios.get(`${API_BASE}/locations/recent`),
                axios.get(`${API_BASE}/coordinates/recent`),
                axios.get(`${API_BASE}/persons/recent`),
            ]);
            setOrganizations(org.data || []);
            setAddresses(addr.data || []);
            setLocations(loc.data || []);
            setCoordinatesList(coord.data || []);
            setPersons(person.data || []);
        } catch (e) {
            console.error('Load data error', e);
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

    useEffect(() => {
        loadWorkers();
        const interval = setInterval(loadWorkers, POLLING_INTERVAL);
        return () => clearInterval(interval);
    }, [loadWorkers]);

    useEffect(() => {
        loadAllData();
        const interval = setInterval(loadAllData, POLLING_INTERVAL);
        return () => clearInterval(interval);
    }, [loadWorkers]);

    const openWorkerModal = (worker = null) => {
        setEditingWorker(worker);
        const hasP = !!worker?.person;
        setHasPerson(hasP);
        setWorkerForm(worker || {
            name: '',
            coordinates: null,
            salary: 0,
            rating: 0,
            startDate: dayjs().format('YYYY-MM-DD'),
            endDate: null,
            position: '',
            organization: null,
            person: null
        });
        setWorkerModalOpen(true);
    };

    const createAndSelect = async (type, data, setter, targetField = null, nestedSetter = null, nestedField = null) => {
        try {
            let endpoint = '';
            if (type === 'organization') endpoint = '/organizations';
            if (type === 'address') endpoint = '/addresses';
            if (type === 'location') endpoint = '/locations';
            if (type === 'coordinates') endpoint = '/coordinates';
            if (type === 'person') endpoint = '/persons';

            const res = await axios.post(`${API_BASE}${endpoint}`, data);
            const created = res.data;

            setter(prev => [...prev, created]);

            if (targetField) {
                setWorkerForm(prev => ({ ...prev, [targetField]: created }));
            }
            if (nestedSetter && nestedField) {
                nestedSetter(prev => ({ ...prev, [nestedField]: created }));
            }

            await loadAllData();

            setOrgModalOpen(false);
            setAddressModalOpen(false);
            setLocationModalOpen(false);
            setCoordsModalOpen(false);
            setPersonModalOpen(false);
        } catch (e) {
            console.error(e);
            alert('Ошибка создания');
        }
    };

    const saveWorker = async () => {
        try {
            const payload = {
                ...workerForm,
                person: hasPerson ? workerForm.person : null
            };
            const np = {
                name: workerForm.name,
                coordinates: workerForm.coordinates.id,
                salary: workerForm.salary,
                rating: workerForm.rating,
                startDate: workerForm.startDate,
                endDate: workerForm.endDate,
                position: workerForm.position,
                organization: workerForm.organization.id,
                person: workerForm.person.id
            }

            if (editingWorker) {
                await axios.put(`${API_BASE}/workers/update?id=${editingWorker.id}`, payload);
            } else {
                await axios.post(`${API_BASE}/workers`, payload);
            }
            setWorkerModalOpen(false);
            loadWorkers();
        } catch (e) {
            console.error(e);
            alert('Ошибка сохранения');
        }
    };

    const deleteWorker = async (id) => {
        if (window.confirm('Удалить работника?')) {
            try {
                await axios.delete(`${API_BASE}/workers/delete?id=${id}`);
                loadWorkers();
            } catch (e) {
                console.error(e);
            }
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

    const columns = [
        { id: 'id', label: 'ID' },
        { id: 'name', label: 'Имя' },
        { id: 'coordinates', label: 'Координаты', render: w => `${w.coordinates?.x}, ${w.coordinates?.y}` },
        { id: 'creationDate', label: 'Создан' },
        { id: 'organization', label: 'Организация', render: w => w.organization?.fullName || `ID ${w.organization?.id}` },
        { id: 'salary', label: 'Зарплата' },
        { id: 'rating', label: 'Рейтинг' },
        { id: 'position', label: 'Должность' },
        { id: 'actions', label: 'Действия', render: w => (
                <>
                    <IconButton size="small" onClick={() => openWorkerModal(w)}><EditIcon /></IconButton>
                    <IconButton size="small" onClick={() => deleteWorker(w.id)}><DeleteIcon /></IconButton>
                </>
            )}
    ];

    return (
        <LocalizationProvider dateAdapter={AdapterDayjs}>
            <Box p={4}>
                <Typography variant="h4" gutterBottom>Управление работниками</Typography>

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

                {loading ? <CircularProgress /> : (
                    <>
                        <Table>
                            <TableHead>
                                <TableRow>
                                    {columns.map(col => (
                                        <TableCell key={col.id}>
                                            {col.id !== 'actions' ? (
                                                <TableSortLabel
                                                    active={sort === col.id}
                                                    direction={dir}
                                                    onClick={() => {
                                                        setDir(sort === col.id && dir === 'asc' ? 'desc' : 'asc');
                                                        setSort(col.id);
                                                    }}
                                                >
                                                    {col.label}
                                                </TableSortLabel>
                                            ) : col.label}
                                        </TableCell>
                                    ))}
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {workers.map(w => (
                                    <TableRow key={w.id}>
                                        {columns.map(col => (
                                            <TableCell key={col.id}>
                                                {col.render ? col.render(w) : w[col.id]}
                                            </TableCell>
                                        ))}
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>

                        <TablePagination
                            count={total}
                            page={page}
                            onPageChange={(_, p) => setPage(p)}
                            rowsPerPage={rowsPerPage}
                            onRowsPerPageChange={e => { setRowsPerPage(+e.target.value); setPage(0); }}
                        />
                    </>
                )}

                {/* Специальные операции */}
                <Box mt={6} p={3} border={1} borderColor="grey.300" borderRadius={2}>
                    <Typography variant="h6" gutterBottom>Специальные операции</Typography>
                    <Grid container spacing={2}>
                        <Grid item xs={12} md={4}>
                            <TextField fullWidth label="Name contains" value={searchNameContains} onChange={e => setSearchNameContains(e.target.value)} />
                            <Button onClick={() => runSpecialSearch('contains')} sx={{ mt: 1 }}>Найти</Button>
                        </Grid>
                        <Grid item xs={12} md={4}>
                            <TextField fullWidth label="Name starts" value={searchNameStarts} onChange={e => setSearchNameStarts(e.target.value)} />
                            <Button onClick={() => runSpecialSearch('starts')} sx={{ mt: 1 }}>Найти</Button>
                        </Grid>
                        <Grid item xs={12} md={4}>
                            <TextField fullWidth label="Rating <" type="number" value={searchRatingLess} onChange={e => setSearchRatingLess(e.target.value)} />
                            <Button onClick={() => runSpecialSearch('rating')} sx={{ mt: 1 }}>Найти</Button>
                        </Grid>
                    </Grid>
                    {specialResults.length > 0 && (
                        <Box mt={3}>
                            <Typography>Результаты: {specialResults.length}</Typography>
                            {specialResults.map(w => (
                                <Chip key={w.id} label={w.name} sx={{ m: 0.5 }} />
                            ))}
                        </Box>
                    )}
                </Box>

                {/* Worker modal */}
                <Dialog open={workerModalOpen} onClose={() => setWorkerModalOpen(false)} maxWidth="lg" fullWidth>
                    <DialogTitle>{editingWorker ? 'Редактировать' : 'Создать'} работника</DialogTitle>
                    <DialogContent dividers>
                        <Grid container spacing={3}>
                            <Grid item xs={12} md={6}>
                                <TextField fullWidth label="Имя" value={workerForm.name || ''} onChange={e => setWorkerForm({ ...workerForm, name: e.target.value })} />
                            </Grid>
                            <Grid item xs={12} md={6}>
                                <TextField fullWidth label="Зарплата" type="number" value={workerForm.salary || ''} onChange={e => setWorkerForm({ ...workerForm, salary: +e.target.value })} />
                            </Grid>
                            <Grid item xs={12} md={6}>
                                <TextField fullWidth label="Рейтинг" type="number" step="0.01" value={workerForm.rating || ''} onChange={e => setWorkerForm({ ...workerForm, rating: +e.target.value })} />
                            </Grid>
                            <Grid item xs={12} md={6}>
                                <DatePicker label="Дата начала" value={dayjs(workerForm.startDate)} onChange={d => setWorkerForm({ ...workerForm, startDate: d?.format('YYYY-MM-DD') })} />
                            </Grid>
                            <Grid item xs={12} md={6}>
                                <DatePicker label="Дата окончания" value={workerForm.endDate ? dayjs(workerForm.endDate) : null} onChange={d => setWorkerForm({ ...workerForm, endDate: d?.format('YYYY-MM-DD') || null })} />
                            </Grid>
                            <Grid item xs={12} md={6}>
                                <FormControl fullWidth>
                                    <InputLabel>Должность</InputLabel>
                                    <Select value={workerForm.position || ''} onChange={e => setWorkerForm({ ...workerForm, position: e.target.value })}>
                                        <MenuItem value="LABORER">LABORER</MenuItem>
                                        <MenuItem value="HUMAN_RESOURCES">HUMAN_RESOURCES</MenuItem>
                                        <MenuItem value="HEAD_OF_DEPARTMENT">HEAD_OF_DEPARTMENT</MenuItem>
                                    </Select>
                                </FormControl>
                            </Grid>

                            {/* Координаты */}
                            <Grid item xs={12} md={6}>
                                <FormControl fullWidth>
                                    <InputLabel>Координаты</InputLabel>
                                    <Select value={workerForm.coordinates?.id || ''} onChange={e => {
                                        const sel = coordinatesList.find(c => c.id === e.target.value);
                                        setWorkerForm({ ...workerForm, coordinates: sel || null });
                                    }}>
                                        {coordinatesList.map(c => (
                                            <MenuItem key={c.id} value={c.id}>{c.x}, {c.y}</MenuItem>
                                        ))}
                                    </Select>
                                </FormControl>
                                <Button size="small" startIcon={<AddIcon />} onClick={() => setCoordsModalOpen(true)} sx={{ mt: 1 }}>
                                    Новые координаты
                                </Button>
                            </Grid>

                            {/* Организация */}
                            <Grid item xs={12} md={6}>
                                <FormControl fullWidth>
                                    <InputLabel>Организация</InputLabel>
                                    <Select value={workerForm.organization?.id || ''} onChange={e => {
                                        const sel = organizations.find(o => o.id === e.target.value);
                                        setWorkerForm({ ...workerForm, organization: sel || null });
                                    }}>
                                        {organizations.map(o => (
                                            <MenuItem key={o.id} value={o.id}>{o.fullName || `ID ${o.id}`}</MenuItem>
                                        ))}
                                    </Select>
                                </FormControl>
                                <Button size="small" startIcon={<AddIcon />} onClick={() => setOrgModalOpen(true)} sx={{ mt: 1 }}>
                                    Новая организация
                                </Button>
                            </Grid>

                            {/* Человек */}
                            <Grid item xs={12}>
                                <FormControlLabel
                                    control={<Checkbox checked={hasPerson} onChange={e => setHasPerson(e.target.checked)} />}
                                    label="Добавить человека"
                                />
                            </Grid>

                            {hasPerson && (
                                <Grid item xs={12} md={6}>
                                    <FormControl fullWidth>
                                        <InputLabel>Человек</InputLabel>
                                        <Select value={workerForm.person?.id || ''} onChange={e => {
                                            const sel = persons.find(p => p.id === e.target.value);
                                            setWorkerForm({ ...workerForm, person: sel || null });
                                        }}>
                                            {persons.map(p => (
                                                <MenuItem key={p.id} value={p.id}>
                                                    {p.passportID || 'Без паспорта'} (ID {p.id})
                                                </MenuItem>
                                            ))}
                                        </Select>
                                    </FormControl>
                                    <Button size="small" startIcon={<AddIcon />} onClick={() => setPersonModalOpen(true)} sx={{ mt: 1 }}>
                                        Новый человек
                                    </Button>
                                </Grid>
                            )}
                        </Grid>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={() => setWorkerModalOpen(false)}>Отмена</Button>
                        <Button variant="contained" onClick={saveWorker}>Сохранить</Button>
                    </DialogActions>
                </Dialog>

                {/* Модалка организации */}
                <Dialog open={orgModalOpen} onClose={() => setOrgModalOpen(false)} maxWidth="sm" fullWidth>
                    <DialogTitle>Новая организация</DialogTitle>
                    <DialogContent>
                        <Grid container spacing={2}>
                            <Grid item xs={12}><TextField fullWidth label="Название" value={newOrg.fullName} onChange={e => setNewOrg({ ...newOrg, fullName: e.target.value })} /></Grid>
                            <Grid item xs={6}><TextField fullWidth label="Оборот" type="number" value={newOrg.annualTurnover} onChange={e => setNewOrg({ ...newOrg, annualTurnover: +e.target.value })} /></Grid>
                            <Grid item xs={6}><TextField fullWidth label="Рейтинг" type="number" step="0.01" value={newOrg.rating} onChange={e => setNewOrg({ ...newOrg, rating: +e.target.value })} /></Grid>
                            <Grid item xs={12}><TextField fullWidth label="Кол-во сотрудников" type="number" value={newOrg.employeesCount} onChange={e => setNewOrg({ ...newOrg, employeesCount: +e.target.value })} /></Grid>
                            <Grid item xs={12}>
                                <FormControl fullWidth>
                                    <InputLabel>Адрес</InputLabel>
                                    <Select value={newOrg.officialAddress?.id || ''} onChange={e => setNewOrg({ ...newOrg, officialAddress: addresses.find(a => a.id === e.target.value) || null })}>
                                        {addresses.map(a => <MenuItem key={a.id} value={a.id}>{a.street}</MenuItem>)}
                                    </Select>
                                </FormControl>
                                <Button size="small" startIcon={<AddIcon />} onClick={() => setAddressModalOpen(true)} sx={{ mt: 1 }}>Новый адрес</Button>
                            </Grid>
                        </Grid>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={() => setOrgModalOpen(false)}>Отмена</Button>
                        <Button variant="contained" onClick={() => createAndSelect('organization', newOrg, setOrganizations, 'organization')}>Создать</Button>
                    </DialogActions>
                </Dialog>

                {/* Модалка адреса */}
                <Dialog open={addressModalOpen} onClose={() => setAddressModalOpen(false)} maxWidth="sm" fullWidth>
                    <DialogTitle>Новый адрес</DialogTitle>
                    <DialogContent>
                        <Grid container spacing={2}>
                            <Grid item xs={12}><TextField fullWidth label="Улица" value={newAddress.street} onChange={e => setNewAddress({ ...newAddress, street: e.target.value })} /></Grid>
                            <Grid item xs={12}>
                                <FormControl fullWidth>
                                    <InputLabel>Город</InputLabel>
                                    <Select value={newAddress.town?.id || ''} onChange={e => setNewAddress({ ...newAddress, town: locations.find(l => l.id === e.target.value) || null })}>
                                        {locations.map(l => <MenuItem key={l.id} value={l.id}>{l.name || `${l.x},${l.y}`}</MenuItem>)}
                                    </Select>
                                </FormControl>
                                <Button size="small" startIcon={<AddIcon />} onClick={() => setLocationModalOpen(true)} sx={{ mt: 1 }}>Новая локация</Button>
                            </Grid>
                        </Grid>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={() => setAddressModalOpen(false)}>Отмена</Button>
                        <Button variant="contained" onClick={() => createAndSelect('address', newAddress, setAddresses, null, setNewOrg, 'officialAddress')}>Создать</Button>
                    </DialogActions>
                </Dialog>

                {/* Модалка локации */}
                <Dialog open={locationModalOpen} onClose={() => setLocationModalOpen(false)} maxWidth="sm" fullWidth>
                    <DialogTitle>Новая локация</DialogTitle>
                    <DialogContent>
                        <Grid container spacing={2}>
                            <Grid item xs={4}><TextField fullWidth label="X" type="number" value={newLocation.x} onChange={e => setNewLocation({ ...newLocation, x: +e.target.value })} /></Grid>
                            <Grid item xs={4}><TextField fullWidth label="Y" type="number" value={newLocation.y} onChange={e => setNewLocation({ ...newLocation, y: +e.target.value })} /></Grid>
                            <Grid item xs={4}><TextField fullWidth label="Z" type="number" value={newLocation.z} onChange={e => setNewLocation({ ...newLocation, z: +e.target.value })} /></Grid>
                            <Grid item xs={12}><TextField fullWidth label="Название" value={newLocation.name || ''} onChange={e => setNewLocation({ ...newLocation, name: e.target.value })} /></Grid>
                        </Grid>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={() => setLocationModalOpen(false)}>Отмена</Button>
                        <Button variant="contained" onClick={() => createAndSelect('location', newLocation, setLocations, null, addressModalOpen ? setNewAddress : setNewPerson, 'town')}>Создать</Button>
                    </DialogActions>
                </Dialog>

                {/* Модалка координат */}
                <Dialog open={coordsModalOpen} onClose={() => setCoordsModalOpen(false)} maxWidth="sm" fullWidth>
                    <DialogTitle>Новые координаты</DialogTitle>
                    <DialogContent>
                        <Grid container spacing={2}>
                            <Grid item xs={6}><TextField fullWidth label="X (> -573)" type="number" value={newCoords.x} onChange={e => setNewCoords({ ...newCoords, x: +e.target.value })} /></Grid>
                            <Grid item xs={6}><TextField fullWidth label="Y (> -236)" type="number" value={newCoords.y} onChange={e => setNewCoords({ ...newCoords, y: +e.target.value })} /></Grid>
                        </Grid>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={() => setCoordsModalOpen(false)}>Отмена</Button>
                        <Button variant="contained" onClick={() => createAndSelect('coordinates', newCoords, setCoordinatesList, 'coordinates')}>Создать</Button>
                    </DialogActions>
                </Dialog>

                {/* Модалка человека */}
                <Dialog open={personModalOpen} onClose={() => setPersonModalOpen(false)} maxWidth="sm" fullWidth>
                    <DialogTitle>Новый человек</DialogTitle>
                    <DialogContent>
                        <Grid container spacing={2}>
                            <Grid item xs={12}><TextField fullWidth label="Паспорт" value={newPerson.passportID} onChange={e => setNewPerson({ ...newPerson, passportID: e.target.value })} /></Grid>
                            <Grid item xs={6}><TextField fullWidth label="Рост" type="number" value={newPerson.height} onChange={e => setNewPerson({ ...newPerson, height: +e.target.value })} /></Grid>
                            <Grid item xs={6}>
                                <FormControl fullWidth>
                                    <InputLabel>Цвет глаз</InputLabel>
                                    <Select value={newPerson.eyeColor} onChange={e => setNewPerson({ ...newPerson, eyeColor: e.target.value })}>
                                        <MenuItem value="RED">RED</MenuItem>
                                        <MenuItem value="BLACK">BLACK</MenuItem>
                                        <MenuItem value="YELLOW">YELLOW</MenuItem>
                                        <MenuItem value="ORANGE">ORANGE</MenuItem>
                                        <MenuItem value="WHITE">WHITE</MenuItem>
                                    </Select>
                                </FormControl>
                            </Grid>
                            <Grid item xs={6}>
                                <FormControl fullWidth>
                                    <InputLabel>Цвет волос</InputLabel>
                                    <Select value={newPerson.hairColor} onChange={e => setNewPerson({ ...newPerson, hairColor: e.target.value })}>
                                        <MenuItem value="RED">RED</MenuItem>
                                        <MenuItem value="BLACK">BLACK</MenuItem>
                                        <MenuItem value="YELLOW">YELLOW</MenuItem>
                                        <MenuItem value="ORANGE">ORANGE</MenuItem>
                                        <MenuItem value="WHITE">WHITE</MenuItem>
                                    </Select>
                                </FormControl>
                            </Grid>
                            <Grid item xs={6}>
                                <FormControl fullWidth>
                                    <InputLabel>Национальность</InputLabel>
                                    <Select value={newPerson.nationality || ''} onChange={e => setNewPerson({ ...newPerson, nationality: e.target.value || null })}>
                                        <MenuItem value="">Нет</MenuItem>
                                        <MenuItem value="RUSSIA">RUSSIA</MenuItem>
                                        <MenuItem value="UNITED_KINGDOM">UNITED_KINGDOM</MenuItem>
                                        <MenuItem value="FRANCE">FRANCE</MenuItem>
                                        <MenuItem value="INDIA">INDIA</MenuItem>
                                        <MenuItem value="THAILAND">THAILAND</MenuItem>
                                    </Select>
                                </FormControl>
                            </Grid>
                            <Grid item xs={12}>
                                <FormControl fullWidth>
                                    <InputLabel>Локация</InputLabel>
                                    <Select value={newPerson.location?.id || ''} onChange={e => setNewPerson({ ...newPerson, location: locations.find(l => l.id === e.target.value) || null })}>
                                        {locations.map(l => <MenuItem key={l.id} value={l.id}>{l.name || `${l.x},${l.y}`}</MenuItem>)}
                                    </Select>
                                </FormControl>
                                <Button size="small" startIcon={<AddIcon />} onClick={() => setLocationModalOpen(true)} sx={{ mt: 1 }}>Новая локация</Button>
                            </Grid>
                        </Grid>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={() => setPersonModalOpen(false)}>Отмена</Button>
                        <Button variant="contained" onClick={() => createAndSelect('person', newPerson, setPersons, 'person')}>Создать</Button>
                    </DialogActions>
                </Dialog>
            </Box>
        </LocalizationProvider>
    );
}

export default App;