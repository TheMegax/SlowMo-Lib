package io.themegax.slowmo.mixin.client;

import io.themegax.slowmo.ext.SoundSystemExt;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static io.themegax.slowmo.SlowmoClient.SOUND_PITCH;

@Mixin(SoundSystem.class)
public abstract class SoundSystemMixin implements SoundSystemExt {
    public void updateSoundPitch(float pitch) {
        SoundSystem soundSystem = ((SoundSystem)(Object)this);
        SoundSystemAccessor accessor = ((SoundSystemAccessor)(soundSystem));

        if (!accessor.getStarted()) {
            return;
        }
        accessor.getSources().forEach((source2, sourceManager) -> {
            float f = getAdjustedNewPitch(pitch);
            sourceManager.run(source -> source.setPitch(f));
        });
    }

    private float getAdjustedNewPitch(float pitch) {
        return MathHelper.clamp(pitch, 0.3f, 3f);
    }

    @Inject(method = "getAdjustedPitch", at = @At("HEAD"), cancellable = true)
    private void getAdjustedPitch(SoundInstance sound, CallbackInfoReturnable<Float> cir) {
        if (!sound.getId().getPath().contains("ui")) {
            float pitch = sound.getPitch() * SOUND_PITCH;
            cir.setReturnValue(getAdjustedNewPitch(pitch));
        }
    }
}
