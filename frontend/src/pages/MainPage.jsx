import React, {useRef, useState} from "react";
import NestedTable from "../components/NestedTable";
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
import { Dialog, DialogTitle, DialogContent, Button, Table, TableHead, TableRow, TableCell, TableBody } from "@mui/material";

const MainPage = () => {

    const [workers, setWorkers] = useState([]);
    const [page, setPage] = useState(0);
    const [loading, setLoading] = useState(false);

    const [data, setData] = useState(null);
    const [expandedRowId, setExpandedRowId] = useState(null);
    const [open, setOpen] = useState(false);
    const [expandedField, setExpandedField] = useState(null);
    const [pageSize, setPageSize] = useState(10);
    const [currentPage, setCurrentPage] = useState(null);
    const [isAddActive, setIsAddActive] = useState(false);
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

    const handleAdd = () => {
        setIsAddActive(!isAddActive);
        console.log(currentCreatedWorker);
    }

    const handleAddConfirm = async () => {
        currentCreatedWorker.startDate = new Date(startDate);
        currentCreatedWorker.endDate = new Date(endDate);
        await axios.post(
            "http://localhost:8081/is-1-1.0-SNAPSHOT/workers/add",
            currentCreatedWorker
        )
            .then(response =>{
                console.log(response);
            })
            .catch(err =>{

            })
    }

    const fetchWorkers = async () => {
        setLoading(true);
            await axios.post(`http://localhost:8081/is-1-1.0-SNAPSHOT/workers?page=${page}&size=${pageSize}`)
            .then(response =>{

            })
                .catch(err => {

                })
            const data = await response.json();
            setWorkers(data);
            setLoading(false);
    };

    // Fetch data when component mounts or page changes
    useEffect(() => {
        fetchWorkers();
    }, [page]);

    // Periodically refresh every 10 seconds
    useEffect(() => {
        const intervalId = setInterval(() => {
            fetchWorkers();
        }, 10000); // 10 seconds
        return () => clearInterval(intervalId);
    }, [page]);

    const handleExpand = (rowId, fieldName) => {
        if (expandedRowId === rowId && expandedField === fieldName) {
            setExpandedRowId(null);
            setExpandedField(null);
        } else {
            setExpandedRowId(rowId);
            setExpandedField(fieldName);
        }
    };

    const setAndGet = (object, field, set) => {
        object[field] = set;
        return object;
    }

    return (
    <div>
        <div className="mainContainer">
            <button className="addButton" onClick={handleAdd}>Add</button>

            <table className="mainTable">
                <thead>
                {isAddActive ? (null) :(
                <th>ID</th>)}
                <th>Name</th>
                <th>Coordinates</th>
                {isAddActive ? (null) : (
                    <th>Creation Date</th>)}
                <th>Organization</th>
                <th>Salary</th>
                <th>Rating</th>
                <th>Start Date</th>
                <th>End Date</th>
                <th>Position</th>
                <th>Person</th>
                </thead>
                {isAddActive ? (
                    <tbody>
                        <tr>
                            <td><input type={"text"} value={currentCreatedWorker.name} onChange={(e) => {
                                setCurrentCreatedWorker(prev => new Worker({
                                    ...prev,
                                    name: e.target.value
                                }));
                            }}/></td>
                            <td onClick={(e) => setIsCoordCreationActive(true)}>[configure]</td>
                            <td onClick={(e) => setIsOrgCreationActive(!isOrgCreationActive)}>[configure]</td>
                            <td><input type={"number"} value={currentCreatedWorker.salary} placeholder={"0..."} onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                ...prev,
                                salary: e.target.value
                            }))}/></td>
                            <td><input type={"number"} value={currentCreatedWorker.rating} placeholder={"0..."} onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                ...prev,
                                rating: e.target.value
                            }))}/></td>
                            <td><input type={"date"} value={startDate} placeholder={"0.."} onChange={(e) => setStartDate(e.target.value)}/></td>
                            <td><input type={"date"} value={endDate} placeholder={"0..."} onChange={(e) => setEndDate(e.target.value)}/></td>
                            <td><select name="positions" value={currentCreatedWorker.position}  className="positionSelect" onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                ...prev,
                                position: e.target.value
                            }))}>
                                <option value="">--Please choose an option--</option>
                                <option value={Position.LABORER}>Laborer</option>
                                <option value={Position.HEAD_OF_DEPARTMENT}>Head of Dept.</option>
                                <option value={Position.HUMAN_RESOURCES}>HR</option>
                            </select></td>
                            <td onClick={(e) => setIsPersonCreationActive(!isPersonCreationActive)}>[configure]</td>
                        </tr>
                        {isCoordCreationActive ? (
                            <Dialog open={isCoordCreationActive} onClose={() => setIsCoordCreationActive(false)} fullWidth maxWidth="sm">
                                <DialogTitle>Worker Coordinates</DialogTitle>
                                <DialogContent>
                            <tbody>
                            <th>x</th>
                            <th>y</th>
                            <tr>
                                <td><input type={"number"} value={currentCreatedWorker.coordinates.x} placeholder={"0..."} onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                    ...prev,
                                    coordinates: new Coordinates({...prev.coordinates, x: Number(e.target.value)})
                                }))}/></td>
                                <td><input type={"number"} value={currentCreatedWorker.coordinates.y} placeholder={"0..."} onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                    ...prev,
                                    coordinates: new Coordinates({...prev.coordinates, y: Number(e.target.value)})
                                }))}/></td>

                            </tr>
                        </tbody>
                                </DialogContent>
                            </Dialog>
                    ) : (null)}


                        {isOrgCreationActive ? (
                            <Dialog open={isOrgCreationActive} onClose={() => setIsOrgCreationActive(false)} fullWidth maxWidth="sm">
                                <DialogTitle>Organization</DialogTitle>
                                <DialogContent>
                            <tbody>
                                <th>Official Address</th>
                                <th>Annual Turnover</th>
                                <th>Employee Count</th>
                                <th>Full Name</th>
                                <th>Rating</th>
                                <tr>
                                    <td onClick={(e) => setIsOrgAddressCreationActive(!isOrgAddressCreationActive)}>[configure]</td>
                                    {isOrgAddressCreationActive? (
                                        <Dialog open={isOrgAddressCreationActive} onClose={() => setIsOrgAddressCreationActive(false)} fullWidth maxWidth="sm">
                                            <DialogTitle>Organization Address</DialogTitle>
                                            <DialogContent>
                                        <tbody>
                                            <th>Street</th>
                                            <th>Town</th>
                                            <tr>
                                                <td><input type={"text"} value={currentCreatedWorker.organization.officialAddress.street} placeholder={"0..."} onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                                    ...prev,
                                                    organization: new Organization({...prev.organization, officialAddress: new Address({...prev.organization.officialAddress, street:e.target.value})})
                                                }))}/></td>
                                                <td onClick={(e) => setIsOrgAddressLocationCreationActive((!isOrgAddressLocationCreationActive))}>[configure]</td>
                                            </tr>
                                            {isOrgAddressLocationCreationActive ? (
                                                <Dialog open={isOrgAddressLocationCreationActive} onClose={() => setIsOrgAddressLocationCreationActive(false)} fullWidth maxWidth="sm">
                                                    <DialogTitle>Organization Town</DialogTitle>
                                                    <DialogContent>
                                                <tbody>
                                                    <th>x</th>
                                                    <th>y</th>
                                                    <th>z</th>
                                                    <th>Name</th>
                                                    <tr>
                                                        <td><input type={"number"} value={currentCreatedWorker.organization.officialAddress.town.x} placeholder={"0..."} onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                                            ...prev,
                                                            organization: new Organization({...prev.organization, officialAddress: new Address({...prev.organization.officialAddress, town:new Location({
                                                                        ...prev.organization.officialAddress.town,
                                                                        x: e.target.value
                                                                    })})})
                                                        }))}/></td>
                                                        <td><input type={"number"} value={currentCreatedWorker.organization.officialAddress.town.y} placeholder={"0..."} onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                                            ...prev,
                                                            organization: new Organization({...prev.organization, officialAddress: new Address({...prev.organization.officialAddress, town:new Location({
                                                                        ...prev.organization.officialAddress.town,
                                                                        y: e.target.value
                                                                    })})})
                                                        }))}/></td>
                                                        <td><input type={"number"} value={currentCreatedWorker.organization.officialAddress.town.z} placeholder={"0..."} onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                                            ...prev,
                                                            organization: new Organization({...prev.organization, officialAddress: new Address({...prev.organization.officialAddress, town:new Location({
                                                                        ...prev.organization.officialAddress.town,
                                                                        z: e.target.value
                                                                    })})})
                                                        }))}/></td>
                                                        <td><input type={"text"} value={currentCreatedWorker.organization.officialAddress.town.name} placeholder={"0..."} onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                                            ...prev,
                                                            organization: new Organization({...prev.organization, officialAddress: new Address({...prev.organization.officialAddress, town:new Location({
                                                                        ...prev.organization.officialAddress.town,
                                                                        name: e.target.value
                                                                    })})})
                                                        }))}/></td>
                                                    </tr>
                                                </tbody>
                                                    </DialogContent>
                                                </Dialog>

                                            ) : (null)}
                                        </tbody>
                                            </DialogContent>
                                        </Dialog>
                                    ) : (null)}
                                    <td><input type={"number"} value={currentCreatedWorker.organization.annualTurnover} placeholder={"0..."}
                                               onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                                   ...prev,
                                                   organization: new Organization({...prev.organization, annualTurnover: e.target.value})}))}/>
                                    </td>
                                    <td><input type={"number"} value={currentCreatedWorker.organization.employeesCount} placeholder={"0..."}
                                               onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                                   ...prev,
                                                   organization: new Organization({...prev.organization, employeesCount: e.target.value})}))}/>
                                    </td>
                                    <td><input type={"text"} value={currentCreatedWorker.organization.fullName} placeholder={"OOO..."}
                                               onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                                   ...prev,
                                                   organization: new Organization({...prev.organization, fullName: e.target.value})}))}/>
                                    </td>
                                    <td><input type={"number"} value={currentCreatedWorker.organization.rating} placeholder={"0..."}
                                               onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                                   ...prev,
                                                   organization: new Organization({...prev.organization, rating: e.target.value})}))}/>
                                    </td>
                                </tr>
                            </tbody>
                                </DialogContent>
                            </Dialog>
                        ) : (null)}


                        {isPersonCreationActive ? (
                            <Dialog open={isPersonCreationActive} onClose={() => setIsPersonCreationActive(false)} fullWidth maxWidth="sm">
                                <DialogTitle>Person</DialogTitle>
                                <DialogContent>
                            <tbody>
                                <th>Eye Color</th>
                                <th>Hair Color</th>
                                <th>Location</th>
                                <th>Height</th>
                                <th>Passport ID</th>
                                <th>Nationality</th>
                                <tr>
                                    <td><select name="color" value={currentCreatedWorker.person.eyeColor}  className="colorSelect" onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                        ...prev,
                                        person: new Person({...prev.person, eyeColor: e.target.value})}))}>
                                        <option value="">--Please choose an option--</option>
                                        <option value={Color.RED}>Red</option>
                                        <option value={Color.BLACK}>Black</option>
                                        <option value={Color.WHITE}>White</option>
                                        <option value={Color.ORANGE}>Orange</option>
                                        <option value={Color.YELLOW}>Yellow</option>
                                    </select></td>
                                    <td><select name="color" value={currentCreatedWorker.person.hairColor} className="colorSelect" onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                        ...prev,
                                        person: new Person({...prev.person, hairColor: e.target.value})}))}>
                                        <option value="">--Please choose an option--</option>
                                        <option value={Color.RED}>Red</option>
                                        <option value={Color.BLACK}>Black</option>
                                        <option value={Color.WHITE}>White</option>
                                        <option value={Color.ORANGE}>Orange</option>
                                        <option value={Color.YELLOW}>Yellow</option>
                                    </select></td>
                                    <td onClick={(e) => setIsPersonLocationCreationActive(!isPersonLocationCreationActive)}>[configure]</td>

                                    <td><input type={"number"} value={currentCreatedWorker.person.height} placeholder={"OOO..."}
                                               onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                                   ...prev,
                                                   person: new Person({...prev.person, height: e.target.value})}))}/>
                                    </td>
                                    <td><input type={"text"} value={currentCreatedWorker.person.passportID} placeholder={"0..."}
                                               onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                                   ...prev,
                                                   person: new Person({...prev.person, passportID: e.target.value})}))}/>
                                    </td>
                                    <td><select name="color" value={currentCreatedWorker.person.nationality} className="colorSelect" onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                        ...prev,
                                        person: new Person({...prev.person, nationality: e.target.value})}))}>
                                        <option value="">--Please choose an option--</option>
                                        <option value={Country.RUSSIA}>Russia</option>
                                        <option value={Country.UNITED_KINGDOM}>UK</option>
                                        <option value={Country.FRANCE}>France</option>
                                        <option value={Country.INDIA}>India</option>
                                        <option value={Country.THAILAND}>Thailand</option>
                                    </select></td>
                                    {isPersonLocationCreationActive ? (
                                        <Dialog open={isPersonLocationCreationActive} onClose={() => setIsPersonLocationCreationActive(false)} fullWidth maxWidth="sm">
                                            <DialogTitle>Person Location</DialogTitle>
                                            <DialogContent>
                                    <tbody>
                                        <th>x</th>
                                        <th>y</th>
                                        <th>z</th>
                                        <th>Name</th>
                                        <tr>
                                            <td><input type={"number"} value={currentCreatedWorker.person.location.x} placeholder={"0..."} onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                                ...prev,
                                                person: new Person({...prev.person, location: new Location({...prev.person.location, x: e.target.value
                                                        })})
                                            }))}/></td>
                                            <td><input type={"number"} value={currentCreatedWorker.person.location.y} placeholder={"0..."} onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                                ...prev,
                                                person: new Person({...prev.person, location: new Location({...prev.person.location, y: e.target.value
                                                    })})
                                            }))}/></td>
                                            <td><input type={"number"} value={currentCreatedWorker.person.location.z} placeholder={"0..."} onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                                ...prev,
                                                person: new Person({...prev.person, location: new Location({...prev.person.location, z: e.target.value
                                                    })})
                                            }))}/></td>
                                            <td><input type={"text"} value={currentCreatedWorker.person.location.name} placeholder={"0..."} onChange={(e) => setCurrentCreatedWorker(prev => new Worker({
                                                ...prev,
                                                person: new Person({...prev.person, location: new Location({...prev.person.location, name: e.target.value
                                                    })})
                                            }))}/></td>
                                        </tr>
                                    </tbody>
                                        </DialogContent>
                                        </Dialog>
                                ) : (null)}
                                </tr>
                            </tbody>
                                </DialogContent>
                            </Dialog>
                        ) : (null)}
                        <button type={"button"} onClick={handleAddConfirm}>Confirm</button>
                    </tbody>

                ) : (null)}

                {data != null ? (
                    <tbody>
                    {data.map((row) => (
                        <React.Fragment key={row.id}>
                        <tr>
                            <td>{row.id}</td>
                            <td>{row.name}</td>
                            <td>{row.organization}</td>
                            <td
                                style={{cursor: 'pointer', color: 'blue'}}
                                onClick={() => handleExpand(row.id, 'address')}
                            >
                                [Click to view]
                            </td>
                            <td
                                style={{cursor: 'pointer', color: 'blue'}}
                                onClick={() => handleExpand(row.id, 'orders')}
                            >
                                [Click to view]
                            </td>
                        </tr>
                        {expandedRowId === row.id && expandedField && (
                            <tr>
                                <td colSpan="5">
                                    <NestedTable data={row[expandedField]} field={expandedField} />
                                </td>
                            </tr>
                        )}
                    </React.Fragment>
                ))}
                </tbody>

                    ):(<div/>)}
            </table>
        </div>
    </div>)
}
export default MainPage;