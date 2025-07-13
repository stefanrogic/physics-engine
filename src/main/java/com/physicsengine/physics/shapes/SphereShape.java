package com.physicsengine.physics.shapes;

import org.joml.Vector3f;

/**
 * Sphere collision shape
 */
public class SphereShape extends Shape {
    
    private float radius;
    
    public SphereShape(float radius) {
        super();
        this.radius = radius;
    }
    
    @Override
    public float calculateVolume() {
        return (4.0f / 3.0f) * (float) Math.PI * radius * radius * radius;
    }
    
    @Override
    public Vector3f calculateInertia(float mass) {
        float inertia = (2.0f / 5.0f) * mass * radius * radius;
        return new Vector3f(inertia, inertia, inertia);
    }
    
    @Override
    public boolean intersects(Shape other, Vector3f position1, Vector3f position2) {
        if (other instanceof SphereShape) {
            SphereShape otherSphere = (SphereShape) other;
            float distance = position1.distance(position2);
            return distance <= (this.radius + otherSphere.radius);
        } else if (other instanceof BoxShape) {
            // Simplified sphere-box collision (can be improved)
            BoxShape box = (BoxShape) other;
            Vector3f closest = new Vector3f();
            
            // Find closest point on box to sphere center
            closest.x = Math.max(position2.x - box.getHalfExtents().x, 
                        Math.min(position1.x, position2.x + box.getHalfExtents().x));
            closest.y = Math.max(position2.y - box.getHalfExtents().y, 
                        Math.min(position1.y, position2.y + box.getHalfExtents().y));
            closest.z = Math.max(position2.z - box.getHalfExtents().z, 
                        Math.min(position1.z, position2.z + box.getHalfExtents().z));
            
            float distance = position1.distance(closest);
            return distance <= radius;
        }
        return false;
    }
    
    public float getRadius() {
        return radius;
    }
    
    public void setRadius(float radius) {
        this.radius = radius;
    }
}
