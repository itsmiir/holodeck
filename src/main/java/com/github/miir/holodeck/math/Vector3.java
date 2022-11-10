package com.github.miir.holodeck.math;

import net.minecraft.util.math.*;

public class Vector3 {
    public double x;
    public double y;
    public double z;
    public Vector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public Vector3(Vec3d v) {
        this(v.x, v.y, v.z);
    }
    public Vec3d toVec3d() {
        return new Vec3d(this.x, this.y, this.z);
    }
    public Vec3f toVec3f() {
        return new Vec3f(((float) this.x), ((float) this.y), ((float) this.z));
    }
    public double length() {
        return Math.sqrt(x*x + y*y +z*z);
    }
    public double distanceTo(Vector3 other) {
        double x1 = this.x-other.x;
        double y1 = this.y-other.y;
        double z1 = this.z-other.z;
        double a = x1*x1 + y1*y1 +z1*z1;
        return Math.sqrt(a);
    }
    public Vector3 plus(Vector3 other) {
        return new Vector3(this.x + other.x, this.y + other.y, this.z + other.z);
    }
    public Vector3 plus(double x, double y, double z) {
        return new Vector3(this.x + x, this.y + y, this.z + z);
    }
    public Vector3 minus(Vector3 other) {
        return new Vector3(this.x - other.x, this.y - other.y, this.z - other.z);
    }
    public Vector3 times(double d) {
        return new Vector3(this.x*d, this.y*d, this.z*d);
    }
    public double dot(Vector3 other) {
        return this.x*other.x + this.y*other.y + this.z*other.z;
    }
    public Vector3 cross(Vector3 other) {
        return new Vector3(
                this.y*other.z - this.z*other.y,
                this.z*other.x - this.x*other.z,
                this.x*other.y - this.y*other.x
                );
    }
    public Vector3 normalized() {
        return this.times(MathHelper.fastInverseSqrt(x * x + y * y + z * z));
    }

    public Vector3 transform(Matrix4f matrix) {
        Vector4f v = new Vector4f(((float) this.x), ((float) this.y), ((float) this.z), 1);
        v.transform(matrix);
        this.x = v.getX();
        this.y = v.getY();
        this.z = v.getZ();
        return this;
    }
    public Vector3 copy() {
        return new Vector3(this.x, this.y, this.z);
    }

    @Override
    public String toString() {
        return "Vector3{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }

    public Vector3 inverted() {
        return new Vector3(-x, -y, -z);
    }
}
