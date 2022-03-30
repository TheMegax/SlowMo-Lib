package io.themegax.slowmo.mixin.client;

import io.themegax.slowmo.SlowmoMain;
import io.themegax.slowmo.ext.SoundSystemExt;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;

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
            sourceManager.run(source -> {
                source.setPitch(f);
                SlowmoMain.LOGGER.info(String.valueOf(f));
            });
        });
    }

    private float getAdjustedNewPitch(float pitch) {
        return MathHelper.clamp(pitch, 0.3f, 3f);
    }
}
