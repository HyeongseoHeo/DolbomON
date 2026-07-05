package com.piuda.careon.ai.service;

import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class AudioConvertService {

    public Path convertM4aToWav(Path m4aPath) {

        try {

            Path wavPath = Files.createTempFile("consultation_", ".wav");

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "ffmpeg",
                    "-y",
                    "-i", m4aPath.toString(),

                    "-ac", "1",          // mono
                    "-ar", "16000",      // 16kHz
                    "-acodec", "pcm_s16le",

                    wavPath.toString()
            );

            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            String error = new String(
                    process.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException(
                        "FFmpeg 변환 실패\nExitCode=" + exitCode + "\n" + error
                );
            }

            return wavPath;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("m4a → wav 변환 중 인터럽트 발생", e);
        } catch (IOException e) {
            throw new RuntimeException("m4a → wav 변환 실패", e);
        }

    }

}
