// Backend base URL (update for deployment)
const backendBaseUrl = 'http://localhost:8080';

// Global state
let currentGameId = null;
let currentGameType = null;

// DOM elements
const startScreen = document.getElementById('start-screen');
const gameScreen = document.getElementById('game-screen');
const loading = document.getElementById('loading');
const error = document.getElementById('error');
const gameTypeDisplay = document.getElementById('game-type');
const attemptsDisplay = document.getElementById('attempts');
const gameIdDisplay = document.getElementById('game-id');
const displayMessage = document.getElementById('display-message');
const answerArea = document.getElementById('answer-area');
const giveUpButton = document.getElementById('give-up-button');
const finalResult = document.getElementById('final-result');
const finalResultText = document.getElementById('final-result-text');
const playAgainButton = document.getElementById('play-again-button');
const switchModeButton = document.getElementById('switch-mode-button');
const startNumberGameButton = document.getElementById('start-number-game');
const startCharacterGameButton = document.getElementById('start-character-game');
const canvas = document.getElementById('background-canvas');
const ctx = canvas.getContext('2d');

// Initialize event listeners
startNumberGameButton.addEventListener('click', () => startGame('NUMBER'));
startCharacterGameButton.addEventListener('click', () => startGame('CHARACTER'));
giveUpButton.addEventListener('click', giveUp);
playAgainButton.addEventListener('click', () => startGame(currentGameType));
switchModeButton.addEventListener('click', () => startGame(currentGameType === 'NUMBER' ? 'CHARACTER' : 'NUMBER'));

// Canvas setup for oil-on-water effect
function setupCanvas() {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
    window.addEventListener('resize', () => {
        canvas.width = window.innerWidth;
        canvas.height = window.innerHeight;
    });
}

// Oil-on-water animation
const particles = [];
const colors = ['#ff6b81', '#6b48ff', '#00ddeb', '#ffd700', '#2ecc71'];

class Particle {
    constructor() {
        this.x = Math.random() * canvas.width;
        this.y = Math.random() * canvas.height;
        this.vx = (Math.random() - 0.5) * 2;
        this.vy = (Math.random() - 0.5) * 2;
        this.size = Math.random() * 20 + 10;
        this.color = colors[Math.floor(Math.random() * colors.length)];
    }

    update() {
        this.x += this.vx;
        this.y += this.vy;

        if (this.x < 0 || this.x > canvas.width) this.vx *= -1;
        if (this.y < 0 || this.y > canvas.height) this.vy *= -1;

        this.size += (Math.random() - 0.5) * 0.2;
        if (this.size < 10) this.size = 10;
        if (this.size > 30) this.size = 30;
    }

    draw() {
        ctx.beginPath();
        ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2);
        ctx.fillStyle = this.color;
        ctx.globalAlpha = 0.4;
        ctx.fill();
        ctx.globalAlpha = 1;
    }
}

function initParticles() {
    for (let i = 0; i < 50; i++) {
        particles.push(new Particle());
    }
}

function animateBackground() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    particles.forEach(p => {
        p.update();
        p.draw();
        particles.forEach(p2 => {
            if (p !== p2) {
                const dx = p.x - p2.x;
                const dy = p.y - p2.y;
                const distance = Math.sqrt(dx * dx + dy * dy);
                if (distance < 100) {
                    ctx.beginPath();
                    ctx.moveTo(p.x, p.y);
                    ctx.lineTo(p2.x, p2.y);
                    ctx.strokeStyle = p.color;
                    ctx.globalAlpha = 0.2;
                    ctx.stroke();
                    ctx.globalAlpha = 1;
                }
            }
        });
    });
    requestAnimationFrame(animateBackground);
}

// Initialize canvas and animation
setupCanvas();
initParticles();
animateBackground();

// Timeout wrapper for API calls
const fetchWithTimeout = (url, options, timeout = 10000) => {
    return Promise.race([
        fetch(url, options),
        new Promise((_, reject) =>
            setTimeout(() => reject(new Error('Превышено время ожидания ответа сервера')), timeout)
        )
    ]);
};

// Start a new game
async function startGame(type) {
    console.log('Starting game:', type);
    showLoading();
    disableButtons();
    clearError();

    try {
        const response = await fetchWithTimeout(
            `${backendBaseUrl}/api/game/start?type=${type}`,
            {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            },
            10000
        );

        if (!response.ok) {
            throw new Error(`Ошибка при старте игры: ${response.statusText}`);
        }

        const gameProgress = await response.json();
        console.log('Game started:', gameProgress);
        currentGameId = gameProgress.gameId;
        currentGameType = gameProgress.gameType;
        updateGameUI(gameProgress);
        showGameScreen();
    } catch (err) {
        console.error('Start game error:', err.message);
        showError(err.message);
        showStartScreen();
    } finally {
        try {
            hideLoading();
            enableButtons();
            console.log('Loading hidden, buttons enabled');
        } catch (finallyErr) {
            console.error('Error in finally block:', finallyErr.message);
        }
    }
}

// Submit an answer
async function sendAnswer(answer) {
    if (!currentGameId) {
        console.warn('No current game ID');
        return;
    }

    console.log('Sending answer:', answer);
    showLoading();
    disableButtons();
    clearError();

    try {
        const response = await fetchWithTimeout(
            `${backendBaseUrl}/api/game/${currentGameId}/answer`,
            {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ answer })
            },
            10000
        );

        if (!response.ok) {
            throw new Error(`Ошибка при отправке ответа: ${response.statusText}`);
        }

        const gameProgress = await response.json();
        console.log('Answer response:', gameProgress);
        updateGameUI(gameProgress);
    } catch (err) {
        console.error('Send answer error:', err.message);
        showError(err.message);
    } finally {
        try {
            hideLoading();
            enableButtons();
            console.log('Loading hidden, buttons enabled');
        } catch (finallyErr) {
            console.error('Error in finally block:', finallyErr.message);
        }
    }
}

// Player gives up (CHARACTER game only)
async function giveUp() {
    if (!currentGameId || currentGameType !== 'CHARACTER') {
        console.warn('Cannot give up: No game ID or not CHARACTER game');
        return;
    }

    console.log('Giving up game:', currentGameId);
    showLoading();
    disableButtons();
    clearError();

    try {
        const response = await fetchWithTimeout(
            `${backendBaseUrl}/api/game/${currentGameId}/lose`,
            {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            },
            10000
        );

        if (!response.ok) {
            throw new Error(`Ошибка при сдаче: ${response.statusText}`);
        }

        const gameProgress = await response.json();
        console.log('Give up response:', gameProgress);
        updateGameUI(gameProgress);
    } catch (err) {
        console.error('Give up error:', err.message);
        showError(err.message);
    } finally {
        try {
            hideLoading();
            enableButtons();
            console.log('Loading hidden, buttons enabled');
        } catch (finallyErr) {
            console.error('Error in finally block:', finallyErr.message);
        }
    }
}

// Update the UI based on GameProgressResponse
function updateGameUI(gameProgress) {
    console.log('Updating UI with:', gameProgress);
    try {
        // Update status
        gameTypeDisplay.textContent = gameProgress.gameType === 'NUMBER' ? 'Число' : 'Персонаж';
        attemptsDisplay.textContent = gameProgress.attempts;
        gameIdDisplay.textContent = gameProgress.gameId;
        // Use nextQuestion instead of displayMessage
        displayMessage.textContent = gameProgress.nextQuestion || '';

        // Clear answer area
        answerArea.innerHTML = '';

        // Handle game over state
        if (gameProgress.gameOver) {
            answerArea.classList.add('hidden');
            giveUpButton.classList.add('hidden');
            finalResult.classList.remove('hidden');
            // Use nextQuestion for final result display
            finalResultText.textContent = gameProgress.nextQuestion || '';

            // Show play again and switch mode buttons
            playAgainButton.classList.remove('hidden');
            switchModeButton.classList.remove('hidden');
            switchModeButton.textContent = currentGameType === 'NUMBER' ? 'Играть в Угадай персонажа' : 'Играть в Угадай число';

            // Style final result based on content
            if (gameProgress.nextQuestion && gameProgress.nextQuestion.includes('found your number')) {
                finalResult.classList.add('win');
                finalResult.classList.remove('loss');
            } else if (gameProgress.finalResult && gameProgress.finalResult.includes('Поражение')) {
                finalResult.classList.add('loss');
                finalResult.classList.remove('win');
            }
            console.log('Game over, UI updated');
            return;
        }

        // Show answer area and give up button (if applicable)
        answerArea.classList.remove('hidden');
        giveUpButton.classList.toggle('hidden', currentGameType !== 'CHARACTER');
        playAgainButton.classList.add('hidden');
        switchModeButton.classList.add('hidden');

        // Create answer buttons based on game type and state
        if (gameProgress.gameType === 'NUMBER') {
            createAnswerButton('Да', () => sendAnswer('yes'));
            createAnswerButton('Нет', () => sendAnswer('no'));
        } else if (gameProgress.gameType === 'CHARACTER') {
            if (gameProgress.guessAttempt) {
                createAnswerButton('Да, угадал!', () => sendAnswer('yes'));
                createAnswerButton('Нет, не угадал', () => sendAnswer('no'));
            } else {
                const answers = ['Да', 'Нет', 'Я не знаю', 'Возможно, частично', 'Скорее нет'];
                answers.forEach(answer => createAnswerButton(answer, () => sendAnswer(answer)));
            }
        }
        console.log('UI updated successfully');
    } catch (err) {
        console.error('UI update error:', err.message);
        showError('Ошибка при обновлении интерфейса');
    }
}

// Helper to create an answer button
function createAnswerButton(text, onClick) {
    const button = document.createElement('button');
    button.textContent = text;
    button.addEventListener('click', onClick);
    answerArea.appendChild(button);
}

// UI control functions
function showStartScreen() {
    console.log('Showing start screen');
    startScreen.classList.remove('hidden');
    gameScreen.classList.add('hidden');
    finalResult.classList.add('hidden');
    playAgainButton.classList.add('hidden');
    switchModeButton.classList.add('hidden');
}

function showGameScreen() {
    console.log('Showing game screen');
    startScreen.classList.add('hidden');
    gameScreen.classList.remove('hidden');
}

function showLoading() {
    console.log('Showing loading');
    loading.classList.remove('hidden');
}

function hideLoading() {
    console.log('Hiding loading');
    loading.classList.add('hidden');
}

function showError(message) {
    console.log('Showing error:', message);
    error.textContent = message;
    error.classList.remove('hidden');
}

function clearError() {
    console.log('Clearing error');
    error.textContent = '';
    error.classList.add('hidden');
}

function disableButtons() {
    console.log('Disabling buttons');
    document.querySelectorAll('button').forEach(button => button.disabled = true);
}

function enableButtons() {
    console.log('Enabling buttons');
    document.querySelectorAll('button').forEach(button => button.disabled = false);
}