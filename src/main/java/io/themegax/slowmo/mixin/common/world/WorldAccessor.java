package io.themegax.slowmo.mixin.common.world;

import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(World.class)
public interface WorldAccessor {
    @Invoker("tickBlockEntities")
    void invokeTickBlockEntities();
}
