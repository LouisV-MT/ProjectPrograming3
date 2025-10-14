package org.example.recipeapp.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;

@Service
public class ImageStorageService {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${b2.bucket.name}")
    private String bucketName;

    @Value("${b2.endpoint}")
    private String endpoint;

    public ImageStorageService(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    public String upload(MultipartFile file, String fileName) throws IOException {
        PutObjectRequest request= PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .build();


        s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        System.out.println("Successfully uploaded "+fileName);

        String endpointDomain = endpoint.replaceAll("https?://", "");

        return String.format("https://%s.%s/%s", bucketName, endpointDomain, fileName);
    }

    public String generatePresignedUrl(String objectKey) {
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(60))
                .getObjectRequest(objectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        return presignedRequest.url().toExternalForm();
    }

    public void delete(String objectKey){
        if (objectKey == null || objectKey.isBlank()){
            return;
        }
        try{
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
            System.out.println("Successfully deleted file from S3: " + objectKey);

        }catch (Exception e) {
            System.err.println("Error deleting file from S3: " + objectKey);
            e.printStackTrace();
        }
    }
}
