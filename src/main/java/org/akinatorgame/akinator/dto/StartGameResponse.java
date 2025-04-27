package org.akinatorgame.akinator.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class StartGameResponse {
    private String gameId;
    private String initQuestion;
    private String firstQuestion;
}
