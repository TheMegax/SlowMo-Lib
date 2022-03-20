package net.themegax.slowmo.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.themegax.slowmo.ext.PlayerEntityExt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.themegax.slowmo.SlowmoMain.DEFAULT_TICKRATE;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements PlayerEntityExt {
    private float PLAYER_TICKS_PER_SECOND = DEFAULT_TICKRATE;

    public float getPlayerTicks() {
        return PLAYER_TICKS_PER_SECOND;
    }
    public void setPlayerTicks(float playerTicks) {
        PLAYER_TICKS_PER_SECOND = playerTicks;
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
    private void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putFloat("playerTicksPerSecond", this.PLAYER_TICKS_PER_SECOND);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    private void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        this.PLAYER_TICKS_PER_SECOND = nbt.getFloat("playerTicksPerSecond");
    }
}
