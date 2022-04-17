package io.themegax.slowmo.mixin.common;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftServer.class)
public interface MinecraftServerAccessor {
    @Accessor
    void setWaitingForNextTick(boolean b);

    @Accessor
    void setTimeReference(long l);

    @Accessor
    void setNextTickTimestamp(long l);

}
