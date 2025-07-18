package com.stefanrogic.assets.celestial.jupiter;

import org.joml.Vector3f;
import com.stefanrogic.core.astronomy.AstronomicalCalculator;
import com.stefanrogic.assets.Sphere;
import com.stefanrogic.core.rendering.Model;
import com.stefanrogic.core.rendering.OBJLoader;
import com.stefanrogic.core.rendering.TextureLoader;

public class Jupiter {
    private Sphere sphere;
    private Model jupiterModel;
    private Vector3f position;
    private Vector3f color;
    private int VAO, VBO, EBO;
    private boolean useOBJModel = true; // Flag to switch between OBJ and procedural sphere
    
    // JUPITER'S MOONS (GALILEAN MOONS)
    private Io io;
    private Europa europa;
    private Ganymede ganymede;
    private Callisto callisto;
    
    // JUPITER DATA (SCALE: 1 UNIT = 10,000 KM)
    private static final float JUPITER_RADIUS = 6.991f; // 69,911 KM ACTUAL RADIUS (REALISTIC SCALE)
    private static final float DISTANCE_FROM_SUN = 77850.0f; // 778.5 MILLION KM ACTUAL DISTANCE (5.2 AU FROM SUN CENTER)
    private static final int SPHERE_DETAIL = 24; // HIGH DETAIL FOR GAS GIANT
    
    // ORBITAL DATA
    private static final float ORBITAL_PERIOD = 4333.0f; // JUPITER'S ORBITAL PERIOD IN EARTH DAYS (~11.86 YEARS)
    private float currentAngle; // CURRENT ORBITAL ANGLE IN RADIANS
    
    // ROTATION DATA
    private static final float ROTATION_PERIOD = 0.41f; // JUPITER'S ROTATION PERIOD IN EARTH DAYS (~9.9 HOURS)
    private float rotationAngle; // CURRENT ROTATION ANGLE IN RADIANS
    
    public Jupiter() {
        // JUPITER'S CURRENT POSITION BASED ON REAL-TIME ASTRONOMICAL DATA
        this.currentAngle = (float) AstronomicalCalculator.getCurrentOrbitalAngle("JUPITER");
        updatePosition();
        this.color = new Vector3f(0.8f, 0.7f, 0.4f); // ORANGE-BROWN COLOR FOR JUPITER
        
        // Load OBJ model if available, otherwise use procedural sphere
        try {
            System.out.println("Loading Jupiter OBJ model...");
            OBJLoader.ModelData modelData = OBJLoader.loadOBJ("models/jupiter_model.obj");
            
            // Load Jupiter texture from JPG file
            int jupiterTexture = TextureLoader.loadTextureFromResources("textures/Jupiter_diff.jpg");
            System.out.println("Successfully loaded Jupiter diffuse texture");
            
            this.jupiterModel = new Model(modelData, jupiterTexture);
            System.out.println("Successfully loaded Jupiter OBJ model with texture");
        } catch (Exception e) {
            System.err.println("Failed to load Jupiter OBJ model: " + e.getMessage());
            this.useOBJModel = false;
            this.sphere = new Sphere(JUPITER_RADIUS, SPHERE_DETAIL, SPHERE_DETAIL);
        }
        
        setupBuffers();
        
        // INITIALIZE JUPITER'S MOONS
        this.io = new Io(this);
        this.europa = new Europa(this);
        this.ganymede = new Ganymede(this);
        this.callisto = new Callisto(this);
        
        System.out.println("Created Jupiter's moons: Io(" + io.getRadius() + "), Europa(" + europa.getRadius() + "), Ganymede(" + ganymede.getRadius() + "), Callisto(" + callisto.getRadius() + ")");
    }
    
    private void setupBuffers() {
        // THIS WILL BE CALLED FROM THE RENDERER TO SET UP OPENGL BUFFERS
        // WE'LL IMPLEMENT THIS IN THE RENDERER CLASS
    }
    
    /**
     * Get Jupiter's distinctive orange-brown color
     */
    public Vector3f getColor() {
        return color;
    }
    
    /**
     * Get Jupiter's current position in 3D space
     */
    public Vector3f getPosition() {
        return position;
    }
    
    /**
     * Get Jupiter's radius for collision detection and rendering
     */
    public float getRadius() { return JUPITER_RADIUS; }
    
    /**
     * Get Jupiter's moons
     */
    public Io getIo() { return io; }
    public Europa getEuropa() { return europa; }
    public Ganymede getGanymede() { return ganymede; }
    public Callisto getCallisto() { return callisto; }
    
    /**
     * Get Jupiter's distance from the Sun for orbital calculations
     */
    public float getDistanceFromSun() { return DISTANCE_FROM_SUN; }
    
    /**
     * Get Jupiter's orbital period for animation timing
     */
    public float getOrbitalPeriod() { return ORBITAL_PERIOD; }
    
    /**
     * Get Jupiter's rotation period for spin animation
     */
    public float getRotationPeriod() { return ROTATION_PERIOD; }
    
    /**
     * Get the current rotation angle for rendering
     */
    public float getRotationAngle() { return rotationAngle; }
    
    /**
     * Get the sphere object for procedural rendering
     */
    public Sphere getSphere() { return sphere; }
    
    /**
     * Get the OBJ model for high-quality rendering
     */
    public Model getModel() { return jupiterModel; }
    
    /**
     * Check if using OBJ model or procedural sphere
     */
    public boolean isUsingOBJModel() { return useOBJModel && jupiterModel != null; }
    
    /**
     * Update Jupiter's orbital position based on time
     */
    public void updateOrbit(float deltaTime) {
        // CALCULATE ORBITAL SPEED (RADIANS PER SECOND)
        float orbitalSpeed = (float) (2 * Math.PI / (ORBITAL_PERIOD * 24 * 3600)); // REAL-TIME SPEED
        
        // UPDATE ORBITAL ANGLE
        currentAngle += orbitalSpeed * deltaTime;
        
        // KEEP ANGLE IN RANGE [0, 2π]
        if (currentAngle > 2 * Math.PI) {
            currentAngle -= 2 * Math.PI;
        }
        
        // UPDATE POSITION
        updatePosition();
        
        // UPDATE JUPITER'S MOONS
        io.updateOrbit(deltaTime);
        europa.updateOrbit(deltaTime);
        ganymede.updateOrbit(deltaTime);
        callisto.updateOrbit(deltaTime);
    }
    
    /**
     * Update Jupiter's rotation angle based on time
     */
    public void updateRotation(float deltaTime) {
        // CALCULATE ROTATION SPEED (RADIANS PER SECOND)
        float rotationSpeed = (float) (2 * Math.PI / (ROTATION_PERIOD * 24 * 3600)); // REAL-TIME SPEED
        
        // UPDATE ROTATION ANGLE
        rotationAngle += rotationSpeed * deltaTime;
        
        // KEEP ANGLE IN RANGE [0, 2π]
        if (rotationAngle > 2 * Math.PI) {
            rotationAngle -= 2 * Math.PI;
        }
    }
    
    /**
     * Calculate Jupiter's position based on current orbital angle
     */
    private void updatePosition() {
        // CIRCULAR ORBIT AROUND SUN (SUN IS AT ORIGIN)
        float x = (float) (DISTANCE_FROM_SUN * Math.cos(currentAngle));
        float z = (float) (DISTANCE_FROM_SUN * Math.sin(currentAngle));
        this.position = new Vector3f(x, 0, z);
    }
    
    /**
     * Get OpenGL buffer objects for direct rendering
     */
    public int getVAO() { return VAO; }
    public int getVBO() { return VBO; }
    public int getEBO() { return EBO; }
}
