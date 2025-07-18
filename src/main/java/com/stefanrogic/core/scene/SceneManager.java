package com.stefanrogic.core.scene;

import com.stefanrogic.assets.celestial.*;
import com.stefanrogic.assets.celestial.earth.*;
import com.stefanrogic.assets.celestial.mars.*;
import com.stefanrogic.assets.celestial.jupiter.Jupiter;
import org.joml.Vector3f;

/**
 * Manages all celestial objects in the solar system
 */
public class SceneManager {
    
    // SUN AND PLANETS
    private Sun sun;
    private Mercury mercury;
    private Venus venus;
    private Earth earth;
    private Moon moon;
    private Mars mars;
    private Phobos phobos;
    private Deimos deimos;
    private Jupiter jupiter;
    
    // TIME TRACKING FOR ORBITAL MOTION
    private long lastTime;
    private static final float TIME_ACCELERATION = 1.0f; // REAL TIME - NO ACCELERATION
    
    // State flags
    private boolean orbitalMotionPaused = false;
    
    public SceneManager() {
        // INITIALIZE TIME TRACKING FOR ORBITAL MOTION
        lastTime = System.nanoTime();
    }
    
    // Getters
    public Sun getSun() { return sun; }
    public Mercury getMercury() { return mercury; }
    public Venus getVenus() { return venus; }
    public Earth getEarth() { return earth; }
    public Moon getMoon() { return moon; }
    public Mars getMars() { return mars; }
    public Phobos getPhobos() { return phobos; }
    public Deimos getDeimos() { return deimos; }
    public Jupiter getJupiter() { return jupiter; }
    
    // Jupiter's moons
    public com.stefanrogic.assets.celestial.jupiter.Io getIo() { return jupiter != null ? jupiter.getIo() : null; }
    public com.stefanrogic.assets.celestial.jupiter.Europa getEuropa() { return jupiter != null ? jupiter.getEuropa() : null; }
    public com.stefanrogic.assets.celestial.jupiter.Ganymede getGanymede() { return jupiter != null ? jupiter.getGanymede() : null; }
    public com.stefanrogic.assets.celestial.jupiter.Callisto getCallisto() { return jupiter != null ? jupiter.getCallisto() : null; }
    
    public boolean isOrbitalMotionPaused() { return orbitalMotionPaused; }
    
    public void setOrbitalMotionPaused(boolean paused) {
        this.orbitalMotionPaused = paused;
        if (!paused) {
            // RESET TIME TRACKING WHEN RESUMING TO AVOID JUMPS
            lastTime = System.nanoTime();
        }
    }
    
    public void createCelestialObjects() {
        sun = new Sun();
        mercury = new Mercury();
        venus = new Venus();
        earth = new Earth();
        mars = new Mars(); 
        jupiter = new Jupiter();
        moon = new Moon(earth); // CREATE MOON AFTER EARTH
        phobos = new Phobos(mars); // CREATE PHOBOS AFTER MARS
        deimos = new Deimos(mars); // CREATE DEIMOS AFTER MARS
        
        // DEBUG: PRINT PLANETARY POSITIONS AT STARTUP
        System.out.println("=== PLANETARY POSITIONS ===");
        System.out.println("Sun: " + sun.getPosition().x + ", " + sun.getPosition().y + ", " + sun.getPosition().z);
        System.out.println("Mercury: " + mercury.getPosition().x + ", " + mercury.getPosition().y + ", " + mercury.getPosition().z + " (distance: " + mercury.getDistanceFromSun() + ")");
        System.out.println("Venus: " + venus.getPosition().x + ", " + venus.getPosition().y + ", " + venus.getPosition().z + " (distance: " + venus.getDistanceFromSun() + ")");
        System.out.println("Earth: " + earth.getPosition().x + ", " + earth.getPosition().y + ", " + earth.getPosition().z + " (distance: " + earth.getDistanceFromSun() + ")");
        System.out.println("Mars: " + mars.getPosition().x + ", " + mars.getPosition().y + ", " + mars.getPosition().z + " (distance: " + mars.getDistanceFromSun() + ")");
        System.out.println("Jupiter: " + jupiter.getPosition().x + ", " + jupiter.getPosition().y + ", " + jupiter.getPosition().z + " (distance: " + jupiter.getDistanceFromSun() + ")");
        System.out.println("========================");
    }
    
    public void updateOrbitalMotion() {
        if (!orbitalMotionPaused) {
            // CALCULATE DELTA TIME
            long currentTime = System.nanoTime();
            double deltaTimeSeconds = (currentTime - lastTime) / 1_000_000_000.0; // CONVERT TO SECONDS
            lastTime = currentTime;
            
            // APPLY TIME ACCELERATION
            deltaTimeSeconds *= TIME_ACCELERATION;
            
            // UPDATE PLANETARY POSITIONS BASED ON THEIR ORBITAL PERIODS
            mercury.updateOrbitalPosition((float) deltaTimeSeconds);
            venus.updateOrbitalPosition((float) deltaTimeSeconds);
            earth.updateOrbitalPosition((float) deltaTimeSeconds);
            
            // UPDATE MOON ORBIT AROUND EARTH (MOON ORBITS EARTH, NOT SUN)
            moon.updateOrbitalPosition((float) deltaTimeSeconds);
            
            // UPDATE MARS ORBIT
            mars.updateOrbitalPosition((float) deltaTimeSeconds);
            
            // UPDATE JUPITER ORBIT
            jupiter.updateOrbit((float) deltaTimeSeconds);
            jupiter.updateRotation((float) deltaTimeSeconds);
            
            // UPDATE MARS MOONS ORBIT AROUND MARS
            phobos.updateOrbitalPosition((float) deltaTimeSeconds);
            deimos.updateOrbitalPosition((float) deltaTimeSeconds);
        }
    }
    
    // Convenience methods for camera tracking
    public Vector3f getSunPosition() {
        return new Vector3f(0, 0, 0); // Sun is at origin
    }
    
    public Vector3f getMercuryPosition() {
        return mercury.getPosition();
    }
    
    public Vector3f getVenusPosition() {
        return venus.getPosition();
    }
    
    public Vector3f getEarthPosition() {
        return earth.getPosition();
    }
    
    public Vector3f getMarsPosition() {
        return mars.getPosition();
    }
}
