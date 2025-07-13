# Physics Simulator

A 3D physics engine built with Java and LWJGL, featuring real-time physics simulation with collision detection and response.

## Features

- **Real-time Physics Simulation**: Gravity, velocity, acceleration, and collision dynamics
- **Collision Detection**: Sphere-sphere, box-box, and sphere-box collision detection
- **Collision Response**: Realistic collision response with restitution and impulse-based resolution
- **3D Rendering**: OpenGL-based rendering using LWJGL
- **Multiple Shape Types**: Support for spheres and boxes
- **Configurable Physics**: Adjustable gravity, damping, friction, and restitution

## Requirements

- Java 17 or higher
- Maven 3.6+
- LWJGL 3.3.3+ (automatically handled by Maven)

## Project Structure

```
src/main/java/com/physicsengine/
├── Main.java                           # Application entry point
├── core/
│   └── PhysicsEngine.java             # Main physics engine management
├── physics/
│   ├── PhysicsWorld.java              # Physics world simulation
│   ├── RigidBody.java                 # Rigid body representation
│   └── shapes/
│       ├── Shape.java                 # Base shape class
│       ├── SphereShape.java           # Sphere collision shape
│       └── BoxShape.java              # Box collision shape
└── rendering/
    ├── Window.java                    # GLFW window management
    ├── Renderer.java                  # OpenGL rendering
    └── ShaderProgram.java             # Shader program management
```

## Building and Running

### Build the project:

```bash
mvn clean compile
```

### Run the simulation:

```bash
mvn exec:java
```

### Alternative run method:

```bash
mvn clean compile exec:java -Dexec.mainClass="com.physicsengine.Main"
```

## Controls

- **ESC**: Exit the application
- **Window**: The simulation runs automatically with a sample scene

## Physics Features

### Rigid Bodies

- Mass-based dynamics
- Static and dynamic bodies
- Configurable material properties (restitution, friction)

### Collision Detection

- Broad-phase and narrow-phase collision detection
- Support for primitive shapes (spheres and boxes)
- Efficient collision algorithms

### Collision Response

- Impulse-based collision resolution
- Realistic bounce and friction effects
- Position correction to prevent object penetration

### Physics Integration

- Verlet integration for stable simulation
- Configurable timestep
- Gravity and force application

## Sample Scene

The default scene includes:

- A ground plane (static box)
- Several falling spheres with different colors
- Some falling boxes
- Realistic gravity and collision interactions

## Extending the Engine

### Adding New Shape Types

1. Extend the `Shape` base class
2. Implement required methods: `calculateVolume()`, `calculateInertia()`, `intersects()`
3. Add rendering support in `Renderer.java`
4. Update `RigidBody.render()` method

### Customizing Physics

- Modify gravity in `PhysicsWorld`
- Adjust material properties (restitution, friction) on individual `RigidBody` objects
- Tune damping and integration parameters

### Adding Forces

- Apply forces using `RigidBody.applyForce(Vector3f force)`
- Apply impulses using `RigidBody.applyImpulse(Vector3f impulse)`

## Dependencies

- **LWJGL**: Low-level graphics, audio, and parallel computing wrapper
- **JOML**: Java OpenGL Math Library for 3D mathematics
- **JUnit**: Testing framework (for future unit tests)

## Development Notes

- The engine uses OpenGL 3.3 Core Profile
- Physics simulation runs at 60 FPS target
- Rendering uses simple colored primitive shapes
- Memory management follows LWJGL best practices

## Future Improvements

- [ ] More sophisticated collision shapes (meshes, capsules, etc.)
- [ ] Constraints and joints
- [ ] Improved broad-phase collision detection (spatial partitioning)
- [ ] Better integration methods (RK4, etc.)
- [ ] Soft body dynamics
- [ ] Particle systems
- [ ] Performance optimizations
- [ ] User interaction (mouse picking, object manipulation)
- [ ] Scene loading and saving

## License

This project is open source and available under the MIT License.
