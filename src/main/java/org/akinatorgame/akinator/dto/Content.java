package org.akinatorgame.akinator.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class Content {
    private String role;
    private List<Part> parts;

    public Content(String role, String text){
        this.role = role;
        this.parts = Collections.singletonList(new Part(text));
    }
}
