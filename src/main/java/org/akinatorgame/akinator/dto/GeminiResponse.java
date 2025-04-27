package org.akinatorgame.akinator.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GeminiResponse {
    List<Candidate> candidates;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Candidate{
        private Content content;
    }
}
