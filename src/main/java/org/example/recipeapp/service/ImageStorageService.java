package org.example.recipeapp.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

@Service
public class ImageStorageService {
    private final S3Client s3Client;

    @Value("${b2.bucket.name}")
    private String bucketName;

    @Value("${b2.endpoint}")
    private String endpoint;

    public ImageStorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }
    public String upload(MultipartFile file,String fileName) throws IOException {
        PutObjectRequest request= PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .build();


        s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        System.out.println("Successfully uploaded "+fileName);

        return String.format("https://%s.%s/%s", bucketName, endpoint, fileName);
    }
}
