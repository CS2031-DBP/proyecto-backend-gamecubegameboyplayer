package com.artpond.backend.report.application;

import com.artpond.backend.report.domain.ReportService;
import com.artpond.backend.report.dto.CreateReportDto;
import com.artpond.backend.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<Void> createReport(@RequestBody @Valid CreateReportDto dto,
                                             @AuthenticationPrincipal User user) {
        reportService.createReport(dto, user.getUserId());
        return ResponseEntity.ok().build();
    }
}
