# Akinator / Акинатор 🧞‍♂️🔢

**LLM Character & Classic Number Guessing Game**

[![Java 17]
[![Spring Boot]
[![LLM-Powered]

Welcome to **Akinator / Акинатор**! This project offers two engaging guessing games:

1.  **Угадай персонажа (Guess the Character):** A modern twist on the classic Akinator, powered by a Large Language Model (LLM, specifically Gemini) to guess characters you think of based on your answers to its questions. The gameplay and LLM interaction are designed to be in **Russian**.
2.  **Угадай число (Guess the Number):** A simple, classic game where the computer tries to guess a number you've thought of within a range (1-1000) using a binary search algorithm.

The project is structured as a web application with a Spring Boot backend providing the game logic and API, and a simple HTML/CSS/JavaScript frontend for the user interface.

---

## 🌟 Features

*   **Dual Game Modes:** Play either the LLM-driven Character game or the classic Number game.
*   **LLM-Powered Character Guessing:** Utilizes the Gemini API with a carefully crafted Russian prompt to guide the LLM's questioning and deduction process.
*   **Intelligent Questioning:** The LLM dynamically generates questions for the Character game based on conversation history and an algorithmic strategy aiming for efficiency.
*   **Classic Number Guessing:** A simple, efficient binary search implementation for the Number game.
*   **Spring Boot Backend:** Robust backend handling game state, API calls, and game logic.
*   **In-Memory Game Management:** Active games are managed in memory on the backend.
*   **Dynamic UI:** The frontend updates dynamically based on game progress received from the backend API.
*   **Russian Language Support:** The game interface and the LLM interaction prompt are primarily in Russian.

---

## 🖼️ Demo / Screenshot




---

## 🛠️ Getting Started

Follow these steps to get your local copy up and running.

### Prerequisites

*   Java Development Kit (JDK) 17 or higher
*   Maven
*   A Gemini API Key (or API key for the LLM configured in `application.properties`/`application.yml` or environment variables)

### Installation

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/Nurdaulet-no/Akinator-LLM.git
    ```
2.  **Navigate into the backend directory:**
    ```bash
    cd Akinator-LLM
    ```
3.  **Build the Spring Boot backend:**
    ```bash
    mvn clean package
    ```
    This will compile the Java code and create an executable JAR file in the `target` directory.

### Configuration

1.  **Obtain your LLM API Key:** Get your API key from your chosen provider (e.g., Google AI Studio for Gemini).
2.  **Configure API Key and URL:** Spring Boot can read configuration from various sources (environment variables, `application.properties`, `application.yml`). The code uses `@Value("${gemini.api.url}")` and `@Value("${gemini.api.key}")`.
    *   **Recommended (Environment Variables):** Set the API key and URL as environment variables before running the JAR:
        ```bash
        export GEMINI_API_URL='YOUR_GEMINI_API_URL' # e.g., https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=
        export GEMINI_API_KEY='YOUR_API_KEY_HERE'
        ```
        (Replace the URL example and `YOUR_API_KEY_HERE` with your actual values. Check Gemini API documentation for the exact endpoint URL.)
    *   **(Alternative - application.properties):** Create `src/main/resources/application.properties` if it doesn't exist and add:
        ```properties
        gemini.api.url=YOUR_GEMINI_API_URL
        gemini.api.key=YOUR_API_KEY_HERE
        ```
        (Use this method only if you understand the security implications of committing API keys, or if you plan to exclude this file from git or use Spring profiles.)

---

## ▶️ Usage

1.  **Run the Spring Boot backend:**
    Navigate to the project root directory (`Akinator-LLM`) in your terminal.
    ```bash
    java -jar target/akinator-0.0.1-SNAPSHOT.jar # Replace 0.0.1-SNAPSHOT with the actual version if different
    ```
    The backend should start, typically running on `http://localhost:8080`.


2.  **Play the Game!**
    *   Choose "Угадай число (Тест)" or "Угадай персонажа (LLM)" on the start screen.
    *   Follow the prompts on the screen. For the Number game, think of a number between 1 and 1000 and answer "Да" or "Нет". For the Character game, think of a character and answer the LLM's questions using the provided buttons ("Да", "Нет", "Я не знаю", etc.).
    *   In the Character game, you can click "Сдаться / Я загадал другое" if you want to end the current game prematurely.

---

## 🤔 How it Works

*   **Frontend (HTML/CSS/JS):** Provides the user interface. It captures user input (start game button clicks, answer button clicks) and sends requests to the backend API using `fetch`. It then receives game progress updates from the backend and dynamically updates the HTML to show the next question, game status, or final result. The canvas handles the background animation.
*   **Backend (Spring Boot):**
    *   Manages active game sessions using a `ConcurrentHashMap`.
    *   The `GameService` contains the core game logic for both modes.
    *   For the **Number Game**, it performs a binary search calculation internally based on "Да" or "Нет" answers to narrow the range.
    *   For the **Character Game**, it maintains the conversation history (`List<Message>`). On receiving an answer, it adds the user's response to the history and calls the `AkinatorLLMService`.
    *   The `AkinatorLLMService` formats the history and the detailed Russian prompt into a request for the Gemini API.
    *   It sends the request to the configured LLM endpoint and parses the LLM's response, expecting the specific `Вопрос: ...` or `Предположение: ...` format.
    *   Back in `GameService`, the parsed LLM response determines the next state: either asking a new question or attempting a guess.
    *   The `GameController` exposes REST endpoints for the frontend to interact with the `GameService`.
*   **LLM (Gemini):** Receives the conversation history and prompt. Uses its training data and the prompt's strategy guidelines to generate the next logical question or make a highly probable guess based on the provided constraints (the user's answers).

---

## 💻 Technologies Used

*   **Backend:**
    *   Java 17
    *   Spring Boot 3.x
    *   Maven
    *   Lombok
    *   RestTemplate
    *   Gemini API
*   **Frontend:**
    *   HTML5
    *   CSS3
    *   JavaScript (Vanilla)
    *   Fetch API

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
*(**Note:** If you don't have a LICENSE file, create one in the root of your repository and add the standard MIT license text.)*

---

## 🙏 Acknowledgements

*   Inspired by the original Akinator game.
*   Powered by the Gemini LLM API by Google.
*   Built with the help of the Spring Boot framework and the broader Java/JavaScript open-source communities.

---