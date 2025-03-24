document.addEventListener("DOMContentLoaded", function () {
    const objectiveContainer = document.getElementById("objective-container");
    const concessionContainer = document.getElementById("concession-container");
    const constraintContainer = document.getElementById("constraint-container");
    const addObjectiveButton = document.getElementById("add-objective");
    const addConstraintButton = document.getElementById("add-constraint");

    let objectiveCount = 2; // Начинаем с 2 функций цели
    let constraintCount = 0;

    // Функция для создания поля целевой функции
    function createObjectiveField(index) {
        const div = document.createElement("div");
        div.classList.add("objective-group");
        div.dataset.index = index;

        const input = document.createElement("input");
        input.type = "text";
        input.placeholder = `Функция цели ${index + 1}`;
        input.oninput = () => {
            formatInput(input, preview);
            collectDataAndSubmit(); // Обновляем результаты при изменении
        };

        const select = document.createElement("select");
        select.innerHTML = "<option value='max'>max</option><option value='min'>min</option>";
        select.onchange = () => collectDataAndSubmit(); // Обновляем результаты при изменении

        const preview = document.createElement("span");
        preview.classList.add("preview");

        div.appendChild(input);
        div.appendChild(select);
        div.appendChild(preview);

        if (index >= 2) {
            const removeButton = document.createElement("button");
            removeButton.textContent = "Удалить";
            removeButton.onclick = function () {
                objectiveContainer.removeChild(div);
                removeConcessionField(index - 1);
                objectiveCount--;
                collectDataAndSubmit(); // Обновляем результаты после удаления
            };
            div.appendChild(removeButton);
        }

        objectiveContainer.appendChild(div);

        if (index > 0) {
            createConcessionField(index - 1);
        }
    }

    // Функция для создания поля константы уступки
    function createConcessionField(index) {
        const div = document.createElement("div");
        div.classList.add("concession-group");
        div.dataset.index = index;

        const input = document.createElement("input");
        input.type = "text";
        input.placeholder = `Константа уступки ${index + 1}`;
        input.oninput = () => collectDataAndSubmit(); // Обновляем результаты при изменении

        div.appendChild(input);
        concessionContainer.appendChild(div);
    }

    // Функция для удаления поля константы уступки
    function removeConcessionField(index) {
        const fieldToRemove = concessionContainer.querySelector(`.concession-group[data-index='${index}']`);
        if (fieldToRemove) {
            concessionContainer.removeChild(fieldToRemove);
        }
    }

    // Функция для создания поля ограничения
    function createConstraintField() {
        if (!constraintContainer) {
            console.error("Ошибка: контейнер ограничений не найден!");
            return;
        }

        const div = document.createElement("div");
        div.classList.add("constraint-group");
        div.dataset.index = constraintCount;

        const input = document.createElement("input");
        input.type = "text";
        input.placeholder = `Ограничение ${constraintCount + 1}`;
        input.oninput = () => {
            formatInput(input, preview);
            collectDataAndSubmit(); // Обновляем результаты при изменении
        };

        const preview = document.createElement("span");
        preview.classList.add("preview");

        const removeButton = document.createElement("button");
        removeButton.textContent = "Удалить";
        removeButton.onclick = function () {
            constraintContainer.removeChild(div);
            constraintCount--;
            collectDataAndSubmit(); // Обновляем результаты после удаления
        };

        div.appendChild(input);
        div.appendChild(preview);
        div.appendChild(removeButton);
        constraintContainer.appendChild(div);

        constraintCount++;
    }

    // Функция для форматирования ввода
    function formatInput(input, output) {
        let formatted = input.value
            .replace(/x_(\d+)\^(\d+)/g, 'x<sub>$1</sub><sup>$2</sup>') 
            .replace(/x\^(\d+)/g, 'x<sup>$1</sup>') 
            .replace(/x_(\d+)/g, 'x<sub>$1</sub>') 
            .replace(/>=/g, '≥')
            .replace(/<=/g, '≤')
            .replace(/\*/g, ' ');

        output.innerHTML = formatted;
    }

    // Функция для сбора данных и отправки на сервер
    function collectDataAndSubmit() {
        const objectives = [];
        document.querySelectorAll(".objective-group").forEach(group => {
            const input = group.querySelector("input");
            const select = group.querySelector("select");
            objectives.push({
                function: input.value,
                optimization: select.value
            });
        });

        const concessions = [];
        document.querySelectorAll(".concession-group input").forEach(input => {
            concessions.push(input.value);
        });

        const constraints = [];
        document.querySelectorAll(".constraint-group input").forEach(input => {
            constraints.push(input.value);
        });

        const data = {
            objectives,
            concessions,
            constraints
        };

        fetch('http://localhost:8080/api/optimize', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        })
        .then(response => response.json())
        .then(data => {
            // Получаем элементы для вывода результатов
            const functionResultsElement = document.getElementById('function-results');
            const variableResultsElement = document.getElementById('variable-results');

            // Проверяем, что элементы существуют
            if (!functionResultsElement || !variableResultsElement) {
                console.error('Ошибка: элементы для вывода результатов не найдены!');
                return;
            }

            // Очищаем предыдущие результаты
            functionResultsElement.innerHTML = '<h3>Результаты функций:</h3>';
            variableResultsElement.innerHTML = '<h3>Значения переменных:</h3>';

            // Отображаем результаты функций
            data.functionResults.forEach((value, index) => {
                const p = document.createElement('p');
                p.innerHTML = `F<sub>${index + 1}</sub> = ${value}`;
                functionResultsElement.appendChild(p);
            });

            // Отображаем значения переменных
            data.coefficientsResults.forEach((value, index) => {
                const p = document.createElement('p');
                p.innerHTML = `x<sub>${index + 1}</sub> = ${value}`;
                variableResultsElement.appendChild(p);
            });
        })
        .catch(error => {
            console.error('Ошибка:', error);
        });
    }

    // Добавляем обработчики событий
    addObjectiveButton.addEventListener("click", function () {
        createObjectiveField(objectiveCount);
        objectiveCount++;
    });

    addConstraintButton.addEventListener("click", createConstraintField);

    // Инициализация начальных полей
    createObjectiveField(0);
    createObjectiveField(1);
});