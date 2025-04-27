package org.akinatorgame.akinator.service;

import lombok.RequiredArgsConstructor;
import org.akinatorgame.akinator.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class AkinatorLLMService {
    @Value("${gemini.api.url}")
    private String GEMINI_API_URL;

    @Value("${gemini.api.key}")
    private String API_KEY;

    private final RestTemplate restTemplate;

    public String getLLMResponse(List<Message> history){
        String url = GEMINI_API_URL+API_KEY;

        List<Message> historyForLLM = new ArrayList<>(history); // Копируем историю
        historyForLLM.add(new Message("user", "Напомни формат ответа: 'Вопрос: ...' или 'Предположение: ...'."));
        GeminiRequest requestBody = new GeminiRequest(historyForLLM);

        System.out.println("Sending request to Gemini with history size " + historyForLLM.size());

        try {
            ResponseEntity<GeminiResponse> geminiResponseResponseEntity = restTemplate.postForEntity(
                    url,
                    requestBody,
                    GeminiResponse.class
            );

            if(geminiResponseResponseEntity.getStatusCode().is2xxSuccessful())
            {
                GeminiResponse response = geminiResponseResponseEntity.getBody();

                String llmTextResponse = Optional.of(response)
                        .map(GeminiResponse::getCandidates)
                        .filter(candidates -> !candidates.isEmpty())
                        .map(candidates -> candidates.get(0))
                        .map(GeminiResponse.Candidate::getContent)
                        .map(Content::getParts)
                        .filter(parts -> !parts.isEmpty())
                        .map(parts -> parts.get(0))
                        .map(Part::getText)
                        .orElseThrow(() -> {
                            System.err.println("Unexpected response structure from Gemini API: " + response);
                            return new RuntimeException("Unexpected format response from Gemini.");
                        });

                System.out.println("Received response from Gemini: " + llmTextResponse);
                return llmTextResponse;
            }else {
                String errorBody = "N/A";
                if (geminiResponseResponseEntity.hasBody()) {
                    geminiResponseResponseEntity.getBody();
                    errorBody = geminiResponseResponseEntity.getBody().toString();
                }
                System.err.println("Error response from Gemini API: Status=" + geminiResponseResponseEntity.getStatusCode() + ", Body=" + errorBody);
                throw new ResponseStatusException(geminiResponseResponseEntity.getStatusCode(), "Ошибка при обращении к LLM");
            }
        }catch (Exception e){
            System.err.println("Error while calling gemini Api " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error in Gemini");
        }

    }

    public List<Message> createInitPrompt(){
        Message message = new Message("user", "Ты играешь в Акинатор, функционируя как предельно эффективный алгоритм угадывания персонажей, нацеленный на минимизацию количества вопросов. Я загадал персонажа.\n" +
                "Твоя **единственная задача** — угадать моего персонажа, задавая вопросы, на которые можно ответить ТОЛЬКО одной из следующих фраз:\n" +
                "- Да\n" +
                "- Нет\n" +
                "- Я не знаю\n" +
                "- Возможно, частично\n" +
                "- Скорее нет\n" +
                "**ЖДИ МОЕГО ОТВЕТА** после каждого заданного вопроса.\n" +
                "**КРАЙНЕ ВАЖНАЯ АЛГОРИТМИЧЕСКАЯ СТРАТЕГИЯ ДЛЯ МАКСИМАЛЬНОЙ ЭФФЕКТИВНОСТИ:**\n" +
                "1.  **НАЧАЛО:** Задавай самые общие вопросы, которые помогут определить базовые свойства и источник персонажа, и стремятся максимально разделить ВСЕХ возможных персонажей:\n" +
                "    *   Вымышленный / Реальный?\n" +
                "    *   Мужчина / Женщина / Другое?\n" +
                "    *   Тип медиа (Аниме/Манга, Фильм, Сериал, Видеоигра, Книга, Комикс, Спорт, Политика и т.п.). **Начинай с наиболее популярных и массовых типов.**\n" +
                "    *   Примерный возраст или статус (ребенок, подросток, взрослый).\n" +
                "    *   Главный герой или второстепенный.\n" +
                "2.  **СУЖЕНИЕ КРУГА:** Как только основные категории определены, переходи к вопросам о принадлежности к конкретным произведениям или франшизам. **Начинай с самых популярных и вероятных франшиз** в соответствующем медиа-типе.\n" +
                "3.  **ПОИСК УНИКАЛЬНОГО ПРИЗНАКА:** Как только круг возможных персонажей сузился до конкретного произведения, франшизы или небольшой группы, твоя главная цель — найти вопрос об УНИКАЛЬНОМ, ХАРАКТЕРНОМ и ОЧЕНЬ УЗНАВАЕМОМ признаке, который отличает ОСТАВШИХСЯ кандидатов. Это может быть:\n" +
                "    *   Специфическая способность (например, способность растягивать тело).\n" +
                "    *   Уникальный предмет (особый меч, шрам, вид одежды).\n" +
                "    *   Очень известное действие или событие из их биографии (реальной или вымышленной).\n" +
                "    *   **Выбирай такой вопрос, который максимально быстро отличит одного вероятного кандидата от других, оставшихся в списке.**\n" +
                "4.  **НЕУКОСНИТЕЛЬНОЕ ИСКЛЮЧЕНИЕ:** **ЭТО САМОЕ ВАЖНОЕ ПРАВИЛО.** Используй КАЖДЫЙ мой ответ, особенно \"Нет\", для НЕМЕДЛЕННОГО и ПОЛНОГО ИСКЛЮЧЕНИЯ всех персонажей и категорий, несовместимых с этим ответом. **НИКОГДА, НИ ПРИ КАКИХ УСЛОВИЯХ не задавай вопрос, который противоречит ЛЮБОЙ части информации, полученной ранее.** Если я сказал \"Нет\" на \"Ваш персонаж мужчина?\", ты больше никогда не спрашиваешь о мужских персонажах или специфических мужчинах. Если я сказал \"Нет\" на \"Из фильма?\", ты больше не спрашиваешь о фильмах или персонажах из фильмов.\n" +
                "5.  **Предположение:** Делай предположение ТОЛЬКО тогда, когда собранная информация С ВЫСОЧАЙШЕЙ ВЕРОЯТНОСТЬЮ или однозначно указывает на одного конкретного персонажа.\n" +
                "**ФОРМАТ ОТВЕТА (СТРОГО СЛЕДУЙ ЭТОМУ):**\n" +
                "- Если задаёшь вопрос: `Вопрос: [Текст твоего вопроса]`\n" +
                "- Если делаешь предположение: `Предположение: [Имя персонажа]`\n" +
                "**НИКАКОГО** другого текста, пояснений, приветствий или завершающих фраз не должно быть перед `Вопрос:` или `Предположение:`.\n" +
                "Я готов. Примени свою алгоритмическую эффективность и задай свой первый вопрос.");
        return new ArrayList<>(List.of(message));
    }
}
