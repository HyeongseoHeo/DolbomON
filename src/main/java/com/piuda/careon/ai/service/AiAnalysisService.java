package com.piuda.careon.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.piuda.careon.ai.dto.AiAnalysisResult;
import com.piuda.careon.ai.dto.AiChangeItem;
import com.piuda.careon.consultation.entity.ConsultationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiAnalysisService {


    @Value("${gemini.api-key}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiAnalysisResult analyze(String sttText) {
        try {
            String prompt = buildPrompt(sttText);

            WebClient webClient = WebClient.builder()
                    .baseUrl("https://generativelanguage.googleapis.com")
                    .build();

            String response = webClient.post()
                    .uri("/v1beta/models/gemini-2.5-flash-lite:generateContent?key=" + apiKey)
                    .bodyValue("""
                            {
                              "contents": [
                                {
                                  "parts": [
                                    {
                                      "text": %s
                                    }
                                  ]
                                }
                              ]
                            }
                            """.formatted(objectMapper.writeValueAsString(prompt)))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            String jsonText = extractText(response);
            return parseResult(jsonText);

        } catch (WebClientResponseException e) {
        System.out.println("Gemini Status = " + e.getStatusCode());
        System.out.println("Gemini Error Body = " + e.getResponseBodyAsString());

        return fallbackResult(sttText);

        } catch (Exception e) {
            e.printStackTrace();
            return fallbackResult(sttText);
        }
    }

    private AiAnalysisResult fallbackResult(String sttText) {
        String preview = sttText;

        if (preview != null && preview.length() > 50) {
            preview = preview.substring(0, 50) + "...";
        }

        return new AiAnalysisResult(
                ConsultationStatus.NEED_REVIEW,
                List.of("AI분석실패", "검토필요"),
                sttText,
                preview,
                List.of(),
                "AI 분석 호출에 실패했습니다. STT 원문을 바탕으로 직접 검토가 필요합니다."
        );
    }

    private String buildPrompt(String sttText) {
        return """
            너는 재가노인 돌봄 상담을 분석하는 AI다.

            절대로 의료 진단을 하지 마라.
            상담 내용을 요약하고 아래의 '표준 태그' 중에서만 선택하여 반환한다.

            ==========================
            표준 태그 목록 (이 외의 태그는 절대 생성하지 말 것)

            - 식사감소
            - 식사거부
            - 수면문제
            - 외로움
            - 우울감
            - 불안
            - 혼동
            - 기억력저하
            - 반복발화
            - 두통
            - 어지럼증
            - 통증
            - 낙상위험
            - 거동불편
            - 복약문제
            - 위생문제
            - 체중감소
            - 사회적고립
            - 가족지원필요
            - 응급상황

            ==========================

            규칙

            1. 태그는 반드시 위 목록에서만 선택한다.
            2. 새로운 태그를 만들지 않는다.
            3. 태그는 최대 5개만 선택한다.
            4. 근거가 없는 태그는 넣지 않는다.
            5. 응답은 JSON만 반환한다.
            6. ```json 또는 설명 문장은 절대 출력하지 않는다.

            상담 원문

            %s

            아래 형식으로만 응답한다.

            {
              "status":"NORMAL|NEED_REVIEW|SPECIAL_NOTE",
              "tags":[
                "식사감소",
                "외로움"
              ],
              "summary":"2~3문장 요약",
              "summaryPreview":"한 줄 요약",
              "changes":[
                {
                  "title":"변화 항목",
                  "description":"변화 내용",
                  "type":"increase|decrease|normal"
                }
              ],
              "socialWorkerOpinion":"사회복지사에게 전달할 의견"
            }
            """.formatted(sttText);
    }

    private String extractText(String response) throws Exception {
        JsonNode root = objectMapper.readTree(response);

        return root.path("candidates")
                .get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text")
                .asText()
                .replace("```json", "")
                .replace("```", "")
                .trim();
    }

    private AiAnalysisResult parseResult(String jsonText) throws Exception {
        JsonNode node = objectMapper.readTree(jsonText);

        ConsultationStatus status = ConsultationStatus.valueOf(
                node.path("status").asText("NORMAL")
        );

        List<String> tags = new ArrayList<>();
        for (JsonNode tagNode : node.path("tags")) {
            tags.add(tagNode.asText());
        }

        List<AiChangeItem> changes = new ArrayList<>();
        for (JsonNode changeNode : node.path("changes")) {
            changes.add(new AiChangeItem(
                    changeNode.path("title").asText(),
                    changeNode.path("description").asText(),
                    changeNode.path("type").asText()
            ));
        }

        return new AiAnalysisResult(
                status,
                tags,
                node.path("summary").asText(),
                node.path("summaryPreview").asText(),
                changes,
                node.path("socialWorkerOpinion").asText()
        );
    }
}
