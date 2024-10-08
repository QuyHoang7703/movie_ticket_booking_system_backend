package com.bytecinema.MovieTicketBookingSystem.service;

import java.io.IOException;

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

    public String uploadFile(MultipartFile file, String folderName) {
        String originalFilename = file.getOriginalFilename();
         String filePath = folderName + "/" + originalFilename;

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize()); //Set size for file
            // Push file to S3
            PutObjectResult putObjectResult = amazonS3.putObject(bucketName, filePath, file.getInputStream(), metadata);
            return getImageUrl(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to S3: " + e.getMessage(), e);
        }
    }


}
