package com.stefanrogic.assets.celestial.jupiter;

import org.joml.Vector3f;
import com.stefanrogic.assets.Sphere;

public class Callisto {
    private Sphere sphere;
    private Vector3f position;
    private Vector3f color;
    private int VAO;
    
    // CALLISTO DATA (SCALE: 1 UNIT = 10,000 KM)
    private static final float CALLISTO_RADIUS = 1.205f; // 2,410.3 KM ACTUAL RADIUS (scaled up 5x for visibility)
    private static final float DISTANCE_FROM_JUPITER = 188.2f; // 1,882,000 KM ACTUAL DISTANCE
    private static final int SPHERE_DETAIL = 12; // MEDIUM DETAIL FOR CALLISTO
    
    // ORBITAL DATA
    private static final float ORBITAL_PERIOD = 16.689f; // CALLISTO'S ORBITAL PERIOD IN EARTH DAYS
    private static final float CALLISTO_TIME_SCALE = 0.08f; // SLOWEST SPEED
    private float currentAngle; // CURRENT ORBITAL ANGLE IN RADIANS
    
    // ROTATION DATA (CALLISTO IS TIDALLY LOCKED)
    private float rotationAngle; // CURRENT ROTATION ANGLE IN RADIANS
    
    // REFERENCE TO JUPITER FOR ORBITAL CALCULATIONS
    private Jupiter jupiter;
    
    public Callisto(Jupiter jupiter) {
        this.jupiter = jupiter;
        this.currentAngle = 3.0f * (float) Math.PI / 2.0f; // START AT 270 DEGREES
        updatePosition();
        this.color = new Vector3f(0.3f, 0.3f, 0.35f); // DARK GRAYISH COLOR (HEAVILY CRATERED)
        this.sphere = new Sphere(CALLISTO_RADIUS, SPHERE_DETAIL, SPHERE_DETAIL);
        
        setupBuffers();
    }
    
    private void setupBuffers() {
        // THIS WILL BE CALLED FROM THE RENDERER TO SET UP OPENGL BUFFERS
    }
    
    public Vector3f getColor() { return color; }
    
    /**
     * Get enhanced Callisto color with crater variations
     */
    public Vector3f getEnhancedColor() {
        // SIMULATE CALLISTO'S HEAVILY CRATERED SURFACE
        float variation = (float) Math.sin(currentAngle * 10.0f) * 0.06f;
        return new Vector3f(
            Math.max(0.25f, color.x + variation),
            Math.max(0.25f, color.y + variation),
            Math.max(0.3f, color.z + variation)
        );
    }
    
    public Vector3f getPosition() { return position; }
    public float getRadius() { return CALLISTO_RADIUS; }
    public float getRotationAngle() { return rotationAngle; }
    public int getVAO() { return VAO; }
    public int getIndexCount() { return sphere.getIndices().length; }
    public Sphere getSphere() { return sphere; }
    
    /**
     * Update Callisto's orbital position around Jupiter
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
     * Update Callisto's orbital motion
     */
    public void updateOrbit(float deltaTime) {
        if (deltaTime > 0) {
            float angularVelocity = (2.0f * (float) Math.PI) / ORBITAL_PERIOD;
            currentAngle += angularVelocity * deltaTime * CALLISTO_TIME_SCALE;
            
            // KEEP ANGLE IN RANGE [0, 2π]
            if (currentAngle > 2.0f * Math.PI) {
                currentAngle -= 2.0f * (float) Math.PI;
            }
            
            updatePosition();
        }
    }
    
    public void setVAO(int vao) { this.VAO = vao; }
}
