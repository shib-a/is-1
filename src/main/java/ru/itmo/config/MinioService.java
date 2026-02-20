package ru.itmo.config;

import io.minio.*;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.logging.Logger;

@ApplicationScoped
public class MinioService {

    private static final Logger LOGGER = Logger.getLogger(MinioService.class.getName());

    private static final String MINIO_ENDPOINT = System.getProperty("minio.endpoint", "http://localhost:31167");
    private static final String MINIO_ACCESS_KEY = System.getProperty("minio.accessKey", "minioadmin");
    private static final String MINIO_SECRET_KEY = System.getProperty("minio.secretKey", "minioadmin");
    private static final String BUCKET_NAME = "import-files";

    private MinioClient minioClient;
    private boolean available = false;

    @PostConstruct
    public void init() {
        try {
            minioClient = MinioClient.builder()
                    .endpoint(MINIO_ENDPOINT)
                    .credentials(MINIO_ACCESS_KEY, MINIO_SECRET_KEY)
                    .build();
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET_NAME).build());
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET_NAME).build());
                LOGGER.info("Bucket '" + BUCKET_NAME + "' created");
            }

            available = true;
            LOGGER.info("MinIO client initialized successfully. Endpoint: " + MINIO_ENDPOINT);
        } catch (Exception e) {
            LOGGER.warning("Could not initialize MinIO client: " + e.getMessage());
            available = false;
        }
    }

    public boolean isAvailable() {
        if (!available) return false;
        try {
            minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET_NAME).build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String uploadFile(String fileName, byte[] data) throws MinioException {
        if (!available) {
            init();
            throw new MinioException("MinIO is not available");
        }

        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(fileName)
                    .stream(inputStream, data.length, -1)
                    .contentType("application/json")
                    .build());

            LOGGER.info("File uploaded to MinIO: " + fileName);
            return fileName;
        } catch (Exception e) {
            throw new MinioException("Failed to upload file: " + e.getMessage());
        }
    }

    public byte[] downloadFile(String fileName) throws MinioException {
        if (!available) {
            init();
            throw new MinioException("MinIO is not available");
        }

        try (InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(BUCKET_NAME)
                .object(fileName)
                .build())) {
            return stream.readAllBytes();
        } catch (Exception e) {
            throw new MinioException("Failed to download file: " + e.getMessage());
        }
    }

    public void deleteFile(String fileName) throws MinioException {
        if (!available) {
            init();
            throw new MinioException("MinIO is not available");
        }

        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(fileName)
                    .build());
            LOGGER.info("File deleted from MinIO: " + fileName);
        } catch (Exception e) {
            throw new MinioException("Failed to delete file: " + e.getMessage());
        }
    }

    public boolean fileExists(String fileName) {
        if (!available) {
            init();
            return false;
        }

        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(fileName)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static class MinioException extends Exception {
        public MinioException(String message) {
            super(message);
        }
    }
}

