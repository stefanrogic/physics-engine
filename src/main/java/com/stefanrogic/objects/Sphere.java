package com.stefanrogic.objects;

import java.util.ArrayList;
import java.util.List;

public class Sphere {
    private float[] vertices;
    private int[] indices;
    private float radius;
    private int latitudeSegments;
    private int longitudeSegments;

    public Sphere(float radius, int latitudeSegments, int longitudeSegments) {
        this.radius = radius;
        this.latitudeSegments = latitudeSegments;
        this.longitudeSegments = longitudeSegments;
        generateSphere();
    }

    private void generateSphere() {
        List<Float> vertexList = new ArrayList<>();
        List<Integer> indexList = new ArrayList<>();
        
        // GENERATE VERTICES
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

    public float[] getVertices() { return vertices; }
    public int[] getIndices() { return indices; }
    public float getRadius() { return radius; }
}
