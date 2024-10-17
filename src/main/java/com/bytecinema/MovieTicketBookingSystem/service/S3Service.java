package com.bytecinema.MovieTicketBookingSystem.service;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class S3Service {
    @Value("${bucketName}")
    private String bucketName;
    @Value("${aws.url.images}")
    private String imageUrl;

    private final AmazonS3 amazonS3;

    


    public String getImageUrl(String imagePath) {
        return imageUrl + imagePath; 
    }

    public String uploadFile(MultipartFile file) {
        // return "abc";
        String originalFilename = file.getOriginalFilename();

        String newFilename = UUID.randomUUID().toString() + "_" + originalFilename;

//         String filePath = folderName + "/" + newFilename;
        String filePath =  newFilename;


        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize()); //Set size for file
            String contentType = file.getContentType(); // Lấy Content-Type từ MultipartFile
            System.out.println(">>>>>>>>>>>> CONTENT TYPE: " + contentType);
            if (contentType != null && contentType.startsWith("image/")) {
                metadata.setContentType(contentType);
            } else {
                // Nếu không phải là hình ảnh, có thể đặt giá trị mặc định hoặc xử lý lỗi
                throw new IllegalArgumentException("Uploaded file is not an image.");
            }
            // metadata.setContentType(contentType != null ? contentType : "multipart/form-data");
            // Push file to S3
            PutObjectResult putObjectResult = amazonS3.putObject(bucketName, filePath, file.getInputStream(), metadata);
            return getImageUrl(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to S3: " + e.getMessage(), e);
        }
    }


}
