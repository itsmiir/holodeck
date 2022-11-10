package com.github.miir.holodeck;

import com.github.miir.holodeck.api.Hologram;
import com.github.miir.holodeck.math.Vector3;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Holodeck implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("holodeck");
    public static final String MOD_ID = "holodeck";
    public static final Hologram TEST_HOLOGRAM = Hologram.interactable(
            new Vector3(0, 0, 0), null, 2, 2, false,
            10, 5,
            context -> {

            }, context -> {

            }, context -> {

            }, context -> {

            });
    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }



    @Override
    public void onInitialize() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register((worldRenderContext) -> TEST_HOLOGRAM.render(worldRenderContext));
    }
}
