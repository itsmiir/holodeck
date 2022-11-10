package com.github.miir.holodeck.impl;

import com.github.miir.holodeck.Holodeck;
import com.github.miir.holodeck.api.Hologram;
import com.github.miir.holodeck.math.Vector3;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

public class HologramImpl {
    public static boolean render(Hologram hologram, Vec3d cameraPos, MatrixStack matrices, MinecraftClient client) {
        if (client == null || client.world == null) {
            Holodeck.LOGGER.error("tried to render a hologram when the client was not in a world!");
            return false;
        }
        if ((client.player.age % 20 == 0 && client.getTickDelta() < 0.5)) {
            float yaw = (float) Math.toRadians(client.player.getYaw());
            float pitch = (float) Math.toRadians(client.player.getPitch());
//            System.out.println(hologram.rebasePoint(new Vector3(Math.sin(yaw)*Math.cos(pitch), -Math.sin(pitch), Math.cos(yaw)*Math.cos(pitch)), true));
        }
        if (client.player.getStackInHand(Hand.MAIN_HAND).isOf(Items.STICK)) {
            client.world.addParticle(ParticleTypes.CLOUD, hologram.getQuad().pp.x, hologram.getQuad().pp.y, hologram.getQuad().pp.z, 0, 0, 0);
            client.world.addParticle(ParticleTypes.CLOUD, hologram.getQuad().pn.x, hologram.getQuad().pn.y, hologram.getQuad().pn.z, 0, 0, 0);
            client.world.addParticle(ParticleTypes.CLOUD, hologram.getQuad().nn.x, hologram.getQuad().nn.y, hologram.getQuad().nn.z, 0, 0, 0);
            client.world.addParticle(ParticleTypes.CLOUD, hologram.getQuad().np.x, hologram.getQuad().np.y, hologram.getQuad().np.z, 0, 0, 0);
        }

        if (hologram.getPos().minus(new Vector3(cameraPos)).length() > hologram.getRenderRange()) return false;
        Vec3d pos = hologram.getPos().toVec3d();
//        hologram.resize(2*(Math.sin((client.player.age+client.getTickDelta())/5)+1.1), 2*(Math.cos((client.player.age+client.getTickDelta())/5)+1.1));
        hologram.isBeingLookedAtBy(client.player);
//        client.world.addParticle(ParticleTypes.DRIPPING_LAVA, hologram.getQuad().pp.x, hologram.getQuad().pp.y, hologram.getQuad().pp.z, 0, 0, 0);
//        client.world.addParticle(ParticleTypes.WAX_ON, hologram.getQuad().pn.x, hologram.getQuad().pn.y, hologram.getQuad().pn.z, 0, 0, 0);
        if (client.player.getStackInHand(Hand.MAIN_HAND).isOf(Items.STONE)) {
            hologram.rotate(new Matrix4f(new Vec3f(((float) Math.sin((client.player.age % 1000)/20f)), (float) Math.sin((client.player.age % 1000)/20f), (float) Math.sin((client.player.age % 1000)/20f)).getDegreesQuaternion((client.getTickDelta()+(client.player.age%10000000)) / 10000)));
        }
        Vec3d relativeOrigin = Vec3d.ZERO.subtract(cameraPos);
        int lightmapCoords = hologram.getEmissivity() == 0 ? WorldRenderer.getLightmapCoordinates(
                client.world, new BlockPos(hologram.getPos().toVec3d())) : 0;
        int color = hologram.getColor();
        matrices.push();
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        matrices.translate(relativeOrigin.x, relativeOrigin.y, relativeOrigin.z);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableTexture(); // allows for rendering textures
        RenderSystem.enableDepthTest(); // culls based on distance from camera
        RenderSystem.enableBlend(); // allows for variations in alpha
        RenderSystem.defaultBlendFunc(); // prevents some sort of weirdness /shrug
        RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
        RenderSystem.setShaderTexture(0, hologram.getTexture());
        RenderSystem.setShaderColor(1, 1, 1, 1);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT);
        bufferBuilder.vertex(positionMatrix, ((float) hologram.getQuad().nn.x), ((float) hologram.getQuad().nn.y), ((float) hologram.getQuad().nn.z)).color(color).texture(0f, 1f).light(lightmapCoords).next();//normal(0.0f, -1.0f, 0.0f).next();
        bufferBuilder.vertex(positionMatrix, ((float) hologram.getQuad().pn.x), ((float) hologram.getQuad().pn.y), ((float) hologram.getQuad().pn.z)).color(color).texture(1f, 1f).light(lightmapCoords).next();//normal(0.0f, -1.0f, 0.0f).next();
        bufferBuilder.vertex(positionMatrix, ((float) hologram.getQuad().pp.x), ((float) hologram.getQuad().pp.y), ((float) hologram.getQuad().pp.z)).color(color).texture(1f, 0f).light(lightmapCoords).next();//normal(0.0f, -1.0f, 0.0f).next();
        bufferBuilder.vertex(positionMatrix, ((float) hologram.getQuad().np.x), ((float) hologram.getQuad().np.y), ((float) hologram.getQuad().np.z)).color(color).texture(0f, 0f).light(lightmapCoords).next();//normal(0.0f, -1.0f, 0.0f).next();
        BufferRenderer.drawWithShader(bufferBuilder.end());

        if (hologram.isDoubleSided()) {
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT);
            bufferBuilder.vertex(positionMatrix, ((float) hologram.getQuad().pn.x), ((float) hologram.getQuad().pn.y), ((float) hologram.getQuad().pn.z)).color(color).texture(1f, 0f).light(lightmapCoords).next();//.normal(1.0f, 1.0f, 1.0f).next();
            bufferBuilder.vertex(positionMatrix, ((float) hologram.getQuad().nn.x), ((float) hologram.getQuad().nn.y), ((float) hologram.getQuad().nn.z)).color(color).texture(1f, 1f).light(lightmapCoords).next();//.normal(1.0f, 1.0f, 1.0f).next();
            bufferBuilder.vertex(positionMatrix, ((float) hologram.getQuad().np.x), ((float) hologram.getQuad().np.y), ((float) hologram.getQuad().np.z)).color(color).texture(0f, 1f).light(lightmapCoords).next();//.normal(1.0f, 1.0f, 1.0f).next();
            bufferBuilder.vertex(positionMatrix, ((float) hologram.getQuad().pp.x), ((float) hologram.getQuad().pp.y), ((float) hologram.getQuad().pp.z)).color(color).texture(0f, 0f).light(lightmapCoords).next();//.normal(1.0f, 1.0f, 1.0f).next();
            BufferRenderer.drawWithShader(bufferBuilder.end());
        }
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        matrices.pop();
        return true;
    }
}
