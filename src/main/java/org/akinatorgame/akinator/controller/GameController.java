package org.akinatorgame.akinator.controller;

import lombok.RequiredArgsConstructor;
import org.akinatorgame.akinator.dto.AnswerRequest;
import org.akinatorgame.akinator.dto.GameProgressResponse;
import org.akinatorgame.akinator.model.GameState;
import org.akinatorgame.akinator.model.GameType;
import org.akinatorgame.akinator.service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameController {
    private final GameService gameService;

    @PostMapping("/start")
    public ResponseEntity<GameProgressResponse> startGameResponseMapping(@RequestParam GameType type){
        GameState gameState = gameService.startGame(type);

        return ResponseEntity.ok(gameService.getGameProgress(gameState.getGameId()));
    }

    @PostMapping("/{gameId}/answer")
    public ResponseEntity<GameProgressResponse> answerRequestMapping(@PathVariable String gameId, @RequestBody AnswerRequest answerRequest){
        GameState gameState = gameService.handleAnswer(gameId, answerRequest.getAnswer());

        GameProgressResponse response = gameService.getGameProgress(gameState.getGameId());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameProgressResponse> getGameStatus(@PathVariable String gameId){
        return ResponseEntity.ok(gameService.getGameProgress(gameId));
    }

    @PostMapping("/{gameId}/lose")
    public ResponseEntity<GameProgressResponse> llmLoses(@PathVariable String gameId){
        GameState gameState = gameService.getGameState(gameId);
        if (gameState == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Игра с ID " + gameId + " не найдена.");
        }
        if (gameState.isGameOver()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Игра с ID " + gameId + " уже завершена.");
        }

        gameState.setGameOver(true);
        gameState.setFinalResult("Поражение. Я не смог угадать вашего персонажа.");
        gameState.setGuessAttempt(false);
        gameState.setPossibleGuess(null);
        gameState.setCurrentQuestion("Game Over.");

        System.out.println("Character game " + gameId + " over. Player declared lose.");

        return ResponseEntity.ok(gameService.getGameProgress(gameId));
    }
}
