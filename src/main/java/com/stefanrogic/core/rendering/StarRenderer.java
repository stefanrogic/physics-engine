package com.stefanrogic.core.rendering;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.*;

import java.nio.FloatBuffer;
import java.util.Random;
import org.lwjgl.BufferUtils;
import org.joml.Matrix4f;

/**
 * Renders stars as distant points of light in the background
 */
public class StarRenderer {
    
    private ShaderManager.ShaderPrograms shaders;
    private int starVAO;
    private int starVBO;
    private int starCount;
    private static final int MAX_STARS = 2000;
    
    public StarRenderer(ShaderManager.ShaderPrograms shaders) {
        this.shaders = shaders;
    }
    
    /**
     * Generate and initialize star positions and sizes
     */
    public void createStars() {
        Random random = new Random(42); // Fixed seed for consistent star field
        this.starCount = MAX_STARS;
        
        // Create star data: position (x,y,z) + size (w) + brightness (r,g,b)
        float[] starData = new float[starCount * 7]; // 3 pos + 1 size + 3 color
        
        for (int i = 0; i < starCount; i++) {
            int index = i * 7;
            
            // Generate random position on a distant sphere
            float distance = 150000.0f; // Way beyond Jupiter (78,000 units) - nearly double Jupiter's distance
            float phi = random.nextFloat() * 2.0f * (float) Math.PI; // 0 to 2π
            float cosTheta = 2.0f * random.nextFloat() - 1.0f; // -1 to 1
            float sinTheta = (float) Math.sqrt(1.0f - cosTheta * cosTheta);
            
            // Convert spherical to cartesian coordinates
            starData[index] = distance * sinTheta * (float) Math.cos(phi); // x
            starData[index + 1] = distance * cosTheta; // y
            starData[index + 2] = distance * sinTheta * (float) Math.sin(phi); // z
            
            // Random star size (point size) - much larger for very distant stars
            float starSize = 4.0f + random.nextFloat() * 8.0f; // 4-12 pixels
            starData[index + 3] = starSize;
            
            // Star color - most stars are white/blue-white, some are yellow/orange/red
            float colorType = random.nextFloat();
            if (colorType < 0.7f) {
                // Blue-white stars (most common)
                starData[index + 4] = 0.8f + random.nextFloat() * 0.2f; // r
                starData[index + 5] = 0.8f + random.nextFloat() * 0.2f; // g
                starData[index + 6] = 0.9f + random.nextFloat() * 0.1f; // b
            } else if (colorType < 0.85f) {
                // Yellow stars (like our Sun)
                starData[index + 4] = 0.9f + random.nextFloat() * 0.1f; // r
                starData[index + 5] = 0.8f + random.nextFloat() * 0.2f; // g
                starData[index + 6] = 0.6f + random.nextFloat() * 0.2f; // b
            } else if (colorType < 0.95f) {
                // Orange stars
                starData[index + 4] = 0.9f + random.nextFloat() * 0.1f; // r
                starData[index + 5] = 0.6f + random.nextFloat() * 0.2f; // g
                starData[index + 6] = 0.4f + random.nextFloat() * 0.2f; // b
            } else {
                // Red stars (rare)
                starData[index + 4] = 0.8f + random.nextFloat() * 0.2f; // r
                starData[index + 5] = 0.4f + random.nextFloat() * 0.2f; // g
                starData[index + 6] = 0.3f + random.nextFloat() * 0.2f; // b
            }
            
            // Vary brightness - increased brightness for better visibility at distance
            float brightness = 0.6f + random.nextFloat() * 0.4f; // 0.6 to 1.0 (brighter)
            starData[index + 4] *= brightness;
            starData[index + 5] *= brightness;
            starData[index + 6] *= brightness;
        }
        
        // Create VAO and VBO
        starVAO = glGenVertexArrays();
        starVBO = glGenBuffers();
        
        glBindVertexArray(starVAO);
        glBindBuffer(GL_ARRAY_BUFFER, starVBO);
        
        FloatBuffer starBuffer = BufferUtils.createFloatBuffer(starData.length);
        starBuffer.put(starData);
        starBuffer.flip();
        
        glBufferData(GL_ARRAY_BUFFER, starBuffer, GL_STATIC_DRAW);
        
        // Position attribute (location 0)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 7 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        // Size attribute (location 1)
        glVertexAttribPointer(1, 1, GL_FLOAT, false, 7 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);
        
        // Color attribute (location 2)
        glVertexAttribPointer(2, 3, GL_FLOAT, false, 7 * Float.BYTES, 4 * Float.BYTES);
        glEnableVertexAttribArray(2);
        
        glBindVertexArray(0);
        
        System.out.println("Created " + starCount + " stars for background rendering");
    }
    
    /**
     * Render the star field
     */
    public void renderStars(Matrix4f mvpMatrix) {
        glUseProgram(shaders.starShaderProgram);
        
        // Enable blending for smooth star appearance
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        // Disable depth writing for stars so they don't interfere with other objects
        glDepthMask(false);
        
        // Enable point sprite rendering
        glEnable(GL_PROGRAM_POINT_SIZE);
        
        // Set MVP matrix
        FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
        mvpMatrix.get(matrixBuffer);
        glUniformMatrix4fv(shaders.starMvpLocation, false, matrixBuffer);
        
        // Render stars as points
        glBindVertexArray(starVAO);
        glDrawArrays(GL_POINTS, 0, starCount);
        glBindVertexArray(0);
        
        // Restore OpenGL state
        glDisable(GL_PROGRAM_POINT_SIZE);
        glDepthMask(true);
        glDisable(GL_BLEND);
    }
    
    /**
     * Clean up resources
     */
    public void cleanup() {
        if (starVAO != 0) {
            glDeleteVertexArrays(starVAO);
        }
        if (starVBO != 0) {
            glDeleteBuffers(starVBO);
        }
    }
}
