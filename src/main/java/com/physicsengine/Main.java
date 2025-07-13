package com.physicsengine;

import com.physicsengine.core.PhysicsEngine;
import com.physicsengine.rendering.Window;
import com.physicsengine.rendering.Renderer;

/**
 * Main entry point for the Physics Simulator
 */
public class Main {
    
    private static final int WINDOW_WIDTH = 1200;
    private static final int WINDOW_HEIGHT = 800;
    private static final String WINDOW_TITLE = "Physics Engine";
    
    public static void main(String[] args) {
        System.out.println("Starting Physics Engine...");
        
        try {
            // Initialize window
            Window window = new Window(WINDOW_WIDTH, WINDOW_HEIGHT, WINDOW_TITLE);
            window.init();
            
            // Initialize renderer
            Renderer renderer = new Renderer();
            renderer.init();
            
            // Initialize physics engine
            PhysicsEngine physicsEngine = new PhysicsEngine();
            physicsEngine.init();
            
            // Main game loop
            while (!window.shouldClose()) {
                // Update physics
                physicsEngine.update(0.016f); // 60 FPS target
                
                // Render frame
                renderer.clear();
                physicsEngine.render(renderer);
                
                // Swap buffers and poll events
                window.update();
            }
            
            // Cleanup
            physicsEngine.cleanup();
            renderer.cleanup();
            window.cleanup();
            
        } catch (Exception e) {
            System.err.println("Error running physics engine: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("Physics Engine terminated.");
    }
}
