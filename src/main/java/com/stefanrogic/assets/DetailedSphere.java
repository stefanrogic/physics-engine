package com.stefanrogic.assets;

import java.util.ArrayList;
import java.util.List;
import org.joml.Vector3f;

/**
 * Enhanced sphere generator for planetary surfaces with color variations
 */
public class DetailedSphere extends Sphere {
    
    public DetailedSphere(float radius, int latitudeSegments, int longitudeSegments) {
        super(radius, latitudeSegments, longitudeSegments);
    }
    
    /**
     * Generate sphere with color variations based on surface patterns
     * @param surfaceColorFunction Function that takes (latitude, longitude) and returns color
     */
    public float[] generateVerticesWithColors(SurfaceColorFunction surfaceColorFunction) {
        List<Float> vertexList = new ArrayList<>();
        
        // GENERATE VERTICES WITH POSITION, NORMAL, TEXTURE COORDINATES, AND COLOR
        for (int lat = 0; lat <= getLatitudeSegments(); lat++) {
            float theta = (float) (lat * Math.PI / getLatitudeSegments());
            float sinTheta = (float) Math.sin(theta);
            float cosTheta = (float) Math.cos(theta);
            
            for (int lon = 0; lon <= getLongitudeSegments(); lon++) {
                float phi = (float) (lon * 2 * Math.PI / getLongitudeSegments());
                float sinPhi = (float) Math.sin(phi);
                float cosPhi = (float) Math.cos(phi);
                
                float x = cosPhi * sinTheta;
                float y = cosTheta;
                float z = sinPhi * sinTheta;
                
                // POSITION
                vertexList.add(x * getRadius());
                vertexList.add(y * getRadius());
                vertexList.add(z * getRadius());
                
                // NORMAL (SAME AS POSITION FOR UNIT SPHERE)
                vertexList.add(x);
                vertexList.add(y);
                vertexList.add(z);
                
                // TEXTURE COORDINATES (U, V)
                float u = (float) lon / getLongitudeSegments();
                float v = (float) lat / getLatitudeSegments();
                vertexList.add(u);
                vertexList.add(v);
                
                // SURFACE COLOR BASED ON POSITION
                Vector3f color = surfaceColorFunction.getColor(theta - (float)Math.PI/2, phi - (float)Math.PI);
                vertexList.add(color.x);
                vertexList.add(color.y);
                vertexList.add(color.z);
            }
        }
        
        // CONVERT TO ARRAY
        float[] vertices = new float[vertexList.size()];
        for (int i = 0; i < vertexList.size(); i++) {
            vertices[i] = vertexList.get(i);
        }
        
        return vertices;
    }
    
    private int getLatitudeSegments() {
        return latitudeSegments;
    }
    
    private int getLongitudeSegments() {
        return longitudeSegments;
    }
    
    /**
     * Interface for surface color functions
     */
    @FunctionalInterface
    public interface SurfaceColorFunction {
        Vector3f getColor(float latitude, float longitude);
    }
}
