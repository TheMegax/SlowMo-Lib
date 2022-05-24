package io.themegax.slowmo.mixin.common;

import io.themegax.slowmo.SlowmoMain;
import io.themegax.slowmo.ext.MinecraftServerExt;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import static io.themegax.slowmo.SlowmoMain.millisecondsPerTick;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin extends ReentrantThreadExecutor<ServerTask> implements CommandOutput, AutoCloseable, MinecraftServerExt {
    public MinecraftServerMixin(String string) {
        super(string);
    }

    // Represents the amount of milliseconds per tick
    @ModifyConstant(method = "runServer", constant = @Constant(longValue = 50L))
    private long ticks(long x) {
        return (millisecondsPerTick);
    }

    public float getServerTickrate() {
        return SlowmoMain.ticksPerSecond;
    }

}