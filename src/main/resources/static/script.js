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

        const cell = document.querySelector(`td[data-function-name='${functionName}'][data-metric-name='${metricName}']`);

        if (cell) {
            cell.textContent = value;
        }
    };

    socket.onclose = function() {
        console.log("WebSocket connection closed");
    };
});