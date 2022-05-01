package io.themegax.slowmo.mixin.client;

import io.themegax.slowmo.SlowmoConfig;
import io.themegax.slowmo.ext.SoundSystemExt;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static io.themegax.slowmo.SlowmoClient.CHANGE_SOUND;
import static io.themegax.slowmo.SlowmoClient.SOUND_PITCH;

@Mixin(SoundSystem.class)
public abstract class SoundSystemMixin implements SoundSystemExt {
    SoundSystem soundSystem = ((SoundSystem)(Object)this);
    public void updateSoundPitch(float pitch) {
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
        if (SlowmoConfig.doClampPitch) {
            return MathHelper.clamp(pitch, 0.3f, 3f);
        }
        else return pitch;
    }

    @Inject(method = "getAdjustedPitch", at = @At("HEAD"), cancellable = true)
    private void getAdjustedPitch(SoundInstance sound, CallbackInfoReturnable<Float> cir) {
        float pitchMod = CHANGE_SOUND ? SlowmoConfig.getPitchPercentage() : SOUND_PITCH;
        if (!sound.getId().getPath().contains("ui.")) {
            float pitch = sound.getPitch() * SOUND_PITCH * pitchMod;
            cir.setReturnValue(getAdjustedNewPitch(pitch));
        }
    }
}
