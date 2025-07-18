package com.stefanrogic.core.rendering;

import org.joml.Vector3f;
import org.joml.Vector2f;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Simple OBJ file loader for loading 3D models
 */
public class OBJLoader {
    
    public static class ModelData {
        public float[] vertices;
        public int[] indices;
        public float[] normals;
        public float[] texCoords;
        
        public ModelData(float[] vertices, int[] indices, float[] normals, float[] texCoords) {
            this.vertices = vertices;
            this.indices = indices;
            this.normals = normals;
            this.texCoords = texCoords;
        }
    }
    
    /**
     * Load an OBJ file from the resources directory
     * @param resourcePath Path to the OBJ file in resources (e.g., "models/earth_model.obj")
     * @return ModelData containing vertices, indices, normals, and texture coordinates
     */
    public static ModelData loadOBJ(String resourcePath) {
        System.out.println("DEBUG: Starting OBJ loading for: " + resourcePath);
        List<Vector3f> vertices = new ArrayList<>();
        List<Vector2f> texCoords = new ArrayList<>();
        List<Vector3f> normals = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        
        Map<String, Integer> vertexMap = new HashMap<>();
        List<Vector3f> finalVertices = new ArrayList<>();
        List<Vector2f> finalTexCoords = new ArrayList<>();
        List<Vector3f> finalNormals = new ArrayList<>();
        
        try {
            InputStream inputStream = OBJLoader.class.getClassLoader().getResourceAsStream(resourcePath);
            if (inputStream == null) {
                throw new RuntimeException("Could not find resource: " + resourcePath);
            }
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split("\\s+");
                
                if (tokens.length == 0) continue;
                
                switch (tokens[0]) {
                    case "v":
                        // Vertex position
                        if (tokens.length >= 4) {
                            float x = Float.parseFloat(tokens[1]);
                            float y = Float.parseFloat(tokens[2]);
                            float z = Float.parseFloat(tokens[3]);
                            vertices.add(new Vector3f(x, y, z));
                        }
                        break;
                        
                    case "vt":
                        // Texture coordinate
                        if (tokens.length >= 3) {
                            float u = Float.parseFloat(tokens[1]);
                            float v = Float.parseFloat(tokens[2]);
                            float originalV = v;
                            // Flip V coordinate for proper texture mapping
                            v = 1.0f - v;
                            texCoords.add(new Vector2f(u, v));
                            
                            // Debug: Log first few texture coordinates
                            if (texCoords.size() <= 5) {
                                System.out.println("DEBUG: Texture coord " + texCoords.size() + ": " + u + ", " + originalV + " -> " + v + " (flipped)");
                            }
                        }
                        break;
                        
                    case "vn":
                        // Normal
                        if (tokens.length >= 4) {
                            float x = Float.parseFloat(tokens[1]);
                            float y = Float.parseFloat(tokens[2]);
                            float z = Float.parseFloat(tokens[3]);
                            normals.add(new Vector3f(x, y, z));
                        }
                        break;
                        
                    case "f":
                        // Face
                        if (tokens.length >= 4) {
                            // Handle both triangles and quads
                            if (tokens.length == 4) {
                                // Triangle
                                for (int i = 1; i < tokens.length; i++) {
                                    String[] vertexData = tokens[i].split("/");
                                    
                                    // Parse vertex index
                                    int vertexIndex = Integer.parseInt(vertexData[0]) - 1;
                                    Vector3f vertex = vertices.get(vertexIndex);
                                    
                                    // Parse texture coordinate index (if present)
                                    Vector2f texCoord = new Vector2f(0, 0);
                                    if (vertexData.length > 1 && !vertexData[1].isEmpty()) {
                                        int texIndex = Integer.parseInt(vertexData[1]) - 1;
                                        if (texIndex >= 0 && texIndex < texCoords.size()) {
                                            texCoord = texCoords.get(texIndex);
                                            // Don't flip V coordinate - use original
                                        }
                                    }
                                    
                                    // Parse normal index (if present)
                                    Vector3f normal = new Vector3f(0, 1, 0);
                                    if (vertexData.length > 2 && !vertexData[2].isEmpty()) {
                                        int normalIndex = Integer.parseInt(vertexData[2]) - 1;
                                        if (normalIndex >= 0 && normalIndex < normals.size()) {
                                            normal = normals.get(normalIndex);
                                        }
                                    }
                                    
                                    // Find or create vertex index
                                    int finalIndex = findOrCreateVertex(vertex, texCoord, normal, 
                                        finalVertices, finalTexCoords, finalNormals);
                                    indices.add(finalIndex);
                                }
                            } else if (tokens.length == 5) {
                                // Quad - convert to two triangles
                                int[] quadIndices = new int[4];
                                
                                // Parse all 4 vertices of the quad
                                for (int i = 1; i < tokens.length; i++) {
                                    String[] vertexData = tokens[i].split("/");
                                    
                                    // Parse vertex index
                                    int vertexIndex = Integer.parseInt(vertexData[0]) - 1;
                                    Vector3f vertex = vertices.get(vertexIndex);
                                    
                                    // Parse texture coordinate index (if present)
                                    Vector2f texCoord = new Vector2f(0, 0);
                                    if (vertexData.length > 1 && !vertexData[1].isEmpty()) {
                                        int texIndex = Integer.parseInt(vertexData[1]) - 1;
                                        if (texIndex >= 0 && texIndex < texCoords.size()) {
                                            texCoord = texCoords.get(texIndex);
                                            // Don't flip V coordinate - use original
                                        }
                                    }
                                    
                                    // Parse normal index (if present)
                                    Vector3f normal = new Vector3f(0, 1, 0);
                                    if (vertexData.length > 2 && !vertexData[2].isEmpty()) {
                                        int normalIndex = Integer.parseInt(vertexData[2]) - 1;
                                        if (normalIndex >= 0 && normalIndex < normals.size()) {
                                            normal = normals.get(normalIndex);
                                        }
                                    }
                                    
                                    // Find or create vertex index
                                    int finalIndex = findOrCreateVertex(vertex, texCoord, normal, 
                                        finalVertices, finalTexCoords, finalNormals);
                                    quadIndices[i - 1] = finalIndex;
                                }
                                
                                // Create two triangles from the quad
                                // Triangle 1: v0, v1, v2
                                indices.add(quadIndices[0]);
                                indices.add(quadIndices[1]);
                                indices.add(quadIndices[2]);
                                
                                // Triangle 2: v0, v2, v3
                                indices.add(quadIndices[0]);
                                indices.add(quadIndices[2]);
                                indices.add(quadIndices[3]);
                            }
                        }
                        break;
                }
            }
            
            reader.close();
            
        } catch (Exception e) {
            throw new RuntimeException("Error loading OBJ file: " + resourcePath, e);
        }
        
        // Convert to arrays
        float[] vertexArray = new float[finalVertices.size() * 3];
        float[] texCoordArray = new float[finalTexCoords.size() * 2];
        float[] normalArray = new float[finalNormals.size() * 3];
        int[] indexArray = new int[indices.size()];
        
        for (int i = 0; i < finalVertices.size(); i++) {
            Vector3f vertex = finalVertices.get(i);
            vertexArray[i * 3] = vertex.x;
            vertexArray[i * 3 + 1] = vertex.y;
            vertexArray[i * 3 + 2] = vertex.z;
        }
        
        for (int i = 0; i < finalTexCoords.size(); i++) {
            Vector2f texCoord = finalTexCoords.get(i);
            texCoordArray[i * 2] = texCoord.x;
            texCoordArray[i * 2 + 1] = texCoord.y;
        }
        
        for (int i = 0; i < finalNormals.size(); i++) {
            Vector3f normal = finalNormals.get(i);
            normalArray[i * 3] = normal.x;
            normalArray[i * 3 + 1] = normal.y;
            normalArray[i * 3 + 2] = normal.z;
        }
        
        for (int i = 0; i < indices.size(); i++) {
            indexArray[i] = indices.get(i);
        }
        
        // If no normals were provided, calculate them
        if (finalNormals.isEmpty()) {
            normalArray = calculateNormals(vertexArray, indexArray);
        }
        
        return new ModelData(vertexArray, indexArray, normalArray, texCoordArray);
    }
    
    private static int findOrCreateVertex(Vector3f vertex, Vector2f texCoord, Vector3f normal,
                                        List<Vector3f> vertices, List<Vector2f> texCoords, List<Vector3f> normals) {
        // Look for existing vertex
        for (int i = 0; i < vertices.size(); i++) {
            Vector3f existingVertex = vertices.get(i);
            Vector2f existingTexCoord = texCoords.get(i);
            Vector3f existingNormal = normals.get(i);
            
            if (existingVertex.equals(vertex) && existingTexCoord.equals(texCoord) && existingNormal.equals(normal)) {
                return i;
            }
        }
        
        // Create new vertex
        vertices.add(new Vector3f(vertex));
        texCoords.add(new Vector2f(texCoord));
        normals.add(new Vector3f(normal));
        
        return vertices.size() - 1;
    }
    
    /**
     * Calculate normals for a mesh if they weren't provided in the OBJ file
     */
    private static float[] calculateNormals(float[] vertices, int[] indices) {
        float[] normals = new float[vertices.length];
        
        // Initialize normals to zero
        for (int i = 0; i < normals.length; i++) {
            normals[i] = 0.0f;
        }
        
        // Calculate face normals and accumulate vertex normals
        for (int i = 0; i < indices.length; i += 3) {
            int i0 = indices[i] * 3;
            int i1 = indices[i + 1] * 3;
            int i2 = indices[i + 2] * 3;
            
            // Get vertices
            Vector3f v0 = new Vector3f(vertices[i0], vertices[i0 + 1], vertices[i0 + 2]);
            Vector3f v1 = new Vector3f(vertices[i1], vertices[i1 + 1], vertices[i1 + 2]);
            Vector3f v2 = new Vector3f(vertices[i2], vertices[i2 + 1], vertices[i2 + 2]);
            
            // Calculate face normal
            Vector3f edge1 = new Vector3f(v1).sub(v0);
            Vector3f edge2 = new Vector3f(v2).sub(v0);
            Vector3f faceNormal = new Vector3f(edge1).cross(edge2).normalize();
            
            // Accumulate normals for each vertex
            normals[i0] += faceNormal.x;
            normals[i0 + 1] += faceNormal.y;
            normals[i0 + 2] += faceNormal.z;
            
            normals[i1] += faceNormal.x;
            normals[i1 + 1] += faceNormal.y;
            normals[i1 + 2] += faceNormal.z;
            
            normals[i2] += faceNormal.x;
            normals[i2 + 1] += faceNormal.y;
            normals[i2 + 2] += faceNormal.z;
        }
        
        // Normalize the accumulated normals
        for (int i = 0; i < normals.length; i += 3) {
            Vector3f normal = new Vector3f(normals[i], normals[i + 1], normals[i + 2]);
            if (normal.length() > 0) {
                normal.normalize();
                normals[i] = normal.x;
                normals[i + 1] = normal.y;
                normals[i + 2] = normal.z;
            }
        }
        
        return normals;
    }
}
