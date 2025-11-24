package com.artpond.backend.image.domain;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.artpond.backend.definitions.exception.ForbiddenException;
import com.artpond.backend.image.dto.ImageUploadDto;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageService {
    
    private final AmazonS3 s3Client;
    private final WatermarkService watermarkService;
    private final ImageProcessingService imageProcessingService;
    
    @Value("${aws.s3.bucket}")
    private String bucketName;
    
    private static final String CLEAN_PREFIX = "artworks/clean/";
    private static final String PUBLIC_PREFIX = "artworks/public/";
    private static final int MAX_IMAGES = 5;
    
    public List<ImageUploadDto> uploadImagesForPublication(List<MultipartFile> files, String userId, Long publicationId) throws IOException {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("At least one image is required");
        }
        
        if (files.size() > MAX_IMAGES) {
            throw new IllegalArgumentException("Cannot upload more than " + MAX_IMAGES + " images");
        }
        
        BufferedImage watermark = watermarkService.getUserWatermark(userId);
        List<ImageUploadDto> results = new ArrayList<>();
        
        for (int i = 0; i < files.size(); i++) {
            ImageUploadDto result = uploadSingleImage(files.get(i), userId, publicationId, i, watermark);
            results.add(result);
        }
        
        return results;
    }
    
    private ImageUploadDto uploadSingleImage(MultipartFile file, String userId, Long publicationId, 
                                               int index, BufferedImage watermark) throws IOException {
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        String format = getImageFormat(file.getContentType());
        String timestamp = String.valueOf(System.currentTimeMillis());
        String baseFileName = userId + "/pub-" + publicationId + "-img-" + index + "-" + timestamp;
        
        String cleanFileName = CLEAN_PREFIX + baseFileName + "." + format;
        byte[] cleanBytes = imageProcessingService.bufferedImageToBytes(originalImage, format);
        uploadToS3(cleanFileName, cleanBytes, file.getContentType(), false);
        
        String publicFileName = PUBLIC_PREFIX + baseFileName + "." + format;
        
        if (watermark != null) {
            BufferedImage watermarkedImage = imageProcessingService.applyWatermark(originalImage, watermark);
            byte[] watermarkedBytes = imageProcessingService.bufferedImageToBytes(watermarkedImage, format);
            uploadToS3(publicFileName, watermarkedBytes, file.getContentType(), true);
        } else {
            uploadToS3(publicFileName, cleanBytes, file.getContentType(), true);
        }
        
        String publicUrl = s3Client.getUrl(bucketName, publicFileName).toString();
        return new ImageUploadDto(cleanFileName, publicFileName, publicUrl);
    }
    
    private void uploadToS3(String fileName, byte[] data, String contentType, boolean isPublic) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(data.length);
        metadata.setContentType(contentType);
        
        if (isPublic) {
            metadata.setHeader("x-amz-acl", "public-read");
        }
        
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        s3Client.putObject(bucketName, fileName, inputStream, metadata);
    }
    
    public byte[] downloadCleanImage(String fileName, String userId) throws IOException {
        if (!fileName.contains(userId)) {
            throw new ForbiddenException("Unauthorized access");
        }
        
        S3Object s3Object = s3Client.getObject(bucketName, fileName);
        return s3Object.getObjectContent().readAllBytes();
    }
    
    public String getPublicUrl(String fileName) {
        return s3Client.getUrl(bucketName, fileName).toString();
    }
    
    public void deleteImage(String cleanFileKey, String publicFileKey) {
        if (cleanFileKey != null && !cleanFileKey.isEmpty()) {
            s3Client.deleteObject(bucketName, cleanFileKey);
        }
        if (publicFileKey != null && !publicFileKey.isEmpty()) {
            s3Client.deleteObject(bucketName, publicFileKey);
        }
    }
    
    public void deleteMultipleImages(List<String> cleanFileKeys, List<String> publicFileKeys) {
        for (int i = 0; i < cleanFileKeys.size(); i++) {
            deleteImage(cleanFileKeys.get(i), publicFileKeys.get(i));
        }
    }
    
    private String getImageFormat(String contentType) {
        if (contentType.contains("png")) return "png";
        if (contentType.contains("jpeg") || contentType.contains("jpg")) return "jpg";
        if (contentType.contains("webp")) return "webp";
        return "png";
    }
}