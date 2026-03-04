package com.btvn.CaoMinhHuy.controllers;

import com.btvn.CaoMinhHuy.services.AdminDashboardService;
import com.btvn.CaoMinhHuy.services.AdminExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final AdminDashboardService dashboardService;
    private final AdminExportService exportService;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Admin Dashboard");
        model.addAttribute("stats", dashboardService.getOverview());
        model.addAttribute("content", "admin/dashboard :: content");
        return "layout/layout";
    }

    @GetMapping("/export/excel")
    public ResponseEntity<ByteArrayResource> exportExcel() {
        byte[] data = exportService.exportExcel();
        ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=dashboard.xlsx")
                .contentLength(data.length)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    @GetMapping("/export/word")
    public ResponseEntity<ByteArrayResource> exportWord() {
        byte[] data = exportService.exportWord();
        ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=dashboard.docx")
                .contentLength(data.length)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .body(resource);
    }
}
