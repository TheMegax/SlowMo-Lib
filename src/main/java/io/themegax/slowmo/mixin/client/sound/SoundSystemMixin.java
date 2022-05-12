package io.themegax.slowmo.mixin.client.sound;

import io.themegax.slowmo.config.SlowmoConfig;
import io.themegax.slowmo.ext.SoundSystemExt;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static io.themegax.slowmo.SlowmoClient.changeSound;
import static io.themegax.slowmo.SlowmoClient.soundPitch;

@Mixin(SoundSystem.class)
public abstract class SoundSystemMixin implements SoundSystemExt {

    SoundSystem soundSystem = ((SoundSystem)(Object)this);
    public void updateSoundPitch() {
        SoundSystemAccessor accessor = ((SoundSystemAccessor)(soundSystem));

        if (!accessor.getStarted()) {
            return;
        }
        accessor.getSources().forEach((source2, sourceManager) -> {
            float f = pitchModifierStrenght(source2);
            sourceManager.run(source -> source.setPitch(f));
        });
    }

    private float getAdjustedNewPitch(float pitch) {
        if (SlowmoConfig.doClampPitch) {
            return MathHelper.clamp(pitch, 0.3f, 3f);
        }
        else return pitch;
    }

    private float pitchModifierStrenght(SoundInstance sound) {
        float pitchMod = SlowmoConfig.getPitchPercentage();
        if (!sound.getId().getPath().contains("ui.")) {
            float pitch = sound.getPitch() * (soundPitch / pitchMod);
            return getAdjustedNewPitch(pitch);
        }
        return sound.getPitch();
    }

    @Inject(method = "getAdjustedPitch", at = @At("HEAD"), cancellable = true)
    private void getAdjustedPitch(SoundInstance sound, CallbackInfoReturnable<Float> cir) {
        if (changeSound) {
            cir.setReturnValue(pitchModifierStrenght(sound));
        }
    }
}
