package com.stefanrogic.assets.celestial.jupiter;

import org.joml.Vector3f;
import com.stefanrogic.assets.Sphere;

public class Europa {
    private Sphere sphere;
    private Vector3f position;
    private Vector3f color;
    private int VAO;
    
    // EUROPA DATA (SCALE: 1 UNIT = 10,000 KM)
    private static final float EUROPA_RADIUS = 0.78f; // 1,560.8 KM ACTUAL RADIUS (scaled up 5x for visibility)
    private static final float DISTANCE_FROM_JUPITER = 67.09f; // 670,900 KM ACTUAL DISTANCE
    private static final int SPHERE_DETAIL = 12; // MEDIUM DETAIL FOR EUROPA
    
    // ORBITAL DATA
    private static final float ORBITAL_PERIOD = 3.551f; // EUROPA'S ORBITAL PERIOD IN EARTH DAYS
    private static final float EUROPA_TIME_SCALE = 0.12f; // MODERATE SPEED
    private float currentAngle; // CURRENT ORBITAL ANGLE IN RADIANS
    
    // ROTATION DATA (EUROPA IS TIDALLY LOCKED)
    private float rotationAngle; // CURRENT ROTATION ANGLE IN RADIANS
    
    // REFERENCE TO JUPITER FOR ORBITAL CALCULATIONS
    private Jupiter jupiter;
    
    public Europa(Jupiter jupiter) {
        this.jupiter = jupiter;
        this.currentAngle = (float) Math.PI / 2.0f; // START AT 90 DEGREES
        updatePosition();
        this.color = new Vector3f(0.9f, 0.95f, 1.0f); // BLUISH-WHITE ICY COLOR
        this.sphere = new Sphere(EUROPA_RADIUS, SPHERE_DETAIL, SPHERE_DETAIL);
        
        setupBuffers();
    }
    
    private void setupBuffers() {
        // THIS WILL BE CALLED FROM THE RENDERER TO SET UP OPENGL BUFFERS
    }
    
    public Vector3f getColor() { return color; }
    
    /**
     * Get enhanced Europa color with ice variations
     */
    public Vector3f getEnhancedColor() {
        // SIMULATE EUROPA'S ICY SURFACE WITH CRACK PATTERNS
        float variation = (float) Math.sin(currentAngle * 6.0f) * 0.05f;
        return new Vector3f(
            Math.max(0.8f, color.x + variation),
            Math.max(0.85f, color.y + variation),
            Math.min(1.0f, color.z + variation * 0.3f)
        );
    }
    
    public Vector3f getPosition() { return position; }
    public float getRadius() { return EUROPA_RADIUS; }
    public float getRotationAngle() { return rotationAngle; }
    public int getVAO() { return VAO; }
    public int getIndexCount() { return sphere.getIndices().length; }
    public Sphere getSphere() { return sphere; }
    
    /**
     * Update Europa's orbital position around Jupiter
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
     * Update Europa's orbital motion
     */
    public void updateOrbit(float deltaTime) {
        if (deltaTime > 0) {
            float angularVelocity = (2.0f * (float) Math.PI) / ORBITAL_PERIOD;
            currentAngle += angularVelocity * deltaTime * EUROPA_TIME_SCALE;
            
            // KEEP ANGLE IN RANGE [0, 2π]
            if (currentAngle > 2.0f * Math.PI) {
                currentAngle -= 2.0f * (float) Math.PI;
            }
            
            updatePosition();
        }
    }
    
    public void setVAO(int vao) { this.VAO = vao; }
}
