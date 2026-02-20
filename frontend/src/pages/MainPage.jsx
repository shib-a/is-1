import React, { useState, useEffect } from "react";
import NestedTable from "../components/NestedTable";
import CacheAndTestPanel from "../components/CacheAndTestPanel";
import Coordinates from "../classes/Coordinates";
import Worker from "../classes/Worker";
import Organization from "../classes/Organization";
import Person from "../classes/Person";
import Position from "../classes/Position";
import Color from "../classes/Color";
import Country from "../classes/Country";
import Address from "../classes/Address";
import Location from "../classes/Location";
import axios from "axios";
import { Dialog, DialogTitle, DialogContent, Button, Table, TableHead, TableRow, TableCell, TableBody, TextField, Select, MenuItem, Pagination, Box, Tabs, Tab, CircularProgress, Alert } from "@mui/material";
import { Memory as MemoryIcon, Settings as SettingsIcon, CheckCircle as CheckCircleIcon, Error as ErrorIcon } from '@mui/icons-material';

const MainPage = () => {

    const [workers, setWorkers] = useState([]);
    const [currentPage, setCurrentPage] = useState(1);
    const [loading, setLoading] = useState(false);
    const [currentTab, setCurrentTab] = useState(0);
    const [cacheLoading, setCacheLoading] = useState(false);
    const [cacheStats, setCacheStats] = useState(null);
    const [cacheMessage, setCacheMessage] = useState(null);
    const [test2pcResult, setTest2pcResult] = useState(null);
    const [test2pcLoading, setTest2pcLoading] = useState(false);

    const [data, setData] = useState(null);
    const [expandedRowId, setExpandedRowId] = useState(null);
    const [expandedField, setExpandedField] = useState(null);
    const [pageSize, setPageSize] = useState(10);
    const [totalPages, setTotalPages] = useState(1);
    const [isAddOpen, setIsAddOpen] = useState(false);
    const [isEditOpen, setIsEditOpen] = useState(false);
    const [isViewOpen, setIsViewOpen] = useState(false);
    const [selectedWorker, setSelectedWorker] = useState(null);
    const [isDeleteConfirmOpen, setIsDeleteConfirmOpen] = useState(false);
    const [deleteId, setDeleteId] = useState(null);
    const [isNameContainsOpen, setIsNameContainsOpen] = useState(false);
    const [isNameStartsOpen, setIsNameStartsOpen] = useState(false);
    const [isRatingLessOpen, setIsRatingLessOpen] = useState(false);
    const [isHireOpen, setIsHireOpen] = useState(false);
    const [isMoveOpen, setIsMoveOpen] = useState(false);
    const [startDate, setStartDate] = useState(null);
    const [endDate, setEndDate] = useState(null);
    const [currentCreatedWorker, setCurrentCreatedWorker] = useState(() =>  new Worker({
        name: "",
        creationDate: null,
        endDate: null,
        salary: null,
        rating: null,
        startDate: null,
        organization: new Organization({
            officialAddress: new Address({
                town: new Location({x:null, y:null, z:null, name:null})
            })
        }),
        coordinates: new Coordinates({x: null, y: null}),
        person: new Person({location: new Location({})}),
    }));
    const [isCoordCreationActive, setIsCoordCreationActive] = useState(false);
    const [isPersonCreationActive, setIsPersonCreationActive] = useState(false);
    const [isPersonLocationCreationActive, setIsPersonLocationCreationActive] = useState(false);
    const [isOrgCreationActive, setIsOrgCreationActive] = useState(false);
    const [isOrgAddressCreationActive, setIsOrgAddressCreationActive] = useState(false);
    const [isOrgAddressLocationCreationActive, setIsOrgAddressLocationCreationActive] = useState(false);
    const [editedId, setEditedId] = useState(null);
    const [nameSubstring, setNameSubstring] = useState('');
    const [ratingThreshold, setRatingThreshold] = useState(0);
    const [hireWorkerId, setHireWorkerId] = useState(null);
    const [hireOrgId, setHireOrgId] = useState(null);
    const [moveWorkerId, setMoveWorkerId] = useState(null);
    const [moveNewOrgId, setMoveNewOrgId] = useState(null);
    const [searchType, setSearchType] = useState(null);
    const [searchParam, setSearchParam] = useState(null);
    const [isSubViewOpen, setIsSubViewOpen] = useState(false);
    const [subViewData, setSubViewData] = useState(null);
    const [subViewField, setSubViewField] = useState(null);

    const handleAddOpen = () => {
        setCurrentCreatedWorker(new Worker({
            name: "",
            creationDate: null,
            endDate: null,
            salary: null,
            rating: null,
            startDate: null,
            organization: new Organization({
                officialAddress: new Address({
                    town: new Location({x:null, y:null, z:null, name:null})
                })
            }),
            coordinates: new Coordinates({x: null, y: null}),
            person: new Person({location: new Location({})}),
        }));
        setStartDate(null);
        setEndDate(null);
        setIsAddOpen(true);
    };

    const handleAddConfirm = async () => {
        currentCreatedWorker.startDate = startDate ? new Date(startDate) : null;
        currentCreatedWorker.endDate = endDate ? new Date(endDate) : null;
        await axios.post(
            "http://localhost:25203/is-1-1.0-SNAPSHOT/workers/add",
            currentCreatedWorker
        )
            .then(response =>{
                console.log(response);
                setIsAddOpen(false);
                fetchData(currentPage);
            })
            .catch(err =>{

            })
    }

    const handleEdit = (row) => {
        setCurrentCreatedWorker(new Worker(row));
        setStartDate(row.startDate ? row.startDate.split('T')[0] : null);
        setEndDate(row.endDate ? row.endDate.split('T')[0] : null);
        setEditedId(row.id);
        setIsEditOpen(true);
    };

    const handleEditConfirm = async () => {
        currentCreatedWorker.startDate = startDate ? new Date(startDate) : null;
        currentCreatedWorker.endDate = endDate ? new Date(endDate) : null;
        await axios.put(
            `http://localhost:25203/is-1-1.0-SNAPSHOT/workers/edit`,
    currentCreatedWorker
)
.then(response => {
    console.log(response);
    setIsEditOpen(false);
    fetchData(currentPage);
})
    .catch(err => {

    })
};

const handleView = async (id) => {
    await axios.get(`http://localhost:25203/is-1-1.0-SNAPSHOT/workers/${id}`)
        .then(response => {
            setSelectedWorker(response.data);
            setIsViewOpen(true);
        })
        .catch(err => {
            console.error(err);
        });
};

const handleDelete = (id) => {
    setDeleteId(id);
    setIsDeleteConfirmOpen(true);
};

const handleDeleteConfirm = async () => {
    await axios.delete(`http://localhost:25203/is-1-1.0-SNAPSHOT/workers/delete?id=${deleteId}`)
        .then(response => {
            console.log(response);
            setIsDeleteConfirmOpen(false);
            fetchData(currentPage);
        })
        .catch(err => {
            console.error(err);
        });
};

const handleNameContainsConfirm = async () => {
    setSearchType('name-contains');
    setSearchParam(nameSubstring);
    setCurrentPage(1);
    setIsNameContainsOpen(false);
};

const handleNameStartsConfirm = async () => {
    setSearchType('name-starts');
    setSearchParam(nameSubstring);
    setCurrentPage(1);
    setIsNameStartsOpen(false);
};

const handleRatingLessConfirm = async () => {
    setSearchType('rating-less');
    setSearchParam(ratingThreshold);
    setCurrentPage(1);
    setIsRatingLessOpen(false);
};

const handleReset = () => {
    setSearchType(null);
    setSearchParam(null);
    setCurrentPage(1);
};

const handleHireConfirm = async () => {
    await axios.post(
        `http://localhost:25203/is-1-1.0-SNAPSHOT/workers/hire`,
        {},
        { params: { workerId: hireWorkerId, organizationId: hireOrgId } }
    )
        .then(response => {
            console.log(response);
            setIsHireOpen(false);
            fetchData(currentPage);
        })
        .catch(err => {

        });
};

const handleMoveConfirm = async () => {
    await axios.post(
        `http://localhost:25203/is-1-1.0-SNAPSHOT/workers/move`,
        {},
        { params: { workerId: moveWorkerId, newOrganizationId: moveNewOrgId } }
    )
        .then(response => {
            console.log(response);
            setIsMoveOpen(false);
            fetchData(currentPage);
        })
        .catch(err => {

        });
};

const getCacheStats = async () => {
    setCacheLoading(true);
    try {
        const response = await axios.get('http://localhost:25203/is-1-1.0-SNAPSHOT/api/cache/stats');
        setCacheStats(response.data);
        setCacheMessage(`Hits: ${response.data.hits}, Misses: ${response.data.misses}, Logging: ${response.data.loggingEnabled ? 'ON' : 'OFF'}`);
        setTimeout(() => setCacheMessage(null), 5000);
    } catch (error) {
        setCacheMessage('Ошибка при получении статистики кэша');
        setTimeout(() => setCacheMessage(null), 5000);
    }
    setCacheLoading(false);
};

const toggleCacheLogging = async () => {
    setCacheLoading(true);
    try {
        const endpoint = cacheStats?.loggingEnabled
            ? 'http://localhost:25203/is-1-1.0-SNAPSHOT/api/cache/logging/disable'
            : 'http://localhost:25203/is-1-1.0-SNAPSHOT/api/cache/logging/enable';
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
        await axios.post('http://localhost:25203/is-1-1.0-SNAPSHOT/api/cache/clear');
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
            ? 'http://localhost:25203/is-1-1.0-SNAPSHOT/api/test/2pc/test-db-failure'
            : 'http://localhost:25203/is-1-1.0-SNAPSHOT/api/test/2pc/test-business-logic-failure';
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

const fetchData = async (page) => {
    setLoading(true);
    let url = `http://localhost:25203/is-1-1.0-SNAPSHOT/workers/get?page=${page}&size=${pageSize}`;
    if (searchType === 'name-contains') {
        url = `http://localhost:25203/is-1-1.0-SNAPSHOT/workers/name-contains?substring=${searchParam}&page=${page}&size=${pageSize}`;
    } else if (searchType === 'name-starts') {
        url = `http://localhost:25203/is-1-1.0-SNAPSHOT/workers/name-starts?substring=${searchParam}&page=${page}&size=${pageSize}`;
    } else if (searchType === 'rating-less') {
        url = `http://localhost:25203/is-1-1.0-SNAPSHOT/workers/rating-less?rating=${searchParam}&page=${page}&size=${pageSize}`;
    }
    await axios.get(url)
        .then(response =>{
            setData(response.data);
            setLoading(false);
        })
        .catch(err => {

        })
};

const fetchCount = async () => {
    let url = `http://localhost:25203/is-1-1.0-SNAPSHOT/workers/getTotalPages`;
    if (searchType === 'name-contains') {
        url = `http://localhost:25203/is-1-1.0-SNAPSHOT/workers/name-contains/count?substring=${searchParam}`;
    } else if (searchType === 'name-starts') {
        url = `http://localhost:25203/is-1-1.0-SNAPSHOT/workers/name-starts/count?substring=${searchParam}`;
    } else if (searchType === 'rating-less') {
        url = `http://localhost:25203/is-1-1.0-SNAPSHOT/workers/rating-less/count?rating=${searchParam}`;
    }
    await axios.get(url)
        .then(response => {
            const count = response.data;
            setTotalPages(Math.ceil(count / pageSize));
        })
        .catch(err => {

        });
};

// Fetch data when component mounts or dependencies change
useEffect(() => {
    fetchData(currentPage);
    fetchCount();
}, [currentPage, searchType, searchParam, pageSize]);

// Periodically refresh every 10 seconds
useEffect(() => {
    const intervalId = setInterval(() => {
        fetchData(currentPage);
    }, 10000); // 10 seconds
    return () => clearInterval(intervalId);
}, [currentPage]);

const handleSubView = (row, fieldName) => {
    setSubViewData(row[fieldName]);
    setSubViewField(fieldName);
    setIsSubViewOpen(true);
};

const handlePageChange = (event, value) => {
    setCurrentPage(value);
};

const renderSubViewContent = () => {
    if (!subViewData) return null;

    if (subViewField === 'coordinates') {
        return (
            <Table>
                <TableHead>
                    <TableRow>
                        <TableCell>x</TableCell>
                        <TableCell>y</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    <TableRow>
                        <TableCell>{subViewData.x}</TableCell>
                        <TableCell>{subViewData.y}</TableCell>
                    </TableRow>
                </TableBody>
            </Table>
        );
    } else if (subViewField === 'organization') {
        return (
            <>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>Annual Turnover</TableCell>
                            <TableCell>Employee Count</TableCell>
                            <TableCell>Full Name</TableCell>
                            <TableCell>Rating</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        <TableRow>
                            <TableCell>{subViewData.annualTurnover}</TableCell>
                            <TableCell>{subViewData.employeesCount}</TableCell>
                            <TableCell>{subViewData.fullName}</TableCell>
                            <TableCell>{subViewData.rating}</TableCell>
                        </TableRow>
                    </TableBody>
                </Table>
                <DialogTitle>Official Address</DialogTitle>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>Street</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        <TableRow>
                            <TableCell>{subViewData.officialAddress.street}</TableCell>
                        </TableRow>
                    </TableBody>
                </Table>
                <DialogTitle>Town</DialogTitle>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>x</TableCell>
                            <TableCell>y</TableCell>
                            <TableCell>z</TableCell>
                            <TableCell>Name</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        <TableRow>
                            <TableCell>{subViewData.officialAddress.town.x}</TableCell>
                            <TableCell>{subViewData.officialAddress.town.y}</TableCell>
                            <TableCell>{subViewData.officialAddress.town.z}</TableCell>
                            <TableCell>{subViewData.officialAddress.town.name}</TableCell>
                        </TableRow>
                    </TableBody>
                </Table>
            </>
        );
    } else if (subViewField === 'person') {
        return (
            <>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>Eye Color</TableCell>
                            <TableCell>Hair Color</TableCell>
                            <TableCell>Height</TableCell>
                            <TableCell>Passport ID</TableCell>
                            <TableCell>Nationality</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        <TableRow>
                            <TableCell>{subViewData.eyeColor}</TableCell>
                            <TableCell>{subViewData.hairColor}</TableCell>
                            <TableCell>{subViewData.height}</TableCell>
                            <TableCell>{subViewData.passportID}</TableCell>
                            <TableCell>{subViewData.nationality}</TableCell>
                        </TableRow>
                    </TableBody>
                </Table>
                <DialogTitle>Location</DialogTitle>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>x</TableCell>
                            <TableCell>y</TableCell>
                            <TableCell>z</TableCell>
                            <TableCell>Name</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        <TableRow>
                            <TableCell>{subViewData.location.x}</TableCell>
                            <TableCell>{subViewData.location.y}</TableCell>
                            <TableCell>{subViewData.location.z}</TableCell>
                            <TableCell>{subViewData.location.name}</TableCell>
                        </TableRow>
                    </TableBody>
                </Table>
            </>
        );
    }
    return null;
};

return (
    <div>
        <div className="mainContainer">
            <button className="addButton" onClick={handleAddOpen}>Add Worker</button>
            <button onClick={() => setIsNameContainsOpen(true)}>Search Name Contains</button>
            <button onClick={() => setIsNameStartsOpen(true)}>Search Name Starts With</button>
            <button onClick={() => setIsRatingLessOpen(true)}>Search Rating Less Than</button>
            <button onClick={() => setIsHireOpen(true)}>Hire Worker</button>
            <button onClick={() => setIsMoveOpen(true)}>Move Worker</button>
            <button onClick={handleReset}>Reset Search</button>

            <table className="mainTable">
                <thead>
                <th>ID</th>
                <th>Name</th>
                <th>Coordinates</th>
                <th>Organization</th>
                <th>Salary</th>
                <th>Rating</th>
                <th>Start Date</th>
                <th>End Date</th>
                <th>Position</th>
                <th>Person</th>
                <th>Creation Date</th>
                <th>Actions</th>
                </thead>
                {data != null ? (
                    <tbody>
                    {data.map((row) => (
                        <React.Fragment key={row.id}>
                            <tr>
                                <td>{row.id}</td>
                                <td>{row.name}</td>
                                <td
                                    style={{cursor: 'pointer', color: 'blue'}}
                                    onClick={() => handleSubView(row, 'coordinates')}
                                >
                                    [Click to view]
                                </td>
                                <td
                                    style={{cursor: 'pointer', color: 'blue'}}
                                    onClick={() => handleSubView(row, 'organization')}
                                >
                                    [Click to view]
                                </td>
                                <td>{row.salary}</td>
                                <td>{row.rating}</td>
                                <td>{row.startDate}</td>
                                <td>{row.endDate}</td>
                                <td>{row.position}</td>
                                <td
                                    style={{cursor: 'pointer', color: 'blue'}}
                                    onClick={() => handleSubView(row, 'person')}
                                >
                                    [Click to view]
                                </td>
                                <td>{row.creationDate}</td>
                                <td>
                                    <button onClick={() => handleView(row.id)}>View</button>
                                    <button onClick={() => handleEdit(row)}>Edit</button>
                                    <button onClick={() => handleDelete(row.id)}>Delete</button>
                                </td>
                            </tr>
                        </React.Fragment>
                    ))}
                    </tbody>

                ):(<div/>)}
            </table>
            <Pagination count={totalPages} page={currentPage} onChange={handlePageChange} />
        </div>
        {isCoordCreationActive && (
            <Dialog open={isCoordCreationActive} onClose={() => setIsCoordCreationActive(false)} fullWidth maxWidth="sm">
                <DialogTitle>Worker Coordinates</DialogTitle>
                <DialogContent>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell>x</TableCell>
                                <TableCell>y</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            <TableRow>
                                <TableCell><input type={"number"} value={currentCreatedWorker.coordinates.x} placeholder={"0..."} onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                    ...prev,
                                    coordinates: new Coordinates({...prev.coordinates, x: Number(e.target.value)})
                                }))}/></TableCell>
                                <TableCell><input type={"number"} value={currentCreatedWorker.coordinates.y} placeholder={"0..."} onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                    ...prev,
                                    coordinates: new Coordinates({...prev.coordinates, y: Number(e.target.value)})
                                }))}/></TableCell>
                            </TableRow>
                        </TableBody>
                    </Table>
                </DialogContent>
            </Dialog>
        )}
        {isOrgCreationActive && (
            <Dialog open={isOrgCreationActive} onClose={() => setIsOrgCreationActive(false)} fullWidth maxWidth="sm">
                <DialogTitle>Organization</DialogTitle>
                <DialogContent>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell>Official Address</TableCell>
                                <TableCell>Annual Turnover</TableCell>
                                <TableCell>Employee Count</TableCell>
                                <TableCell>Full Name</TableCell>
                                <TableCell>Rating</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            <TableRow>
                                <TableCell onClick={(e) => setIsOrgAddressCreationActive(!isOrgAddressCreationActive)}>[configure]</TableCell>
                                <TableCell><input type={"number"} value={currentCreatedWorker.organization.annualTurnover} placeholder={"0..."}
                                                  onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                                      ...prev,
                                                      organization: new Organization({...prev.organization, annualTurnover: e.target.value})}))}/>
                                </TableCell>
                                <TableCell><input type={"number"} value={currentCreatedWorker.organization.employeesCount} placeholder={"0..."}
                                                  onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                                      ...prev,
                                                      organization: new Organization({...prev.organization, employeesCount: e.target.value})}))}/>
                                </TableCell>
                                <TableCell><input type={"text"} value={currentCreatedWorker.organization.fullName} placeholder={"OOO..."}
                                                  onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                                      ...prev,
                                                      organization: new Organization({...prev.organization, fullName: e.target.value})}))}/>
                                </TableCell>
                                <TableCell><input type={"number"} value={currentCreatedWorker.organization.rating} placeholder={"0..."}
                                                  onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                                      ...prev,
                                                      organization: new Organization({...prev.organization, rating: e.target.value})}))}/>
                                </TableCell>
                            </TableRow>
                        </TableBody>
                    </Table>
                </DialogContent>
            </Dialog>
        )}
        {isOrgAddressCreationActive && (
            <Dialog open={isOrgAddressCreationActive} onClose={() => setIsOrgAddressCreationActive(false)} fullWidth maxWidth="sm">
                <DialogTitle>Organization Address</DialogTitle>
                <DialogContent>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell>Street</TableCell>
                                <TableCell>Town</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            <TableRow>
                                <TableCell><input type={"text"} value={currentCreatedWorker.organization.officialAddress.street} placeholder={"0..."} onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                    ...prev,
                                    organization: new Organization({...prev.organization, officialAddress: new Address({...prev.organization.officialAddress, street:e.target.value})})
                                }))}/></TableCell>
                                <TableCell onClick={(e) => setIsOrgAddressLocationCreationActive((!isOrgAddressLocationCreationActive))}>[configure]</TableCell>
                            </TableRow>
                        </TableBody>
                    </Table>
                </DialogContent>
            </Dialog>
        )}
        {isOrgAddressLocationCreationActive && (
            <Dialog open={isOrgAddressLocationCreationActive} onClose={() => setIsOrgAddressLocationCreationActive(false)} fullWidth maxWidth="sm">
                <DialogTitle>Organization Town</DialogTitle>
                <DialogContent>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell>x</TableCell>
                                <TableCell>y</TableCell>
                                <TableCell>z</TableCell>
                                <TableCell>Name</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            <TableRow>
                                <TableCell><input type={"number"} value={currentCreatedWorker.organization.officialAddress.town.x} placeholder={"0..."} onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                    ...prev,
                                    organization: new Organization({...prev.organization, officialAddress: new Address({...prev.organization.officialAddress, town:new Location({
                                                ...prev.organization.officialAddress.town,
                                                x: e.target.value
                                            })})})
                                }))}/></TableCell>
                                <TableCell><input type={"number"} value={currentCreatedWorker.organization.officialAddress.town.y} placeholder={"0..."} onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                    ...prev,
                                    organization: new Organization({...prev.organization, officialAddress: new Address({...prev.organization.officialAddress, town:new Location({
                                                ...prev.organization.officialAddress.town,
                                                y: e.target.value
                                            })})})
                                }))}/></TableCell>
                                <TableCell><input type={"number"} value={currentCreatedWorker.organization.officialAddress.town.z} placeholder={"0..."} onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                    ...prev,
                                    organization: new Organization({...prev.organization, officialAddress: new Address({...prev.organization.officialAddress, town:new Location({
                                                ...prev.organization.officialAddress.town,
                                                z: e.target.value
                                            })})})
                                }))}/></TableCell>
                                <TableCell><input type={"text"} value={currentCreatedWorker.organization.officialAddress.town.name} placeholder={"0..."} onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                    ...prev,
                                    organization: new Organization({...prev.organization, officialAddress: new Address({...prev.organization.officialAddress, town:new Location({
                                                ...prev.organization.officialAddress.town,
                                                name: e.target.value
                                            })})})
                                }))}/></TableCell>
                            </TableRow>
                        </TableBody>
                    </Table>
                </DialogContent>
            </Dialog>

        )}
        {isPersonCreationActive && (
            <Dialog open={isPersonCreationActive} onClose={() => setIsPersonCreationActive(false)} fullWidth maxWidth="sm">
                <DialogTitle>Person</DialogTitle>
                <DialogContent>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell>Eye Color</TableCell>
                                <TableCell>Hair Color</TableCell>
                                <TableCell>Location</TableCell>
                                <TableCell>Height</TableCell>
                                <TableCell>Passport ID</TableCell>
                                <TableCell>Nationality</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            <TableRow>
                                <TableCell><Select value={currentCreatedWorker.person.eyeColor} onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                    ...prev,
                                    person: new Person({...prev.person, eyeColor: e.target.value})}))}>
                                    <MenuItem value="">--Please choose an option--</MenuItem>
                                    <MenuItem value={Color.RED}>Red</MenuItem>
                                    <MenuItem value={Color.BLACK}>Black</MenuItem>
                                    <MenuItem value={Color.WHITE}>White</MenuItem>
                                    <MenuItem value={Color.ORANGE}>Orange</MenuItem>
                                    <MenuItem value={Color.YELLOW}>Yellow</MenuItem>
                                </Select></TableCell>
                                <TableCell><Select value={currentCreatedWorker.person.hairColor} onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                    ...prev,
                                    person: new Person({...prev.person, hairColor: e.target.value})}))}>
                                    <MenuItem value="">--Please choose an option--</MenuItem>
                                    <MenuItem value={Color.RED}>Red</MenuItem>
                                    <MenuItem value={Color.BLACK}>Black</MenuItem>
                                    <MenuItem value={Color.WHITE}>White</MenuItem>
                                    <MenuItem value={Color.ORANGE}>Orange</MenuItem>
                                    <MenuItem value={Color.YELLOW}>Yellow</MenuItem>
                                </Select></TableCell>
                                <TableCell onClick={(e) => setIsPersonLocationCreationActive(!isPersonLocationCreationActive)}>[configure]</TableCell>
                                <TableCell><input type={"number"} value={currentCreatedWorker.person.height} placeholder={"OOO..."}
                                                  onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                                      ...prev,
                                                      person: new Person({...prev.person, height: e.target.value})}))}/>
                                </TableCell>
                                <TableCell><input type={"text"} value={currentCreatedWorker.person.passportID} placeholder={"0..."}
                                                  onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                                      ...prev,
                                                      person: new Person({...prev.person, passportID: e.target.value})}))}/>
                                </TableCell>
                                <TableCell><Select value={currentCreatedWorker.person.nationality} onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                    ...prev,
                                    person: new Person({...prev.person, nationality: e.target.value})}))}>
                                    <MenuItem value="">--Please choose an option--</MenuItem>
                                    <MenuItem value={Country.RUSSIA}>Russia</MenuItem>
                                    <MenuItem value={Country.UNITED_KINGDOM}>UK</MenuItem>
                                    <MenuItem value={Country.FRANCE}>France</MenuItem>
                                    <MenuItem value={Country.INDIA}>India</MenuItem>
                                    <MenuItem value={Country.THAILAND}>Thailand</MenuItem>
                                </Select></TableCell>
                            </TableRow>
                        </TableBody>
                    </Table>
                </DialogContent>
            </Dialog>
        )}
        {isPersonLocationCreationActive && (
            <Dialog open={isPersonLocationCreationActive} onClose={() => setIsPersonLocationCreationActive(false)} fullWidth maxWidth="sm">
                <DialogTitle>Person Location</DialogTitle>
                <DialogContent>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell>x</TableCell>
                                <TableCell>y</TableCell>
                                <TableCell>z</TableCell>
                                <TableCell>Name</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            <TableRow>
                                <TableCell><input type={"number"} value={currentCreatedWorker.person.location.x} placeholder={"0..."} onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                    ...prev,
                                    person: new Person({...prev.person, location: new Location({...prev.person.location, x: e.target.value
                                        })})
                                }))}/></TableCell>
                                <TableCell><input type={"number"} value={currentCreatedWorker.person.location.y} placeholder={"0..."} onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                    ...prev,
                                    person: new Person({...prev.person, location: new Location({...prev.person.location, y: e.target.value
                                        })})
                                }))}/></TableCell>
                                <TableCell><input type={"number"} value={currentCreatedWorker.person.location.z} placeholder={"0..."} onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                    ...prev,
                                    person: new Person({...prev.person, location: new Location({...prev.person.location, z: e.target.value
                                        })})
                                }))}/></TableCell>
                                <TableCell><input type={"text"} value={currentCreatedWorker.person.location.name} placeholder={"0..."} onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                    ...prev,
                                    person: new Person({...prev.person, location: new Location({...prev.person.location, name: e.target.value
                                        })})
                                }))}/></TableCell>
                            </TableRow>
                        </TableBody>
                    </Table>
                </DialogContent>
            </Dialog>
        )}
        {isAddOpen && (
            <Dialog open={isAddOpen} onClose={() => setIsAddOpen(false)} fullWidth maxWidth="md">
                <DialogTitle>Add Worker</DialogTitle>
                <DialogContent>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell>Name</TableCell>
                                <TableCell>Coordinates</TableCell>
                                <TableCell>Organization</TableCell>
                                <TableCell>Salary</TableCell>
                                <TableCell>Rating</TableCell>
                                <TableCell>Start Date</TableCell>
                                <TableCell>End Date</TableCell>
                                <TableCell>Position</TableCell>
                                <TableCell>Person</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            <TableRow>
                                <TableCell>
                                    <input type={"text"} value={currentCreatedWorker.name} onChange={(e) => {
                                        setCurrentCreatedWorker(prev => new Worker({
                                            ...prev,
                                            name: e.target.value
                                        }));
                                    }}/>
                                </TableCell>
                                <TableCell onClick={(e) => setIsCoordCreationActive(true)}>[configure]</TableCell>
                                <TableCell onClick={(e) => setIsOrgCreationActive(true)}>[configure]</TableCell>
                                <TableCell>
                                    <input type={"number"} value={currentCreatedWorker.salary} placeholder={"0..."} onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                        ...prev,
                                        salary: e.target.value
                                    }))}/>
                                </TableCell>
                                <TableCell>
                                    <input type={"number"} value={currentCreatedWorker.rating} placeholder={"0..."} onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                        ...prev,
                                        rating: e.target.value
                                    }))}/>
                                </TableCell>
                                <TableCell>
                                    <input type={"date"} value={startDate} placeholder={"0.."} onChange={(e) => setStartDate(e.target.value)}/>
                                </TableCell>
                                <TableCell>
                                    <input type={"date"} value={endDate} placeholder={"0..."} onChange={(e) => setEndDate(e.target.value)}/>
                                </TableCell>
                                <TableCell>
                                    <select name="positions" value={currentCreatedWorker.position}  className="positionSelect" onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                        ...prev,
                                        position: e.target.value
                                    }))}>
                                        <option value="">--Please choose an option--</option>
                                        <option value={Position.LABORER}>Laborer</option>
                                        <option value={Position.HEAD_OF_DEPARTMENT}>Head of Dept.</option>
                                        <option value={Position.HUMAN_RESOURCES}>HR</option>
                                    </select>
                                </TableCell>
                                <TableCell onClick={(e) => setIsPersonCreationActive(true)}>[configure]</TableCell>
                            </TableRow>
                        </TableBody>
                    </Table>
                </DialogContent>
                <Button onClick={handleAddConfirm}>Confirm</Button>
            </Dialog>
        )}
        {isEditOpen && (
            <Dialog open={isEditOpen} onClose={() => setIsEditOpen(false)} fullWidth maxWidth="md">
                <DialogTitle>Edit Worker</DialogTitle>
                <DialogContent>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell>Name</TableCell>
                                <TableCell>Coordinates</TableCell>
                                <TableCell>Organization</TableCell>
                                <TableCell>Salary</TableCell>
                                <TableCell>Rating</TableCell>
                                <TableCell>Start Date</TableCell>
                                <TableCell>End Date</TableCell>
                                <TableCell>Position</TableCell>
                                <TableCell>Person</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            <TableRow>
                                <TableCell>
                                    <input type={"text"} value={currentCreatedWorker.name} onChange={(e) => {
                                        setCurrentCreatedWorker(prev => new Worker({
                                            ...prev,
                                            name: e.target.value
                                        }));
                                    }}/>
                                </TableCell>
                                <TableCell onClick={(e) => setIsCoordCreationActive(true)}>[configure]</TableCell>
                                <TableCell onClick={(e) => setIsOrgCreationActive(true)}>[configure]</TableCell>
                                <TableCell>
                                    <input type={"number"} value={currentCreatedWorker.salary} placeholder={"0..."} onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                        ...prev,
                                        salary: e.target.value
                                    }))}/>
                                </TableCell>
                                <TableCell>
                                    <input type={"number"} value={currentCreatedWorker.rating} placeholder={"0..."} onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                        ...prev,
                                        rating: e.target.value
                                    }))}/>
                                </TableCell>
                                <TableCell>
                                    <input type={"date"} value={startDate} placeholder={"0.."} onChange={(e) => setStartDate(e.target.value)}/>
                                </TableCell>
                                <TableCell>
                                    <input type={"date"} value={endDate} placeholder={"0..."} onChange={(e) => setEndDate(e.target.value)}/>
                                </TableCell>
                                <TableCell>
                                    <select name="positions" value={currentCreatedWorker.position}  className="positionSelect" onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                        ...prev,
                                        position: e.target.value
                                    }))}>
                                        <option value="">--Please choose an option--</option>
                                        <option value={Position.LABORER}>Laborer</option>
                                        <option value={Position.HEAD_OF_DEPARTMENT}>Head of Dept.</option>
                                        <option value={Position.HUMAN_RESOURCES}>HR</option>
                                    </select>
                                </TableCell>
                                <TableCell onClick={(e) => setIsPersonCreationActive(true)}>[configure]</TableCell>
                            </TableRow>
                        </TableBody>
                    </Table>
                </DialogContent>
                <Button onClick={handleEditConfirm}>Confirm</Button>
            </Dialog>
        )}
        {isViewOpen && selectedWorker && (
            <Dialog open={isViewOpen} onClose={() => setIsViewOpen(false)} fullWidth maxWidth="md">
                <DialogTitle>Worker Details</DialogTitle>
                <DialogContent>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell>ID</TableCell>
                                <TableCell>Name</TableCell>
                                <TableCell>Salary</TableCell>
                                <TableCell>Rating</TableCell>
                                <TableCell>Start Date</TableCell>
                                <TableCell>End Date</TableCell>
                                <TableCell>Position</TableCell>
                                <TableCell>Creation Date</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            <TableRow>
                                <TableCell>{selectedWorker.id}</TableCell>
                                <TableCell>{selectedWorker.name}</TableCell>
                                <TableCell>{selectedWorker.salary}</TableCell>
                                <TableCell>{selectedWorker.rating}</TableCell>
                                <TableCell>{selectedWorker.startDate}</TableCell>
                                <TableCell>{selectedWorker.endDate}</TableCell>
                                <TableCell>{selectedWorker.position}</TableCell>
                                <TableCell>{selectedWorker.creationDate}</TableCell>
                            </TableRow>
                        </TableBody>
                    </Table>
                    <DialogTitle>Coordinates</DialogTitle>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell>x</TableCell>
                                <TableCell>y</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            <TableRow>
                                <TableCell>{selectedWorker.coordinates.x}</TableCell>
                                <TableCell>{selectedWorker.coordinates.y}</TableCell>
                            </TableRow>
                        </TableBody>
                    </Table>
                    <DialogTitle>Organization</DialogTitle>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell>Annual Turnover</TableCell>
                                <TableCell>Employee Count</TableCell>
                                <TableCell>Full Name</TableCell>
                                <TableCell>Rating</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            <TableRow>
                                <TableCell>{selectedWorker.organization.annualTurnover}</TableCell>
                                <TableCell>{selectedWorker.organization.employeesCount}</TableCell>
                                <TableCell>{selectedWorker.organization.fullName}</TableCell>
                                <TableCell>{selectedWorker.organization.rating}</TableCell>
                            </TableRow>
                        </TableBody>
                    </Table>
                    <DialogTitle>Official Address</DialogTitle>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell>Street</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            <TableRow>
                                <TableCell>{selectedWorker.organization.officialAddress.street}</TableCell>
                            </TableRow>
                        </TableBody>
                    </Table>
                    <DialogTitle>Town</DialogTitle>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell>x</TableCell>
                                <TableCell>y</TableCell>
                                <TableCell>z</TableCell>
                                <TableCell>Name</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            <TableRow>
                                <TableCell>{selectedWorker.organization.officialAddress.town.x}</TableCell>
                                <TableCell>{selectedWorker.organization.officialAddress.town.y}</TableCell>
                                <TableCell>{selectedWorker.organization.officialAddress.town.z}</TableCell>
                                <TableCell>{selectedWorker.organization.officialAddress.town.name}</TableCell>
                            </TableRow>
                        </TableBody>
                    </Table>
                    <DialogTitle>Person</DialogTitle>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell>Eye Color</TableCell>
                                <TableCell>Hair Color</TableCell>
                                <TableCell>Height</TableCell>
                                <TableCell>Passport ID</TableCell>
                                <TableCell>Nationality</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            <TableRow>
                                <TableCell>{selectedWorker.person.eyeColor}</TableCell>
                                <TableCell>{selectedWorker.person.hairColor}</TableCell>
                                <TableCell>{selectedWorker.person.height}</TableCell>
                                <TableCell>{selectedWorker.person.passportID}</TableCell>
                                <TableCell>{selectedWorker.person.nationality}</TableCell>
                            </TableRow>
                        </TableBody>
                    </Table>
                    <DialogTitle>Location</DialogTitle>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell>x</TableCell>
                                <TableCell>y</TableCell>
                                <TableCell>z</TableCell>
                                <TableCell>Name</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            <TableRow>
                                <TableCell>{selectedWorker.person.location.x}</TableCell>
                                <TableCell>{selectedWorker.person.location.y}</TableCell>
                                <TableCell>{selectedWorker.person.location.z}</TableCell>
                                <TableCell>{selectedWorker.person.location.name}</TableCell>
                            </TableRow>
                        </TableBody>
                    </Table>
                    <Button onClick={() => handleEdit(selectedWorker)}>Edit from here</Button>
                </DialogContent>
            </Dialog>
        )}
        {isDeleteConfirmOpen && (
            <Dialog open={isDeleteConfirmOpen} onClose={() => setIsDeleteConfirmOpen(false)}>
                <DialogTitle>Confirm Delete</DialogTitle>
                <DialogContent>
                    Are you sure you want to delete this worker?
                </DialogContent>
                <Button onClick={handleDeleteConfirm}>Yes</Button>
                <Button onClick={() => setIsDeleteConfirmOpen(false)}>No</Button>
            </Dialog>
        )}
        {isSubViewOpen && (
            <Dialog open={isSubViewOpen} onClose={() => setIsSubViewOpen(false)} fullWidth maxWidth="sm">
                <DialogTitle>{subViewField.charAt(0).toUpperCase() + subViewField.slice(1)}</DialogTitle>
                <DialogContent>
                    {renderSubViewContent()}
                </DialogContent>
            </Dialog>
        )}
        {isNameContainsOpen && (
            <Dialog open={isNameContainsOpen} onClose={() => setIsNameContainsOpen(false)}>
                <DialogTitle>Search by Name Contains</DialogTitle>
                <DialogContent>
                    <TextField
                        label="Substring"
                        value={nameSubstring}
                        onChange={(e) => setNameSubstring(e.target.value)}
                    />
                </DialogContent>
                <Button onClick={handleNameContainsConfirm}>Confirm</Button>
            </Dialog>
        )}
        {isNameStartsOpen && (
            <Dialog open={isNameStartsOpen} onClose={() => setIsNameStartsOpen(false)}>
                <DialogTitle>Search by Name Starts With</DialogTitle>
                <DialogContent>
                    <TextField
                        label="Substring"
                        value={nameSubstring}
                        onChange={(e) => setNameSubstring(e.target.value)}
                    />
                </DialogContent>
                <Button onClick={handleNameStartsConfirm}>Confirm</Button>
            </Dialog>
        )}
        {isRatingLessOpen && (
            <Dialog open={isRatingLessOpen} onClose={() => setIsRatingLessOpen(false)}>
                <DialogTitle>Search by Rating Less Than</DialogTitle>
                <DialogContent>
                    <TextField
                        label="Rating Threshold"
                        type="number"
                        value={ratingThreshold}
                        onChange={(e) => setRatingThreshold(e.target.value)}
                    />
                </DialogContent>
                <Button onClick={handleRatingLessConfirm}>Confirm</Button>
            </Dialog>
        )}
        {isHireOpen && (
            <Dialog open={isHireOpen} onClose={() => setIsHireOpen(false)}>
                <DialogTitle>Hire Worker to Organization</DialogTitle>
                <DialogContent>
                    <TextField
                        label="Worker ID"
                        type="number"
                        value={hireWorkerId}
                        onChange={(e) => setHireWorkerId(e.target.value)}
                    />
                    <TextField
                        label="Organization ID"
                        type="number"
                        value={hireOrgId}
                        onChange={(e) => setHireOrgId(e.target.value)}
                    />
                </DialogContent>
                <Button onClick={handleHireConfirm}>Confirm</Button>
            </Dialog>
        )}
        {isMoveOpen && (
            <Dialog open={isMoveOpen} onClose={() => setIsMoveOpen(false)}>
                <DialogTitle>Move Worker to New Organization</DialogTitle>
                <DialogContent>
                    <TextField
                        label="Worker ID"
                        type="number"
                        value={moveWorkerId}
                        onChange={(e) => setMoveWorkerId(e.target.value)}
                    />
                    <TextField
                        label="New Organization ID"
                        type="number"
                        value={moveNewOrgId}
                        onChange={(e) => setMoveNewOrgId(e.target.value)}
                    />
                </DialogContent>
                <Button onClick={handleMoveConfirm}>Confirm</Button>
            </Dialog>
        )}
        <Box sx={{ width: '100%', marginTop: 2 }}>
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

            {cacheStats && (
                <Box sx={{
                    padding: 1.5,
                    backgroundColor: '#e3f2fd',
                    borderRadius: 1,
                    marginBottom: 2,
                    fontSize: '0.9rem'
                }}>
                    <strong>Cache Stats:</strong> Hits: {cacheStats.hits}, Misses: {cacheStats.misses}, Logging: {cacheStats.loggingEnabled ? '✓ ON' : '✗ OFF'}
                </Box>
            )}

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

            <Tabs
                value={currentTab}
                onChange={(e, newValue) => setCurrentTab(newValue)}
                sx={{ borderBottom: 1, borderColor: 'divider' }}
            >
                <Tab label="Workers" />
                <Tab label="Cache & Tests" />
            </Tabs>
            <Box sx={{ display: currentTab === 0 ? 'block' : 'none', marginTop: 2 }}>
                <NestedTable data={data} />
            </Box>
            <Box sx={{ display: currentTab === 1 ? 'block' : 'none', marginTop: 2 }}>
                <CacheAndTestPanel />
            </Box>
        </Box>
    </div>)
}
export default MainPage;