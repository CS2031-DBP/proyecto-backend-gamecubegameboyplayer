package com.artpond.backend.publication.domain;

import com.artpond.backend.image.domain.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiDetectionService {

    private final ImageService imageService;

    @Value("${app.scripts.path:scripts/ai/}")
    private String scriptsPath;

    @Value("${app.python.command:python3}")
    private String pythonCommand;

    public boolean analyzeImage(Long publicationId, String imageKey, PubType type) {
        Path tempFile = null;
        try {
            byte[] imageBytes = imageService.downloadCleanImage(imageKey, ""); 
            tempFile = Files.createTempFile("ai-check-" + publicationId, ".tmp");
            Files.write(tempFile, imageBytes);

            String scriptName = (type == PubType.PHOTOGRAPHY) ? "detect_photo.py" : "detect_illustration.py";
            File scriptFile = new File(scriptsPath, scriptName);
            
            if (!scriptFile.exists()) {
                scriptFile = Paths.get(System.getProperty("user.dir"), scriptsPath, scriptName).toFile();
                if (!scriptFile.exists()) {
                    log.error("AI Script not found at: {}", scriptFile.getAbsolutePath());
                    return false;
                }
            }

            ProcessBuilder pb = new ProcessBuilder(
                pythonCommand, 
                scriptFile.getAbsolutePath(), 
                tempFile.toAbsolutePath().toString()
            );
            pb.redirectErrorStream(true); 
            
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String lastOutput = "FALSE";
            
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) lastOutput = line.trim(); 
            }

            boolean finished = process.waitFor(60, TimeUnit.SECONDS);
            
            if (!finished) {
                process.destroy();
                throw new RuntimeException("AI analysis script timed out");
            }

            if (process.exitValue() == 0) {
                boolean isAi = Boolean.parseBoolean(lastOutput) || "TRUE".equalsIgnoreCase(lastOutput);
                log.info("AI Check Pub #{}: Result={} (Raw='{}')", publicationId, isAi, lastOutput);
                return isAi;
            } else {
                log.warn("AI Script exited with code {}. Output: {}", process.exitValue(), lastOutput);
                return false; 
            }

        } catch (Exception e) {
            log.error("Error in AI analysis: {}", e.getMessage());
            throw new RuntimeException("AI Analysis failed: " + e.getMessage(), e);
        } finally {
            try { if (tempFile != null) Files.deleteIfExists(tempFile); } catch (Exception e) {}
        }
    }
}
