package com.stefanrogic;

import com.stefanrogic.core.window.Window;
import static org.lwjgl.glfw.GLFW.*;

public class Main {
    private static final int WINDOW_WIDTH = 1200;
    private static final int WINDOW_HEIGHT = 800;
    private static final String WINDOW_TITLE = "The Solar System";

    public static void main(String[] args) {
        System.out.println("Starting...");
        
        // Initialize GLFW
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        
        // Configure GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        
        // Create window
        long windowHandle = glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, WINDOW_TITLE, 0, 0);
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