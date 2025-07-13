package com.physicsengine.physics.shapes;

import org.joml.Vector3f;

/**
 * Base class for collision shapes
 */
public abstract class Shape {
    
    protected Vector3f center;
    
    public Shape() {
        this.center = new Vector3f(0, 0, 0);
    }
    
    public abstract float calculateVolume();
    public abstract Vector3f calculateInertia(float mass);
    public abstract boolean intersects(Shape other, Vector3f position1, Vector3f position2);
    
    public Vector3f getCenter() {
        return new Vector3f(center);
    }
    
    public void setCenter(Vector3f center) {
        this.center.set(center);
    }
}
