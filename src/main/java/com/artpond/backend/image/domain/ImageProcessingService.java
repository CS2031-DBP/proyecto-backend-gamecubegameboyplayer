package com.artpond.backend.image.domain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;

@Service
public class ImageProcessingService {
    
    public BufferedImage applyWatermark(BufferedImage originalImage, BufferedImage watermark) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        
        BufferedImage watermarkedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = watermarkedImage.createGraphics();
        g2d.drawImage(originalImage, 0, 0, null);
        
        int watermarkWidth = Math.min(watermark.getWidth(), width / 4);
        int watermarkHeight = (int) (watermark.getHeight() * ((double) watermarkWidth / watermark.getWidth()));
        
        int x = width - watermarkWidth - 20;
        int y = height - watermarkHeight - 20;
        
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
        g2d.drawImage(watermark, x, y, watermarkWidth, watermarkHeight, null);
        g2d.dispose();
        
        return watermarkedImage;
    }
    
    public byte[] bufferedImageToBytes(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        return baos.toByteArray();
    }
    
    public BufferedImage bytesToBufferedImage(byte[] imageBytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
        return ImageIO.read(bais);
    }
}
