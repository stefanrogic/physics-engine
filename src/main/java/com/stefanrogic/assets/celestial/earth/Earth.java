package com.stefanrogic.assets.celestial.earth;

import org.joml.Vector3f;
import com.stefanrogic.core.astronomy.AstronomicalCalculator;
import com.stefanrogic.assets.Sphere;
import com.stefanrogic.core.rendering.Model;
import com.stefanrogic.core.rendering.OBJLoader;
import com.stefanrogic.core.rendering.TextureLoader;

public class Earth {
    private Sphere sphere;
    private Model earthModel;
    private Vector3f position;
    private Vector3f oceanColor;
    private Vector3f landColor;
    private Vector3f iceColor;
    private Vector3f mountainColor;
    private int VAO, VBO, EBO;
    private boolean useOBJModel = true; // Flag to switch between OBJ and procedural sphere
    private int cloudsTextureId = 0; // Store clouds texture separately
    private int bumpTextureId = 0; // Store bump texture separately
    private int nightLightsTextureId = 0; // Store night lights texture separately
    
    // EARTH DATA (SCALE: 1 UNIT = 10,000 KM)
    private static final float EARTH_RADIUS = 0.637f; // 6,371 KM ACTUAL RADIUS (REALISTIC SCALE)
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
        this.oceanColor = new Vector3f(0.02f, 0.15f, 0.6f); // DEEPER BLUE OCEANS
        this.landColor = new Vector3f(0.05f, 0.5f, 0.05f); // RICHER GREEN CONTINENTS
        this.iceColor = new Vector3f(0.95f, 0.98f, 1.0f); // BRIGHT WHITE ICE CAPS
        this.mountainColor = new Vector3f(0.3f, 0.25f, 0.15f); // BROWNISH MOUNTAINS
        
        // Load OBJ model if available, otherwise use procedural sphere
        try {
            System.out.println("Loading Earth OBJ model...");
            OBJLoader.ModelData modelData = OBJLoader.loadOBJ("models/earth_model.obj");
            
            // Load diffuse texture from PNG file
            int earthTexture = TextureLoader.loadTextureFromResources("textures/Diffuse_2K.png");
            System.out.println("Successfully loaded Earth diffuse texture");
            
            // Load clouds texture
            try {
                cloudsTextureId = TextureLoader.loadTextureFromResources("textures/Clouds_2K.png");
                System.out.println("Successfully loaded clouds texture");
            } catch (Exception e) {
                System.err.println("Failed to load clouds texture: " + e.getMessage());
                cloudsTextureId = 0;
            }
            
            // Load bump texture
            try {
                bumpTextureId = TextureLoader.loadTextureFromResources("textures/Bump_2K.png");
                System.out.println("Successfully loaded bump texture");
            } catch (Exception e) {
                System.err.println("Failed to load bump texture: " + e.getMessage());
                bumpTextureId = 0;
            }
            
            // Load night lights texture
            try {
                nightLightsTextureId = TextureLoader.loadTextureFromResources("textures/Night_lights_2K.png");
                System.out.println("Successfully loaded night lights texture");
            } catch (Exception e) {
                System.err.println("Failed to load night lights texture: " + e.getMessage());
                nightLightsTextureId = 0;
            }
            
            this.earthModel = new Model(modelData, earthTexture);
            System.out.println("Successfully loaded Earth OBJ model with PNG texture");
        } catch (Exception e) {
            System.err.println("Failed to load Earth OBJ model: " + e.getMessage());
            this.useOBJModel = false;
            this.sphere = new Sphere(EARTH_RADIUS, SPHERE_DETAIL, SPHERE_DETAIL);
        }
        
        setupBuffers();
    }
    
    private void setupBuffers() {
        // THIS WILL BE CALLED FROM THE RENDERER TO SET UP OPENGL BUFFERS
        // WE'LL IMPLEMENT THIS IN THE RENDERER CLASS
    }
    
    // FOR NOW, WE'LL USE A BLEND OF OCEAN AND LAND COLORS
    // LATER WE CAN IMPLEMENT A MORE COMPLEX SHADER FOR CONTINENTS
    /**
     * Get base Earth color with atmospheric effect
     */
    public Vector3f getColor() { 
        // EARTH WITH ATMOSPHERIC BLUE GLOW
        return new Vector3f(0.15f, 0.35f, 0.65f); // MORE REALISTIC EARTH BLUE
    }
    
    /**
     * Get enhanced Earth color that simulates continent/ocean variations
     */
    public Vector3f getEnhancedColor() {
        // BLEND OCEAN, LAND, AND ICE COLORS FOR REALISTIC APPEARANCE
        return new Vector3f(
            oceanColor.x * 0.65f + landColor.x * 0.25f + iceColor.x * 0.1f,
            oceanColor.y * 0.65f + landColor.y * 0.25f + iceColor.y * 0.1f,
            oceanColor.z * 0.65f + landColor.z * 0.25f + iceColor.z * 0.1f
        );
    }
    
    /**
     * GET COLOR BASED ON SPHERICAL COORDINATES TO SIMULATE CONTINENTS
     * This method creates a simple continent pattern based on latitude and longitude
     */
    public Vector3f getColorAtPosition(float latitude, float longitude) {
        // NORMALIZE LATITUDE AND LONGITUDE TO [0, 1] RANGE
        float lat = (latitude + (float)Math.PI/2) / (float)Math.PI; // 0 to 1 (south to north)
        float lon = (longitude + (float)Math.PI) / (2*(float)Math.PI); // 0 to 1 (west to east)
        
        // ICE CAPS AT POLES
        if (lat < 0.1f || lat > 0.9f) {
            return iceColor; // WHITE ICE CAPS
        }
        
        // SIMPLE CONTINENT PATTERN - APPROXIMATING EARTH'S LANDMASSES
        boolean isLand = false;
        
        // AFRICA AND EUROPE (longitude 0.0-0.25, latitude 0.3-0.8)
        if (lon >= 0.0f && lon <= 0.25f && lat >= 0.3f && lat <= 0.8f) {
            isLand = true;
        }
        // ASIA (longitude 0.2-0.7, latitude 0.4-0.85)
        else if (lon >= 0.2f && lon <= 0.7f && lat >= 0.4f && lat <= 0.85f) {
            isLand = true;
        }
        // NORTH AMERICA (longitude 0.75-1.0, latitude 0.5-0.85)
        else if (lon >= 0.75f && lat >= 0.5f && lat <= 0.85f) {
            isLand = true;
        }
        // SOUTH AMERICA (longitude 0.8-0.95, latitude 0.15-0.55)
        else if (lon >= 0.8f && lon <= 0.95f && lat >= 0.15f && lat <= 0.55f) {
            isLand = true;
        }
        // AUSTRALIA (longitude 0.6-0.75, latitude 0.15-0.35)
        else if (lon >= 0.6f && lon <= 0.75f && lat >= 0.15f && lat <= 0.35f) {
            isLand = true;
        }
        
        // ADD SOME NOISE FOR MORE REALISTIC COASTLINES
        float noise = (float)(Math.sin(lat * 20) * Math.cos(lon * 30) * 0.1);
        
        if (isLand) {
            // VARY LAND COLOR BASED ON LATITUDE (GREENER NEAR EQUATOR, BROWNER NEAR POLES)
            float greenness = 1.0f - Math.abs(lat - 0.5f) * 2.0f; // MORE GREEN NEAR EQUATOR
            return new Vector3f(
                landColor.x + (mountainColor.x - landColor.x) * (1.0f - greenness) + noise,
                landColor.y * greenness + noise,
                landColor.z + noise
            );
        } else {
            // OCEAN COLOR WITH SLIGHT DEPTH VARIATION
            float depth = 0.8f + noise * 0.2f;
            return new Vector3f(
                oceanColor.x * depth,
                oceanColor.y * depth,
                oceanColor.z
            );
        }
    }
    
    public Vector3f getOceanColor() { return oceanColor; }
    public Vector3f getLandColor() { return landColor; }
    public Vector3f getIceColor() { return iceColor; }
    public Vector3f getMountainColor() { return mountainColor; }
    
    public Vector3f getPosition() { return position; }
    public Sphere getSphere() { return sphere; }
    public Model getModel() { return earthModel; }
    public boolean isUsingOBJModel() { return useOBJModel; }
    public int getCloudsTextureId() { return cloudsTextureId; }
    public int getBumpTextureId() { return bumpTextureId; }
    public int getNightLightsTextureId() { return nightLightsTextureId; }
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
    public float getRotationAngle() { return rotationAngle; }
    
    // CLEANUP METHOD
    public void cleanup() {
        if (earthModel != null) {
            earthModel.cleanup();
        }
    }
}
