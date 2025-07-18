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
        public int planetDiffuseTextureLocation, planetUseTextureLocation;
        public int planetCloudsTextureLocation, planetUseCloudsLocation;
        public int planetBumpTextureLocation, planetUseBumpLocation;
        public int planetNightLightsTextureLocation, planetUseNightLightsLocation;
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
                layout (location = 2) in vec2 aTexCoord;
                
                uniform mat4 mvpMatrix;
                uniform mat4 modelMatrix;
                uniform vec3 sunPosition;
                
                out vec3 fragPos;
                out vec3 normal;
                out vec3 sunDir;
                out vec2 texCoord;
                
                void main() {
                    // TRANSFORM VERTEX TO WORLD SPACE
                    vec4 worldPos = modelMatrix * vec4(aPos, 1.0);
                    fragPos = worldPos.xyz;
                    
                    // TRANSFORM NORMAL TO WORLD SPACE (FOR PROPER LIGHTING)
                    normal = normalize(mat3(modelMatrix) * aNormal);
                    
                    // CALCULATE SUN DIRECTION IN WORLD SPACE
                    // Sun is at origin (0,0,0), planet is at its orbital position
                    sunDir = normalize(sunPosition - fragPos);
                    
                    // PASS TEXTURE COORDINATES TO FRAGMENT SHADER
                    texCoord = aTexCoord;
                    
                    gl_Position = mvpMatrix * vec4(aPos, 1.0);
                }
                """;
            
            String planetFragmentShader = """
                #version 330 core
                in vec3 fragPos;
                in vec3 normal;
                in vec3 sunDir;
                in vec2 texCoord;
                
                uniform vec3 planetColor;
                uniform sampler2D diffuseTexture;
                uniform sampler2D cloudsTexture;
                uniform sampler2D bumpTexture;
                uniform sampler2D nightLightsTexture;
                uniform bool useTexture;
                uniform bool useClouds;
                uniform bool useBump;
                uniform bool useNightLights;
                
                out vec4 FragColor;
                
                void main() {
                    // START WITH BASE NORMAL
                    vec3 norm = normalize(normal);
                    
                    // APPLY BUMP MAPPING IF AVAILABLE
                    if (useBump) {
                        // Sample bump map (grayscale height map)
                        float bumpHeight = texture(bumpTexture, texCoord).r;
                        
                        // Create simple bump effect by perturbing the normal
                        // This is a simplified approach - for more accuracy, we'd need tangent space
                        float bumpStrength = 0.1; // Adjust this for more/less bump effect
                        vec3 bumpOffset = vec3(0.0, 0.0, (bumpHeight - 0.5) * bumpStrength);
                        norm = normalize(norm + bumpOffset);
                    }
                    
                    // CALCULATE BASIC DIFFUSE LIGHTING
                    // Since planets are spheres, the sun direction should work with surface normals
                    float NdotL = dot(norm, sunDir);
                    float diffuse = max(NdotL, 0.0);
                    
                    // ADD AMBIENT LIGHTING SO DARK SIDE IS STILL VISIBLE
                    float ambient = 0.18; // Lower ambient lighting for more dramatic effect
                    
                    // COMBINE LIGHTING (82% DIFFUSE + 18% AMBIENT)
                    float finalLighting = ambient + diffuse * 0.82;
                    
                    // GET BASE COLOR FROM TEXTURE OR UNIFORM
                    vec3 baseColor;
                    if (useTexture) {
                        // Get day side texture (diffuse)
                        vec3 dayColor = texture(diffuseTexture, texCoord).rgb;
                        
                        // Get night side texture (city lights)
                        vec3 nightColor = vec3(0.0);
                        if (useNightLights) {
                            nightColor = texture(nightLightsTexture, texCoord).rgb;
                        }
                        
                        // Create a smooth transition between day and night
                        // NdotL ranges from -1 to 1, we want smooth transition around 0
                        float dayNightTransition = smoothstep(-0.1, 0.1, NdotL);
                        
                        // Blend between night lights and day texture
                        baseColor = mix(nightColor, dayColor, dayNightTransition);
                    } else {
                        baseColor = planetColor;
                    }
                    
                    // APPLY LIGHTING TO BASE COLOR
                    vec3 result;
                    if (useTexture && useNightLights) {
                        // For night lights, we want them to glow without being affected by lighting
                        vec3 dayColor = texture(diffuseTexture, texCoord).rgb * finalLighting;
                        vec3 nightColor = texture(nightLightsTexture, texCoord).rgb;
                        
                        // Blend between lit day side and glowing night side
                        float dayNightTransition = smoothstep(-0.1, 0.1, NdotL);
                        result = mix(nightColor, dayColor, dayNightTransition);
                    } else {
                        // Standard lighting for planets without night lights
                        result = baseColor * finalLighting;
                    }
                    
                    // ADD CLOUDS LAYER IF AVAILABLE
                    if (useClouds) {
                        vec4 cloudsColor = texture(cloudsTexture, texCoord);
                        // Use only the red channel as cloud density
                        float cloudDensity = cloudsColor.r;
                        
                        // Make clouds slightly off-white for better visibility
                        vec3 cloudColor = vec3(0.9, 0.95, 1.0);
                        
                        // Blend clouds at high opacity (75% maximum) for strong visibility
                        result = mix(result, cloudColor * finalLighting, cloudDensity * 0.75);
                    }
                    
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
            planetDiffuseTextureLocation = glGetUniformLocation(planetShaderProgram, "diffuseTexture");
            planetUseTextureLocation = glGetUniformLocation(planetShaderProgram, "useTexture");
            planetCloudsTextureLocation = glGetUniformLocation(planetShaderProgram, "cloudsTexture");
            planetUseCloudsLocation = glGetUniformLocation(planetShaderProgram, "useClouds");
            planetBumpTextureLocation = glGetUniformLocation(planetShaderProgram, "bumpTexture");
            planetUseBumpLocation = glGetUniformLocation(planetShaderProgram, "useBump");
            planetNightLightsTextureLocation = glGetUniformLocation(planetShaderProgram, "nightLightsTexture");
            planetUseNightLightsLocation = glGetUniformLocation(planetShaderProgram, "useNightLights");
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
