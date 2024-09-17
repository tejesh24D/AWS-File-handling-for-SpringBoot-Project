package com.example.filehandling.service;



import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class S3Service {

    private final S3Client s3Client;
    private final String bucketName;

    public S3Service(@Value("${aws.s3.access-key}") String accessKey,
                     @Value("${aws.s3.secret-key}") String secretKey,
                     @Value("${aws.s3.region}") String region,
                     @Value("${aws.s3.bucket-name}") String bucketName) {

        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();

        this.bucketName = bucketName;
    }

    // Upload file to S3
    public String uploadFile(String fileName, InputStream inputStream) throws Exception {
        File tempFile = File.createTempFile("temp", fileName);
        FileOutputStream outputStream = new FileOutputStream(tempFile);
        byte[] buffer = new byte[inputStream.available()];
        inputStream.read(buffer);
        outputStream.write(buffer);
        outputStream.close();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        s3Client.putObject(putObjectRequest, tempFile.toPath());
        Files.deleteIfExists(tempFile.toPath());

        return fileName;
    }

    // Download file from S3
    public byte[] downloadFile(String fileName) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        return s3Client.getObjectAsBytes(getObjectRequest).asByteArray();
    }

    // Delete file from S3
    public void deleteFile(String fileName) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }

    // List all files in the bucket
    public List<String> listFiles() {
        ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();

        return s3Client.listObjectsV2(listObjectsRequest)
                .contents()
                .stream()
                .map(S3Object::key)
                .collect(Collectors.toList());
    }
}
