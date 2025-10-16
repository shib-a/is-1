const NestedTable = (data) => {
    return (<table border="1" cellPadding="5" style={{marginTop: '10px'}}>
        <thead>
        <tr>
            {Object.keys(data[0] || {}).map((key) => (
                <th key={key}>{key}</th>
            ))}
        </tr>
        </thead>
        <tbody>
        {data.map((item, idx) => (
            <tr key={idx}>
                {Object.values(item).map((val, i) => (
                    <td key={i}>{val}</td>
                ))}
            </tr>
        ))}
        </tbody>
    </table>)
}
export default NestedTable;