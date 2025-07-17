package com.stefanrogic.assets;

import java.util.ArrayList;
import java.util.List;
import org.joml.Vector3f;

/**
 * Enhanced sphere generator with procedural surface patterns
 */
public class ProceduralSphere extends Sphere {
    
    public ProceduralSphere(float radius, int latitudeSegments, int longitudeSegments) {
        super(radius, latitudeSegments, longitudeSegments);
    }
    
    /**
     * Generate Earth-like surface with continents and oceans
     */
    public float[] generateEarthSurface() {
        List<Float> vertexList = new ArrayList<>();
        
        Vector3f oceanColor = new Vector3f(0.02f, 0.15f, 0.6f);
        Vector3f landColor = new Vector3f(0.05f, 0.5f, 0.05f);
        Vector3f iceColor = new Vector3f(0.95f, 0.98f, 1.0f);
        Vector3f mountainColor = new Vector3f(0.3f, 0.25f, 0.15f);
        
        for (int lat = 0; lat <= latitudeSegments; lat++) {
            float theta = (float) (lat * Math.PI / latitudeSegments);
            float sinTheta = (float) Math.sin(theta);
            float cosTheta = (float) Math.cos(theta);
            
            for (int lon = 0; lon <= longitudeSegments; lon++) {
                float phi = (float) (lon * 2 * Math.PI / longitudeSegments);
                float sinPhi = (float) Math.sin(phi);
                float cosPhi = (float) Math.cos(phi);
                
                float x = cosPhi * sinTheta;
                float y = cosTheta;
                float z = sinPhi * sinTheta;
                
                // POSITION
                vertexList.add(x * radius);
                vertexList.add(y * radius);
                vertexList.add(z * radius);
                
                // NORMAL
                vertexList.add(x);
                vertexList.add(y);
                vertexList.add(z);
                
                // TEXTURE COORDINATES
                float u = (float) lon / longitudeSegments;
                float v = (float) lat / latitudeSegments;
                vertexList.add(u);
                vertexList.add(v);
                
                // EARTH SURFACE COLOR
                Vector3f surfaceColor = getEarthColorAtPosition(theta - (float)Math.PI/2, phi - (float)Math.PI, 
                                                               oceanColor, landColor, iceColor, mountainColor);
                vertexList.add(surfaceColor.x);
                vertexList.add(surfaceColor.y);
                vertexList.add(surfaceColor.z);
            }
        }
        
        return convertToArray(vertexList);
    }
    
    /**
     * Generate Moon-like surface with craters
     */
    public float[] generateMoonSurface() {
        List<Float> vertexList = new ArrayList<>();
        
        Vector3f baseColor = new Vector3f(0.6f, 0.6f, 0.65f);
        
        for (int lat = 0; lat <= latitudeSegments; lat++) {
            float theta = (float) (lat * Math.PI / latitudeSegments);
            float sinTheta = (float) Math.sin(theta);
            float cosTheta = (float) Math.cos(theta);
            
            for (int lon = 0; lon <= longitudeSegments; lon++) {
                float phi = (float) (lon * 2 * Math.PI / longitudeSegments);
                float sinPhi = (float) Math.sin(phi);
                float cosPhi = (float) Math.cos(phi);
                
                float x = cosPhi * sinTheta;
                float y = cosTheta;
                float z = sinPhi * sinTheta;
                
                // POSITION
                vertexList.add(x * radius);
                vertexList.add(y * radius);
                vertexList.add(z * radius);
                
                // NORMAL
                vertexList.add(x);
                vertexList.add(y);
                vertexList.add(z);
                
                // TEXTURE COORDINATES
                float u = (float) lon / longitudeSegments;
                float v = (float) lat / latitudeSegments;
                vertexList.add(u);
                vertexList.add(v);
                
                // MOON SURFACE COLOR WITH CRATERS
                Vector3f surfaceColor = getMoonColorAtPosition(theta - (float)Math.PI/2, phi - (float)Math.PI, baseColor);
                vertexList.add(surfaceColor.x);
                vertexList.add(surfaceColor.y);
                vertexList.add(surfaceColor.z);
            }
        }
        
        return convertToArray(vertexList);
    }
    
    private Vector3f getEarthColorAtPosition(float latitude, float longitude, 
                                           Vector3f oceanColor, Vector3f landColor, 
                                           Vector3f iceColor, Vector3f mountainColor) {
        // NORMALIZE COORDINATES
        float lat = (latitude + (float)Math.PI/2) / (float)Math.PI;
        float lon = (longitude + (float)Math.PI) / (2*(float)Math.PI);
        
        // ICE CAPS AT POLES
        if (lat < 0.08f || lat > 0.92f) {
            return new Vector3f(iceColor);
        }
        
        // SIMPLE CONTINENT PATTERN
        boolean isLand = false;
        
        // AFRICA AND EUROPE
        if (lon >= 0.0f && lon <= 0.25f && lat >= 0.3f && lat <= 0.8f) {
            isLand = true;
        }
        // ASIA
        else if (lon >= 0.2f && lon <= 0.7f && lat >= 0.45f && lat <= 0.85f) {
            isLand = true;
        }
        // NORTH AMERICA
        else if (lon >= 0.75f && lat >= 0.5f && lat <= 0.85f) {
            isLand = true;
        }
        // SOUTH AMERICA
        else if (lon >= 0.8f && lon <= 0.95f && lat >= 0.15f && lat <= 0.55f) {
            isLand = true;
        }
        // AUSTRALIA
        else if (lon >= 0.6f && lon <= 0.75f && lat >= 0.15f && lat <= 0.35f) {
            isLand = true;
        }
        
        // ADD NOISE FOR COASTLINES
        float noise = (float)(Math.sin(lat * 30) * Math.cos(lon * 40) * 0.08);
        
        if (isLand) {
            float greenness = 1.0f - Math.abs(lat - 0.5f) * 1.5f;
            greenness = Math.max(0.2f, Math.min(1.0f, greenness));
            
            return new Vector3f(
                landColor.x + (mountainColor.x - landColor.x) * (1.0f - greenness) + noise * 0.5f,
                landColor.y * greenness + noise * 0.3f,
                landColor.z + noise * 0.2f
            );
        } else {
            float depth = 0.7f + noise * 0.3f;
            return new Vector3f(
                oceanColor.x * depth,
                oceanColor.y * depth,
                oceanColor.z * (0.9f + noise * 0.1f)
            );
        }
    }
    
    private Vector3f getMoonColorAtPosition(float latitude, float longitude, Vector3f baseColor) {
        float lat = (latitude + (float)Math.PI/2) / (float)Math.PI;
        float lon = (longitude + (float)Math.PI) / (2*(float)Math.PI);
        
        Vector3f result = new Vector3f(baseColor);
        
        // MAJOR CRATERS
        float[][] craters = {
            {0.6f, 0.3f, 0.12f}, {0.4f, 0.7f, 0.10f}, {0.8f, 0.5f, 0.15f},
            {0.3f, 0.4f, 0.08f}, {0.7f, 0.8f, 0.06f}, {0.5f, 0.6f, 0.11f},
            {0.2f, 0.2f, 0.07f}, {0.9f, 0.4f, 0.09f}, {0.45f, 0.35f, 0.10f}
        };
        
        float craterEffect = 0.0f;
        for (float[] crater : craters) {
            float deltaLat = lat - crater[0];
            float deltaLon = lon - crater[1];
            if (deltaLon > 0.5f) deltaLon -= 1.0f;
            if (deltaLon < -0.5f) deltaLon += 1.0f;
            
            float distance = (float)Math.sqrt(deltaLat * deltaLat + deltaLon * deltaLon);
            
            if (distance < crater[2]) {
                float depthFactor = 1.0f - (distance / crater[2]);
                if (distance < crater[2] * 0.7f) {
                    craterEffect -= depthFactor * 0.25f; // CRATER INTERIOR
                } else {
                    craterEffect += depthFactor * 0.15f; // CRATER RIM
                }
            }
        }
        
        // MARIA (DARK PLAINS)
        float maria = 0.0f;
        if ((lat >= 0.4f && lat <= 0.7f && lon >= 0.1f && lon <= 0.4f) ||
            (lat >= 0.3f && lat <= 0.6f && lon >= 0.5f && lon <= 0.8f)) {
            maria = -0.2f;
        }
        
        // SURFACE ROUGHNESS
        float roughness = (float)(Math.sin(lat * 50) * Math.cos(lon * 60) * 0.04);
        
        float totalEffect = craterEffect + maria + roughness;
        totalEffect = Math.max(-0.4f, Math.min(0.3f, totalEffect));
        
        return new Vector3f(
            Math.max(0.1f, result.x + totalEffect),
            Math.max(0.1f, result.y + totalEffect),
            Math.max(0.1f, result.z + totalEffect * 0.8f)
        );
    }
    
    private float[] convertToArray(List<Float> vertexList) {
        float[] vertices = new float[vertexList.size()];
        for (int i = 0; i < vertexList.size(); i++) {
            vertices[i] = vertexList.get(i);
        }
        return vertices;
    }
}
