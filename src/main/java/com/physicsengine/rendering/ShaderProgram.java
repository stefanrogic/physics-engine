package com.physicsengine.rendering;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;

/**
 * Manages OpenGL shader programs
 */
public class ShaderProgram {
    
    private int programId;
    private int vertexShaderId;
    private int fragmentShaderId;
    private Map<String, Integer> uniforms;
    
    public ShaderProgram() {
        programId = glCreateProgram();
        if (programId == 0) {
            throw new RuntimeException("Could not create Shader Program");
        }
        uniforms = new HashMap<>();
    }
    
    public void createVertexShader(String shaderCode) {
        vertexShaderId = createShader(shaderCode, GL_VERTEX_SHADER);
    }
    
    public void createFragmentShader(String shaderCode) {
        fragmentShaderId = createShader(shaderCode, GL_FRAGMENT_SHADER);
    }
    
    protected int createShader(String shaderCode, int shaderType) {
        int shaderId = glCreateShader(shaderType);
        if (shaderId == 0) {
            throw new RuntimeException("Error creating shader. Type: " + shaderType);
        }
        
        glShaderSource(shaderId, shaderCode);
        glCompileShader(shaderId);
        
        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
            throw new RuntimeException("Error compiling Shader code: " + glGetShaderInfoLog(shaderId, 1024));
        }
        
        glAttachShader(programId, shaderId);
        
        return shaderId;
    }
    
    public void link() {
        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            throw new RuntimeException("Error linking Shader code: " + glGetProgramInfoLog(programId, 1024));
        }
        
        if (vertexShaderId != 0) {
            glDetachShader(programId, vertexShaderId);
        }
        if (fragmentShaderId != 0) {
            glDetachShader(programId, fragmentShaderId);
        }
        
        glValidateProgram(programId);
        if (glGetProgrami(programId, GL_VALIDATE_STATUS) == 0) {
            System.err.println("Warning validating Shader code: " + glGetProgramInfoLog(programId, 1024));
        }
    }
    
    public void bind() {
        glUseProgram(programId);
    }
    
    public void unbind() {
        glUseProgram(0);
    }
    
    public void cleanup() {
        unbind();
        if (programId != 0) {
            glDeleteProgram(programId);
        }
    }
    
    public void createUniform(String uniformName) {
        int uniformLocation = glGetUniformLocation(programId, uniformName);
        if (uniformLocation < 0) {
            throw new RuntimeException("Could not find uniform: " + uniformName);
        }
        uniforms.put(uniformName, uniformLocation);
    }
    
    public void setUniform(String uniformName, Matrix4f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Get uniform location (create if doesn't exist)
            Integer location = uniforms.get(uniformName);
            if (location == null) {
                location = glGetUniformLocation(programId, uniformName);
                if (location != -1) {
                    uniforms.put(uniformName, location);
                } else {
                    return; // Uniform not found, skip silently
                }
            }
            
            FloatBuffer fb = stack.mallocFloat(16);
            value.get(fb);
            glUniformMatrix4fv(location, false, fb);
        }
    }
    
    public void setUniform(String uniformName, Vector3f value) {
        Integer location = uniforms.get(uniformName);
        if (location == null) {
            location = glGetUniformLocation(programId, uniformName);
            if (location != -1) {
                uniforms.put(uniformName, location);
            } else {
                return; // Uniform not found, skip silently
            }
        }
        
        glUniform3f(location, value.x, value.y, value.z);
    }
    
    public void setUniform(String uniformName, float value) {
        Integer location = uniforms.get(uniformName);
        if (location == null) {
            location = glGetUniformLocation(programId, uniformName);
            if (location != -1) {
                uniforms.put(uniformName, location);
            } else {
                return; // Uniform not found, skip silently
            }
        }
        
        glUniform1f(location, value);
    }
    
    public void setUniform(String uniformName, int value) {
        Integer location = uniforms.get(uniformName);
        if (location == null) {
            location = glGetUniformLocation(programId, uniformName);
            if (location != -1) {
                uniforms.put(uniformName, location);
            } else {
                return; // Uniform not found, skip silently
            }
        }
        
        glUniform1i(location, value);
    }
}
