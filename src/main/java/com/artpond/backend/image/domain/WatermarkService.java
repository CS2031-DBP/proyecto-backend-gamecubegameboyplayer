package com.artpond.backend.image.domain;

import java.io.IOException;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.awt.image.BufferedImage;

@Service
public class WatermarkService {
    
    private final AmazonS3 s3Client;
    
    @Value("${aws.s3.bucket}")
    private String bucketName;
    
    private static final String WATERMARK_PREFIX = "watermarks/";
    
    public WatermarkService(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }
    
    public String uploadWatermark(MultipartFile file, String userId) throws IOException {
        deleteUserWatermark(userId);
        
        String fileName = WATERMARK_PREFIX + userId + "/" + System.currentTimeMillis() + ".png";
        
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType("image/png");
        
        s3Client.putObject(bucketName, fileName, file.getInputStream(), metadata);
        
        return fileName;
    }
    
    public BufferedImage getUserWatermark(String userId) throws IOException {
        ListObjectsV2Request listRequest = new ListObjectsV2Request()
                .withBucketName(bucketName)
                .withPrefix(WATERMARK_PREFIX + userId + "/");
        
        ListObjectsV2Result result = s3Client.listObjectsV2(listRequest);
        
        if (result.getObjectSummaries().isEmpty()) {
            return null;
        }
        
        String watermarkKey = result.getObjectSummaries().get(0).getKey();
        S3Object s3Object = s3Client.getObject(bucketName, watermarkKey);
        
        return ImageIO.read(s3Object.getObjectContent());
    }
    
    public void deleteUserWatermark(String userId) {
        ListObjectsV2Request listRequest = new ListObjectsV2Request()
                .withBucketName(bucketName)
                .withPrefix(WATERMARK_PREFIX + userId + "/");
        
        ListObjectsV2Result result = s3Client.listObjectsV2(listRequest);
        
        for (S3ObjectSummary summary : result.getObjectSummaries()) {
            s3Client.deleteObject(bucketName, summary.getKey());
        }
    }
}
