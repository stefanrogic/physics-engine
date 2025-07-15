package com.physicsengine.core;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;

import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;


public class Window {
    private long windowHandle;

    public void create(int width, int height, String title) {
        // INIT
        if(!glfwInit()) 
            throw new RuntimeException("Failed to initialize");

        // CREATE WINDOW
        windowHandle = glfwCreateWindow(width, height, title, 0, 0);
        if(windowHandle == 0) 
            throw new RuntimeException("Failed to create window");

        // CENTER WINDOW
        GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(windowHandle, 
            (videoMode.width() - width) / 2, 
            (videoMode.height() - height) / 2);
        
        // OPENGL SETUP
        glfwMakeContextCurrent(windowHandle);
        GL.createCapabilities();
        
        // BG COLOR
        glClearColor(0.2f, 0.3f, 0.8f, 1.0f);
        
        // SHOW WINDOW
        glfwShowWindow(windowHandle);
        
        // ESC KEY TO CLOSE
        glfwSetKeyCallback(windowHandle, (window, key, _, action, _) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                glfwSetWindowShouldClose(window, true);
            }
        });
    }

    // CHECK IF WINDOW SHOULD CLOSE
    public boolean shouldClose() {
        return glfwWindowShouldClose(windowHandle);
    }
    
    // UPDATE WINDOW
    public void update() {
        glClear(GL_COLOR_BUFFER_BIT);
        glfwSwapBuffers(windowHandle);
        glfwPollEvents();
    }

    // DESTROY WINDOW
    public void destroy() {
        glfwDestroyWindow(windowHandle);
        glfwTerminate();
    }
}
