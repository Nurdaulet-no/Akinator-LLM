package org.akinatorgame.akinator.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.akinatorgame.akinator.dto.Message;

import java.util.ArrayList;
import java.util.List;




@Getter
@Setter
@NoArgsConstructor
public class GameState {
    private String gameId;
    private GameType gameType;
    private boolean isGameOver;
    private int attempts;
    private String currentQuestion;

    // Guess the numbers
    private Integer min;
    private Integer max;
    private Integer guessedNumber;

    private List<Message> history;
    private String possibleGuess;
    private boolean isGuessAttempt;

    private String finalResult;

    // Guess number
    public GameState(String gameId, int initMin, int initMax){
        this.gameId = gameId;
        this.gameType = GameType.NUMBER; // default game is Guess Number
        this.min = initMin;
        this.max = initMax;
        this.attempts = 0;
        this.isGameOver = false;
        this.guessedNumber = null;
        this.currentQuestion = null;

        this.history = null;
        this.possibleGuess = null;
        this.isGuessAttempt = false;
        this.finalResult = null;
    }

    public GameState(String gameId, List<Message> initHistory, String initQuestion){
        this.gameId = gameId;
        this.gameType = GameType.CHARACTER;
        this.history = new ArrayList<>(initHistory);
        this.currentQuestion = initQuestion;
        this.attempts = 0;
        this.isGameOver = false;

        this.min = null;
        this.max = null;
        this.guessedNumber = null;

        this.possibleGuess = null;
        this.isGuessAttempt = false;
        this.finalResult = null;
    }

    public void incrementAttempts(){
        this.attempts++;
    }
    public void addMessageToHistory(Message message){
        if(this.history != null){
            this.history.add(message);
        }
    }
}
