package io.themegax.slowmo.mixin.common;

import io.themegax.slowmo.SlowmoMain;
import io.themegax.slowmo.ext.MinecraftServerExt;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import static io.themegax.slowmo.SlowmoMain.MILLISECONDS_PER_TICK;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements MinecraftServerExt {
    // Represents the amount of milliseconds per tick
    @ModifyConstant(method = "runServer", constant = @Constant(longValue = 50L))
    private long ticks(long x) {
        return (MILLISECONDS_PER_TICK);
    }

    public float getServerTickrate() {
        return SlowmoMain.TICKS_PER_SECOND;
    }
}
