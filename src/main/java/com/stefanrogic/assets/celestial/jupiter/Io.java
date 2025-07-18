package com.stefanrogic.assets.celestial.jupiter;

import org.joml.Vector3f;
import com.stefanrogic.assets.Sphere;

public class Io {
    private Sphere sphere;
    private Vector3f position;
    private Vector3f color;
    private int VAO;
    
    // IO DATA (SCALE: 1 UNIT = 10,000 KM)
    private static final float IO_RADIUS = 0.91f; // 1,821.6 KM ACTUAL RADIUS (scaled up 5x for visibility)
    private static final float DISTANCE_FROM_JUPITER = 42.16f; // 421,600 KM ACTUAL DISTANCE
    private static final int SPHERE_DETAIL = 12; // MEDIUM DETAIL FOR IO
    
    // ORBITAL DATA
    private static final float ORBITAL_PERIOD = 1.769f; // IO'S ORBITAL PERIOD IN EARTH DAYS
    private static final float IO_TIME_SCALE = 0.15f; // SLIGHTLY FASTER THAN EARTH'S MOON
    private float currentAngle; // CURRENT ORBITAL ANGLE IN RADIANS
    
    // ROTATION DATA (IO IS TIDALLY LOCKED)
    private float rotationAngle; // CURRENT ROTATION ANGLE IN RADIANS
    
    // REFERENCE TO JUPITER FOR ORBITAL CALCULATIONS
    private Jupiter jupiter;
    
    public Io(Jupiter jupiter) {
        this.jupiter = jupiter;
        this.currentAngle = 0.0f; // START AT 0 DEGREES
        updatePosition();
        this.color = new Vector3f(1.0f, 0.9f, 0.4f); // YELLOWISH COLOR DUE TO SULFUR
        this.sphere = new Sphere(IO_RADIUS, SPHERE_DETAIL, SPHERE_DETAIL);
        
        setupBuffers();
    }
    
    private void setupBuffers() {
        // THIS WILL BE CALLED FROM THE RENDERER TO SET UP OPENGL BUFFERS
    }
    
    public Vector3f getColor() { return color; }
    
    /**
     * Get enhanced Io color with volcanic activity
     */
    public Vector3f getEnhancedColor() {
        // SIMULATE IO'S VOLCANIC SURFACE WITH SULFUR DEPOSITS
        float variation = (float) Math.sin(currentAngle * 8.0f) * 0.1f;
        return new Vector3f(
            Math.min(1.0f, color.x + variation),
            Math.min(1.0f, color.y + variation * 0.5f),
            Math.max(0.2f, color.z - variation * 0.3f)
        );
    }
    
    public Vector3f getPosition() { return position; }
    public float getRadius() { return IO_RADIUS; }
    public float getRotationAngle() { return rotationAngle; }
    public int getVAO() { return VAO; }
    public int getIndexCount() { return sphere.getIndices().length; }
    public Sphere getSphere() { return sphere; }
    
    /**
     * Update Io's orbital position around Jupiter
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
     * Update Io's orbital motion
     */
    public void updateOrbit(float deltaTime) {
        if (deltaTime > 0) {
            float angularVelocity = (2.0f * (float) Math.PI) / ORBITAL_PERIOD;
            currentAngle += angularVelocity * deltaTime * IO_TIME_SCALE;
            
            // KEEP ANGLE IN RANGE [0, 2π]
            if (currentAngle > 2.0f * Math.PI) {
                currentAngle -= 2.0f * (float) Math.PI;
            }
            
            updatePosition();
        }
    }
    
    public void setVAO(int vao) { this.VAO = vao; }
}
