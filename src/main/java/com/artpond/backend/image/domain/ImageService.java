package com.artpond.backend.image.domain;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.artpond.backend.definitions.exception.BadRequestException;
import com.artpond.backend.definitions.exception.ForbiddenException;
import com.artpond.backend.image.dto.ImageUploadDto;
import com.artpond.backend.user.domain.Role;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

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

    public static final int MAX_IMAGES_ARTIST = 5; // Antes era MAX_IMAGES
    public static final int MAX_IMAGES_USER = 3;

    private static final String AVATAR_PREFIX = "avatars/";

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp");

    public String uploadAvatar(MultipartFile file, Long userId) throws IOException {
        validateImageFile(file);
        String format = getImageFormat(file.getContentType());
        String fileName = AVATAR_PREFIX + userId + "/" + System.currentTimeMillis() + "." + format;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());
        metadata.setHeader("x-amz-acl", "public-read"); // Hacerlo público

        s3Client.putObject(bucketName, fileName, file.getInputStream(), metadata);

        return s3Client.getUrl(bucketName, fileName).toString();
    }

    public String generatePresignedUrl(String objectKey) {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * 60;
        expiration.setTime(expTimeMillis);

        URL url = s3Client.generatePresignedUrl(bucketName, objectKey, expiration);
        return url.toString();
    }

    public List<ImageUploadDto> uploadImagesForPublication(
            List<MultipartFile> files,
            String userId,
            Long publicationId,
            Role userRole) throws IOException {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("At least one image is required");
        }

        int limit = (userRole == Role.ARTIST) ? MAX_IMAGES_ARTIST : MAX_IMAGES_USER;

        if (files.size() > limit) {
            throw new IllegalArgumentException("Cannot upload more than " + limit + " images for role " + userRole);
        }

        BufferedImage watermark = null;
        if (userRole == Role.ARTIST) {
            watermark = watermarkService.getUserWatermark(userId);
            if (watermark == null)
                watermark = watermarkService.getDefaultWatermark();
        } else {
            watermark = watermarkService.getDefaultWatermark();
        }

        List<ImageUploadDto> results = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            validateImageFile(files.get(i));
            results.add(uploadSingleImage(files.get(i), userId, publicationId, i, watermark));
        }

        return results;
    }

    private ImageUploadDto uploadSingleImage(MultipartFile file, String userId, Long publicationId,
            int index, BufferedImage watermark) throws IOException {
        validateImageFile(file);
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
        if (!fileName.startsWith(CLEAN_PREFIX + userId + "/")) {
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
        if (contentType.contains("png"))
            return "png";
        if (contentType.contains("jpeg") || contentType.contains("jpg"))
            return "jpg";
        if (contentType.contains("webp"))
            return "webp";
        return "png";
    }

    private void validateImageFile(MultipartFile file) {
        if (!ALLOWED_MIME_TYPES.contains(file.getContentType())) {
            throw new BadRequestException("Tipo de archivo no permitido");
        }
        if (file.getSize() > 10 * 1024 * 1024) { // 10MB
            throw new BadRequestException("Archivo demasiado grande");
        }
    }
}