package com.stefanrogic.objects;

import org.joml.Vector3f;

public class Earth {
    private Sphere sphere;
    private Vector3f position;
    private Vector3f oceanColor;
    private Vector3f landColor;
    private int VAO, VBO, EBO;
    
    // EARTH DATA (SCALE: 1 UNIT = 10,000 KM)
    private static final float EARTH_RADIUS = 1.5f; // INCREASED FOR BETTER VISIBILITY (ORIGINALLY 0.637f)
    private static final float DISTANCE_FROM_SUN = 14960.0f; // 149.6 MILLION KM ACTUAL DISTANCE (1 AU)
    private static final int SPHERE_DETAIL = 24; // HIGHEST DETAIL FOR HOME PLANET
    
    // ORBITAL DATA
    private static final float ORBITAL_PERIOD = 365.25f; // EARTH'S ORBITAL PERIOD IN EARTH DAYS
    private float currentAngle; // CURRENT ORBITAL ANGLE IN RADIANS
    
    // ROTATION DATA
    private static final float ROTATION_PERIOD = 1.0f; // EARTH'S ROTATION PERIOD IN EARTH DAYS (24 HOURS)
    private float rotationAngle; // CURRENT ROTATION ANGLE IN RADIANS
    
    public Earth() {
        // EARTH'S CURRENT POSITION BASED ON REAL-TIME ASTRONOMICAL DATA
        this.currentAngle = (float) AstronomicalCalculator.getCurrentOrbitalAngle("EARTH");
        updatePosition();
        this.oceanColor = new Vector3f(0.1f, 0.3f, 0.8f); // DEEP BLUE OCEANS
        this.landColor = new Vector3f(0.2f, 0.6f, 0.2f); // GREEN CONTINENTS
        this.sphere = new Sphere(EARTH_RADIUS, SPHERE_DETAIL, SPHERE_DETAIL);
        
        setupBuffers();
    }
    
    private void setupBuffers() {
        // THIS WILL BE CALLED FROM THE RENDERER TO SET UP OPENGL BUFFERS
        // WE'LL IMPLEMENT THIS IN THE RENDERER CLASS
    }
    
    // FOR NOW, WE'LL USE A BLEND OF OCEAN AND LAND COLORS
    // LATER WE CAN IMPLEMENT A MORE COMPLEX SHADER FOR CONTINENTS
    public Vector3f getColor() { 
        // EARTH APPEARS AS A BLUE-GREEN BLEND FROM DISTANCE
        return new Vector3f(0.2f, 0.4f, 0.7f); // EARTH BLUE-GREEN BLEND
    }
    public Vector3f getOceanColor() { return oceanColor; }
    public Vector3f getLandColor() { return landColor; }
    
    public Vector3f getPosition() { return position; }
    public Sphere getSphere() { return sphere; }
    public float getRadius() { return EARTH_RADIUS; }
    public float getDistanceFromSun() { return DISTANCE_FROM_SUN; }
    
    // GETTERS FOR OPENGL BUFFER OBJECTS
    public int getVAO() { return VAO; }
    public int getVBO() { return VBO; }
    public int getEBO() { return EBO; }
    
    // SETTERS FOR BUFFER OBJECTS (TO BE SET BY RENDERER)
    public void setVAO(int VAO) { this.VAO = VAO; }
    public void setVBO(int VBO) { this.VBO = VBO; }
    public void setEBO(int EBO) { this.EBO = EBO; }
    
    // UPDATE ORBITAL POSITION AND ROTATION BASED ON TIME
    public void updateOrbitalPosition(float deltaTime) {
        // CALCULATE ORBITAL ANGULAR VELOCITY (RADIANS PER SECOND)
        float orbitalAngularVelocity = (float) (2.0 * Math.PI) / (ORBITAL_PERIOD * 24.0f * 3600.0f); // CONVERT DAYS TO SECONDS
        
        // CALCULATE ROTATIONAL ANGULAR VELOCITY (RADIANS PER SECOND)
        float rotationalAngularVelocity = (float) (2.0 * Math.PI) / (ROTATION_PERIOD * 24.0f * 3600.0f); // CONVERT DAYS TO SECONDS
        
        // UPDATE ORBITAL ANGLE
        currentAngle += orbitalAngularVelocity * deltaTime;
        
        // UPDATE ROTATION ANGLE (EARTH ROTATES FAST - 24 HOURS)
        rotationAngle += rotationalAngularVelocity * deltaTime;
        
        // KEEP ANGLES IN 0-2π RANGE
        if (currentAngle > 2.0 * Math.PI) {
            currentAngle -= (float) (2.0 * Math.PI);
        }
        if (rotationAngle > 2.0 * Math.PI) {
            rotationAngle -= (float) (2.0 * Math.PI);
        }
        
        updatePosition();
    }
    
    // CALCULATE POSITION FROM CURRENT ANGLE
    private void updatePosition() {
        float x = (float) (DISTANCE_FROM_SUN * Math.cos(currentAngle));
        float z = (float) (DISTANCE_FROM_SUN * Math.sin(currentAngle));
        this.position = new Vector3f(x, 0.0f, z);
    }
    
    // GETTERS FOR ORBITAL DATA
    public float getOrbitalPeriod() { return ORBITAL_PERIOD; }
    public float getCurrentAngle() { return currentAngle; }
    public float getRotationPeriod() { return ROTATION_PERIOD; }
    public float getRotationAngle() { return rotationAngle; }
}
