package com.github.miir.holodeck.api;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public record HologramContext(Hologram hologram, World world, @Nullable Entity viewer, double x, double y) {
}
