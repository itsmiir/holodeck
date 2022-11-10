package com.github.miir.holodeck.api;

import com.github.miir.holodeck.Holodeck;
import com.github.miir.holodeck.impl.HologramImpl;
import com.github.miir.holodeck.math.QuadPos;
import com.github.miir.holodeck.math.Vector3;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

public class Hologram {

    private static final Identifier MISSING_TEXTURE = Holodeck.id("textures/missing.png");

    private QuadPos quad;
    private Identifier texture;
    private int color;
    private float emissivity;
    private float opacity;
    private Vector3 pos;
    private double interactionRange;
    private double renderRange;
    private Consumer<HologramContext> hoverListener;
    private Consumer<HologramContext> attackListener;
    private Consumer<HologramContext> useListener;
    private Consumer<HologramContext> contactListener;
    private Consumer<HologramContext> ticker;
    private boolean doubleSided;
    private double height;
    private double width;
    private final int id;

    private Hologram(double x, double y, double z, QuadPos pos) {
        this.quad = pos;
        this.pos = new Vector3(x, y, z);
        this.emissivity = 0;
        this.opacity = 1;
        this.color = 0xFFFFFFFF;
        this.texture = null;
        this.id = this.hashCode();
    }

    public QuadPos getQuad() {return this.quad;}
    public void setQuad(QuadPos quad) {this.quad = quad;}
    public void rotate(Matrix4f matrix) {
        this.quad.transform(matrix, this.getPos());
    }
    public void scale(double by) {
        this.quad.scale(by);
        this.width *= by;
        this.height *= by;
    }
    public void resize(double w, double h) {
        this.quad.resize(w, h);
        this.width = w;
        this.height = h;
    }

    public void setPos(Vector3 pos) {
        this.pos = pos;
        this.quad = this.quad.setPos(pos);
    }
    public Vector3 getPos() {return this.pos;}
    public boolean isDoubleSided() {return this.doubleSided;}
    public void setDoubleSided(boolean b) {this.doubleSided=b;}

    public double getInteractionRange() {return interactionRange;}
    public void setInteractionRange(double interactionRange) {this.interactionRange = interactionRange;}
    public double getRenderRange() {return renderRange;}
    public void setRenderRange(double renderRange) {this.renderRange = renderRange;}

    public Consumer<HologramContext> getHoverListener() {return hoverListener == null ? (context -> {}) : hoverListener;}
    public void setHoverListener(Consumer<HologramContext> hoverListener) {this.hoverListener = hoverListener;}
    public Consumer<HologramContext> getAttackListener() {return attackListener == null ? (context -> {}) : attackListener;}
    public void setAttackListener(Consumer<HologramContext> attackListener) {this.attackListener = attackListener;}
    public Consumer<HologramContext> getUseListener() {return useListener == null ? (context -> {}) : useListener;}
    public void setUseListener(Consumer<HologramContext> useListener) {this.useListener = useListener;}
    public Consumer<HologramContext> getContactListener() {return contactListener == null ? (context -> {}) : contactListener;}
    public void setContactListener(Consumer<HologramContext> contactListener) {this.contactListener = contactListener;}

    public void setColor(int color) {this.color = color;}
    public int getColor() {return this.color;}
    public void setTexture(Identifier texture) {this.texture = texture;}
    public Identifier getTexture() {return this.texture == null ? MISSING_TEXTURE : this.texture;}
    public float getEmissivity() {return emissivity;}
    public void setEmissivity(float emissivity) {this.emissivity = emissivity;}
    public float getOpacity() {return opacity;}
    public void setOpacity(float opacity) {this.opacity = opacity;}

    /**
     * renders a hologram on the clientside. this code should not be called on the server.
     * @param context a . you should be able to get this
     * @return true if the hologram was successfully rendered.
     */
    @Environment(EnvType.CLIENT)
    public boolean render(WorldRenderContext context) {
        return HologramImpl.render(this, context.camera().getPos(), context.matrixStack(), context.gameRenderer().getClient());
    }

    /**
     * creates a hologram that can be interacted with in the world.
     * @param pos the absolute position to spawn the center of the hologram.
     * @param rotation the hologram will be rotated around its center, transformed by this matrix. leave null for no
     *                 rotation, or pass a <code>new Matrix4f()</code>. default orientation is facing the positive x
     *                 direction.
     * @param width the width of the hologram, in meters.
     * @param height the height of the hologram, in meters.
     * @param doubleSided whether the hologram should render from both sides
     * @param renderRange the distance to the center of the hologram that a player needs to be within for it to render
     *                    to their client.
     * @param interactionRange the distance to the center of the hologram that a player needs to be within to be able to
     *                        interact (click and hover).
     * @param clickListener a listener that will run whenever the hologram is attacked or punched. a
     *                     <code>HologramContext</code> is provided.
     * @param rightClickListener a listener that will run whenever the hologram is right-clicked. a
     *                          <code>HologramContext</code> is provided.
     * @param hoverListener a listener that will run when the hologram is hovered over with the cursor. a
     *                     <code>HologramContext</code> is provided.
     * @param contactListener a listener that will run when the hologram comes into contact with an entity. a
     *                       <code>HologramContext</code> is provided.
     * @return a new hologram.
     */
    public static Hologram interactable(
            Vector3 pos, Matrix4f rotation,
            double width, double height, boolean doubleSided,
            double renderRange, double interactionRange,
            @Nullable Consumer<HologramContext> clickListener,
            @Nullable Consumer<HologramContext> rightClickListener,
            @Nullable Consumer<HologramContext> hoverListener,
            @Nullable Consumer<HologramContext> contactListener) {
        Vector3 pp = new Vector3(pos.x + width / 2, pos.y + height / 2, pos.z);
        Vector3 np = new Vector3(pos.x - width / 2, pos.y + height / 2, pos.z);
        Vector3 nn = new Vector3(pos.x - width / 2, pos.y - height / 2, pos.z);
        Vector3 pn = new Vector3(pos.x + width / 2, pos.y - height / 2, pos.z);
        Hologram h = new Hologram(pos.x, pos.y,  pos.z, new QuadPos(pp, np, nn, pn));
        h.rotate(rotation == null ? new Matrix4f(Vec3f.POSITIVE_Y.getDegreesQuaternion(0)) : rotation);
        h.pos = pos;
        h.width = width;
        h.height = height;
        h.doubleSided = doubleSided;
        h.setAttackListener(clickListener);
        h.setHoverListener(hoverListener);
        h.setUseListener(rightClickListener);
        h.setContactListener(contactListener);
        h.setRenderRange(renderRange);
        h.setInteractionRange(interactionRange);
        return h;
    }

    public void tick() {

    }
    public Vector3 rebasePoint(Vector3 point, boolean relative) {
        return this.quad.rebasePoint(relative ? point : point.minus(this.getPos()));
    }
    public Vector3 rebaseAbs(Vector3 point) {
        return this.quad.rebaseAbs(point);
    }
    private boolean containsPoint(Vector3 point) {
        Vector3 p = this.rebasePoint(point, false);
        return Math.abs(p.x) <= this.width / 2 && Math.abs(p.y) <= this.height / 2 && Math.abs(p.z) < 1e-5;
    }
    public Hologram translate(Vector3 by) {
        this.quad.translate(by);
        return this;
    }

    public boolean isBeingLookedAtBy(LivingEntity entity) {
        Vector3 xUnit = new Vector3(1, 0, 0);
        Vector3 zUnit = new Vector3(0, 0, 1);
        Vector3 entityPos = new Vector3(entity.getEyePos());
        double distance = this.pos.minus(entityPos).length();
        if (distance < this.getRenderRange()) {
//            float yaw = (float) Math.toRadians(entity.getYaw());
//            float pitch = (float) Math.toRadians(entity.getPitch());
//            Vector3 look = this.rebasePoint(new Vector3(Math.sin(yaw)*Math.cos(pitch), -Math.sin(pitch), Math.cos(yaw)*Math.cos(pitch)), true);
            Vector3 rPos = this.rebasePoint(this.quad.pp, false);
            xUnit = this.rebasePoint(xUnit, false);
//            System.out.println(rPos);
//            double z = rPos.z;
//            double d = z / Math.acos(look.z);
            Vector3 yUnit = new Vector3(0, 1, 0);

//            rPos = this.rebaseAbs(new Vector3(rPos.x, 0, 0));
            zUnit = this.rebaseAbs(zUnit);
            yUnit = this.rebaseAbs(yUnit);
            xUnit = this.rebaseAbs(xUnit);
//            entity.world.addParticle(ParticleTypes.WAX_OFF, 0, 0, 0, 0, 0, 0);
//            entity.world.addParticle(ParticleTypes.WAX_ON, 1, 0, 0, 0, 0, 0);
            entity.world.addParticle(ParticleTypes.WAX_OFF, rPos.x, rPos.y, rPos.z, 0, 0, 0);
//            entity.world.addParticle(ParticleTypes.WAX_OFF, yUnit.x, yUnit.y, yUnit.z, 0, 0, 0);
            System.out.println(rPos);


            return true;
        } else return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hologram hologram = (Hologram) o;
        return getColor() == hologram.getColor() && Float.compare(hologram.getEmissivity(), getEmissivity()) == 0 && Float.compare(hologram.getOpacity(), getOpacity()) == 0 && Double.compare(hologram.getInteractionRange(), getInteractionRange()) == 0 && Double.compare(hologram.getRenderRange(), getRenderRange()) == 0 && isDoubleSided() == hologram.isDoubleSided() && Double.compare(hologram.height, height) == 0 && Double.compare(hologram.width, width) == 0 && id == hologram.id && getQuad().equals(hologram.getQuad()) && getTexture().equals(hologram.getTexture()) && getPos().equals(hologram.getPos()) && getHoverListener().equals(hologram.getHoverListener()) && getAttackListener().equals(hologram.getAttackListener()) && getUseListener().equals(hologram.getUseListener()) && getContactListener().equals(hologram.getContactListener()) && ticker.equals(hologram.ticker);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getQuad(), getTexture(), getColor(), getEmissivity(), getOpacity(), getPos(), getInteractionRange(), getRenderRange(), getHoverListener(), getAttackListener(), getUseListener(), getContactListener(), ticker, isDoubleSided(), height, width);
    }

    public Integer getID() {
        return this.id;
    }
}
