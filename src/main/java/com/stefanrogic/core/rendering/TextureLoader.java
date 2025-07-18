package com.stefanrogic.core.rendering;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * Texture loader for PNG files using STB Image
 */
public class TextureLoader {
    
    /**
     * Load a PNG texture from the resources directory
     * @param resourcePath Path to the PNG file in resources (e.g., "textures/earth.png")
     * @return OpenGL texture ID
     */
    public static int loadTextureFromResources(String resourcePath) {
        ByteBuffer imageBuffer;
        int width, height;
        
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);
            
            // Load from resources using class loader
            try (var inputStream = TextureLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
                if (inputStream == null) {
                    throw new RuntimeException("Resource not found: " + resourcePath);
                }
                
                // Read all bytes from input stream
                byte[] imageBytes = inputStream.readAllBytes();
                ByteBuffer imageData = BufferUtils.createByteBuffer(imageBytes.length);
                imageData.put(imageBytes);
                imageData.flip();
                
                // Load image data with STB
                imageBuffer = STBImage.stbi_load_from_memory(imageData, w, h, channels, 4);
                if (imageBuffer == null) {
                    throw new RuntimeException("Failed to load texture: " + resourcePath + " - " + STBImage.stbi_failure_reason());
                }
                
                width = w.get();
                height = h.get();
                
                System.out.println("Loaded texture from resources: " + resourcePath + " (" + width + "x" + height + ")");
                
            } catch (Exception e) {
                throw new RuntimeException("Error loading texture from resources: " + resourcePath, e);
            }
        }
        
        // Create OpenGL texture
        int textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        
        // Set texture parameters
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        
        // Upload texture data
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, imageBuffer);
        
        // Generate mipmaps
        glGenerateMipmap(GL_TEXTURE_2D);
        
        // Free STB image memory
        STBImage.stbi_image_free(imageBuffer);
        
        glBindTexture(GL_TEXTURE_2D, 0);
        
        return textureId;
    }
    
    /**
     * Load Earth diffuse texture
     * @return OpenGL texture ID
     */
    public static int createEarthTexture() {
        // Try to load the diffuse texture from resources
        try {
            return loadTextureFromResources("textures/Diffuse_2K.png");
        } catch (Exception e) {
            System.err.println("Failed to load Earth diffuse texture: " + e.getMessage());
            return createFallbackTexture();
        }
    }
    
    /**
     * Create a simple fallback texture if PNG loading fails
     */
    private static int createFallbackTexture() {
        // Create a simple 2x2 blue texture as fallback
        ByteBuffer buffer = BufferUtils.createByteBuffer(2 * 2 * 4);
        
        // Blue color (RGBA)
        for (int i = 0; i < 4; i++) {
            buffer.put((byte) 0x1e);  // R
            buffer.put((byte) 0x3a);  // G
            buffer.put((byte) 0x5f);  // B
            buffer.put((byte) 0xff);  // A
        }
        buffer.flip();
        
        int textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, 2, 2, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        
        glBindTexture(GL_TEXTURE_2D, 0);
        
        return textureId;
    }
    
    /**
     * Delete a texture
     */
    public static void deleteTexture(int textureId) {
        glDeleteTextures(textureId);
    }
}
