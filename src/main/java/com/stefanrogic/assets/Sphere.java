package com.stefanrogic.assets;

import java.util.ArrayList;
import java.util.List;

public class Sphere {
    protected float[] vertices;
    protected int[] indices;
    protected float radius;
    protected int latitudeSegments;
    protected int longitudeSegments;

    public Sphere(float radius, int latitudeSegments, int longitudeSegments) {
        this.radius = radius;
        this.latitudeSegments = latitudeSegments;
        this.longitudeSegments = longitudeSegments;
        generateSphere();
    }

    protected void generateSphere() {
        List<Float> vertexList = new ArrayList<>();
        List<Integer> indexList = new ArrayList<>();
        
        // GENERATE VERTICES WITH POSITION, NORMAL, AND TEXTURE COORDINATES
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
                
                // NORMAL (SAME AS POSITION FOR UNIT SPHERE)
                vertexList.add(x);
                vertexList.add(y);
                vertexList.add(z);
                
                // TEXTURE COORDINATES (U, V)
                float u = (float) lon / longitudeSegments;
                float v = (float) lat / latitudeSegments;
                vertexList.add(u);
                vertexList.add(v);
            }
        }

        // GENERATE INDICES
        for (int lat = 0; lat < latitudeSegments; lat++) {
            for (int lon = 0; lon < longitudeSegments; lon++) {
                int first = lat * (longitudeSegments + 1) + lon;
                int second = first + longitudeSegments + 1;
                
                // FIRST TRIANGLE
                indexList.add(first);
                indexList.add(second);
                indexList.add(first + 1);
                
                // SECOND TRIANGLE
                indexList.add(second);
                indexList.add(second + 1);
                indexList.add(first + 1);
            }
        }
        
        // CONVERT TO ARRAYS
        vertices = new float[vertexList.size()];
        for (int i = 0; i < vertexList.size(); i++) {
            vertices[i] = vertexList.get(i);
        }
        
        indices = new int[indexList.size()];
        for (int i = 0; i < indexList.size(); i++) {
            indices[i] = indexList.get(i);
        }
    }
    
    /**
     * Generate vertices with surface color variations
     */
    public float[] generateVerticesWithColors(ColorFunction colorFunc) {
        List<Float> vertexList = new ArrayList<>();
        
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
                
                // SURFACE COLOR BASED ON POSITION
                if (colorFunc != null) {
                    org.joml.Vector3f color = colorFunc.getColor(theta - (float)Math.PI/2, phi - (float)Math.PI);
                    vertexList.add(color.x);
                    vertexList.add(color.y);
                    vertexList.add(color.z);
                }
            }
        }
        
        float[] result = new float[vertexList.size()];
        for (int i = 0; i < vertexList.size(); i++) {
            result[i] = vertexList.get(i);
        }
        return result;
    }
    
    @FunctionalInterface
    public interface ColorFunction {
        org.joml.Vector3f getColor(float latitude, float longitude);
    }

    public float[] getVertices() { return vertices; }
    public int[] getIndices() { return indices; }
    public float getRadius() { return radius; }
}
