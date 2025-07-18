package com.stefanrogic;

import com.stefanrogic.core.window.Window;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.GLFWVidMode;

public class Main {
    private static final String WINDOW_TITLE = "The Solar System";

    public static void main(String[] args) {
        System.out.println("Starting...");
        
        // Initialize GLFW
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        
        // Get the primary monitor
        long primaryMonitor = glfwGetPrimaryMonitor();
        if (primaryMonitor == 0) {
            glfwTerminate();
            throw new RuntimeException("Failed to get primary monitor");
        }
        
        // Get the video mode of the primary monitor
        GLFWVidMode videoMode = glfwGetVideoMode(primaryMonitor);
        if (videoMode == null) {
            glfwTerminate();
            throw new RuntimeException("Failed to get video mode");
        }
        
        int screenWidth = videoMode.width();
        int screenHeight = videoMode.height();
        int refreshRate = videoMode.refreshRate();
        
        System.out.println("Native resolution: " + screenWidth + "x" + screenHeight + " @ " + refreshRate + "Hz");
        
        // Configure GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_REFRESH_RATE, refreshRate);
        
        // Create fullscreen window at native resolution
        long windowHandle = glfwCreateWindow(screenWidth, screenHeight, WINDOW_TITLE, primaryMonitor, 0);
        if (windowHandle == 0) {
            glfwTerminate();
            throw new RuntimeException("Failed to create GLFW window");
        }
        
        // Make the OpenGL context current
        glfwMakeContextCurrent(windowHandle);
        glfwSwapInterval(1); // Enable v-sync
        
        // Create Window object with actual handle
        Window window = new Window(windowHandle);
        window.create();
        
        // MAIN LOOP
        while (!window.shouldClose()) {
            window.update();
        }
        
        window.destroy();
        System.out.println("Done!");
    }
}