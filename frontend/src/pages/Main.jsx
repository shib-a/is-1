// // App.js (pure JavaScript + JSX, no TypeScript)
// import React, { useState, useEffect, useCallback } from 'react';
// import axios from 'axios';
// import {
//     Table, TableHead, TableBody, TableRow, TableCell, TablePagination, TableSortLabel,
//     TextField, InputAdornment, Button, Dialog, DialogTitle, DialogContent, DialogActions,
//     MenuItem, Select, InputLabel, FormControl, CircularProgress, Box, Typography, Grid,
//     Chip, IconButton
// } from '@mui/material';
// import {
//     Search as SearchIcon, Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon,
//     Business as OrgIcon, Person as PersonIcon
// } from '@mui/icons-material';
// import { DatePicker } from '@mui/x-date-pickers/DatePicker';
// import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
// import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
// import dayjs from 'dayjs';
//
// const API_BASE = 'http://localhost:8081/is-1-1.0-SNAPSHOT/api';
//
// const POLLING_INTERVAL = 5000; // 5 seconds
//
// function Main() {
//     const [workers, setWorkers] = useState([]);
//     const [organizations, setOrganizations] = useState([]);
//     const [page, setPage] = useState(0);
//     const [rowsPerPage, setRowsPerPage] = useState(20);
//     const [total, setTotal] = useState(0);
//     const [sort, setSort] = useState('id');
//     const [dir, setDir] = useState('asc');
//     const [filter, setFilter] = useState('');
//     const [loading, setLoading] = useState(false);
//
//     // CRUD modal
//     const [openModal, setOpenModal] = useState(false);
//     const [editingWorker, setEditingWorker] = useState(null);
//     const [formData, setFormData] = useState({});
//
//     // Special searches
//     const [searchNameContains, setSearchNameContains] = useState('');
//     const [searchNameStarts, setSearchNameStarts] = useState('');
//     const [searchRatingLess, setSearchRatingLess] = useState('');
//     const [specialResults, setSpecialResults] = useState([]);
//
//     const loadOrganizations = async () => {
//         try {
//             const res = await axios.get(`${API_BASE}/organizations`);
//             setOrganizations(res.data);
//         } catch (e) {
//             console.error(e);
//         }
//     };
//
//     const loadWorkers = useCallback(async () => {
//         setLoading(true);
//         try {
//             const params = new URLSearchParams({
//                 page: page.toString(),
//                 size: rowsPerPage.toString(),
//                 sort,
//                 dir,
//                 ...(filter && { filter }),
//             });
//             const res = await axios.get(`${API_BASE}/workers?${params}`);
//             setWorkers(res.data.content || []);
//             setTotal(res.data.totalElements || 0);
//         } catch (e) {
//             console.error(e);
//         }
//         setLoading(false);
//     }, [page, rowsPerPage, sort, dir, filter]);
//
//     // Initial load + polling
//     useEffect(() => {
//         loadWorkers();
//         loadOrganizations();
//
//         const interval = setInterval(() => {
//             loadWorkers(); // silently refresh the table for real-time updates
//         }, POLLING_INTERVAL);
//
//         return () => clearInterval(interval);
//     }, [loadWorkers]);
//
//     const handleOpenModal = (worker = null) => {
//         setEditingWorker(worker);
//         setFormData(worker || {
//             name: '',
//             coordinates: { x: 0, y: 0 },
//             salary: 0,
//             rating: 0,
//             startDate: dayjs().format('YYYY-MM-DD'),
//             position: '',
//             organization: organizations[0] || null,
//             person: null,
//         });
//         setOpenModal(true);
//     };
//
//     const handleSave = async () => {
//         try {
//             if (editingWorker) {
//                 await axios.put(`${API_BASE}/workers/update?id=${editingWorker.id}`, formData);
//             } else {
//                 await axios.post(`${API_BASE}/workers`, formData);
//             }
//             setOpenModal(false);
//             loadWorkers(); // immediate refresh after save
//         } catch (e) {
//             console.error(e);
//         }
//     };
//
//     const handleDelete = async (id) => {
//         if (window.confirm('Удалить работника?')) {
//             await axios.delete(`${API_BASE}/workers/delete?id=${id}`);
//             loadWorkers();
//         }
//     };
//
//     const runSpecialSearch = async (type) => {
//         let url = '';
//         if (type === 'contains') url = `${API_BASE}/workers/search/name-contains?q=${encodeURIComponent(searchNameContains)}`;
//         if (type === 'starts') url = `${API_BASE}/workers/search/name-starts?q=${encodeURIComponent(searchNameStarts)}`;
//         if (type === 'rating') url = `${API_BASE}/workers/search/rating-less?value=${searchRatingLess}`;
//
//         if (!url) return;
//         try {
//             const res = await axios.get(url);
//             setSpecialResults(res.data);
//         } catch (e) {
//             console.error(e);
//         }
//     };
//
//     const columns = [
//         { id: 'id', label: 'ID' },
//         { id: 'name', label: 'Имя' },
//         { id: 'coordinates', label: 'Координаты', render: w => `${w.coordinates.x}, ${w.coordinates.y}` },
//         { id: 'creationDate', label: 'Создан' },
//         { id: 'organization', label: 'Организация', render: w => w.organization.fullName || `ID ${w.organization.id}` },
//         { id: 'salary', label: 'Зарплата' },
//         { id: 'rating', label: 'Рейтинг' },
//         { id: 'position', label: 'Должность' },
//         { id: 'actions', label: 'Действия', render: w => (
//                 <>
//                     <IconButton size="small" onClick={() => handleOpenModal(w)}><EditIcon /></IconButton>
//                     <IconButton size="small" onClick={() => handleDelete(w.id)}><DeleteIcon /></IconButton>
//                 </>
//             )},
//     ];
//
//     return (
//         <LocalizationProvider dateAdapter={AdapterDayjs}>
//             <Box p={4}>
//                 <Typography variant="h4" gutterBottom>Управление работниками</Typography>
//
//                 <TextField
//                     fullWidth
//                     margin="normal"
//                     label="Поиск по всем строковым полям"
//                     value={filter}
//                     onChange={(e) => { setFilter(e.target.value); setPage(0); }}
//                     InputProps={{ startAdornment: <InputAdornment position="start"><SearchIcon /></InputAdornment> }}
//                 />
//
//                 <Button variant="contained" startIcon={<AddIcon />} onClick={() => handleOpenModal()} sx={{ mb: 2 }}>
//                     Добавить работника
//                 </Button>
//
//                 {loading ? <CircularProgress /> : (
//                     <>
//                         <Table stickyHeader>
//                             <TableHead>
//                                 <TableRow>
//                                     {columns.map(col => (
//                                         <TableCell key={col.id}>
//                                             {col.id !== 'actions' ? (
//                                                 <TableSortLabel
//                                                     active={sort === col.id}
//                                                     direction={dir}
//                                                     onClick={() => {
//                                                         setDir(sort === col.id && dir === 'asc' ? 'desc' : 'asc');
//                                                         setSort(col.id);
//                                                     }}
//                                                 >
//                                                     {col.label}
//                                                 </TableSortLabel>
//                                             ) : col.label}
//                                         </TableCell>
//                                     ))}
//                                 </TableRow>
//                             </TableHead>
//                             <TableBody>
//                                 {workers.map(w => (
//                                     <TableRow key={w.id} hover>
//                                         {columns.map(col => (
//                                             <TableCell key={col.id}>
//                                                 {col.render ? col.render(w) : w[col.id]}
//                                             </TableCell>
//                                         ))}
//                                     </TableRow>
//                                 ))}
//                             </TableBody>
//                         </Table>
//
//                         <TablePagination
//                             component="div"
//                             count={total}
//                             page={page}
//                             onPageChange={(_, p) => setPage(p)}
//                             rowsPerPage={rowsPerPage}
//                             onRowsPerPageChange={(e) => { setRowsPerPage(+e.target.value); setPage(0); }}
//                         />
//                     </>
//                 )}
//
//                 {/* Special operations */}
//                 <Box mt={6} p={3} border={1} borderColor="grey.300" borderRadius={2}>
//                     <Typography variant="h6" gutterBottom>Специальные операции</Typography>
//                     <Grid container spacing={2} alignItems="flex-end">
//                         <Grid item xs={12} sm={4}>
//                             <TextField fullWidth label="Name contains" value={searchNameContains} onChange={e => setSearchNameContains(e.target.value)} />
//                             <Button onClick={() => runSpecialSearch('contains')} variant="outlined" sx={{ mt: 1 }}>Найти</Button>
//                         </Grid>
//                         <Grid item xs={12} sm={4}>
//                             <TextField fullWidth label="Name starts with" value={searchNameStarts} onChange={e => setSearchNameStarts(e.target.value)} />
//                             <Button onClick={() => runSpecialSearch('starts')} variant="outlined" sx={{ mt: 1 }}>Найти</Button>
//                         </Grid>
//                         <Grid item xs={12} sm={4}>
//                             <TextField fullWidth label="Rating <" type="number" value={searchRatingLess} onChange={e => setSearchRatingLess(e.target.value)} />
//                             <Button onClick={() => runSpecialSearch('rating')} variant="outlined" sx={{ mt: 1 }}>Найти</Button>
//                         </Grid>
//                     </Grid>
//
//                     {specialResults.length > 0 && (
//                         <Box mt={3}>
//                             <Typography>Результаты: {specialResults.length}</Typography>
//                             {specialResults.map(w => (
//                                 <Chip key={w.id} label={`${w.name} (ID ${w.id})`} sx={{ m: 0.5 }} />
//                             ))}
//                         </Box>
//                     )}
//                 </Box>
//
//                 {/* CRUD Modal */}
//                 <Dialog open={openModal} onClose={() => setOpenModal(false)} maxWidth="md" fullWidth>
//                     <DialogTitle>{editingWorker ? 'Редактировать' : 'Создать'} работника</DialogTitle>
//                     <DialogContent dividers>
//                         <Grid container spacing={2}>
//                             <Grid item xs={6}>
//                                 <TextField fullWidth label="Имя" value={formData.name || ''} onChange={e => setFormData({ ...formData, name: e.target.value })} />
//                             </Grid>
//                             <Grid item xs={6}>
//                                 <FormControl fullWidth>
//                                     <InputLabel>Организация</InputLabel>
//                                     <Select
//                                         value={formData.organization?.id || ''}
//                                         onChange={e => setFormData({ ...formData, organization: organizations.find(o => o.id === e.target.value) })}
//                                     >
//                                         {organizations.map(o => (
//                                             <MenuItem key={o.id} value={o.id}>{o.fullName || `ID ${o.id}`}</MenuItem>
//                                         ))}
//                                     </Select>
//                                 </FormControl>
//                             </Grid>
//
//                             <Grid item xs={6}>
//                                 <TextField fullWidth label="Salary" type="number" value={formData.salary || ''} onChange={e => setFormData({ ...formData, salary: +e.target.value })} />
//                             </Grid>
//                             <Grid item xs={6}>
//                                 <TextField fullWidth label="Rating" type="number" step="0.01" value={formData.rating || ''} onChange={e => setFormData({ ...formData, rating: +e.target.value })} />
//                             </Grid>
//
//                             <Grid item xs={6}>
//                                 <DatePicker label="Start date" value={dayjs(formData.startDate)} onChange={d => setFormData({ ...formData, startDate: d?.format('YYYY-MM-DD') })} />
//                             </Grid>
//                             <Grid item xs={6}>
//                                 <DatePicker label="End date (optional)" value={formData.endDate ? dayjs(formData.endDate) : null} onChange={d => setFormData({ ...formData, endDate: d?.format('YYYY-MM-DD') || undefined })} />
//                             </Grid>
//
//                             <Grid item xs={6}>
//                                 <TextField fullWidth label="Coordinates X" type="number" value={formData.coordinates?.x || ''} onChange={e => setFormData({ ...formData, coordinates: { ...formData.coordinates, x: +e.target.value } })} />
//                             </Grid>
//                             <Grid item xs={6}>
//                                 <TextField fullWidth label="Coordinates Y" type="number" value={formData.coordinates?.y || ''} onChange={e => setFormData({ ...formData, coordinates: { ...formData.coordinates, y: +e.target.value } })} />
//                             </Grid>
//
//                             <Grid item xs={12}>
//                                 <Typography variant="subtitle1" gutterBottom><PersonIcon /> Персональные данные (необязательно)</Typography>
//                             </Grid>
//                             <Grid item xs={6}>
//                                 <TextField fullWidth label="Passport ID" value={formData.person?.passportID || ''} onChange={e => setFormData({ ...formData, person: { ...formData.person, passportID: e.target.value } })} />
//                             </Grid>
//                             <Grid item xs={6}>
//                                 <TextField fullWidth label="Height" type="number" value={formData.person?.height || ''} onChange={e => setFormData({ ...formData, person: { ...formData.person, height: +e.target.value } })} />
//                             </Grid>
//                             {/* Add more Person fields as needed */}
//                         </Grid>
//                     </DialogContent>
//                     <DialogActions>
//                         <Button onClick={() => setOpenModal(false)}>Отмена</Button>
//                         <Button variant="contained" onClick={handleSave}>Сохранить</Button>
//                     </DialogActions>
//                 </Dialog>
//             </Box>
//         </LocalizationProvider>
//     );
// }
//
// export default Main;