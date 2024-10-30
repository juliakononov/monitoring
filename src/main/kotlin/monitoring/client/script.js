const socket = new WebSocket("ws://localhost:8080/server/updates?sessionGuid=$guid");
socket.onmessage = function(event) {
    const updatedMetrics = event.data.split(",");
    for (const metricName of updatedMetrics) {
        // Здесь обновляем только изменённые ячейки
        updateMetricCells(metricName);
    }
};

function updateMetricCells(metricName) {
    // Логика обновления ячеек таблицы по метрике
    const cells = document.querySelectorAll(`td[data-metric-name="${metricName}"]`);
    cells.forEach(cell => {
        // Обновляем значение ячейки через AJAX запрос
        fetch(`/server/send-functions/$guid`)
            .then(response => response.json())
            .then(data => {
                // Найти новую метрику и обновить ячейку
                const functionName = cell.getAttribute("data-function-name");
                const newMetricValue = data.find(f => f.funName === functionName)?.metrics[metricName] || "-";
                cell.innerText = newMetricValue;
            });
    });
}
