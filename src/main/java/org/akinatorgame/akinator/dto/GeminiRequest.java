package org.akinatorgame.akinator.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Getter
@Setter
@NoArgsConstructor
public class GeminiRequest {
    private List<Content> contents;

    public GeminiRequest(List<Message> history){
        this.contents = history.stream()
                .map(msg -> new Content(msg.getRole(), msg.getText()))
                .collect(Collectors.toList());
    }
}
