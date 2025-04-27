package org.akinatorgame.akinator.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GameProgressResponse {
    private String gameId;
    private String gameType;
    private boolean isGameOver;
    private String nextQuestion;
    private Integer guessedNum;
    private int attempts;
    private String possibleGuess;
    private boolean isGuessAttempt;
    private String finalResult;
}
