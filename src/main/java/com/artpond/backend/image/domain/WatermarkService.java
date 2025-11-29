package com.artpond.backend.image.domain;

import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.artpond.backend.definitions.exception.BadRequestException;

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
        if (!"image/png".equals(file.getContentType())) {
            throw new BadRequestException("La marca de agua debe ser PNG");
        }
        if (file.getSize() > 2 * 1024 * 1024) { // 2MB
            throw new BadRequestException("La marca de agua no puede exceder 2MB");
        }
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

    public BufferedImage getDefaultWatermark() throws IOException {
        try {
            ClassPathResource resource = new ClassPathResource("static/images/artpondo_text.png");
            try (InputStream inputStream = resource.getInputStream()) {
                return ImageIO.read(inputStream);
            }
        } catch (IOException e) {
            System.err.println("YAPPING!!!! No se encontró la marca de agua de artpond. jelou? donde esta?");
            return null;
        }
    }
}
