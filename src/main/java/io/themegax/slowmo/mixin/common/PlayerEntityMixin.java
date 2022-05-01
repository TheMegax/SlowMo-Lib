package io.themegax.slowmo.mixin.common;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import io.themegax.slowmo.ext.PlayerEntityExt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.themegax.slowmo.SlowmoMain.DEFAULT_TICKRATE;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements PlayerEntityExt {
    private float PLAYER_TICKS_PER_SECOND = DEFAULT_TICKRATE;
    private float TICK_DELTA = 0f;
    private float ODD_TICKS = 0f;

    public float getPlayerTicks() {
        return PLAYER_TICKS_PER_SECOND;
    }
    public void setPlayerTicks(float playerTicks) {
        this.PLAYER_TICKS_PER_SECOND = playerTicks;
    }

    public float getTickDelta() {
        return TICK_DELTA;
    }
    public void setTickDelta(float tickDelta) {
        this.TICK_DELTA = tickDelta;
    }

    public float getOddTicks() {
        return ODD_TICKS;
    }

    public void setOddTicks(float oddTicks) {
        this.ODD_TICKS = oddTicks;
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
    private void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putFloat("playerTicksPerSecond", this.PLAYER_TICKS_PER_SECOND);
        nbt.putFloat("tickDelta", this.TICK_DELTA);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    private void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("playerTicksPerSecond")) {
            this.PLAYER_TICKS_PER_SECOND = nbt.getFloat("playerTicksPerSecond");
        }
        if (nbt.contains("tickDelta")) {
            this.TICK_DELTA = nbt.getFloat("tickDelta");
        }
    }
}