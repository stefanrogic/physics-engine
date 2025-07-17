package com.stefanrogic.core.rendering;

import static org.lwjgl.opengl.GL20.*;

/**
 * Manages shader compilation and program creation
 */
public class ShaderManager {
    
    public static int createShaderProgram(String vertexSource, String fragmentSource) {
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexSource);
        glCompileShader(vertexShader);
        
        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentSource);
        glCompileShader(fragmentShader);
        
        int program = glCreateProgram();
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);
        glLinkProgram(program);
        
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
        
        return program;
    }
    
    public static class ShaderPrograms {
        public int gridShaderProgram;
        public int sunShaderProgram;
        public int planetShaderProgram;
        public int surfaceShaderProgram; // NEW SHADER FOR SURFACE VARIATIONS
        public int uiShaderProgram;
        
        public int gridMvpLocation;
        public int sunMvpLocation, sunColorLocation;
        public int planetMvpLocation, planetColorLocation, planetSunPosLocation, planetModelLocation;
        public int surfaceMvpLocation, surfaceModelLocation, surfaceSunPosLocation; // NEW LOCATIONS
        public int uiMvpLocation, uiColorLocation;
        
        public ShaderPrograms() {
            // GRID SHADER
            String gridVertexShader = """
                #version 330 core
                layout (location = 0) in vec3 aPos;
                uniform mat4 mvpMatrix;
                void main() {
                    gl_Position = mvpMatrix * vec4(aPos, 1.0);
                }
                """;
            
            String gridFragmentShader = """
                #version 330 core
                out vec4 FragColor;
                void main() {
                    FragColor = vec4(0.3, 0.3, 0.3, 1.0);
                }
                """;
            
            // SUN SHADER - NO LIGHTING, JUST EMISSIVE GLOW
            String sunVertexShader = """
                #version 330 core
                layout (location = 0) in vec3 aPos;
                
                uniform mat4 mvpMatrix;
                
                void main() {
                    gl_Position = mvpMatrix * vec4(aPos, 1.0);
                }
                """;
            
            String sunFragmentShader = """
                #version 330 core
                out vec4 FragColor;
                
                uniform vec3 sunColor;
                
                void main() {
                    // SUN RADIATES LIGHT - UNIFORM BRIGHT COLOR
                    FragColor = vec4(sunColor, 1.0);
                }
                """;
            
            // PLANET LIGHTING SHADER - RECEIVES LIGHT FROM SUN
            String planetVertexShader = """
                #version 330 core
                layout (location = 0) in vec3 aPos;
                layout (location = 1) in vec3 aNormal;
                
                uniform mat4 mvpMatrix;
                uniform mat4 modelMatrix;
                uniform vec3 sunPosition;
                
                out vec3 fragPos;
                out vec3 normal;
                out vec3 sunDir;
                
                void main() {
                    // TRANSFORM VERTEX TO WORLD SPACE
                    vec4 worldPos = modelMatrix * vec4(aPos, 1.0);
                    fragPos = worldPos.xyz;
                    
                    // TRANSFORM NORMAL TO WORLD SPACE (FOR PROPER LIGHTING)
                    normal = normalize(mat3(modelMatrix) * aNormal);
                    
                    // CALCULATE SUN DIRECTION IN WORLD SPACE
                    // Sun is at origin (0,0,0), planet is at its orbital position
                    sunDir = normalize(sunPosition - fragPos);
                    
                    gl_Position = mvpMatrix * vec4(aPos, 1.0);
                }
                """;
            
            String planetFragmentShader = """
                #version 330 core
                in vec3 fragPos;
                in vec3 normal;
                in vec3 sunDir;
                
                uniform vec3 planetColor;
                
                out vec4 FragColor;
                
                void main() {
                    // NORMALIZE THE NORMAL VECTOR
                    vec3 norm = normalize(normal);
                    
                    // CALCULATE BASIC DIFFUSE LIGHTING
                    // Since planets are spheres, the sun direction should work with surface normals
                    float NdotL = dot(norm, sunDir);
                    float diffuse = max(NdotL, 0.0);
                    
                    // ADD AMBIENT LIGHTING SO DARK SIDE IS STILL VISIBLE
                    float ambient = 0.2;
                    
                    // COMBINE LIGHTING (80% DIFFUSE + 20% AMBIENT)
                    float finalLighting = ambient + diffuse * 0.8;
                    
                    // APPLY LIGHTING TO PLANET COLOR
                    vec3 result = planetColor * finalLighting;
                    FragColor = vec4(result, 1.0);
                }
                """;
            
            // SURFACE SHADER - FOR EARTH AND MOON WITH VERTEX COLORS
            String surfaceVertexShader = """
                #version 330 core
                layout (location = 0) in vec3 aPos;
                layout (location = 1) in vec3 aNormal;
                layout (location = 2) in vec2 aTexCoord;
                layout (location = 3) in vec3 aSurfaceColor;
                
                uniform mat4 mvpMatrix;
                uniform mat4 modelMatrix;
                uniform vec3 sunPosition;
                
                out vec3 fragPos;
                out vec3 normal;
                out vec3 sunDir;
                out vec3 surfaceColor;
                
                void main() {
                    // TRANSFORM VERTEX TO WORLD SPACE
                    vec4 worldPos = modelMatrix * vec4(aPos, 1.0);
                    fragPos = worldPos.xyz;
                    
                    // TRANSFORM NORMAL TO WORLD SPACE
                    normal = normalize(mat3(modelMatrix) * aNormal);
                    
                    // CALCULATE SUN DIRECTION
                    sunDir = normalize(sunPosition - fragPos);
                    
                    // PASS SURFACE COLOR TO FRAGMENT SHADER
                    surfaceColor = aSurfaceColor;
                    
                    gl_Position = mvpMatrix * vec4(aPos, 1.0);
                }
                """;
            
            String surfaceFragmentShader = """
                #version 330 core
                in vec3 fragPos;
                in vec3 normal;
                in vec3 sunDir;
                in vec3 surfaceColor;
                
                out vec4 FragColor;
                
                void main() {
                    // NORMALIZE THE NORMAL VECTOR
                    vec3 norm = normalize(normal);
                    
                    // CALCULATE DIFFUSE LIGHTING
                    float NdotL = dot(norm, sunDir);
                    float diffuse = max(NdotL, 0.0);
                    
                    // ADD AMBIENT LIGHTING
                    float ambient = 0.25;
                    
                    // ENHANCED LIGHTING FOR SURFACE DETAILS
                    float finalLighting = ambient + diffuse * 0.75;
                    
                    // APPLY LIGHTING TO SURFACE COLOR (FROM VERTEX)
                    vec3 result = surfaceColor * finalLighting;
                    FragColor = vec4(result, 1.0);
                }
                """;
            
            // UI SHADER (2D OVERLAY)
            String uiVertexShader = """
                #version 330 core
                layout (location = 0) in vec2 aPos;
                uniform mat4 mvpMatrix;
                void main() {
                    gl_Position = mvpMatrix * vec4(aPos, 0.0, 1.0);
                }
                """;
            
            String uiFragmentShader = """
                #version 330 core
                out vec4 FragColor;
                uniform vec3 uiColor;
                void main() {
                    FragColor = vec4(uiColor, 0.8);
                }
                """;
            
            // Compile all shaders
            gridShaderProgram = createShaderProgram(gridVertexShader, gridFragmentShader);
            sunShaderProgram = createShaderProgram(sunVertexShader, sunFragmentShader);
            planetShaderProgram = createShaderProgram(planetVertexShader, planetFragmentShader);
            surfaceShaderProgram = createShaderProgram(surfaceVertexShader, surfaceFragmentShader);
            uiShaderProgram = createShaderProgram(uiVertexShader, uiFragmentShader);
            
            // Get uniform locations
            gridMvpLocation = glGetUniformLocation(gridShaderProgram, "mvpMatrix");
            sunMvpLocation = glGetUniformLocation(sunShaderProgram, "mvpMatrix");
            sunColorLocation = glGetUniformLocation(sunShaderProgram, "sunColor");
            planetMvpLocation = glGetUniformLocation(planetShaderProgram, "mvpMatrix");
            planetColorLocation = glGetUniformLocation(planetShaderProgram, "planetColor");
            planetSunPosLocation = glGetUniformLocation(planetShaderProgram, "sunPosition");
            planetModelLocation = glGetUniformLocation(planetShaderProgram, "modelMatrix");
            surfaceMvpLocation = glGetUniformLocation(surfaceShaderProgram, "mvpMatrix");
            surfaceModelLocation = glGetUniformLocation(surfaceShaderProgram, "modelMatrix");
            surfaceSunPosLocation = glGetUniformLocation(surfaceShaderProgram, "sunPosition");
            uiMvpLocation = glGetUniformLocation(uiShaderProgram, "mvpMatrix");
            uiColorLocation = glGetUniformLocation(uiShaderProgram, "uiColor");
        }
    }
    
    public ShaderPrograms createShaders() {
        return new ShaderPrograms();
    }
}
