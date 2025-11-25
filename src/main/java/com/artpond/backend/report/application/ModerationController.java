package com.artpond.backend.report.application;

import com.artpond.backend.report.domain.Report;
import com.artpond.backend.report.domain.ReportService;
import com.artpond.backend.report.domain.ReportStatus;
import com.artpond.backend.publication.domain.PublicationService;
import com.artpond.backend.publication.dto.FailedAiTaskDto;
import com.artpond.backend.publication.dto.FailedPlaceTaskDto;
import com.artpond.backend.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/moderation")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')") 
public class ModerationController {
    private final ReportService reportService;
    private final PublicationService publicationService;

    @GetMapping("/reports")
    public ResponseEntity<Page<Report>> getReports(
            @RequestParam(defaultValue = "PENDING") ReportStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(reportService.getReportsByStatus(status, pageable));
    }

    @PatchMapping("/reports/{id}/resolve")
    public ResponseEntity<Void> resolveReport(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal User moderator) {
        
        ReportStatus status = ReportStatus.valueOf(body.get("status"));
        String notes = body.getOrDefault("notes", "");
        
        reportService.resolveReport(id, status, notes, moderator.getUserId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/tasks/place/failed")
    public ResponseEntity<Page<FailedPlaceTaskDto>> getFailedTasks(Pageable pageable) {
        return ResponseEntity.ok(publicationService.getFailedPlaceTasks(pageable));
    }

    @PostMapping("/tasks/place/{taskId}/retry")
    public ResponseEntity<Void> retryTask(@PathVariable Long taskId) {
        publicationService.retryFailedTask(taskId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/tasks/place{taskId}")
    public ResponseEntity<Void> dismissTask(@PathVariable Long taskId) {
        publicationService.deleteFailedTask(taskId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tasks/ai/failed")
    public ResponseEntity<Page<FailedAiTaskDto>> getFailedAiTasks(Pageable pageable) {
        return ResponseEntity.ok(publicationService.getFailedAiTasks(pageable));
    }

    @PostMapping("/tasks/ai/{taskId}/retry")
    public ResponseEntity<Void> retryAiTask(@PathVariable Long taskId) {
        publicationService.retryFailedAiTask(taskId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/tasks/ai/{taskId}")
    public ResponseEntity<Void> dismissAiTask(@PathVariable Long taskId) {
        publicationService.deleteFailedAiTask(taskId);
        return ResponseEntity.noContent().build();
    }
}
