package com.physicsengine.physics;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the physics simulation world
 */
public class PhysicsWorld {
    
    private List<RigidBody> rigidBodies;
    private Vector3f gravity;
    private float damping;
    
    public PhysicsWorld() {
        this.rigidBodies = new ArrayList<>();
        this.gravity = new Vector3f(0, -9.81f, 0);
        this.damping = 0.98f; // Air resistance/damping factor
    }
    
    public void addRigidBody(RigidBody rigidBody) {
        rigidBodies.add(rigidBody);
    }
    
    public void removeRigidBody(RigidBody rigidBody) {
        rigidBodies.remove(rigidBody);
    }
    
    public void step(float deltaTime) {
        // Apply gravity to all non-static bodies
        for (RigidBody body : rigidBodies) {
            if (!body.isStatic()) {
                Vector3f gravityForce = new Vector3f(gravity).mul(body.getMass());
                body.applyForce(gravityForce);
            }
        }
        
        // Check for collisions and resolve them
        resolveCollisions();
        
        // Update all rigid bodies
        for (RigidBody body : rigidBodies) {
            body.update(deltaTime);
            
            // Apply damping
            if (!body.isStatic()) {
                body.getVelocity().mul(damping);
            }
        }
    }
    
    private void resolveCollisions() {
        for (int i = 0; i < rigidBodies.size(); i++) {
            for (int j = i + 1; j < rigidBodies.size(); j++) {
                RigidBody bodyA = rigidBodies.get(i);
                RigidBody bodyB = rigidBodies.get(j);
                
                if (bodyA.intersects(bodyB)) {
                    resolveCollision(bodyA, bodyB);
                }
            }
        }
    }
    
    private void resolveCollision(RigidBody bodyA, RigidBody bodyB) {
        // Simple collision resolution
        Vector3f relativeVelocity = new Vector3f(bodyA.getVelocity()).sub(bodyB.getVelocity());
        Vector3f collisionNormal = new Vector3f(bodyA.getPosition()).sub(bodyB.getPosition()).normalize();
        
        // Calculate relative velocity in collision normal direction
        float velAlongNormal = relativeVelocity.dot(collisionNormal);
        
        // Don't resolve if velocities are separating
        if (velAlongNormal > 0) return;
        
        // Calculate restitution
        float restitution = Math.min(bodyA.getRestitution(), bodyB.getRestitution());
        
        // Calculate impulse scalar
        float impulseScalar = -(1 + restitution) * velAlongNormal;
        impulseScalar /= bodyA.getInverseMass() + bodyB.getInverseMass();
        
        // Apply impulse
        Vector3f impulse = new Vector3f(collisionNormal).mul(impulseScalar);
        
        if (!bodyA.isStatic()) {
            bodyA.applyImpulse(new Vector3f(impulse).mul(bodyA.getInverseMass()));
        }
        if (!bodyB.isStatic()) {
            bodyB.applyImpulse(new Vector3f(impulse).mul(-bodyB.getInverseMass()));
        }
        
        // Position correction to prevent sinking
        separateBodies(bodyA, bodyB, collisionNormal);
    }
    
    private void separateBodies(RigidBody bodyA, RigidBody bodyB, Vector3f normal) {
        // Simple separation - move bodies apart along collision normal
        float separationAmount = 0.01f; // Small amount to prevent overlap
        
        if (!bodyA.isStatic() && !bodyB.isStatic()) {
            // Both bodies are dynamic, move both
            Vector3f separation = new Vector3f(normal).mul(separationAmount * 0.5f);
            bodyA.setPosition(new Vector3f(bodyA.getPosition()).add(separation));
            bodyB.setPosition(new Vector3f(bodyB.getPosition()).sub(separation));
        } else if (!bodyA.isStatic()) {
            // Only bodyA is dynamic
            Vector3f separation = new Vector3f(normal).mul(separationAmount);
            bodyA.setPosition(new Vector3f(bodyA.getPosition()).add(separation));
        } else if (!bodyB.isStatic()) {
            // Only bodyB is dynamic
            Vector3f separation = new Vector3f(normal).mul(separationAmount);
            bodyB.setPosition(new Vector3f(bodyB.getPosition()).sub(separation));
        }
    }
    
    public void cleanup() {
        rigidBodies.clear();
    }
    
    // Getters and setters
    public Vector3f getGravity() {
        return new Vector3f(gravity);
    }
    
    public void setGravity(Vector3f gravity) {
        this.gravity.set(gravity);
    }
    
    public float getDamping() {
        return damping;
    }
    
    public void setDamping(float damping) {
        this.damping = damping;
    }
    
    public List<RigidBody> getRigidBodies() {
        return new ArrayList<>(rigidBodies);
    }
    
    public int getBodyCount() {
        return rigidBodies.size();
    }
}
