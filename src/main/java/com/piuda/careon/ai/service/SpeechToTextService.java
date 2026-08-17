package com.piuda.careon.ai.service;

import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechRecognitionResult;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class SpeechToTextService {

    public String transcribe(Path filePath, String languageCode) {

        // 프론트에서 ko / ja 또는 locale 형태 모두 받을 수 있도록 통일
        String normalizedLanguageCode = normalizeLanguageCode(languageCode);

        try (SpeechClient speechClient = SpeechClient.create()) {

            byte[] data = Files.readAllBytes(filePath);
            ByteString audioBytes = ByteString.copyFrom(data);

            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                    .setSampleRateHertz(16000)
                    .setLanguageCode(normalizedLanguageCode)
                    .setEnableAutomaticPunctuation(true)
                    .build();

            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setContent(audioBytes)
                    .build();

            RecognizeResponse response =
                    speechClient.recognize(config, audio);

            StringBuilder resultText = new StringBuilder();

            for (SpeechRecognitionResult result :
                    response.getResultsList()) {

                if (result.getAlternativesCount() > 0) {
                    resultText
                            .append(result.getAlternatives(0).getTranscript())
                            .append(" ");
                }
            }

            return resultText.toString().trim();

        } catch (IOException e) {
            throw new RuntimeException("STT 변환에 실패했습니다.", e);
        }
    }

    private String normalizeLanguageCode(String languageCode) {

        if (languageCode == null || languageCode.isBlank()) {
            throw new IllegalArgumentException(
                    "STT 언어 코드가 필요합니다."
            );
        }

        return switch (languageCode) {
            case "ko", "ko-KR" -> "ko-KR";
            case "ja", "ja-JP" -> "ja-JP";

            default -> throw new IllegalArgumentException(
                    "지원하지 않는 STT 언어입니다: " + languageCode
            );
        };
    }
}
