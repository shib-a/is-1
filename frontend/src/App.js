import { useState, useEffect, useCallback } from 'react';
import React from 'react';
import axios from 'axios';
import {
    Table, TableHead, TableBody, TableRow, TableCell, TablePagination, TableSortLabel,
    TextField, InputAdornment, Button, Dialog, DialogTitle, DialogContent, DialogActions,
    MenuItem, Select, InputLabel, FormControl, CircularProgress, Box, Typography, Grid,
    Chip, IconButton, FormControlLabel, Checkbox, AppBar, Toolbar, Alert, Tabs, Tab
} from '@mui/material';
import {
    Search as SearchIcon, Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon,
    Download as DownloadIcon, Memory as MemoryIcon, CheckCircle as CheckCircleIcon, Error as ErrorIcon
} from '@mui/icons-material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import dayjs from 'dayjs';
import { Toaster, toast } from 'react-hot-toast';

const API_BASE = 'http://localhost:25203/is-1-1.0-SNAPSHOT/api';
const POLLING_INTERVAL = 5000;

function App() {

    const [currentEntity, setCurrentEntity] = useState('workers');
    const [data, setData] = useState([]);
    const [workers, setWorkers] = useState([]);
    const [organizations, setOrganizations] = useState([]);
    const [addresses, setAddresses] = useState([]);
    const [locations, setLocations] = useState([]);
    const [coordinatesList, setCoordinatesList] = useState([]);
    const [persons, setPersons] = useState([]);
    const [endDateSearch, setEndDateSearch] = useState(null);
    const [activeFilters, setActiveFilters] = useState({});
    const [showFilters, setShowFilters] = useState(false);

    const [indexWorkerId, setIndexWorkerId] = useState('');
    const [indexCoef, setIndexCoef] = useState('');
    const [indexOrgId, setIndexOrgId] = useState('');
    const [indexOrgCoef, setIndexOrgCoef] = useState('');

    const [page, setPage] = useState(0);
    const [rowsPerPage, setRowsPerPage] = useState(20);
    const [total, setTotal] = useState(0);
    const [sort, setSort] = useState('id');
    const [dir, setDir] = useState('asc');
    const [filter, setFilter] = useState('');
    const [loading, setLoading] = useState(false);

    const [editModalOpen, setEditModalOpen] = useState(false);
    const [editingEntity, setEditingEntity] = useState(null);
    const [editingEntityType, setEditingEntityType] = useState('');
    const [editForm, setEditForm] = useState({});
    const [selectedDate, setSelectedDate] = useState(null);
    const [endDateSearchValue, setEndDateSearchValue] = useState(null);
    const [endDateCount, setEndDateCount] = useState(null);

    const [cacheLoading, setCacheLoading] = useState(false);
    const [cacheStats, setCacheStats] = useState(null);
    const [cacheMessage, setCacheMessage] = useState(null);
    const [test2pcResult, setTest2pcResult] = useState(null);
    const [test2pcLoading, setTest2pcLoading] = useState(false);
    const [currentTab, setCurrentTab] = useState(0);

    const openEditModal = (entity, type) => {
        setEditingEntity(entity);
        setEditingEntityType(type);
        setEditForm({ ...entity });
        setEditModalOpen(true);
    };

    const saveEditedEntity = async () => {
        try {
            const endpoint = `/${editingEntityType}/${editingEntity.id}`;
            await axios.put(`${API_BASE}${endpoint}`, editForm);
            setEditModalOpen(false);
            loadAllData();
        } catch (e) {
            console.error(e);
            toast.error('Ошибка при сохранении');
        }
    };

    const deleteEntity = async (id, type) => {
        if (!window.confirm('Удалить запись?')) return;

        try {
            const endpoint = `/${type}/${id}`;
            await axios.delete(`${API_BASE}${endpoint}`);
            loadAllData();
        } catch (e) {
            console.error(e);
            toast.error('Ошибка при удалении');
        }
    };

    const getCacheStats = async () => {
        setCacheLoading(true);
        try {
            const response = await axios.get(`${API_BASE}/cache/stats`);
            const data = response.data;
            setCacheStats(data);
            const hits = data.localHits !== undefined ? data.localHits : 0;
            const misses = data.localMisses !== undefined ? data.localMisses : 0;
            const logging = data.loggingEnabled ? 'ON' : 'OFF';
            const size = data.cacheSize !== undefined ? data.cacheSize : 0;
            setCacheMessage(`Hits: ${hits}, Misses: ${misses}, Size: ${size}, Logging: ${logging}`);
            setTimeout(() => setCacheMessage(null), 5000);
        } catch (error) {
            setCacheMessage('Ошибка при получении статистики кэша');
            setTimeout(() => setCacheMessage(null), 5000);
            console.error('Cache stats error:', error);
        }
        setCacheLoading(false);
    };

    const toggleCacheLogging = async () => {
        setCacheLoading(true);
        try {
            const endpoint = cacheStats?.loggingEnabled
                ? `${API_BASE}/cache/logging/disable`
                : `${API_BASE}/cache/logging/enable`;
            const response = await axios.post(endpoint);
            setCacheStats(response.data);
            setCacheMessage(cacheStats?.loggingEnabled ? 'Логирование кэша отключено' : 'Логирование кэша включено');
            setTimeout(() => setCacheMessage(null), 5000);
        } catch (error) {
            setCacheMessage('Ошибка при переключении логирования');
            setTimeout(() => setCacheMessage(null), 5000);
        }
        setCacheLoading(false);
    };

    const clearCache = async () => {
        setCacheLoading(true);
        try {
            await axios.post(`${API_BASE}/cache/clear`);
            setCacheMessage('Кэш очищен');
            setCacheStats(null);
            setTimeout(() => setCacheMessage(null), 5000);
        } catch (error) {
            setCacheMessage('Ошибка при очистке кэша');
            setTimeout(() => setCacheMessage(null), 5000);
        }
        setCacheLoading(false);
    };

    const run2pcTest = async (testType) => {
        setTest2pcLoading(true);
        try {
            const endpoint = testType === 'db-failure'
                ? `${API_BASE}/test/2pc/test-db-failure`
                : `${API_BASE}/test/2pc/test-business-logic-failure`;
            const response = await axios.post(endpoint);
            setTest2pcResult(response.data);
        } catch (error) {
            setTest2pcResult({
                status: 'ERROR',
                message: error.message,
                testPassed: false
            });
        }
        setTest2pcLoading(false);
    };


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


    const [searchNameContains, setSearchNameContains] = useState('');
    const [searchNameStarts, setSearchNameStarts] = useState('');
    const [searchRatingLess, setSearchRatingLess] = useState('');
    const [specialResults, setSpecialResults] = useState([]);

    const [importModalOpen, setImportModalOpen] = useState(false);
    const [importFile, setImportFile] = useState(null);
    const [importing, setImporting] = useState(false);
    const [importHistoryModalOpen, setImportHistoryModalOpen] = useState(false);
    const [importHistory, setImportHistory] = useState([]);
    const [importHistoryPage, setImportHistoryPage] = useState(0);
    const [importHistoryRowsPerPage, setImportHistoryRowsPerPage] = useState(10);
    const [importHistoryTotal, setImportHistoryTotal] = useState(0);

    const loadAllData = async () => {
        try {
            const endpoints = {
                workers: '/workers',
                organizations: '/organizations/recent',
                addresses: '/addresses/recent',
                locations: '/locations/recent',
                coordinates: '/coordinates/recent',
                persons: '/persons/recent'
            };

            const res = await axios.get(`${API_BASE}${endpoints[currentEntity]}`);
            setData(res.data || []);
            setTotal(res.data.length || 0);
        } catch (e) {
            console.error(e);
        }
    };

    const loadReferenceData = useCallback(async () => {
        try {
            const [orgsRes, addrsRes, locsRes, coordsRes, personsRes] = await Promise.all([
                axios.get(`${API_BASE}/organizations/recent`),
                axios.get(`${API_BASE}/addresses/recent`),
                axios.get(`${API_BASE}/locations/recent`),
                axios.get(`${API_BASE}/coordinates/recent`),
                axios.get(`${API_BASE}/persons/recent`)
            ]);

            setOrganizations(orgsRes.data || []);
            setAddresses(addrsRes.data || []);
            setLocations(locsRes.data || []);
            setCoordinatesList(coordsRes.data || []);
            setPersons(personsRes.data || []);
        } catch (e) {
            console.error('Error loading reference data:', e);
        }
    }, []);

    const loadWorkers = useCallback(async () => {

        try {
            const params = new URLSearchParams({
                page: page.toString(),
                size: rowsPerPage.toString(),
                sort,
                dir,
            });

            if (activeFilters && Object.keys(activeFilters).length > 0) {
                const filterStrings = Object.entries(activeFilters).map(([field, value]) => {
                    if (value.trim()) {
                        return `${field}:${value.trim()}`;
                    }
                    return null;
                }).filter(Boolean);

                if (filterStrings.length > 0) {
                    params.append('filter', filterStrings.join(','));
                }
            }

            const res = await axios.get(`${API_BASE}/workers?${params}`);
            setWorkers(res.data || []);
            setTotal(res.data.length || 0);
        } catch (e) {
            console.error(e);
        }

    }, [page, rowsPerPage, sort, dir, activeFilters]);

    useEffect(() => {
        loadReferenceData();
    }, [loadReferenceData]);

    useEffect(() => {
        const loadCurrent = async () => {
            if (currentEntity === 'workers') {
                await loadWorkers();
            } else {
                try {
                    const endpoints = {
                        workers: '/workers',
                        organizations: '/organizations',
                        persons: '/persons',
                        coordinates: '/coordinates',
                        locations: '/locations',
                        addresses: '/addresses'
                    };
                    const endpoint = endpoints[currentEntity] || '/workers';
                    const params = new URLSearchParams({
                        page: page.toString(),
                        size: rowsPerPage.toString(),
                        sort,
                        dir,
                    });

                    if (activeFilters && Object.keys(activeFilters).length > 0) {
                        const filterStrings = Object.entries(activeFilters).map(([field, value]) => {
                            if (value.trim()) {
                                return `${field}:${value.trim()}`;
                            }
                            return null;
                        }).filter(Boolean);

                        if (filterStrings.length > 0) {
                            params.append('filter', filterStrings.join(','));
                        }
                    }

                    const res = await axios.get(`${API_BASE}${endpoint}?${params}`);
                    setData(res.data || []);
                    setTotal(res.data.length || 0);
                } catch (e) {
                    console.error(e);
                }
            }

            await loadReferenceData();
        };

        loadCurrent();

        const interval = setInterval(loadCurrent, POLLING_INTERVAL);

        return () => clearInterval(interval);
    }, [currentEntity, page, rowsPerPage, sort, dir, activeFilters, loadWorkers, loadReferenceData]);

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

            if (type === 'organization') setOrgModalOpen(false);
            if (type === 'address') setAddressModalOpen(false);
            if (type === 'location') setLocationModalOpen(false);
            if (type === 'coordinates') setCoordsModalOpen(false);
            if (type === 'person') setPersonModalOpen(false);

            await loadReferenceData();

        } catch (e) {
            console.error(e);
            if (e.response?.data?.propertyViolations) {
                const errors = e.response.data.propertyViolations
                    .map(v => `${v.path}: ${v.message}`)
                    .join('; ');
                toast.error(`Ошибка валидации: ${errors}`);
            } else if (e.response?.status === 400) {
                toast.error('Некорректные данные');
            } else {
                toast.error('Ошибка создания');
            }
        }
    };

    const saveWorker = async () => {
        try {
            const payload = {
                name: workerForm.name,
                salary: Number(workerForm.salary),
                rating: Number(workerForm.rating) || null,
                startDate: workerForm.startDate,
                endDate: workerForm.endDate || null,
                position: workerForm.position,
                coordinates: workerForm.coordinates,
                organization: workerForm.organization,
                person: hasPerson ? workerForm.person : null
            };

            if (editingWorker) {
                await axios.put(`${API_BASE}/workers/${editingWorker.id}`, payload);
            } else {
                await axios.post(`${API_BASE}/workers`, payload);
            }

            toast.success('Работник сохранён');
            setWorkerModalOpen(false);
            loadWorkers();

        } catch (e) {
            console.error(e);
            if (e.response?.data?.propertyViolations) {
                const errors = e.response.data.propertyViolations
                    .map(v => `${v.path}: ${v.message}`)
                    .join('; ');
                toast.error(`Ошибка валидации: ${errors}`);
            } else if (e.response?.status === 400) {
                toast.error('Некорректные данные');
            } else {
                toast.error('Ошибка создания');
            }
        }
    };

    const deleteWorker = async (id) => {
        if (window.confirm('Удалить работника?')) {
            try {
                await axios.delete(`${API_BASE}/workers/${id}`);
                loadWorkers();
            } catch (e) {
                console.error(e);
            }
        }
    };

    const runSpecialSearch = async (type) => {
        let url = '';
        if (type === 'contains') url = `${API_BASE}/workers/search/name-contains?q=${searchNameContains}`;

        if (!url) return;
        try {
            const res = await axios.get(url);
            setSpecialResults(res.data);
            setEndDateCount(null);
        } catch (e) {
            console.error(e);
        }
    };

    const handleImportFile = (event) => {
        const file = event.target.files[0];
        if (file && file.type === 'application/json') {
            setImportFile(file);
        } else {
            toast.error('Пожалуйста, выберите JSON файл');
            event.target.value = null;
        }
    };

    const importWorkers = async () => {
        if (!importFile) {
            toast.error('Выберите файл для импорта');
            return;
        }

        setImporting(true);
        try {
            const fileContent = await importFile.text();
            const workers = JSON.parse(fileContent);

            const response = await axios.post(
                `${API_BASE}/import/workers`,
                workers,
                {
                    headers: {
                        'Content-Type': 'application/json'
                    }
                }
            );

            if (response.data.status === 'SUCCESS') {
                toast.success(`Успешно импортировано ${response.data.addedCount} работников`);
                setImportModalOpen(false);
                setImportFile(null);
                loadWorkers();
                await loadReferenceData();
            } else {
                toast.error(`Ошибка импорта: ${response.data.errorMessage}`);
            }
        } catch (e) {
            console.error(e);
            const errorMsg = e.response?.data?.error || e.message || 'Неизвестная ошибка';
            toast.error(`Ошибка импорта: ${errorMsg}`);
        } finally {
            setImporting(false);
        }
    };

    const loadImportHistory = async (page = 0, pageSize = 10) => {
        try {
            const response = await axios.get(`${API_BASE}/import/history`, {
                params: {
                    page: page,
                    size: pageSize,
                    sort: 'timestamp',
                    dir: 'desc'
                }
            });
            setImportHistory(response.data.data || []);
            setImportHistoryTotal(response.data.total || 0);
            setImportHistoryPage(page);
            setImportHistoryModalOpen(true);
        } catch (e) {
            console.error(e);
            toast.error('Ошибка загрузки истории импорта');
        }
    };

    const downloadImportFile = async (fileName) => {
        if (!fileName) {
            toast.error('Файл недоступен');
            return;
        }
        try {
            const response = await axios.get(`${API_BASE}/import/download/${fileName}`, {
                responseType: 'blob'
            });
            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', fileName);
            document.body.appendChild(link);
            link.click();
            link.remove();
            window.URL.revokeObjectURL(url);
            toast.success('Файл скачан');
        } catch (e) {
            console.error(e);
            toast.error('Ошибка скачивания файла');
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
    const columnConfigs = {
        workers: [
            { id: 'id', label: 'ID' },
            { id: 'name', label: 'Имя' },
            { id: 'coordinates', label: 'Координаты', render: w => `${w.coordinates?.x}, ${w.coordinates?.y}` },
            { id: 'creationDate', label: 'Создан', render: w => dayjs(w.creationDate).format('DD.MM.YYYY') },
            { id: 'startDate', label: 'Дата начала', render: w => dayjs(w.startDate).format('DD.MM.YYYY') },
            { id: 'endDate', label: 'Дата окончания', render: w => w.endDate ? dayjs(w.endDate).format('DD.MM.YYYY') : '-' },
            { id: 'salary', label: 'Зарплата' },
            { id: 'rating', label: 'Рейтинг' },
            { id: 'position', label: 'Должность' },
            { id: 'organization', label: 'Организация', render: w => w.organization?.fullName || `ID ${w.organization?.id}` },
            { id: 'person', label: 'Человек', render: w => w.person ? (w.person.passportID || `ID ${w.person.id}`) : '-' },
            { id: 'actions', label: 'Действия', render: w => (
                    <>
                        <IconButton size="small" onClick={() => openWorkerModal(w)}><EditIcon /></IconButton>
                        <IconButton size="small" onClick={() => deleteWorker(w.id)}><DeleteIcon /></IconButton>
                    </>
                )}
        ],
        organizations: [
            { id: 'id', label: 'ID' },
            { id: 'fullName', label: 'Название' },
            { id: 'annualTurnover', label: 'Оборот' },
            { id: 'rating', label: 'Рейтинг' },
            { id: 'employeesCount', label: 'Сотрудников' },
            { id: 'officialAddress', label: 'Адрес', render: o => o.officialAddress?.street || '' },
            { id: 'actions', label: '', render: o => (
                    <>
                        <IconButton size="small" onClick={() => openEditModal(o, 'organizations')}><EditIcon /></IconButton>
                        <IconButton size="small" onClick={() => deleteEntity(o.id)}><DeleteIcon /></IconButton>
                    </>
                )}
        ],
        persons: [
            { id: 'id', label: 'ID' },
            { id: 'passportID', label: 'Паспорт' },
            { id: 'height', label: 'Рост' },
            { id: 'eyeColor', label: 'Цвет глаз' },
            { id: 'hairColor', label: 'Цвет волос' },
            { id: 'nationality', label: 'Национальность' },
            { id: 'actions', label: '', render: p => (
                    <>
                        <IconButton size="small" onClick={() => openEditModal(p, 'persons')}><EditIcon /></IconButton>
                        <IconButton size="small" onClick={() => deleteEntity(p.id, 'persons')}><DeleteIcon /></IconButton>
                    </>
                )}
        ],
        coordinates: [
            { id: 'id', label: 'ID' },
            { id: 'x', label: 'X' },
            { id: 'y', label: 'Y' },
            { id: 'actions', label: '', render: c => (
                    <>
                        <IconButton size="small" onClick={() => openEditModal(c, 'coordinates')}><EditIcon /></IconButton>
                        <IconButton size="small" onClick={() => deleteEntity(c.id, 'coordinates')}><DeleteIcon /></IconButton>
                    </>
                )}
        ],
        locations: [
            { id: 'id', label: 'ID' },
            { id: 'x', label: 'X' },
            { id: 'y', label: 'Y' },
            { id: 'z', label: 'Z' },
            { id: 'name', label: 'Название' },
            { id: 'actions', label: '', render: l => (
                    <>
                        <IconButton size="small" onClick={() => openEditModal(l, 'locations')}><EditIcon /></IconButton>
                        <IconButton size="small" onClick={() => deleteEntity(l.id, 'locations')}><DeleteIcon /></IconButton>
                    </>
                )}
        ],
        addresses: [
            { id: 'id', label: 'ID' },
            { id: 'street', label: 'Улица' },
            { id: 'town', label: 'Город', render: a => a.town?.name || '' },
            { id: 'actions', label: '', render: a => (
                    <>
                        <IconButton size="small" onClick={() => openEditModal(a, 'addresses')}><EditIcon /></IconButton>
                        <IconButton size="small" onClick={() => deleteEntity(a.id, 'addresses')}><DeleteIcon /></IconButton>
                    </>
                )}
        ],
    };

    const currentColumns = columnConfigs[currentEntity] || columnConfigs.workers;
    const currentData = currentEntity === 'workers' ? workers : data;

    return (
        <LocalizationProvider dateAdapter={AdapterDayjs}>
            <Toaster
                position="top-right"
                reverseOrder={false}
                gutter={8}
                toastOptions={{
                    duration: 4000,
                    style: {
                        background: '#363636',
                        color: '#fff',
                    },
                    success: {
                        duration: 3000,
                        icon: '✅',
                    },
                    error: {
                        duration: 5000,
                        icon: '❌',
                    },
                }}
            />
            {/* Header */}
            <AppBar position="static" sx={{ mb: 2 }}>
                <Toolbar>
                    <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
                        Управление работниками
                    </Typography>
                </Toolbar>
            </AppBar>
            <Box p={4}>

                {/* Cache Management Panel */}
                <Box sx={{
                    padding: 2,
                    backgroundColor: '#f5f5f5',
                    borderRadius: 1,
                    marginBottom: 2,
                    display: 'flex',
                    flexWrap: 'wrap',
                    gap: 2,
                    alignItems: 'center'
                }}>
                    <MemoryIcon sx={{ fontSize: 28, color: '#1976d2' }} />
                    <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', flex: 1 }}>
                        <Button
                            variant="contained"
                            size="small"
                            onClick={getCacheStats}
                            disabled={cacheLoading}
                        >
                            {cacheLoading ? <CircularProgress size={20} /> : 'Cache Stats'}
                        </Button>
                        <Button
                            variant="contained"
                            color={cacheStats?.loggingEnabled ? 'error' : 'success'}
                            size="small"
                            onClick={toggleCacheLogging}
                            disabled={cacheLoading}
                        >
                            {cacheLoading ? <CircularProgress size={20} /> : (cacheStats?.loggingEnabled ? 'Disable Log' : 'Enable Log')}
                        </Button>
                        <Button
                            variant="outlined"
                            size="small"
                            onClick={clearCache}
                            disabled={cacheLoading}
                        >
                            Clear Cache
                        </Button>
                        <Button
                            variant="contained"
                            color="error"
                            size="small"
                            onClick={() => run2pcTest('db-failure')}
                            disabled={test2pcLoading}
                        >
                            {test2pcLoading ? <CircularProgress size={20} /> : 'Test DB Fail'}
                        </Button>
                        <Button
                            variant="contained"
                            color="warning"
                            size="small"
                            onClick={() => run2pcTest('business-logic-failure')}
                            disabled={test2pcLoading}
                        >
                            {test2pcLoading ? <CircularProgress size={20} /> : 'Test Logic Fail'}
                        </Button>
                    </Box>
                </Box>

                {cacheMessage && (
                    <Alert severity="info" sx={{ marginBottom: 2 }}>
                        {cacheMessage}
                    </Alert>
                )}

                {/*{cacheStats && (*/}
                {/*    <Box sx={{*/}
                {/*        padding: 1.5,*/}
                {/*        backgroundColor: '#e3f2fd',*/}
                {/*        borderRadius: 1,*/}
                {/*        marginBottom: 2,*/}
                {/*        fontSize: '0.9rem'*/}
                {/*    }}>*/}
                {/*        <strong>Cache Stats:</strong> Hits: {cacheStats.hits !== undefined ? cacheStats.hits : 0}, Misses: {cacheStats.misses !== undefined ? cacheStats.misses : 0}, Logging: {cacheStats.loggingEnabled ? '✓ ON' : '✗ OFF'}*/}
                {/*    </Box>*/}
                {/*)}*/}

                {test2pcResult && (
                    <Box sx={{
                        padding: 1.5,
                        backgroundColor: test2pcResult.testPassed ? '#e8f5e9' : '#ffebee',
                        borderRadius: 1,
                        marginBottom: 2,
                        border: `2px solid ${test2pcResult.testPassed ? '#4caf50' : '#f44336'}`,
                        display: 'flex',
                        alignItems: 'flex-start',
                        gap: 1
                    }}>
                        {test2pcResult.testPassed ?
                            <CheckCircleIcon sx={{ color: '#4caf50', marginTop: 0.5 }} /> :
                            <ErrorIcon sx={{ color: '#f44336', marginTop: 0.5 }} />
                        }
                        <Box>
                            <strong>{test2pcResult.testPassed ? '✓ Test PASSED' : '✗ Test FAILED'}</strong>
                            <div>{test2pcResult.message}</div>
                            {test2pcResult.testFileName && (
                                <div style={{ fontSize: '0.85rem', marginTop: '5px', color: '#666' }}>
                                    File: {test2pcResult.testFileName}
                                </div>
                            )}
                        </Box>
                        <Button
                            size="small"
                            onClick={() => setTest2pcResult(null)}
                            sx={{ marginLeft: 'auto' }}
                        >
                            ✕
                        </Button>
                    </Box>
                )}

                <FormControl fullWidth sx={{ mb: 3 }}>
                    <InputLabel>Сущность</InputLabel>
                    <Select value={currentEntity} onChange={e => {
                        setCurrentEntity(e.target.value);
                        setPage(0);
                        setActiveFilters({});
                    }}>
                        <MenuItem value="workers">Работники</MenuItem>
                        <MenuItem value="organizations">Организации</MenuItem>
                        <MenuItem value="persons">Люди</MenuItem>
                        <MenuItem value="coordinates">Координаты</MenuItem>
                        <MenuItem value="locations">Локации</MenuItem>
                        <MenuItem value="addresses">Адреса</MenuItem>
                    </Select>
                </FormControl>

                <Button variant="contained" startIcon={<AddIcon />} onClick={() => openWorkerModal()} sx={{ mb: 2 }}>
                    Добавить работника
                </Button>

                <Button
                    variant="outlined"
                    onClick={() => setImportModalOpen(true)}
                    sx={{ mb: 2, ml: 2 }}
                >
                    Импорт из файла
                </Button>

                <Button
                    variant="outlined"
                    onClick={() => loadImportHistory()}
                    sx={{ mb: 2, ml: 2 }}
                >
                    История импорта
                </Button>

                <Button
                    variant="outlined"
                    onClick={() => setShowFilters(!showFilters)}
                    sx={{ mb: 2, ml: 2 }}
                >
                    {showFilters ? 'Скрыть фильтры' : 'Показать фильтры'}
                    {Object.keys(activeFilters).length > 0 && ` (${Object.keys(activeFilters).length})`}
                </Button>

                {showFilters && (
                    <Box sx={{ mb: 3, p: 2, border: '1px solid #ddd', borderRadius: 1, backgroundColor: '#f9f9f9' }}>
                        <Typography variant="h6" gutterBottom>
                            Фильтрация (точное совпадение)
                        </Typography>
                        <Grid container spacing={2}>
                            {currentColumns.filter(col => col.id !== 'actions').map(col => (
                                <Grid item xs={12} sm={6} md={4} lg={3} key={col.id}>
                                    <TextField
                                        fullWidth
                                        size="small"
                                        label={`Фильтр: ${col.label}`}
                                        value={activeFilters[col.id] || ''}
                                        onChange={(e) => {
                                            const newFilters = { ...activeFilters };
                                            if (e.target.value) {
                                                newFilters[col.id] = e.target.value;
                                            } else {
                                                delete newFilters[col.id];
                                            }
                                            setActiveFilters(newFilters);
                                            setPage(0);
                                        }}
                                        placeholder={`Введите ${col.label.toLowerCase()}`}
                                    />
                                </Grid>
                            ))}
                        </Grid>
                        {Object.keys(activeFilters).length > 0 && (
                            <Box sx={{ mt: 2 }}>
                                <Button
                                    variant="outlined"
                                    size="small"
                                    onClick={() => {
                                        setActiveFilters({});
                                        setPage(0);
                                    }}
                                >
                                    Очистить все фильтры
                            </Button>
                            <Typography variant="body2" sx={{ mt: 1, color: 'text.secondary' }}>
                                Активных фильтров: {Object.keys(activeFilters).length}
                            </Typography>
                        </Box>
                    )}
                    </Box>
                )}


                {loading ? null : (
                    <>
                        <Table>
                            <TableHead>
                                <TableRow>
                                    {currentColumns.map(col => (
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
                                {currentData.map(item => (
                                    <TableRow key={item.id}>
                                        {currentColumns.map(col => (
                                            <TableCell key={col.id}>
                                                {col.render ? col.render(item) : item[col.id] || '-'}
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


                <Box mt={6} p={4} border={1} borderColor="grey.300" borderRadius={2} bgcolor="background.paper">
                    <Typography variant="h5" gutterBottom color="primary">
                        Специальные функции
                    </Typography>

                    <Grid container spacing={3}>


                        <Grid item xs={12}>
                            <Button fullWidth variant="outlined" onClick={async () => {
                                const res = await axios.get(`${API_BASE}/workers/group/salary`);
                                toast.success(Object.entries(res.data)
                                    .map(([salary, count]) => `Зарплата ${salary}₽ → ${count} чел.`)
                                    .join('\n') || 'Нет данных');
                            }}>
                                Группировка по зарплате
                            </Button>
                        </Grid>


                        <Grid item xs={12} md={6}>
                            <DatePicker
                                label="Дата окончания"
                                slotProps={{ textField: { fullWidth: true } }}
                                value={dayjs(selectedDate)}
                                onChange={(date) => setSelectedDate(dayjs(date))}
                            />
                            <Button
                                variant="contained"
                                onClick={async () => {
                                    if (!selectedDate) return;
                                    const iso = dayjs(selectedDate).format('YYYY-MM-DD');
                                    try {
                                        const res = await axios.get(`${API_BASE}/workers/count/enddate?date=${iso}`);
                                        toast.success(`Сотрудников с endDate = ${iso}: ${res.data}`);
                                    } catch (error) {
                                        toast.error(`Error fetching data: ${error.message}`);
                                    }
                                }}
                            >
                                Найти
                            </Button>
                        </Grid>


                        <Grid item xs={12} md={6}>
                            <TextField
                                fullWidth
                                label="Имя содержит"
                                value={searchNameContains}
                                onChange={e => setSearchNameContains(e.target.value)}
                                onKeyDown={e => e.key === 'Enter' && runSpecialSearch('contains')}
                            />
                            <Button fullWidth sx={{ mt: 1 }} variant="contained" onClick={() => runSpecialSearch('contains')}>
                                Найти по имени
                            </Button>
                        </Grid>

                        <Grid item xs={12} md={6}>
                            <TextField fullWidth label="ID сотрудника" type="number" value={indexWorkerId || ''} onChange={e => setIndexWorkerId(+e.target.value)} />
                            <TextField fullWidth label="Коэффициент" type="number" step="0.01" value={indexCoef || ''} onChange={e => setIndexCoef(+e.target.value)} sx={{ mt: 1 }} />
                            <Button fullWidth sx={{ mt: 1 }} color="secondary" variant="contained"
                                    onClick={async () => {
                                        if (!indexWorkerId || !indexCoef) return toast.error('Заполните поля');
                                        await axios.post(`${API_BASE}/workers/index-salary/worker/${indexWorkerId}?coef=${indexCoef}`);
                                        toast.success('Зарплата проиндексирована');
                                        loadWorkers();
                                    }}>
                                Индексировать зарплату сотруднику
                            </Button>
                        </Grid>

                        <Grid item xs={12} md={6}>
                            <FormControl fullWidth>
                                <InputLabel>Организация</InputLabel>
                                <Select value={indexOrgId || ''} onChange={e => setIndexOrgId(e.target.value)}>
                                    {organizations.map(o => (
                                        <MenuItem key={o.id} value={o.id}>{o.fullName || `ID ${o.id}`}</MenuItem>
                                    ))}
                                </Select>
                            </FormControl>
                            <TextField fullWidth label="Коэффициент" type="number" step="0.01" value={indexOrgCoef || ''} onChange={e => setIndexOrgCoef(+e.target.value)} sx={{ mt: 1 }} />
                            <Button fullWidth sx={{ mt: 1 }} color="secondary" variant="contained"
                                    onClick={async () => {
                                        if (!indexOrgId || !indexOrgCoef) return toast.error('Выберите организацию и коэффициент коэффициент');
                                        await axios.post(`${API_BASE}/workers/index-salary/organization/${indexOrgId}?coef=${indexOrgCoef}`);
                                        toast.success('Зарплата проиндексирована всем сотрудникам организации');
                                        loadWorkers();
                                    }}>
                                Индексировать зарплату организации
                            </Button>
                        </Grid>

                    </Grid>
                    {specialResults.length > 0 && (
                    <Box mt={4}>
                        <Typography variant="h6">
                            Результаты поиска: {specialResults.length} записей
                            {endDateCount !== null && ` (совпадений по дате: ${endDateCount})`}
                        </Typography>
                        <Grid container spacing={1}>
                            {specialResults.map(w => (
                                <Grid item key={w.id}>
                                    <Chip label={`${w.name} (ID ${w.id})`} color="primary" />
                                </Grid>
                            ))}
                        </Grid>
                    </Box>
                )}
                </Box>


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
                        </Grid>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={() => setWorkerModalOpen(false)}>Отмена</Button>
                        <Button variant="contained" onClick={saveWorker}>Сохранить</Button>
                    </DialogActions>
                </Dialog>


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


                <Dialog open={addressModalOpen} onClose={(event, reason) => {
                    if (reason !== 'backdropClick' && reason !== 'escapeKeyDown') {
                        setAddressModalOpen(false);
                    }}}
                    maxWidth="sm" fullWidth>
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


                <Dialog open={locationModalOpen} onClose={(event, reason) => {
                    if (reason !== 'backdropClick' && reason !== 'escapeKeyDown') {
                        setLocationModalOpen(false);
                    }
                }} maxWidth="sm" fullWidth>
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


                <Dialog open={coordsModalOpen} onClose={(event, reason) => {
                    if (reason !== 'backdropClick' && reason !== 'escapeKeyDown') {
                        setCoordsModalOpen(false);
                    }
                }} maxWidth="sm" fullWidth>
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


                <Dialog open={personModalOpen} onClose={(event, reason) => {
                    if (reason !== 'backdropClick' && reason !== 'escapeKeyDown') {
                        setPersonModalOpen(false);
                    }
                }} maxWidth="sm" fullWidth>
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

                <Dialog open={editModalOpen} onClose={() => setEditModalOpen(false)} maxWidth="md" fullWidth>
                    <DialogTitle>Редактировать {editingEntityType}</DialogTitle>
                    <DialogContent dividers>
                        <Grid container spacing={2}>
                            {Object.entries(editForm).map(([key, value]) => {
                                if (key === 'id' || Array.isArray(value) || key === 'workers' || key === 'persons' || key === 'organizations' || key === 'addresses') {
                                    return null;
                            }
                                if (key.toLowerCase().includes('date') || key === 'creationDate' || key === 'startDate' || key === 'endDate') {
                                return (
                                <Grid item xs={12} md={6} key={key}>
                            <DatePicker
                                label={key}
                                value={value ? dayjs(value) : null}
                                onChange={d => setEditForm(prev => ({ ...prev, [key]: d?.format('YYYY-MM-DD') || null }))}
                                slotProps={{ textField: { fullWidth: true } }}
                            />
                        </Grid>
                        );
                        }

                        if (key === 'position' || key === 'eyeColor' || key === 'hairColor' || key === 'nationality') {
                        let options = key === 'position'
                        ? ['LABORER', 'HUMAN_RESOURCES', 'HEAD_OF_DEPARTMENT']
                        : ['RED', 'BLACK', 'YELLOW', 'ORANGE', 'WHITE'];
                        if (key === 'nationality') {
                        options = ['RUSSIA', 'UNITED_KINGDOM', 'FRANCE', 'INDIA', 'THAILAND'];
                        }

                        return (
                        <Grid item xs={12} md={6} key={key}>
                            <FormControl fullWidth>
                                <InputLabel>{key}</InputLabel>
                                <Select value={value || ''} onChange={e => setEditForm(prev => ({ ...prev, [key]: e.target.value }))}>
                                    {options.map(opt => (
                                        <MenuItem key={opt} value={opt}>{opt}</MenuItem>
                                    ))}
                                </Select>
                            </FormControl>
                        </Grid>
                        );
                        }

                        if (typeof value === 'number') {
                        return (
                        <Grid item xs={12} md={6} key={key}>
                            <TextField
                                fullWidth
                                label={key}
                                type="number"
                                value={value}
                                onChange={e => setEditForm(prev => ({ ...prev, [key]: +e.target.value }))}
                            />
                        </Grid>
                        );
                        }

                        return (
                        <Grid item xs={12} md={6} key={key}>
                            <TextField
                                fullWidth
                                label={key}
                                value={value || ''}
                                onChange={e => setEditForm(prev => ({ ...prev, [key]: e.target.value }))}
                            />
                        </Grid>
                        );
                        })}
                    </Grid>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setEditModalOpen(false)}>Отмена</Button>
                    <Button variant="contained" onClick={saveEditedEntity}>Сохранить</Button>
                </DialogActions>
            </Dialog>

            <Dialog open={importModalOpen} onClose={() => !importing && setImportModalOpen(false)} maxWidth="sm" fullWidth>
                <DialogTitle>Импорт работников из JSON</DialogTitle>
                <DialogContent dividers>
                    <Box sx={{ my: 2 }}>
                        <Typography variant="body2" gutterBottom>
                            Выберите JSON файл с массивом работников для импорта.
                        </Typography>
                        <Typography variant="body2" color="text.secondary" gutterBottom>
                            Формат файла должен содержать массив объектов Worker с заполненными полями
                            и вложенными объектами (coordinates, organization, person).
                        </Typography>
                        <Button
                            variant="outlined"
                            component="label"
                            fullWidth
                            sx={{ mt: 2 }}
                            disabled={importing}
                        >
                            {importFile ? importFile.name : 'Выбрать файл'}
                            <input
                                type="file"
                                accept=".json"
                                hidden
                                onChange={handleImportFile}
                            />
                        </Button>
                        {importing && (
                            <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
                                <CircularProgress />
                            </Box>
                        )}
                    </Box>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setImportModalOpen(false)} disabled={importing}>
                        Отмена
                    </Button>
                    <Button
                        variant="contained"
                        onClick={importWorkers}
                        disabled={!importFile || importing}
                    >
                        {importing ? 'Импорт...' : 'Импортировать'}
                    </Button>
                </DialogActions>
            </Dialog>

            <Dialog open={importHistoryModalOpen} onClose={() => setImportHistoryModalOpen(false)} maxWidth="lg" fullWidth>
                <DialogTitle>История импорта</DialogTitle>
                <DialogContent dividers>
                    {importHistory.length === 0 ? (
                        <Typography>История импорта пуста</Typography>
                    ) : (
                        <>
                            <Table>
                                <TableHead>
                                    <TableRow>
                                        <TableCell>ID</TableCell>
                                        <TableCell>Дата и время</TableCell>
                                        <TableCell>Статус</TableCell>
                                        <TableCell>Добавлено</TableCell>
                                        <TableCell>Файл</TableCell>
                                        <TableCell>Ошибка</TableCell>
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    {importHistory.map((record) => (
                                        <TableRow key={record.id}>
                                            <TableCell>{record.id}</TableCell>
                                            <TableCell>
                                                {new Date(record.timestamp).toLocaleString('ru-RU')}
                                            </TableCell>
                                            <TableCell>
                                                <Chip
                                                    label={record.status}
                                                    color={record.status === 'SUCCESS' ? 'success' : 'error'}
                                                    size="small"
                                                />
                                            </TableCell>
                                            <TableCell>
                                                {record.status === 'SUCCESS' ? record.addedCount : '-'}
                                            </TableCell>
                                            <TableCell>
                                                {record.fileName ? (
                                                    <IconButton
                                                        size="small"
                                                        color="primary"
                                                        onClick={() => downloadImportFile(record.fileName)}
                                                        title="Скачать файл импорта"
                                                    >
                                                        <DownloadIcon />
                                                    </IconButton>
                                                ) : '-'}
                                            </TableCell>
                                            <TableCell sx={{ maxWidth: 300, overflow: 'hidden', textOverflow: 'ellipsis' }}>
                                                {record.errorMessage || '-'}
                                            </TableCell>
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                            <TablePagination
                                component="div"
                                count={importHistoryTotal}
                                page={importHistoryPage}
                                onPageChange={(e, newPage) => {
                                    loadImportHistory(newPage, importHistoryRowsPerPage);
                                }}
                                rowsPerPage={importHistoryRowsPerPage}
                                onRowsPerPageChange={(e) => {
                                    const newPageSize = parseInt(e.target.value, 10);
                                    setImportHistoryRowsPerPage(newPageSize);
                                    loadImportHistory(0, newPageSize);
                                }}
                                labelRowsPerPage="Записей на странице:"
                                labelDisplayedRows={({ from, to, count }) => `${from}-${to} из ${count}`}
                            />
                        </>
                    )}
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setImportHistoryModalOpen(false)}>Закрыть</Button>
                </DialogActions>
            </Dialog>
            </Box>

        </LocalizationProvider>

    );
}

export default App;