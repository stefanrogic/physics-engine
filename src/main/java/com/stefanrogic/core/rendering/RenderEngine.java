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
    
    public RenderEngine(SceneManager sceneManager, ShaderManager.ShaderPrograms shaders, Camera camera) {
        this.sceneManager = sceneManager;
        this.shaders = shaders;
        this.camera = camera;
        this.gridRenderer = new GridRenderer(shaders);
    }
    
    /**
     * Initialize the grid through GridRenderer
     */
    public void createGrid() {
        gridRenderer.createGrid();
    }
    
    /**
     * Render the grid if visible
     */
    public void renderGrid(Matrix4f mvpMatrix, boolean gridVisible) {
        gridRenderer.renderGrid(mvpMatrix, gridVisible);
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
    }
    
    private void renderSun(Matrix4f projection, Matrix4f view) {
        glUseProgram(shaders.sunShaderProgram);
        
        // CREATE SUN TRANSFORMATION MATRIX WITH ROTATION
        Matrix4f sunModel = new Matrix4f();
        sunModel.rotateY(sceneManager.getSun().getRotationAngle()); // ROTATE AROUND Y-AXIS
        
        Matrix4f sunMVP = new Matrix4f();
        projection.mul(view, sunMVP);
        sunMVP.mul(sunModel);
        
        FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
        sunMVP.get(matrixBuffer);
        glUniformMatrix4fv(shaders.sunMvpLocation, false, matrixBuffer);
        glUniform3f(shaders.sunColorLocation, sceneManager.getSun().getColor().x, sceneManager.getSun().getColor().y, sceneManager.getSun().getColor().z);
        
        glBindVertexArray(sceneManager.getSun().getVAO());
        glDrawElements(GL_TRIANGLES, sceneManager.getSun().getSphere().getIndices().length, GL_UNSIGNED_INT, 0);
    }
    
    private void renderMercury(Matrix4f projection, Matrix4f view) {
        renderPlanet(sceneManager.getMercury(), projection, view);
    }
    
    private void renderVenus(Matrix4f projection, Matrix4f view) {
        renderPlanet(sceneManager.getVenus(), projection, view);
    }
    
    private void renderEarth(Matrix4f projection, Matrix4f view) {
        renderEarthWithSurface(sceneManager.getEarth(), projection, view);
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
    
    /**
     * Generic planet rendering with lighting
     */
    private void renderPlanet(Object planet, Matrix4f projection, Matrix4f view) {
        glUseProgram(shaders.planetShaderProgram); // USE PLANET LIGHTING SHADER
        
        Vector3f position = getPlanetPosition(planet);
        Vector3f color = getPlanetColor(planet);
        float rotationAngle = getPlanetRotationAngle(planet);
        int vao = getPlanetVAO(planet);
        int indexCount = getPlanetIndexCount(planet);
        
        // CREATE TRANSFORMATION MATRIX
        Matrix4f planetModel = new Matrix4f();
        planetModel.translate(position);
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
        
        glBindVertexArray(vao);
        glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0);
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
        projection.perspective((float) Math.toRadians(55.0f), (float) width / height, 1.0f, 100000.0f);
        
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
        projection.perspective((float) Math.toRadians(55.0f), (float) width / height, 1.0f, 100000.0f);
        
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
     * Clean up rendering resources
     */
    public void cleanup() {
        gridRenderer.cleanup();
    }
}
