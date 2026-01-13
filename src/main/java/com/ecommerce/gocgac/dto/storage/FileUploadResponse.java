package com.ecommerce.gocgac.dto.storage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String contentType;
    private String folder;
}

