package com.physicsengine.core;

import com.physicsengine.physics.PhysicsWorld;
import com.physicsengine.physics.RigidBody;
import com.physicsengine.physics.shapes.BoxShape;
import com.physicsengine.rendering.Renderer;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Main physics engine class that manages the physics simulation
 */
public class PhysicsEngine {
    
    private PhysicsWorld physicsWorld;
    private List<RigidBody> rigidBodies;
    private boolean initialized = false;
    
    public PhysicsEngine() {
        rigidBodies = new ArrayList<>();
    }
    
    public void init() {
        physicsWorld = new PhysicsWorld();
        physicsWorld.setGravity(new Vector3f(0, -9.81f, 0));
        
        // Create some sample objects for demonstration
        createSampleScene();
        
        initialized = true;
        System.out.println("Physics Engine initialized");
    }
    
    private void createSampleScene() {
        // Create ground plane (large static box that acts as the floor)
        RigidBody ground = new RigidBody(
            new BoxShape(new Vector3f(10, 0.2f, 10)), // 10x0.2x10 units
            new Vector3f(0, -1, 0),  // Position at Y = -1
            0.0f // mass = 0 means static (won't move)
        );
        ground.setColor(new Vector3f(0.3f, 0.7f, 0.3f)); // Green ground
        addRigidBody(ground);
        
        // Create just ONE falling box for learning
        RigidBody fallingBox = new RigidBody(
            new BoxShape(new Vector3f(1.0f, 1.0f, 1.0f)), // 1x1x1 cube
            new Vector3f(0, 5, 0),  // Start 5 units above ground
            2.0f // mass = 2kg (will fall due to gravity)
        );
        fallingBox.setColor(new Vector3f(0.8f, 0.2f, 0.2f)); // Red box
        fallingBox.setRestitution(0.7f); // Make it bouncy!
        addRigidBody(fallingBox);
        
        System.out.println("Simple scene created: 1 ground + 1 falling box");
    }
    
    public void addRigidBody(RigidBody rigidBody) {
        rigidBodies.add(rigidBody);
        physicsWorld.addRigidBody(rigidBody);
    }
    
    public void removeRigidBody(RigidBody rigidBody) {
        rigidBodies.remove(rigidBody);
        physicsWorld.removeRigidBody(rigidBody);
    }
    
    public void update(float deltaTime) {
        if (!initialized) return;
        
        physicsWorld.step(deltaTime);
    }
    
    public void render(Renderer renderer) {
        if (!initialized) return;
        
        for (RigidBody body : rigidBodies) {
            body.render(renderer);
        }
    }
    
    public void cleanup() {
        if (physicsWorld != null) {
            physicsWorld.cleanup();
        }
        rigidBodies.clear();
        System.out.println("Physics Engine cleaned up");
    }
    
    public PhysicsWorld getPhysicsWorld() {
        return physicsWorld;
    }
    
    public List<RigidBody> getRigidBodies() {
        return new ArrayList<>(rigidBodies);
    }
}
