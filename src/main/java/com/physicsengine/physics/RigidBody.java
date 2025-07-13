package com.physicsengine.physics;

import com.physicsengine.physics.shapes.Shape;
import com.physicsengine.physics.shapes.SphereShape;
import com.physicsengine.physics.shapes.BoxShape;
import com.physicsengine.rendering.Renderer;
import org.joml.Vector3f;

/**
 * Represents a rigid body in the physics simulation
 */
public class RigidBody {
    
    private Shape shape;
    private Vector3f position;
    private Vector3f velocity;
    private Vector3f acceleration;
    private Vector3f angularVelocity;
    private Vector3f inertia;
    private Vector3f color;
    
    private float mass;
    private float inverseMass;
    private boolean isStatic;
    private float restitution;
    private float friction;
    
    public RigidBody(Shape shape, Vector3f position, float mass) {
        this.shape = shape;
        this.position = new Vector3f(position);
        this.velocity = new Vector3f(0, 0, 0);
        this.acceleration = new Vector3f(0, 0, 0);
        this.angularVelocity = new Vector3f(0, 0, 0);
        this.color = new Vector3f(1, 1, 1); // Default white
        
        this.mass = mass;
        this.isStatic = (mass == 0.0f);
        this.inverseMass = isStatic ? 0.0f : 1.0f / mass;
        this.restitution = 0.6f; // Default bounce
        this.friction = 0.3f; // Default friction
        
        if (!isStatic) {
            this.inertia = shape.calculateInertia(mass);
        } else {
            this.inertia = new Vector3f(0, 0, 0);
        }
    }
    
    public void update(float deltaTime) {
        if (isStatic) return;
        
        // Integrate velocity
        velocity.add(acceleration.x * deltaTime, acceleration.y * deltaTime, acceleration.z * deltaTime);
        
        // Integrate position
        position.add(velocity.x * deltaTime, velocity.y * deltaTime, velocity.z * deltaTime);
        
        // Reset acceleration for next frame
        acceleration.set(0, 0, 0);
    }
    
    public void applyForce(Vector3f force) {
        if (isStatic) return;
        
        // F = ma, so a = F/m
        acceleration.add(force.x * inverseMass, force.y * inverseMass, force.z * inverseMass);
    }
    
    public void applyImpulse(Vector3f impulse) {
        if (isStatic) return;
        
        // Change in momentum: Δp = J, so Δv = J/m
        velocity.add(impulse.x * inverseMass, impulse.y * inverseMass, impulse.z * inverseMass);
    }
    
    public boolean intersects(RigidBody other) {
        return shape.intersects(other.shape, this.position, other.position);
    }
    
    public void render(Renderer renderer) {
        if (shape instanceof SphereShape) {
            SphereShape sphere = (SphereShape) shape;
            renderer.renderSphere(position, sphere.getRadius(), color);
        } else if (shape instanceof BoxShape) {
            BoxShape box = (BoxShape) shape;
            renderer.renderBox(position, box.getSize(), color);
        }
    }
    
    // Getters and setters
    public Shape getShape() {
        return shape;
    }
    
    public Vector3f getPosition() {
        return new Vector3f(position);
    }
    
    public void setPosition(Vector3f position) {
        this.position.set(position);
    }
    
    public Vector3f getVelocity() {
        return new Vector3f(velocity);
    }
    
    public void setVelocity(Vector3f velocity) {
        this.velocity.set(velocity);
    }
    
    public Vector3f getAcceleration() {
        return new Vector3f(acceleration);
    }
    
    public void setAcceleration(Vector3f acceleration) {
        this.acceleration.set(acceleration);
    }
    
    public Vector3f getAngularVelocity() {
        return new Vector3f(angularVelocity);
    }
    
    public void setAngularVelocity(Vector3f angularVelocity) {
        this.angularVelocity.set(angularVelocity);
    }
    
    public Vector3f getColor() {
        return new Vector3f(color);
    }
    
    public void setColor(Vector3f color) {
        this.color.set(color);
    }
    
    public float getMass() {
        return mass;
    }
    
    public float getInverseMass() {
        return inverseMass;
    }
    
    public boolean isStatic() {
        return isStatic;
    }
    
    public float getRestitution() {
        return restitution;
    }
    
    public void setRestitution(float restitution) {
        this.restitution = restitution;
    }
    
    public float getFriction() {
        return friction;
    }
    
    public void setFriction(float friction) {
        this.friction = friction;
    }
    
    public Vector3f getInertia() {
        return new Vector3f(inertia);
    }
}
