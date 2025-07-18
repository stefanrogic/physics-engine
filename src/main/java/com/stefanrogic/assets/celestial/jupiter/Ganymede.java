package com.stefanrogic.assets.celestial.jupiter;

import org.joml.Vector3f;
import com.stefanrogic.assets.Sphere;

public class Ganymede {
    private Sphere sphere;
    private Vector3f position;
    private Vector3f color;
    private int VAO;
    
    // GANYMEDE DATA (SCALE: 1 UNIT = 10,000 KM)
    private static final float GANYMEDE_RADIUS = 1.315f; // 2,634.1 KM ACTUAL RADIUS (LARGEST MOON IN SOLAR SYSTEM) (scaled up 5x for visibility)
    private static final float DISTANCE_FROM_JUPITER = 107.04f; // 1,070,400 KM ACTUAL DISTANCE
    private static final int SPHERE_DETAIL = 14; // HIGHER DETAIL FOR LARGEST MOON
    
    // ORBITAL DATA
    private static final float ORBITAL_PERIOD = 7.155f; // GANYMEDE'S ORBITAL PERIOD IN EARTH DAYS
    private static final float GANYMEDE_TIME_SCALE = 0.1f; // SLOWER SPEED
    private float currentAngle; // CURRENT ORBITAL ANGLE IN RADIANS
    
    // ROTATION DATA (GANYMEDE IS TIDALLY LOCKED)
    private float rotationAngle; // CURRENT ROTATION ANGLE IN RADIANS
    
    // REFERENCE TO JUPITER FOR ORBITAL CALCULATIONS
    private Jupiter jupiter;
    
    public Ganymede(Jupiter jupiter) {
        this.jupiter = jupiter;
        this.currentAngle = (float) Math.PI; // START AT 180 DEGREES
        updatePosition();
        this.color = new Vector3f(0.5f, 0.4f, 0.3f); // DARK BROWNISH-GRAY COLOR
        this.sphere = new Sphere(GANYMEDE_RADIUS, SPHERE_DETAIL, SPHERE_DETAIL);
        
        setupBuffers();
    }
    
    private void setupBuffers() {
        // THIS WILL BE CALLED FROM THE RENDERER TO SET UP OPENGL BUFFERS
    }
    
    public Vector3f getColor() { return color; }
    
    /**
     * Get enhanced Ganymede color with terrain variations
     */
    public Vector3f getEnhancedColor() {
        // SIMULATE GANYMEDE'S MIXED ICE AND ROCK SURFACE
        float variation = (float) Math.sin(currentAngle * 4.0f) * 0.08f;
        return new Vector3f(
            Math.max(0.4f, color.x + variation),
            Math.max(0.3f, color.y + variation * 0.8f),
            Math.max(0.2f, color.z + variation * 0.6f)
        );
    }
    
    public Vector3f getPosition() { return position; }
    public float getRadius() { return GANYMEDE_RADIUS; }
    public float getRotationAngle() { return rotationAngle; }
    public int getVAO() { return VAO; }
    public int getIndexCount() { return sphere.getIndices().length; }
    public Sphere getSphere() { return sphere; }
    
    /**
     * Update Ganymede's orbital position around Jupiter
     */
    public void updatePosition() {
        Vector3f jupiterPos = jupiter.getPosition();
        
        // CALCULATE ORBITAL POSITION
        float x = jupiterPos.x + DISTANCE_FROM_JUPITER * (float) Math.cos(currentAngle);
        float z = jupiterPos.z + DISTANCE_FROM_JUPITER * (float) Math.sin(currentAngle);
        
        this.position = new Vector3f(x, jupiterPos.y, z);
        
        // UPDATE ROTATION (TIDALLY LOCKED)
        this.rotationAngle = currentAngle;
    }
    
    /**
     * Update Ganymede's orbital motion
     */
    public void updateOrbit(float deltaTime) {
        if (deltaTime > 0) {
            float angularVelocity = (2.0f * (float) Math.PI) / ORBITAL_PERIOD;
            currentAngle += angularVelocity * deltaTime * GANYMEDE_TIME_SCALE;
            
            // KEEP ANGLE IN RANGE [0, 2π]
            if (currentAngle > 2.0f * Math.PI) {
                currentAngle -= 2.0f * (float) Math.PI;
            }
            
            updatePosition();
        }
    }
    
    public void setVAO(int vao) { this.VAO = vao; }
}
