package com.btvn.CaoMinhHuy.services;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String saveCover(MultipartFile file);
}
