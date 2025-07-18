package com.stefanrogic.core.rendering;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Represents a 3D model loaded from an OBJ file
 */
public class Model {
    private int VAO;
    private int VBO;
    private int EBO;
    private int normalVBO;
    private int texCoordVBO;
    private int indexCount;
    private int textureId;
    
    /**
     * Create a model from OBJ data
     * @param modelData The loaded OBJ data
     */
    public Model(OBJLoader.ModelData modelData) {
        this(modelData, 0);
    }
    
    /**
     * Create a model from OBJ data with texture
     * @param modelData The loaded OBJ data
     * @param textureId The OpenGL texture ID
     */
    public Model(OBJLoader.ModelData modelData, int textureId) {
        this.indexCount = modelData.indices.length;
        this.textureId = textureId;
        
        // Generate and bind VAO
        VAO = glGenVertexArrays();
        glBindVertexArray(VAO);
        
        // Create VBO for vertices
        VBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, modelData.vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);
        
        // Create VBO for normals
        if (modelData.normals.length > 0) {
            normalVBO = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, normalVBO);
            glBufferData(GL_ARRAY_BUFFER, modelData.normals, GL_STATIC_DRAW);
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(1);
        }
        
        // Create VBO for texture coordinates
        if (modelData.texCoords.length > 0) {
            texCoordVBO = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, texCoordVBO);
            glBufferData(GL_ARRAY_BUFFER, modelData.texCoords, GL_STATIC_DRAW);
            glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(2);
        }
        
        // Create EBO for indices
        EBO = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, modelData.indices, GL_STATIC_DRAW);
        
        // Unbind
        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }
    
    /**
     * Render the model
     */
    public void render() {
        // Ensure we're rendering filled polygons, not wireframes
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        
        // Bind texture if available
        if (textureId != 0) {
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, textureId);
        }
        
        // Disable back-face culling for better model rendering
        glDisable(GL_CULL_FACE);
        
        glBindVertexArray(VAO);
        glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
        
        // Re-enable back-face culling
        glEnable(GL_CULL_FACE);
        
        // Unbind texture
        if (textureId != 0) {
            glBindTexture(GL_TEXTURE_2D, 0);
        }
    }
    
    /**
     * Render the model with clouds texture
     */
    public void renderWithClouds(int cloudsTextureId) {
        // Ensure we're rendering filled polygons, not wireframes
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        
        // Bind diffuse texture to unit 0
        if (textureId != 0) {
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, textureId);
        }
        
        // Bind clouds texture to unit 1
        if (cloudsTextureId != 0) {
            glActiveTexture(GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D, cloudsTextureId);
        }
        
        // Disable back-face culling for better model rendering
        glDisable(GL_CULL_FACE);
        
        glBindVertexArray(VAO);
        glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
        
        // Re-enable back-face culling
        glEnable(GL_CULL_FACE);
        
        // Unbind textures
        if (textureId != 0) {
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, 0);
        }
        if (cloudsTextureId != 0) {
            glActiveTexture(GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D, 0);
        }
    }
    
    /**
     * Render the model with clouds and bump textures
     */
    public void renderWithBump(int cloudsTextureId, int bumpTextureId) {
        // Ensure we're rendering filled polygons, not wireframes
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        
        // Bind diffuse texture to unit 0
        if (textureId != 0) {
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, textureId);
        }
        
        // Bind clouds texture to unit 1
        if (cloudsTextureId != 0) {
            glActiveTexture(GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D, cloudsTextureId);
        }
        
        // Bind bump texture to unit 2
        if (bumpTextureId != 0) {
            glActiveTexture(GL_TEXTURE2);
            glBindTexture(GL_TEXTURE_2D, bumpTextureId);
        }
        
        // Disable back-face culling for better model rendering
        glDisable(GL_CULL_FACE);
        
        glBindVertexArray(VAO);
        glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
        
        // Re-enable back-face culling
        glEnable(GL_CULL_FACE);
        
        // Unbind textures
        if (textureId != 0) {
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, 0);
        }
        if (cloudsTextureId != 0) {
            glActiveTexture(GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D, 0);
        }
        if (bumpTextureId != 0) {
            glActiveTexture(GL_TEXTURE2);
            glBindTexture(GL_TEXTURE_2D, 0);
        }
    }
    
    /**
     * Render the model with clouds, bump, and night lights textures
     */
    public void renderWithNightLights(int cloudsTextureId, int bumpTextureId, int nightLightsTextureId) {
        // Ensure we're rendering filled polygons, not wireframes
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        
        // Bind diffuse texture to unit 0
        if (textureId != 0) {
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, textureId);
        }
        
        // Bind clouds texture to unit 1
        if (cloudsTextureId != 0) {
            glActiveTexture(GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D, cloudsTextureId);
        }
        
        // Bind bump texture to unit 2
        if (bumpTextureId != 0) {
            glActiveTexture(GL_TEXTURE2);
            glBindTexture(GL_TEXTURE_2D, bumpTextureId);
        }
        
        // Bind night lights texture to unit 3
        if (nightLightsTextureId != 0) {
            glActiveTexture(GL_TEXTURE3);
            glBindTexture(GL_TEXTURE_2D, nightLightsTextureId);
        }
        
        // Disable back-face culling for better model rendering
        glDisable(GL_CULL_FACE);
        
        glBindVertexArray(VAO);
        glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
        
        // Re-enable back-face culling
        glEnable(GL_CULL_FACE);
        
        // Unbind textures
        if (textureId != 0) {
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, 0);
        }
        if (cloudsTextureId != 0) {
            glActiveTexture(GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D, 0);
        }
        if (bumpTextureId != 0) {
            glActiveTexture(GL_TEXTURE2);
            glBindTexture(GL_TEXTURE_2D, 0);
        }
        if (nightLightsTextureId != 0) {
            glActiveTexture(GL_TEXTURE3);
            glBindTexture(GL_TEXTURE_2D, 0);
        }
    }
    
    /**
     * Clean up OpenGL resources
     */
    public void cleanup() {
        glDeleteVertexArrays(VAO);
        glDeleteBuffers(VBO);
        glDeleteBuffers(EBO);
        if (normalVBO != 0) glDeleteBuffers(normalVBO);
        if (texCoordVBO != 0) glDeleteBuffers(texCoordVBO);
    }
    
    // Getters
    public int getVAO() { return VAO; }
    public int getVBO() { return VBO; }
    public int getEBO() { return EBO; }
    public int getIndexCount() { return indexCount; }
    public int getTextureId() { return textureId; }
    
    // Setters
    public void setTextureId(int textureId) { this.textureId = textureId; }
}
