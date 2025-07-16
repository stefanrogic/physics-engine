package com.stefanrogic.objects;

import org.joml.Vector3f;

public class Mercury {
    private Sphere sphere;
    private Vector3f position;
    private Vector3f color;
    private int VAO, VBO, EBO;
    
    // MERCURY DATA (SCALE: 1 UNIT = 10,000 KM) - REALISTIC DISTANCE
    private static final float MERCURY_RADIUS = 0.24f; // 2,440 KM ACTUAL RADIUS
    private static final float DISTANCE_FROM_SUN = 5800.0f; // 58 MILLION KM ACTUAL DISTANCE
    private static final int SPHERE_DETAIL = 16; // LOWER DETAIL FOR SMALL PLANET
    
    public Mercury() {
        this.position = new Vector3f(DISTANCE_FROM_SUN, 0.0f, 0.0f); // START AT X-AXIS POSITION
        this.color = new Vector3f(0.8f, 0.7f, 0.6f); // GRAYISH-BROWN MERCURY COLOR
        this.sphere = new Sphere(MERCURY_RADIUS, SPHERE_DETAIL, SPHERE_DETAIL);
        
        setupBuffers();
    }
    
    private void setupBuffers() {
        // THIS WILL BE CALLED FROM THE RENDERER TO SET UP OPENGL BUFFERS
        // WE'LL IMPLEMENT THIS IN THE RENDERER CLASS
    }
    
    public Vector3f getPosition() { return position; }
    public Vector3f getColor() { return color; }
    public Sphere getSphere() { return sphere; }
    public float getRadius() { return MERCURY_RADIUS; }
    public float getDistanceFromSun() { return DISTANCE_FROM_SUN; }
    
    // GETTERS FOR OPENGL BUFFER OBJECTS
    public int getVAO() { return VAO; }
    public int getVBO() { return VBO; }
    public int getEBO() { return EBO; }
    
    // SETTERS FOR BUFFER OBJECTS (TO BE SET BY RENDERER)
    public void setVAO(int VAO) { this.VAO = VAO; }
    public void setVBO(int VBO) { this.VBO = VBO; }
    public void setEBO(int EBO) { this.EBO = EBO; }
}
