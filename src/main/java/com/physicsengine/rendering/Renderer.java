package com.physicsengine.rendering;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Handles OpenGL rendering operations
 */
public class Renderer {
    
    private ShaderProgram shaderProgram;
    private Matrix4f projectionMatrix;
    private Matrix4f viewMatrix;
    private Matrix4f modelMatrix;
    
    public void init() {
        // Create shader program
        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(getVertexShaderSource());
        shaderProgram.createFragmentShader(getFragmentShaderSource());
        shaderProgram.link();
        
        // Initialize matrices
        projectionMatrix = new Matrix4f();
        viewMatrix = new Matrix4f();
        modelMatrix = new Matrix4f();
        
        // Setup projection matrix (perspective)
        float fov = (float) Math.toRadians(70.0f);
        float aspectRatio = 1200.0f / 800.0f;
        float nearPlane = 0.1f;
        float farPlane = 1000.0f;
        projectionMatrix.perspective(fov, aspectRatio, nearPlane, farPlane);
        
        // Setup view matrix (camera at origin looking down negative Z)
        viewMatrix.lookAt(
            new Vector3f(0, 5, 10),  // Camera position
            new Vector3f(0, 0, 0),   // Look at point
            new Vector3f(0, 1, 0)    // Up vector
        );
        
        System.out.println("Renderer initialized successfully");
    }
    
    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }
    
    public void renderSphere(Vector3f position, float radius, Vector3f color) {
        shaderProgram.bind();
        
        // Update model matrix
        modelMatrix.identity()
            .translate(position)
            .scale(radius);
        
        // Set uniforms
        shaderProgram.setUniform("projectionMatrix", projectionMatrix);
        shaderProgram.setUniform("viewMatrix", viewMatrix);
        shaderProgram.setUniform("modelMatrix", modelMatrix);
        shaderProgram.setUniform("color", color);
        
        // Render a simple sphere (using a cube for now, can be improved later)
        renderCube();
        
        shaderProgram.unbind();
    }
    
    public void renderBox(Vector3f position, Vector3f size, Vector3f color) {
        shaderProgram.bind();
        
        // Update model matrix
        modelMatrix.identity()
            .translate(position)
            .scale(size);
        
        // Set uniforms
        shaderProgram.setUniform("projectionMatrix", projectionMatrix);
        shaderProgram.setUniform("viewMatrix", viewMatrix);
        shaderProgram.setUniform("modelMatrix", modelMatrix);
        shaderProgram.setUniform("color", color);
        
        renderCube();
        
        shaderProgram.unbind();
    }
    
    private void renderCube() {
        // Simple cube vertices
        float[] vertices = {
            // Front face
            -0.5f, -0.5f,  0.5f,
             0.5f, -0.5f,  0.5f,
             0.5f,  0.5f,  0.5f,
            -0.5f,  0.5f,  0.5f,
            
            // Back face
            -0.5f, -0.5f, -0.5f,
             0.5f, -0.5f, -0.5f,
             0.5f,  0.5f, -0.5f,
            -0.5f,  0.5f, -0.5f
        };
        
        int[] indices = {
            // Front face
            0, 1, 2, 2, 3, 0,
            // Back face
            4, 5, 6, 6, 7, 4,
            // Left face
            7, 3, 0, 0, 4, 7,
            // Right face
            1, 5, 6, 6, 2, 1,
            // Top face
            3, 2, 6, 6, 7, 3,
            // Bottom face
            0, 1, 5, 5, 4, 0
        };
        
        // Create VAO, VBO, EBO
        int vao = glGenVertexArrays();
        int vbo = glGenBuffers();
        int ebo = glGenBuffers();
        
        glBindVertexArray(vao);
        
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
        
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_INT, 0);
        
        // Cleanup
        glBindVertexArray(0);
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);
    }
    
    public void cleanup() {
        if (shaderProgram != null) {
            shaderProgram.cleanup();
        }
    }
    
    private String getVertexShaderSource() {
        return """
            #version 330 core
            
            layout (location = 0) in vec3 aPos;
            
            uniform mat4 projectionMatrix;
            uniform mat4 viewMatrix;
            uniform mat4 modelMatrix;
            
            void main() {
                gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(aPos, 1.0);
            }
            """;
    }
    
    private String getFragmentShaderSource() {
        return """
            #version 330 core
            
            out vec4 FragColor;
            
            uniform vec3 color;
            
            void main() {
                FragColor = vec4(color, 1.0);
            }
            """;
    }
}
