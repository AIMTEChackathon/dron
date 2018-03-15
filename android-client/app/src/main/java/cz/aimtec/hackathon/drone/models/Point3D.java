package cz.aimtec.hackathon.drone.models;

/**
 * Created by Jan Klik on 10.3.2018.
 */

public class Point3D {
    private float x;
    private float y;
    private float z;

    public Point3D(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    @Override
    public String toString() {
        return "x:" + x + "; y:" + y + "; z:" + z;
    }
}
