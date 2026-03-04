package com.btvn.CaoMinhHuy.controllers.api;

import com.btvn.CaoMinhHuy.dtos.BookCoverExtractDto;
import com.btvn.CaoMinhHuy.services.BookCoverAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AiBookController {

    private final BookCoverAiService coverAiService;

    @PostMapping("/cover/extract")
    public ResponseEntity<BookCoverExtractDto> extract(@RequestParam("file") MultipartFile file) {
        BookCoverExtractDto dto = coverAiService.extractFromCover(file);
        return ResponseEntity.ok(dto);
    }
}
