package net.themegax.slowmo.mixin.client;

import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.themegax.slowmo.SlowmoClient.SERVER_TICKS_PER_SECOND;
import static net.themegax.slowmo.SlowmoMain.*;

@Mixin(SoundSystem.class)
public abstract class SoundSystemMixin {
    @Inject(method = "getAdjustedPitch", at = @At("HEAD"), cancellable = true)
    private void getAdjustedPitch(SoundInstance sound, CallbackInfoReturnable<Float> cir) {
        if (sound.getCategory() != SoundCategory.MUSIC) {
            if(CHANGE_SOUND && SERVER_TICKS_PER_SECOND != DEFAULT_TICKRATE) {
                float pitch = MathHelper.clamp(sound.getPitch(), 0.5f, 2.0f);
                pitch = pitch*(SERVER_TICKS_PER_SECOND / DEFAULT_TICKRATE);
                pitch = MathHelper.clamp(pitch, 0.25f, 3.0f);
                cir.setReturnValue(pitch);
            }
        }
    }
}
