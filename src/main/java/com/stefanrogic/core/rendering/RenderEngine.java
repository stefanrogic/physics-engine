package com.stefanrogic.core.rendering;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.stefanrogic.assets.celestial.Mercury;
import com.stefanrogic.assets.celestial.Venus;
import com.stefanrogic.assets.celestial.Sun;
import com.stefanrogic.assets.celestial.jupiter.Jupiter;
import com.stefanrogic.assets.celestial.earth.Earth;
import com.stefanrogic.assets.celestial.earth.Moon;
import com.stefanrogic.assets.celestial.mars.Mars;
import com.stefanrogic.assets.celestial.mars.Phobos;
import com.stefanrogic.assets.celestial.mars.Deimos;
import com.stefanrogic.core.scene.SceneManager;
import com.stefanrogic.core.input.Camera;

/**
 * Manages rendering of celestial objects
 */
public class RenderEngine {
    
    private SceneManager sceneManager;
    private ShaderManager.ShaderPrograms shaders;
    private Camera camera;
    private GridRenderer gridRenderer;
    private StarRenderer starRenderer;
    
    public RenderEngine(SceneManager sceneManager, ShaderManager.ShaderPrograms shaders, Camera camera) {
        this.sceneManager = sceneManager;
        this.shaders = shaders;
        this.camera = camera;
        this.gridRenderer = new GridRenderer(shaders);
        this.starRenderer = new StarRenderer(shaders);
    }
    
    /**
     * Initialize the grid through GridRenderer
     */
    public void createGrid() {
        gridRenderer.createGrid();
    }
    
    /**
     * Initialize the star field through StarRenderer
     */
    public void createStars() {
        starRenderer.createStars();
    }
    
    /**
     * Render the grid if visible
     */
    public void renderGrid(Matrix4f mvpMatrix, boolean gridVisible) {
        gridRenderer.renderGrid(mvpMatrix, gridVisible);
    }
    
    /**
     * Render the star field in the background
     */
    public void renderStars(Matrix4f mvpMatrix) {
        // Render stars first so they appear behind everything
        starRenderer.renderStars(mvpMatrix);
    }
    
    /**
     * Render all celestial objects
     */
    public void renderCelestialObjects(Matrix4f projection, Matrix4f view, int windowWidth, int windowHeight) {
        // RENDER SUN
        renderSun(projection, view);
        
        // RENDER PLANETS
        renderMercury(projection, view);
        renderVenus(projection, view);
        renderEarth(projection, view);
        renderMoon(projection, view);
        renderMars(projection, view);
        renderPhobos(projection, view);
        renderDeimos(projection, view);
        renderJupiter(projection, view);
        
        // RENDER JUPITER'S MOONS
        renderIo(projection, view);
        renderEuropa(projection, view);
        renderGanymede(projection, view);
        renderCallisto(projection, view);
    }
    
    private void renderSun(Matrix4f projection, Matrix4f view) {
        Sun sun = sceneManager.getSun();
        
        if (sun.isUsingOBJModel()) {
            renderSunWithOBJModel(sun, projection, view);
        } else {
            renderSunWithProcedural(sun, projection, view);
        }
    }
    
    /**
     * Render Sun with OBJ model
     */
    private void renderSunWithOBJModel(Sun sun, Matrix4f projection, Matrix4f view) {
        // Use sun shader program
        glUseProgram(shaders.sunShaderProgram);
        
        // Set MVP matrix
        Matrix4f mvp = new Matrix4f();
        Matrix4f sunModel = new Matrix4f();
        sunModel.rotateY(sun.getRotationAngle()); // ROTATE AROUND Y-AXIS
        
        // Scale the Sun model to match its actual radius
        // Sun OBJ model has radius of about 20,000 units, but we want 69.6 units
        float scale = sun.getRadius() / 20000.0f; // Scale from OBJ model size to intended size
        sunModel.scale(scale);
        
        mvp.mul(projection).mul(view).mul(sunModel);
        
        // Set uniforms
        int mvpLoc = glGetUniformLocation(shaders.sunShaderProgram, "mvpMatrix");
        glUniformMatrix4fv(mvpLoc, false, mvp.get(new float[16]));
        
        int sunColorLoc = glGetUniformLocation(shaders.sunShaderProgram, "sunColor");
        glUniform3f(sunColorLoc, sun.getColor().x, sun.getColor().y, sun.getColor().z);
        
        // Bind the Sun's texture
        if (sun.getSunModel() != null && sun.getSunModel().getTextureId() != 0) {
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, sun.getSunModel().getTextureId());
            int texLoc = glGetUniformLocation(shaders.sunShaderProgram, "diffuseTexture");
            glUniform1i(texLoc, 0);
        }
        
        // Render the OBJ model
        if (sun.getSunModel() != null) {
            sun.getSunModel().render();
        }
    }
    
    /**
     * Render Sun with procedural sphere (fallback)
     */
    private void renderSunWithProcedural(Sun sun, Matrix4f projection, Matrix4f view) {
        glUseProgram(shaders.sunShaderProgram);
        
        // CREATE SUN TRANSFORMATION MATRIX WITH ROTATION
        Matrix4f sunModel = new Matrix4f();
        sunModel.rotateY(sun.getRotationAngle()); // ROTATE AROUND Y-AXIS
        
        Matrix4f sunMVP = new Matrix4f();
        projection.mul(view, sunMVP);
        sunMVP.mul(sunModel);
        
        FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
        sunMVP.get(matrixBuffer);
        glUniformMatrix4fv(shaders.sunMvpLocation, false, matrixBuffer);
        glUniform3f(shaders.sunColorLocation, sun.getColor().x, sun.getColor().y, sun.getColor().z);
        
        glBindVertexArray(sun.getVAO());
        glDrawElements(GL_TRIANGLES, sun.getSphere().getIndices().length, GL_UNSIGNED_INT, 0);
    }
    
    private void renderMercury(Matrix4f projection, Matrix4f view) {
        renderPlanet(sceneManager.getMercury(), projection, view);
    }
    
    private void renderVenus(Matrix4f projection, Matrix4f view) {
        renderPlanet(sceneManager.getVenus(), projection, view);
    }
    
    private void renderEarth(Matrix4f projection, Matrix4f view) {
        Earth earth = sceneManager.getEarth();
        
        if (earth.isUsingOBJModel()) {
            renderEarthWithOBJModel(earth, projection, view);
        } else {
            renderEarthWithSurface(earth, projection, view);
        }
    }
    
    private void renderMoon(Matrix4f projection, Matrix4f view) {
        renderMoonWithCraters(sceneManager.getMoon(), projection, view);
    }
    
    private void renderMars(Matrix4f projection, Matrix4f view) {
        renderPlanet(sceneManager.getMars(), projection, view);
    }
    
    private void renderPhobos(Matrix4f projection, Matrix4f view) {
        renderPlanet(sceneManager.getPhobos(), projection, view);
    }
    
    private void renderDeimos(Matrix4f projection, Matrix4f view) {
        renderPlanet(sceneManager.getDeimos(), projection, view);
    }
    
    private void renderJupiter(Matrix4f projection, Matrix4f view) {
        Jupiter jupiter = sceneManager.getJupiter();
        
        if (jupiter.isUsingOBJModel()) {
            renderJupiterWithOBJModel(jupiter, projection, view);
        } else {
            renderPlanet(jupiter, projection, view);
        }
    }
    
    /**
     * Generic planet rendering with lighting
     */
    private void renderPlanet(Object planet, Matrix4f projection, Matrix4f view) {
        glUseProgram(shaders.planetShaderProgram); // USE PLANET LIGHTING SHADER
        
        Vector3f position = getPlanetPosition(planet);
        Vector3f color = getPlanetColor(planet);
        float rotationAngle = getPlanetRotationAngle(planet);
        float axialTilt = getPlanetAxialTilt(planet);
        int vao = getPlanetVAO(planet);
        int indexCount = getPlanetIndexCount(planet);
        
        // CREATE TRANSFORMATION MATRIX
        Matrix4f planetModel = new Matrix4f();
        planetModel.translate(position);
        
        // Apply axial tilt (rotation around X-axis) if planet has one
        if (axialTilt != 0.0f) {
            planetModel.rotateX((float) Math.toRadians(axialTilt));
        }
        
        // Apply planet's rotation around Y-axis
        planetModel.rotateY(rotationAngle);
        
        Matrix4f planetMVP = new Matrix4f();
        projection.mul(view, planetMVP);
        planetMVP.mul(planetModel);
        
        FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
        planetMVP.get(matrixBuffer);
        glUniformMatrix4fv(shaders.planetMvpLocation, false, matrixBuffer);
        
        // PASS MODEL MATRIX FOR WORLD-SPACE LIGHTING CALCULATIONS
        FloatBuffer modelBuffer = BufferUtils.createFloatBuffer(16);
        planetModel.get(modelBuffer);
        glUniformMatrix4fv(shaders.planetModelLocation, false, modelBuffer);
        
        glUniform3f(shaders.planetColorLocation, color.x, color.y, color.z);
        glUniform3f(shaders.planetSunPosLocation, 0.0f, 0.0f, 0.0f); // SUN IS AT ORIGIN
        
        // SET TEXTURE UNIFORMS - DISABLE ALL TEXTURES FOR PROCEDURAL PLANETS
        glUniform1i(shaders.planetUseTextureLocation, 0); // Don't use textures
        glUniform1i(shaders.planetUseCloudsLocation, 0); // Don't use clouds
        glUniform1i(shaders.planetUseBumpLocation, 0); // Don't use bump maps
        glUniform1i(shaders.planetUseNightLightsLocation, 0); // Don't use night lights

        glBindVertexArray(vao);
        glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0);
    }
    
    /**
     * Render Earth using OBJ model
     */
    private void renderEarthWithOBJModel(Earth earth, Matrix4f projection, Matrix4f view) {
        glUseProgram(shaders.planetShaderProgram); // USE PLANET SHADER FOR OBJ MODELS
        
        Vector3f position = earth.getPosition();
        float rotationAngle = earth.getRotationAngle();
        
        // CREATE TRANSFORMATION MATRIX
        Matrix4f earthModel = new Matrix4f();
        earthModel.translate(position);
        earthModel.rotateY(rotationAngle);
        
        // Scale the model to match Earth's radius (OBJ model is about 5x too large)
        float scale = earth.getRadius() / 3.2f; // Approximate radius of OBJ model is 3.2
        earthModel.scale(scale);
        
        Matrix4f earthMVP = new Matrix4f();
        projection.mul(view, earthMVP);
        earthMVP.mul(earthModel);
        
        FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
        earthMVP.get(matrixBuffer);
        glUniformMatrix4fv(shaders.planetMvpLocation, false, matrixBuffer);
        
        // PASS MODEL MATRIX FOR WORLD-SPACE LIGHTING CALCULATIONS
        FloatBuffer modelBuffer = BufferUtils.createFloatBuffer(16);
        earthModel.get(modelBuffer);
        glUniformMatrix4fv(shaders.planetModelLocation, false, modelBuffer);
        
        // SET EARTH COLOR
        Vector3f earthColor = earth.getColor();
        glUniform3f(shaders.planetColorLocation, earthColor.x, earthColor.y, earthColor.z);
        
        // SET SUN POSITION FOR LIGHTING
        glUniform3f(shaders.planetSunPosLocation, 0.0f, 0.0f, 0.0f);
        
        // SET TEXTURE UNIFORMS
        glUniform1i(shaders.planetDiffuseTextureLocation, 0); // Texture unit 0
        glUniform1i(shaders.planetUseTextureLocation, earth.getModel().getTextureId() != 0 ? 1 : 0);
        
        // SET CLOUDS TEXTURE UNIFORMS
        glUniform1i(shaders.planetCloudsTextureLocation, 1); // Texture unit 1
        glUniform1i(shaders.planetUseCloudsLocation, earth.getCloudsTextureId() != 0 ? 1 : 0);
        
        // SET BUMP TEXTURE UNIFORMS
        glUniform1i(shaders.planetBumpTextureLocation, 2); // Texture unit 2
        glUniform1i(shaders.planetUseBumpLocation, earth.getBumpTextureId() != 0 ? 1 : 0);
        
        // SET NIGHT LIGHTS TEXTURE UNIFORMS
        glUniform1i(shaders.planetNightLightsTextureLocation, 3); // Texture unit 3
        glUniform1i(shaders.planetUseNightLightsLocation, earth.getNightLightsTextureId() != 0 ? 1 : 0);
        
        // RENDER THE OBJ MODEL WITH ALL TEXTURES
        earth.getModel().renderWithNightLights(earth.getCloudsTextureId(), earth.getBumpTextureId(), earth.getNightLightsTextureId());
    }
    
    /**
     * Specialized Jupiter rendering with OBJ model
     */
    private void renderJupiterWithOBJModel(Jupiter jupiter, Matrix4f projection, Matrix4f view) {
        glUseProgram(shaders.planetShaderProgram); // USE PLANET LIGHTING SHADER
        
        Vector3f position = jupiter.getPosition();
        float rotationAngle = jupiter.getRotationAngle();
        float axialTilt = jupiter.getAxialTilt();
        
        // CREATE TRANSFORMATION MATRIX
        Matrix4f jupiterModel = new Matrix4f();
        jupiterModel.translate(position);
        
        // Apply axial tilt (rotation around X-axis)
        jupiterModel.rotateX((float) Math.toRadians(axialTilt));
        
        // Apply Jupiter's rotation around Y-axis first
        jupiterModel.rotateY(rotationAngle);
        
        // Fix the OBJ model's pole orientation (model file has wrong pole direction)
        jupiterModel.rotateX((float) Math.toRadians(90.0f)); // Correct the pole orientation
        
        // Scale the model to match Jupiter's radius
        // Jupiter OBJ model has radius of about 487 units, but we want 6.991 units
        float scale = jupiter.getRadius() / 487.0f; // Scale from OBJ model size to intended size
        jupiterModel.scale(scale);
        
        Matrix4f jupiterMVP = new Matrix4f();
        projection.mul(view, jupiterMVP);
        jupiterMVP.mul(jupiterModel);
        
        FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
        jupiterMVP.get(matrixBuffer);
        glUniformMatrix4fv(shaders.planetMvpLocation, false, matrixBuffer);
        
        // PASS MODEL MATRIX FOR WORLD-SPACE LIGHTING CALCULATIONS
        FloatBuffer modelBuffer = BufferUtils.createFloatBuffer(16);
        jupiterModel.get(modelBuffer);
        glUniformMatrix4fv(shaders.planetModelLocation, false, modelBuffer);
        
        // SET JUPITER COLOR
        Vector3f jupiterColor = jupiter.getColor();
        glUniform3f(shaders.planetColorLocation, jupiterColor.x, jupiterColor.y, jupiterColor.z);
        
        // SET SUN POSITION FOR LIGHTING
        glUniform3f(shaders.planetSunPosLocation, 0.0f, 0.0f, 0.0f);
        
        // SET TEXTURE UNIFORMS
        glUniform1i(shaders.planetDiffuseTextureLocation, 0); // Texture unit 0
        glUniform1i(shaders.planetUseTextureLocation, jupiter.getModel().getTextureId() != 0 ? 1 : 0);
        
        // SET CLOUDS TEXTURE UNIFORMS (Jupiter doesn't have clouds texture)
        glUniform1i(shaders.planetCloudsTextureLocation, 1); // Texture unit 1
        glUniform1i(shaders.planetUseCloudsLocation, 0); // Disable clouds for Jupiter
        
        // SET BUMP TEXTURE UNIFORMS (Jupiter doesn't have bump texture)
        glUniform1i(shaders.planetBumpTextureLocation, 2); // Texture unit 2
        glUniform1i(shaders.planetUseBumpLocation, 0); // Disable bump for Jupiter
        
        // SET NIGHT LIGHTS TEXTURE UNIFORMS (Jupiter doesn't have night lights)
        glUniform1i(shaders.planetNightLightsTextureLocation, 3); // Texture unit 3
        glUniform1i(shaders.planetUseNightLightsLocation, 0); // Disable night lights for Jupiter
        
        // RENDER THE OBJ MODEL WITH JUST THE DIFFUSE TEXTURE
        jupiter.getModel().render(); // Use simple render method for Jupiter
    }
    
    /**
     * Specialized Earth rendering with surface features (continents, oceans, ice caps)
     */
    private void renderEarthWithSurface(Earth earth, Matrix4f projection, Matrix4f view) {
        glUseProgram(shaders.surfaceShaderProgram); // USE SURFACE SHADER WITH VERTEX COLORS
        
        Vector3f position = earth.getPosition();
        float rotationAngle = earth.getRotationAngle();
        
        // CREATE TRANSFORMATION MATRIX
        Matrix4f earthModel = new Matrix4f();
        earthModel.translate(position);
        earthModel.rotateY(rotationAngle);
        
        Matrix4f earthMVP = new Matrix4f();
        projection.mul(view, earthMVP);
        earthMVP.mul(earthModel);
        
        FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
        earthMVP.get(matrixBuffer);
        glUniformMatrix4fv(shaders.surfaceMvpLocation, false, matrixBuffer);
        
        // PASS MODEL MATRIX FOR WORLD-SPACE LIGHTING CALCULATIONS
        FloatBuffer modelBuffer = BufferUtils.createFloatBuffer(16);
        earthModel.get(modelBuffer);
        glUniformMatrix4fv(shaders.surfaceModelLocation, false, modelBuffer);
        
        glUniform3f(shaders.surfaceSunPosLocation, 0.0f, 0.0f, 0.0f);
        
        glBindVertexArray(earth.getVAO());
        glDrawElements(GL_TRIANGLES, earth.getSphere().getIndices().length, GL_UNSIGNED_INT, 0);
    }
    
    /**
     * Specialized Moon rendering with crater patterns
     */
    private void renderMoonWithCraters(Moon moon, Matrix4f projection, Matrix4f view) {
        glUseProgram(shaders.surfaceShaderProgram); // USE SURFACE SHADER WITH VERTEX COLORS
        
        Vector3f position = moon.getPosition();
        float rotationAngle = moon.getRotationAngle();
        
        // CREATE TRANSFORMATION MATRIX
        Matrix4f moonModel = new Matrix4f();
        moonModel.translate(position);
        moonModel.rotateY(rotationAngle);
        
        Matrix4f moonMVP = new Matrix4f();
        projection.mul(view, moonMVP);
        moonMVP.mul(moonModel);
        
        FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
        moonMVP.get(matrixBuffer);
        glUniformMatrix4fv(shaders.surfaceMvpLocation, false, matrixBuffer);
        
        // PASS MODEL MATRIX FOR WORLD-SPACE LIGHTING CALCULATIONS
        FloatBuffer modelBuffer = BufferUtils.createFloatBuffer(16);
        moonModel.get(modelBuffer);
        glUniformMatrix4fv(shaders.surfaceModelLocation, false, modelBuffer);
        
        glUniform3f(shaders.surfaceSunPosLocation, 0.0f, 0.0f, 0.0f);
        
        glBindVertexArray(moon.getVAO());
        glDrawElements(GL_TRIANGLES, moon.getSphere().getIndices().length, GL_UNSIGNED_INT, 0);
    }

    /**
     * Helper methods to extract planet properties using pattern matching
     */
    private Vector3f getPlanetPosition(Object planet) {
        return switch (planet) {
            case Mercury mercury -> mercury.getPosition();
            case Venus venus -> venus.getPosition();
            case Earth earth -> earth.getPosition();
            case Moon moon -> moon.getPosition();
            case Mars mars -> mars.getPosition();
            case Phobos phobos -> phobos.getPosition();
            case Deimos deimos -> deimos.getPosition();
            default -> new Vector3f(0, 0, 0);
        };
    }
    
    private Vector3f getPlanetColor(Object planet) {
        return switch (planet) {
            case Mercury mercury -> mercury.getColor();
            case Venus venus -> venus.getColor();
            case Earth earth -> earth.getColor();
            case Moon moon -> moon.getColor();
            case Mars mars -> mars.getColor();
            case Phobos phobos -> phobos.getColor();
            case Deimos deimos -> deimos.getColor();
            default -> new Vector3f(1, 1, 1);
        };
    }
    
    private float getPlanetRotationAngle(Object planet) {
        return switch (planet) {
            case Mercury mercury -> mercury.getRotationAngle();
            case Venus venus -> venus.getRotationAngle();
            case Earth earth -> earth.getRotationAngle();
            case Moon moon -> moon.getRotationAngle();
            case Mars mars -> mars.getRotationAngle();
            case Phobos phobos -> phobos.getRotationAngle();
            case Deimos deimos -> deimos.getRotationAngle();
            default -> 0.0f;
        };
    }

    private float getPlanetAxialTilt(Object planet) {
        return switch (planet) {
            case Jupiter jupiter -> jupiter.getAxialTilt();
            case Earth earth -> earth.getAxialTilt();
            case Mars mars -> mars.getAxialTilt();
            case Venus venus -> venus.getAxialTilt();
            // Add other planets here when they have axial tilt implemented
            default -> 0.0f; // No axial tilt for planets that don't have it implemented yet
        };
    }
    
    private int getPlanetVAO(Object planet) {
        return switch (planet) {
            case Mercury mercury -> mercury.getVAO();
            case Venus venus -> venus.getVAO();
            case Earth earth -> earth.getVAO();
            case Moon moon -> moon.getVAO();
            case Mars mars -> mars.getVAO();
            case Phobos phobos -> phobos.getVAO();
            case Deimos deimos -> deimos.getVAO();
            default -> 0;
        };
    }
    
    private int getPlanetIndexCount(Object planet) {
        return switch (planet) {
            case Mercury mercury -> mercury.getSphere().getIndices().length;
            case Venus venus -> venus.getSphere().getIndices().length;
            case Earth earth -> earth.getSphere().getIndices().length;
            case Moon moon -> moon.getSphere().getIndices().length;
            case Mars mars -> mars.getSphere().getIndices().length;
            case Phobos phobos -> phobos.getSphere().getIndices().length;
            case Deimos deimos -> deimos.getSphere().getIndices().length;
            default -> 0;
        };
    }
    
    /**
     * Create MVP matrix with camera parameters
     */
    public Matrix4f createMVPMatrix(int width, int height) {
        Matrix4f projection = new Matrix4f();
        projection.perspective((float) Math.toRadians(55.0f), (float) width / height, 1.0f, 500000.0f);
        
        // CALCULATE LOOK-AT TARGET BASED ON CAMERA MODE
        float lookX, lookY, lookZ;
        
        // GET CAMERA PARAMETERS
        float cameraX = camera.getX();
        float cameraY = camera.getY(); 
        float cameraZ = camera.getZ();
        float cameraPitch = camera.getPitch();
        float cameraYaw = camera.getYaw();
        
        if (camera.isTrackingEnabled() && !"NONE".equals(camera.getTrackedObject())) {
            // WHEN TRACKING, ALWAYS LOOK AT THE TRACKED OBJECT
            Vector3f targetPosition = new Vector3f();
            switch (camera.getTrackedObject()) {
                case "SUN":
                    targetPosition.set(0, 0, 0);
                    break;
                case "MERCURY":
                    targetPosition.set(sceneManager.getMercury().getPosition());
                    break;
                case "VENUS":
                    targetPosition.set(sceneManager.getVenus().getPosition());
                    break;
                case "EARTH":
                    targetPosition.set(sceneManager.getEarth().getPosition());
                    break;
                case "MARS":
                    targetPosition.set(sceneManager.getMars().getPosition());
                    break;
                case "JUPITER":
                    targetPosition.set(sceneManager.getJupiter().getPosition());
                    break;
                default:
                    targetPosition.set(0, 0, 0);
            }
            lookX = targetPosition.x;
            lookY = targetPosition.y;
            lookZ = targetPosition.z;
        } else {
            // FREE CAMERA MODE - CALCULATE LOOK-AT TARGET BASED ON CAMERA ROTATION (FPS STYLE)
            float radPitch = (float) Math.toRadians(cameraPitch);
            float radYaw = (float) Math.toRadians(cameraYaw);
            
            // MATCH THE COORDINATE SYSTEM USED IN CAMERA MOVEMENT
            lookX = cameraX + (float) Math.cos(radPitch) * (float) Math.sin(radYaw);
            lookY = cameraY + (float) Math.sin(radPitch);
            lookZ = cameraZ + (float) Math.cos(radPitch) * (float) Math.cos(radYaw);
        }
        
        Matrix4f view = new Matrix4f();
        view.lookAt(cameraX, cameraY, cameraZ, lookX, lookY, lookZ, 0.0f, 1.0f, 0.0f);
        
        Matrix4f mvp = new Matrix4f();
        projection.mul(view, mvp);
        
        return mvp;
    }
    
    /**
     * Get separate projection and view matrices
     */
    public Matrix4f[] getProjectionAndView(int width, int height) {
        Matrix4f projection = new Matrix4f();
        projection.perspective((float) Math.toRadians(55.0f), (float) width / height, 1.0f, 500000.0f);
        
        // CALCULATE LOOK-AT TARGET BASED ON CAMERA MODE
        float lookX, lookY, lookZ;
        
        // GET CAMERA PARAMETERS
        float cameraX = camera.getX();
        float cameraY = camera.getY(); 
        float cameraZ = camera.getZ();
        float cameraPitch = camera.getPitch();
        float cameraYaw = camera.getYaw();
        
        if (camera.isTrackingEnabled() && !"NONE".equals(camera.getTrackedObject())) {
            // WHEN TRACKING, ALWAYS LOOK AT THE TRACKED OBJECT
            Vector3f targetPosition = new Vector3f();
            switch (camera.getTrackedObject()) {
                case "SUN":
                    targetPosition.set(0, 0, 0);
                    break;
                case "MERCURY":
                    targetPosition.set(sceneManager.getMercury().getPosition());
                    break;
                case "VENUS":
                    targetPosition.set(sceneManager.getVenus().getPosition());
                    break;
                case "EARTH":
                    targetPosition.set(sceneManager.getEarth().getPosition());
                    break;
                case "MARS":
                    targetPosition.set(sceneManager.getMars().getPosition());
                    break;
                case "JUPITER":
                    targetPosition.set(sceneManager.getJupiter().getPosition());
                    break;
                default:
                    targetPosition.set(0, 0, 0);
            }
            lookX = targetPosition.x;
            lookY = targetPosition.y;
            lookZ = targetPosition.z;
        } else {
            // FREE CAMERA MODE - CALCULATE LOOK-AT TARGET BASED ON CAMERA ROTATION (FPS STYLE)
            float radPitch = (float) Math.toRadians(cameraPitch);
            float radYaw = (float) Math.toRadians(cameraYaw);
            
            // MATCH THE COORDINATE SYSTEM USED IN CAMERA MOVEMENT
            lookX = cameraX + (float) Math.cos(radPitch) * (float) Math.sin(radYaw);
            lookY = cameraY + (float) Math.sin(radPitch);
            lookZ = cameraZ + (float) Math.cos(radPitch) * (float) Math.cos(radYaw);
        }
        
        Matrix4f view = new Matrix4f();
        view.lookAt(cameraX, cameraY, cameraZ, lookX, lookY, lookZ, 0.0f, 1.0f, 0.0f);
        
        return new Matrix4f[]{projection, view};
    }
    
    /**
     * Render Jupiter's moon Io
     */
    private void renderIo(Matrix4f projection, Matrix4f view) {
        com.stefanrogic.assets.celestial.jupiter.Io io = sceneManager.getIo();
        if (io != null) {
            renderPlanet(io, projection, view);
        }
    }
    
    /**
     * Render Jupiter's moon Europa
     */
    private void renderEuropa(Matrix4f projection, Matrix4f view) {
        com.stefanrogic.assets.celestial.jupiter.Europa europa = sceneManager.getEuropa();
        if (europa != null) {
            renderPlanet(europa, projection, view);
        }
    }
    
    /**
     * Render Jupiter's moon Ganymede
     */
    private void renderGanymede(Matrix4f projection, Matrix4f view) {
        com.stefanrogic.assets.celestial.jupiter.Ganymede ganymede = sceneManager.getGanymede();
        if (ganymede != null) {
            renderPlanet(ganymede, projection, view);
        }
    }
    
    /**
     * Render Jupiter's moon Callisto
     */
    private void renderCallisto(Matrix4f projection, Matrix4f view) {
        com.stefanrogic.assets.celestial.jupiter.Callisto callisto = sceneManager.getCallisto();
        if (callisto != null) {
            renderPlanet(callisto, projection, view);
        }
    }
    
    /**
     * Clean up rendering resources
     */
    public void cleanup() {
        gridRenderer.cleanup();
    }
}
