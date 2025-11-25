package com.artpond.backend.report.application;

import com.artpond.backend.report.domain.Appeal;
import com.artpond.backend.report.domain.AppealService;
import com.artpond.backend.report.domain.AppealStatus;
import com.artpond.backend.report.dto.CreateAppealDto;
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
@RequestMapping("/appeal")
@RequiredArgsConstructor
public class AppealController {

    private final AppealService appealService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> createAppeal(@RequestBody CreateAppealDto dto, 
                                             @AuthenticationPrincipal User user) {
        appealService.createAppeal(dto.getPublicationId(), dto.getJustification(), user.getUserId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<Page<Appeal>> getAppeals(Pageable pageable) {
        return ResponseEntity.ok(appealService.getPendingAppeals(pageable));
    }

    @PatchMapping("/admin/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<Void> resolveAppeal(@PathVariable Long id, 
                                              @RequestBody Map<String, String> body,
                                              @AuthenticationPrincipal User admin) {
        AppealStatus status = AppealStatus.valueOf(body.get("status"));
        String notes = body.getOrDefault("notes", "");
        appealService.resolveAppeal(id, status, notes, admin.getUserId());
        return ResponseEntity.ok().build();
    }
}
