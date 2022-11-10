package com.github.miir.holodeck.mixin;

import com.github.miir.holodeck.api.Hologram;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import java.util.HashMap;

@Mixin(World.class)
public class WorldMixin {
    private HashMap<Integer, Hologram> holograms;
    public void addHologram(Hologram hologram) {
        this.holograms.put(hologram.getID(), hologram);
    }
    public HashMap<Integer, Hologram> getHolograms() {
        return this.holograms;
    }
}
