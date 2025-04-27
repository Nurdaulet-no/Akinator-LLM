package org.akinatorgame.akinator.service;

import org.akinatorgame.akinator.dto.GameProgressResponse;
import org.akinatorgame.akinator.dto.Message;
import org.akinatorgame.akinator.model.GameState;
import org.akinatorgame.akinator.model.GameType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {

    private final Map<String, GameState> activeGames = new ConcurrentHashMap<>();

    private static final int MIN_NUMBER = 1;
    private static final int MAX_NUMBER = 1000;

    private final AkinatorLLMService akinatorLLMService;

    public GameService(AkinatorLLMService akinatorLLMService){
        this.akinatorLLMService = akinatorLLMService;
    }

    /*
    Start new Game
    **/
    public GameState startGame(GameType gameType){
        String gameId = UUID.randomUUID().toString();

        GameState gameState;
        if(gameType == GameType.NUMBER){
            gameState = new GameState(gameId, MIN_NUMBER, MAX_NUMBER);
            gameState.setCurrentQuestion(generateNumberQuestions(gameState));
            System.out.println("New guess number game started with ID: " + gameId);
        }else if(gameType == GameType.CHARACTER){
            List<Message> initPrompt = akinatorLLMService.createInitPrompt();
            String initLLMResponse = akinatorLLMService.getLLMResponse(initPrompt);

            List<Message> initHistory = new ArrayList<>(initPrompt);
            initHistory.add(new Message("model", initLLMResponse));

            gameState = new GameState(gameId, initHistory, initLLMResponse);
            System.out.println("New guess character game started with ID: " + gameId);
        }else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported game type - " + gameType);
        }

        activeGames.put(gameId, gameState);
        return gameState;
    }

    public GameState getGameState(String gameId){
        return activeGames.get(gameId);
    }

    public GameState handleAnswer(String gameId, String answer){
        GameState gameState = activeGames.get(gameId);

        if(gameState == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game with id " + gameId + " not found");
        }

        if(gameState.isGameOver()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This game is already over. ID:"+gameId);
        }

        gameState.incrementAttempts();

        if (gameState.getGameType() == GameType.NUMBER) {
            handleGameNumberAnswer(gameState, answer);
        }else if(gameState.getGameType() == GameType.CHARACTER){
            handleGameCharacterAnswer(gameState, answer);
        }

        return gameState;
    }

    public GameProgressResponse getGameProgress(String gameId){
        GameState gameState = activeGames.get(gameId);

        if(gameState == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game with ID: " + gameId + " not found");
        }

        String displayMessage = "";
        Integer finalGuessedNum = null;
        String finalResult = null;
        String possibleGuess = null;
        boolean isGuessAttempt = false;

        if(gameState.isGameOver()){
            if(gameState.getGameType() == GameType.NUMBER){
                finalGuessedNum = gameState.getGuessedNumber();
                displayMessage = "Game is over. I found your number: " +finalGuessedNum + " in " + gameState.getAttempts() + " attempts";
            }else if(gameState.getGameType() == GameType.CHARACTER){
                finalResult = gameState.getFinalResult();
                displayMessage = finalResult;
                if(gameState.getPossibleGuess() != null){
                    displayMessage += " Character: " + gameState.getPossibleGuess();
                }
                displayMessage += " (" + gameState.getAttempts() + " questions)";
            }
        }else{
            if(gameState.getGameType() == GameType.NUMBER){
                displayMessage = generateNumberQuestions(gameState);
                gameState.setCurrentQuestion(displayMessage);
            }else if(gameState.getGameType() == GameType.CHARACTER){
                if(gameState.isGuessAttempt()){
                    possibleGuess = gameState.getPossibleGuess();
                    isGuessAttempt = true;
                    displayMessage = "I think you guessed: " +possibleGuess + ". Did I guess right?";
                }else{
                    displayMessage = gameState.getCurrentQuestion();
                }
            }
        }

        return new GameProgressResponse(
                gameState.getGameId(),
                gameState.getGameType().name(),
                gameState.isGameOver(),
                displayMessage,
                finalGuessedNum,
                gameState.getAttempts(),
                possibleGuess,
                isGuessAttempt,
                finalResult
        );
    }

    private void handleGameNumberAnswer(GameState gameState, String answer){
        int min = gameState.getMin();
        int max = gameState.getMax();
        int currentGuess = (min + max) / 2;

        if("yes".equalsIgnoreCase(answer)){
            gameState.setMin(currentGuess+1);
        }else if("no".equalsIgnoreCase(answer)){
            gameState.setMax(currentGuess);
        }else{
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Incorrect answer: " + answer + ". Only \"Yes\" or \"No\" ");
        }

        if(gameState.getMin() >= gameState.getMax()){
            gameState.setGameOver(true);
            gameState.setGuessedNumber(gameState.getMin());
            System.out.println("Game over! Your guessed number: " + gameState.getGuessedNumber());
        }else{
            gameState.setCurrentQuestion(generateNumberQuestions(gameState));
            System.out.println("Game " + gameState.getGameId() + " progress: Range [" + gameState.getMin() + ", " + gameState.getMax() + "]");
        }
    }

    private String generateNumberQuestions(GameState gameState){
        if(gameState.getMin() == null || gameState.getMax() == null){
            return "Game error";
        }
        if(gameState.getMin() == gameState.getMax()){
            return "Your number: " + gameState.getMax() + ".";
        }

        int currentGuess = (gameState.getMin() + gameState.getMax()) / 2;
        return "Is your number more than " + currentGuess + " ?";
    }

    private void handleGameCharacterAnswer(GameState gameState, String answer){
        String normalAnswer = mapUserAnswerToLLMFormat(answer);

        if(gameState.isGuessAttempt()){
            if("yes".equalsIgnoreCase(answer)){
                gameState.setGameOver(true);
                gameState.setFinalResult("I won! I guessed your character!");
                System.out.println("Character game " + gameState.getGameId() + " over. Akinator guessed your character!");
            }else if("no".equalsIgnoreCase(answer)){
                gameState.setGuessAttempt(false);
                gameState.setPossibleGuess(null);
                gameState.addMessageToHistory(new Message("user", "Вы не угадали. Мой ответ: " + normalAnswer + ". Задайте следующий вопрос."));
                requestLLMNextTurn(gameState);
            }else{
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Некорректный ответ. Ожидается 'yes' или 'no' после предположения.");
            }
        }else{
            gameState.addMessageToHistory(new Message("user", "Мой ответ: " + answer));
            requestLLMNextTurn(gameState);
        }
    }

    private void requestLLMNextTurn(GameState gameState){
        if(gameState.getHistory().size() > 50){
            System.err.println("Warning: Conversation history for game " + gameState.getGameId() + " is getting long (" + gameState.getHistory().size() + "). Context issues may arise.");
        }

        String llmResponseText = akinatorLLMService.getLLMResponse(gameState.getHistory());
        gameState.addMessageToHistory(new Message("model", llmResponseText));

        if(llmResponseText != null && llmResponseText.startsWith("Предположение:")){
            String guess = llmResponseText.substring("Предположение:".length()).trim();
            gameState.setPossibleGuess(guess);
            gameState.setGuessAttempt(true); // Устанавливаем флаг
            gameState.setCurrentQuestion("Я думаю, вы загадали: " + guess + ". Я угадал?");
            System.out.println("Character game " + gameState.getGameId() + ": Akinator made a guess: " + guess);
        }else{
            String question = llmResponseText != null ? llmResponseText.replaceFirst("^Вопрос:\\s*", "").trim() : "Произошла ошибка при получении вопроса от LLM.";
            gameState.setCurrentQuestion(question);
            gameState.setGuessAttempt(false);
            gameState.setPossibleGuess(null);
            System.out.println("Character game " + gameState.getGameId() + ": Akinator asked: " + question);
        }
    }

    private String mapUserAnswerToLLMFormat(String answer){
        if(answer == null) return "No answer";
        String lowerAnswer = answer.trim().toLowerCase();
        switch (lowerAnswer) {
            case "да": return "Да";
            case "нет": return "Нет";
            case "я не знаю": return "Я не знаю";
            case "возможно частично": return "Возможно, частично";
            case "скорее нет": return "Скорее нет";
            case "yes": return "Да";
            case "no": return "Нет";
            default:
                System.err.println("Нераспознанный ответ пользователя: " + answer);
                return answer;
        }
    }

    public void cleanGame(String gameId){
        activeGames.remove(gameId);
        System.out.println("Cleaned up game with ID: " + gameId);
    }
}
