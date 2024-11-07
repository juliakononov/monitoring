let sessions = [];

function filterSessions() {
    const query = document.getElementById('search').value.toLowerCase();
    const filteredSessions = sessions.filter(session => session.guid.toLowerCase().includes(query));
    const sessionListElement = document.getElementById('session-list');
    sessionListElement.innerHTML = '';
    filteredSessions.forEach(session => {
        const li = document.createElement('li');
        li.textContent = session.guid;
        li.setAttribute('data-guid', session.guid);
        li.onclick = () => window.location.href = '/sessions/' + session.guid;
        sessionListElement.appendChild(li);
    });
}

document.addEventListener("DOMContentLoaded", function() {
    const guid = window.guid;
    const socket = new WebSocket(`ws://localhost:8080/updates/${guid}`);

    socket.onmessage = function(event) {
        const data = JSON.parse(event.data);
        const functionName = data.funName;
        const metricName = data.metricName;
        const value = data.value;

        const table = document.getElementById("metrics-table");
        let metricIndex = -1;
        let functionRow = null;

        const headers = table.querySelectorAll("th");
        for (let i = 0; i < headers.length; i++) {
            if (headers[i].textContent === metricName) {
                metricIndex = i;
                break;
            }
        }

        if (metricIndex === -1) {
            const headerRow = table.querySelector("tr");
            const newTh = document.createElement("th");
            newTh.textContent = metricName;
            headerRow.appendChild(newTh);
            metricIndex = headers.length;

            const existingRows = table.querySelectorAll("tr");
            for (let i = 1; i < existingRows.length; i++) {
                const emptyCell = document.createElement("td");
                existingRows[i].appendChild(emptyCell);
            }
        }

        const rows = table.querySelectorAll("tr");
        for (let i = 1; i < rows.length; i++) {
            const functionCell = rows[i].querySelector("td");
            if (functionCell && functionCell.textContent === functionName) {
                functionRow = rows[i];
                break;
            }
        }

        if (!functionRow) {
            functionRow = document.createElement("tr");
            const functionCell = document.createElement("td");
            functionCell.textContent = functionName;
            functionRow.appendChild(functionCell);

            for (let i = 1; i <= headers.length - 1; i++) {
                const emptyCell = document.createElement("td");
                functionRow.appendChild(emptyCell);
            }
            table.appendChild(functionRow);
        }

        let cell = functionRow.querySelectorAll("td")[metricIndex];
        if (!cell) {
            for (let i = functionRow.children.length; i <= metricIndex; i++) {
                const emptyCell = document.createElement("td");
                functionRow.appendChild(emptyCell);
            }
            cell = functionRow.querySelectorAll("td")[metricIndex];
        }

        cell.textContent = value;
    };

    socket.onclose = function() {
        console.log("WebSocket connection closed");
    };
});