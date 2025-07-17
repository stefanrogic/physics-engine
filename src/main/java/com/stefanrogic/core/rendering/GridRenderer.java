package com.stefanrogic.core.rendering;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.joml.Matrix4f;
import com.stefanrogic.core.rendering.ShaderManager;

/**
 * Handles rendering of the reference grid in the solar system simulation
 */
public class GridRenderer {
    
    private int gridVAO, gridVBO;
    private ShaderManager.ShaderPrograms shaders;
    private int vertexCount;
    
    public GridRenderer(ShaderManager.ShaderPrograms shaders) {
        this.shaders = shaders;
    }
    
    /**
     * Initialize the grid geometry
     */
    public void createGrid() {
        // CREATE GRID LINES
        java.util.List<Float> gridVertices = new java.util.ArrayList<>();
        
        // GRID SIZE AND SPACING (EXPANDED FOR MARS ORBIT)
        int gridSize = 25000; // INCREASED TO COVER MARS ORBIT (22,790 UNITS)
        int spacing = 2500;   // INCREASED GRID LINE SPACING FOR BETTER VISIBILITY AT MARS SCALE
        
        // VERTICAL LINES (NORTH-SOUTH)
        for (int x = -gridSize; x <= gridSize; x += spacing) {
            gridVertices.add((float) x);
            gridVertices.add(0.0f);
            gridVertices.add((float) -gridSize);
            
            gridVertices.add((float) x);
            gridVertices.add(0.0f);
            gridVertices.add((float) gridSize);
        }
        
        // HORIZONTAL LINES (EAST-WEST)
        for (int z = -gridSize; z <= gridSize; z += spacing) {
            gridVertices.add((float) -gridSize);
            gridVertices.add(0.0f);
            gridVertices.add((float) z);
            
            gridVertices.add((float) gridSize);
            gridVertices.add(0.0f);
            gridVertices.add((float) z);
        }
        
        // CONVERT TO ARRAY
        float[] gridArray = new float[gridVertices.size()];
        for (int i = 0; i < gridVertices.size(); i++) {
            gridArray[i] = gridVertices.get(i);
        }
        
        // Store vertex count for rendering
        vertexCount = gridArray.length / 3;
        
        gridVAO = glGenVertexArrays();
        gridVBO = glGenBuffers();
        
        glBindVertexArray(gridVAO);
        glBindBuffer(GL_ARRAY_BUFFER, gridVBO);
        
        FloatBuffer gridBuffer = BufferUtils.createFloatBuffer(gridArray.length);
        gridBuffer.put(gridArray).flip();
        glBufferData(GL_ARRAY_BUFFER, gridBuffer, GL_STATIC_DRAW);
        
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        glBindVertexArray(0);
    }
    
    /**
     * Render the grid if visible
     * 
     * @param mvpMatrix The model-view-projection matrix
     * @param gridVisible Whether the grid should be rendered
     */
    public void renderGrid(Matrix4f mvpMatrix, boolean gridVisible) {
        if (gridVisible) {
            glUseProgram(shaders.gridShaderProgram);
            FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
            mvpMatrix.get(matrixBuffer);
            glUniformMatrix4fv(shaders.gridMvpLocation, false, matrixBuffer);
            glBindVertexArray(gridVAO);
            glDrawArrays(GL_LINES, 0, vertexCount);
        }
    }
    
    /**
     * Clean up OpenGL resources
     */
    public void cleanup() {
        if (gridVAO != 0) {
            glDeleteVertexArrays(gridVAO);
        }
        if (gridVBO != 0) {
            glDeleteBuffers(gridVBO);
        }
    }
}
