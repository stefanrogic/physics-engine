package com.stefanrogic.assets.celestial;

import org.joml.Vector3f;
import com.stefanrogic.assets.Sphere;
import com.stefanrogic.core.rendering.Model;
import com.stefanrogic.core.rendering.OBJLoader;
import com.stefanrogic.core.rendering.TextureLoader;

public class Sun {
    private Sphere sphere;
    private Model sunModel;
    private Vector3f position;
    private Vector3f color;
    private int VAO, VBO, EBO;
    private boolean useOBJModel = true; // Flag to switch between OBJ and procedural sphere
    
    // SCALE: 1 UNIT = 10,000 KM (SO SUN RADIUS OF ~69.6 UNITS REPRESENTS ~696,000 KM ACTUAL RADIUS)
    private static final float SUN_RADIUS = 69.6f; // REAL ASTRONOMICAL SCALE
    private static final int SPHERE_DETAIL = 32; // GOOD BALANCE OF DETAIL VS PERFORMANCE
    
    // ROTATION DATA
    private static final float ROTATION_PERIOD = 27.0f; // SUN'S ROTATION PERIOD IN EARTH DAYS (AT EQUATOR)
    private float rotationAngle; // CURRENT ROTATION ANGLE IN RADIANS
    
    public Sun() {
        this.position = new Vector3f(0.0f, 0.0f, 0.0f); // CENTER OF THE WORLD
        this.color = new Vector3f(1.0f, 0.8f, 0.0f); // YELLOW-ORANGE SUN COLOR
        this.rotationAngle = 0.0f; // START WITH NO ROTATION
        
        // Load OBJ model if available, otherwise use procedural sphere
        try {
            System.out.println("Loading Sun OBJ model...");
            OBJLoader.ModelData modelData = OBJLoader.loadOBJ("models/sun_model.obj");
            
            // Load Sun texture from JPG file
            int sunTexture = TextureLoader.loadTextureFromResources("textures/2k_sun.jpg");
            System.out.println("Successfully loaded Sun diffuse texture");
            
            this.sunModel = new Model(modelData, sunTexture);
            System.out.println("Successfully loaded Sun OBJ model with texture");
        } catch (Exception e) {
            System.err.println("Failed to load Sun OBJ model: " + e.getMessage());
            this.useOBJModel = false;
            this.sphere = new Sphere(SUN_RADIUS, SPHERE_DETAIL, SPHERE_DETAIL);
        }
        
        setupBuffers();
    }
    
    private void setupBuffers() {
        // THIS WILL BE CALLED FROM THE RENDERER TO SET UP OPENGL BUFFERS
        // WE'LL IMPLEMENT THIS IN THE RENDERER CLASS
    }
    
    public Vector3f getPosition() { return position; }
    public Vector3f getColor() { return color; }
    public Sphere getSphere() { return sphere; }
    public float getRadius() { return SUN_RADIUS; }
    
    // OBJ MODEL GETTERS
    public boolean isUsingOBJModel() { return useOBJModel; }
    public Model getSunModel() { return sunModel; }
    
    // GETTERS FOR OPENGL BUFFER OBJECTS
    public int getVAO() { return VAO; }
    public int getVBO() { return VBO; }
    public int getEBO() { return EBO; }
    
    // SETTERS FOR BUFFER OBJECTS (TO BE SET BY RENDERER)
    public void setVAO(int VAO) { this.VAO = VAO; }
    public void setVBO(int VBO) { this.VBO = VBO; }
    public void setEBO(int EBO) { this.EBO = EBO; }
    
    // UPDATE SUN'S ROTATION BASED ON TIME
    public void updateRotation(float deltaTime) {
        // CALCULATE ROTATIONAL ANGULAR VELOCITY (RADIANS PER SECOND)
        float rotationalAngularVelocity = (float) (2.0 * Math.PI) / (ROTATION_PERIOD * 24.0f * 3600.0f); // CONVERT DAYS TO SECONDS
        
        // UPDATE ROTATION ANGLE
        rotationAngle += rotationalAngularVelocity * deltaTime;
        
        // KEEP ANGLE IN 0-2π RANGE
        if (rotationAngle > 2.0 * Math.PI) {
            rotationAngle -= (float) (2.0 * Math.PI);
        }
    }
    
    // GETTERS FOR ROTATION DATA
    public float getRotationAngle() { return rotationAngle; }
}
