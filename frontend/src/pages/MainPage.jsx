import React, {useRef, useState} from "react";
import NestedTable from "../components/NestedTable";
import Address from "../classes/Address";
import Coordinates from "../classes/Coordinates";
import Worker from "../classes/Worker";
import Organization from "../classes/Organization";
import Person from "../classes/Person";

const MainPage = () => {
    const [data, setData] = useState(null);
    const [expandedRowId, setExpandedRowId] = useState(null);
    const [expandedField, setExpandedField] = useState(null);
    const [pageSize, setPageSize] = useState(10);
    const [currentPage, setCurrentPage] = useState(null);
    const [isAddActive, setIsAddActive] = useState(false);
    const [currentCreatedWorker, setCurrentCreatedWorker] = useState(new Worker({
        organization: new Organization({
            officialAddress: new Address({town: new Location()})
        }),
        coordinates: new Coordinates(),
        person: new Person({location: new Location()}),
    }));
    const [isCoordCreationActive, setIsCoordCreationActive] = useState(false);
    const [isOrgCreationActive, setIsOrgCreationActive] = useState(false);
    const [currCoords, setCurrCoords] = useState(new Coordinates());
    const [currOrg, setCurrOrg] = useState(new Organization());

    const handleAdd = () => {
        setIsAddActive(!isAddActive);
        console.log(currentCreatedWorker);
    }

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

    return (<html>
    <head></head>
    <body>
        <div className="mainContainer">
            <button className="addButton" onClick={() => handleAdd()}>Add</button>

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
                    <React.Fragment>
                        <tr>
                            <td><input type={"text"} onChange={(e) => {
                                setCurrentCreatedWorker(currentCreatedWorker.name = e.target.value)
                            }}/></td>
                            <td onClick={(e) => setIsCoordCreationActive(!isCoordCreationActive)}>[configure]</td>
                            <td onClick={(e) => setIsOrgCreationActive(!isOrgCreationActive)}>[configure]</td>
                        </tr>
                        {isCoordCreationActive ? (
                            <React.Fragment>
                            <th>x</th>
                            <th>y</th>
                            <tr>
                                <td><input type={"number"} placeholder={"0..."} onChange={(e) => currentCreatedWorker.coordinates.x = e.target.value}/></td>
                                <td><input type={"number"} placeholder={"0..."} onChange={(e) => currentCreatedWorker.coordinates.y = e.target.value}/></td>
                            </tr>
                        </React.Fragment>
                    ) : (null)}
                        {isOrgCreationActive ? (
                            <React.Fragment>
                                <th>Official Address</th>
                                <th>Annual Turnover</th>
                                <th>Employee Count</th>
                                <th>Full Name</th>
                                <th>Rating</th>
                                <tr>
                                    <td><input type={"number"} placeholder={"0..."}
                                               onChange={(e) => currentCreatedWorker.organization.annualTurnover = e.target.value}/>
                                    </td>
                                    <td><input type={"number"} placeholder={"0..."}
                                               onChange={(e) => currentCreatedWorker.organization.employeesCount = e.target.value}/>
                                    </td>
                                    <td><input type={"text"} placeholder={"OOO..."}
                                               onChange={(e) => currentCreatedWorker.organization.fullName = e.target.value}/>
                                    </td>
                                    <td><input type={"number"} placeholder={"0..."}
                                               onChange={(e) => currentCreatedWorker.organization.rating = e.target.value}/>
                                    </td>
                                </tr>
                            </React.Fragment>
                        ) : (null)}

                    </React.Fragment>

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
    </body>
    </html>)
}
export default MainPage;