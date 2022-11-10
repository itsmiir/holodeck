package com.github.miir.holodeck.math;

import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

public class QuadPos {
    // represents a quadrilateral in R3
    // ccw from top right: pp, np, nn, pn (p refers to positive, n to negative; x or y)
    public Vector3 pp; // uv 1,1 ("top right") of the quad
    public Vector3 np; // uv 0,1
    public Vector3 nn; // uv 0,0
    public Vector3 pn; // uv 1,0
    private double[] transform;
    private double[] invTransform;

    public QuadPos(Vector3 pp, Vector3 np, Vector3 nn, Vector3 pn) {
        this.pp = pp;
        this.np = np;
        this.nn = nn;
        this.pn = pn;
        this.updateTransforms();
    }
    public QuadPos(Vec3d pp, Vec3d np, Vec3d nn, Vec3d pn) {
        this(new Vector3(pp), new Vector3(np), new Vector3(nn),new Vector3(pn));
    }
    private void updateTransforms() {
        Vector3 center = this.center();
        Vector3 a = this.pp.minus(center);
        Vector3 c = this.pn.minus(center);
        Vector3 d = this.np.minus(center);
        Vector3 xP = a.minus(d).normalized();
        Vector3 yP = a.minus(c).normalized();
        Vector3 zP = xP.cross(yP);
        this.transform = new double[] {
                zP.z, xP.z, yP.z, // 0 1 2
                zP.x, xP.x, yP.x, // 3 4 5
                zP.y, xP.y, yP.y  // 6 7 8
        };
        this.invTransform = this.invert();
    }
    private double[] invert() {
        double f = this.transform[4] * this.transform[8] - this.transform[7] * this.transform[5];
        double g = -(this.transform[1] * this.transform[8] - this.transform[7] * this.transform[2]);
        double h = this.transform[1] * this.transform[5] - this.transform[4] * this.transform[2];
        double i = -(this.transform[3] * this.transform[8] - this.transform[3] * this.transform[5]);
        double j = this.transform[0] * this.transform[8] - this.transform[3] * this.transform[2];
        double k = -(this.transform[0] * this.transform[5] - this.transform[3] * this.transform[2]);
        double l = this.transform[3] * this.transform[7] - this.transform[3] * this.transform[4];
        double m = -(this.transform[0] * this.transform[7] - this.transform[3] * this.transform[1]);
        double n = this.transform[0] * this.transform[4] - this.transform[3] * this.transform[1];
        double o = this.transform[0] * f + this.transform[3] * g + this.transform[3] * h;
        return new double[]{f*o, g*o, h*o, i*o, j*o, k*o, l*o, m*o, n*o};
    }

    public Vector3 normal() {
        Vector3 l1 = this.pp.minus(this.np);
        Vector3 l2 = this.pp.minus(this.pn);
        return l1.cross(l2);
    }
    public QuadPos transform(Matrix4f by, Vector3 around) {
        Vector3 a = this.pp.minus(around);
        Vector3 b = this.pn.minus(around);
        Vector3 c = this.nn.minus(around);
        Vector3 d = this.np.minus(around);
        a.transform(by);
        b.transform(by);
        c.transform(by);
        d.transform(by);
        this.pp = a.plus(around);
        this.pn = b.plus(around);
        this.nn = c.plus(around);
        this.np = d.plus(around);
        this.updateTransforms();
        return this;
    }
    public QuadPos translate(Vector3 by) {
        return new QuadPos(
                this.pp.plus(by),
                this.np.plus(by),
                this.nn.plus(by),
                this.pn.plus(by)
        );
    }

    public Vector3 center() {
        return new Vector3(
                (pp.x + pn.x + nn.x + np.x) / 4d,
                (pp.y + pn.y + nn.y + np.y) / 4d,
                (pp.z + pn.z + nn.z + np.z) / 4d);
    }

    public QuadPos scale(double by) {
        Vector3 center = this.center();
        Vector3 a = this.pp.minus(center);
        Vector3 b = this.pn.minus(center);
        Vector3 c = this.nn.minus(center);
        Vector3 d = this.np.minus(center);
        a = a.times(by);
        b = b.times(by);
        c = c.times(by);
        d = d.times(by);
        this.pp = a.plus(center);
        this.pn = b.plus(center);
        this.nn = c.plus(center);
        this.np = d.plus(center);
        this.updateTransforms();
        return this;
    }
    public QuadPos resize(double w, double h) {
        Vector3 center = this.center();
        Vector3 a = this.pp.minus(center);
        Vector3 c = this.pn.minus(center);
        Vector3 d = this.np.minus(center);
        Vector3 xP = a.minus(d).normalized();
        Vector3 yP = a.minus(c).normalized();
        this.pp = xP.times(w/2).plus(yP.times(h/2)).plus(center);
        this.np = xP.times(-w/2).plus(yP.times(h/2)).plus(center);
        this.nn = xP.times(-w/2).plus(yP.times(-h/2)).plus(center);
        this.pn = xP.times(w/2).plus(yP.times(-h/2)).plus(center);
        this.updateTransforms();
        return this;
    }

    public Vector3 rebasePoint(Vector3 p) {
        Vector3 center = this.center();
        Vector3 a = this.pp.minus(center);
        Vector3 c = this.pn.minus(center);
        Vector3 d = this.np.minus(center);
        Vector3 xP = a.minus(d).normalized();
        Vector3 yP = a.minus(c).normalized();
        Vector3 zP = xP.cross(yP);
        return new Vector3(
                p.x*xP.x+p.y*yP.x+p.z*zP.x,
                p.x*xP.y+p.y*yP.y+p.z*zP.y,
                p.x*xP.z+p.y*yP.z+p.z*zP.z);
    }

    public Vector3 rebaseAbs(Vector3 p) {
        Vector3 center = this.center();
//        p = p.plus(center);
        Vector3 xP = this.rebasePoint(new Vector3(1, 0, 0));
        Vector3 yP = this.rebasePoint(new Vector3(0, 1, 0));
        Vector3 zP = this.rebasePoint(new Vector3(0, 0, 1));
//        return new Vector3(
//                p.x*xP.x+p.y*yP.x+p.z*zP.x,
//                p.x*xP.y+p.y*yP.y+p.z*zP.y,
//                p.x*xP.z+p.y*yP.z+p.z*zP.z).plus(center);
        return new Vector3(
//                zP.z, xP.z, yP.z, // 0 1 2
//                zP.x, xP.x, yP.x, // 3 4 5
//                zP.y, xP.y, yP.y  // 6 7 8
                p.z*this.invTransform[3]+p.x*this.invTransform[4]+p.y*this.invTransform[5],
                p.z*this.invTransform[6]+p.x*this.invTransform[7]+p.y*this.invTransform[8],
                p.z*this.invTransform[0]+p.x*this.invTransform[1]+p.y*this.invTransform[2]);
    }

    public QuadPos setPos(Vector3 pos) {
        return this.translate(this.center().inverted().plus(pos));
    }
}
