package com.physicsengine.physics.shapes;

import org.joml.Vector3f;

/**
 * Box collision shape
 */
public class BoxShape extends Shape {
    
    private Vector3f halfExtents;
    
    public BoxShape(Vector3f size) {
        super();
        this.halfExtents = new Vector3f(size.x / 2, size.y / 2, size.z / 2);
    }
    
    @Override
    public float calculateVolume() {
        return 8.0f * halfExtents.x * halfExtents.y * halfExtents.z;
    }
    
    @Override
    public Vector3f calculateInertia(float mass) {
        float x2 = halfExtents.x * halfExtents.x * 4;
        float y2 = halfExtents.y * halfExtents.y * 4;
        float z2 = halfExtents.z * halfExtents.z * 4;
        
        float ix = (mass / 12.0f) * (y2 + z2);
        float iy = (mass / 12.0f) * (x2 + z2);
        float iz = (mass / 12.0f) * (x2 + y2);
        
        return new Vector3f(ix, iy, iz);
    }
    
    @Override
    public boolean intersects(Shape other, Vector3f position1, Vector3f position2) {
        if (other instanceof BoxShape) {
            BoxShape otherBox = (BoxShape) other;
            
            // AABB intersection test
            Vector3f min1 = new Vector3f(position1).sub(halfExtents);
            Vector3f max1 = new Vector3f(position1).add(halfExtents);
            Vector3f min2 = new Vector3f(position2).sub(otherBox.halfExtents);
            Vector3f max2 = new Vector3f(position2).add(otherBox.halfExtents);
            
            return (min1.x <= max2.x && max1.x >= min2.x) &&
                   (min1.y <= max2.y && max1.y >= min2.y) &&
                   (min1.z <= max2.z && max1.z >= min2.z);
                   
        } else if (other instanceof SphereShape) {
            // Delegate to sphere's intersection method
            return other.intersects(this, position2, position1);
        }
        return false;
    }
    
    public Vector3f getHalfExtents() {
        return new Vector3f(halfExtents);
    }
    
    public Vector3f getSize() {
        return new Vector3f(halfExtents.x * 2, halfExtents.y * 2, halfExtents.z * 2);
    }
    
    public void setSize(Vector3f size) {
        this.halfExtents.set(size.x / 2, size.y / 2, size.z / 2);
    }
}
