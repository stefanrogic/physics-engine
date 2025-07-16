package com.stefanrogic;

import com.stefanrogic.core.Window;

public class Main {
    private static final int WINDOW_WIDTH = 1200;
    private static final int WINDOW_HEIGHT = 800;
    private static final String WINDOW_TITLE = "The Solar System";

    public static void main(String[] args) {
        System.out.println("Starting...");
        
        Window window = new Window();
        window.create(WINDOW_WIDTH, WINDOW_HEIGHT, WINDOW_TITLE);
        
        // MAIN LOOP
        while (!window.shouldClose()) {
            window.update();
        }
        
        window.destroy();
        System.out.println("Done!");
    }
}