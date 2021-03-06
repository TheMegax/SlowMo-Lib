package io.themegax.slowmo.mixin.client;

import io.themegax.slowmo.ClientTick;
import io.themegax.slowmo.ext.SoundSystemExt;
import io.themegax.slowmo.mixin.client.sound.SoundManagerAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.themegax.slowmo.ClientTick.renderTick;
import static io.themegax.slowmo.SlowmoClient.*;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;render(FJZ)V"))
    private float tickDelta(float f) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.isPaused()) {
            return ((MinecraftClientAccessor) client).getPausedTickDelta();
        }
        return playerTickCounter.tickDelta;
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;tick()V"))
    private void redirectTick(MinecraftClient client) {
        ClientTick.gameTick(client);
    }

    // Player tick render
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderTickCounter;beginRenderTick(J)I"))
    private void render(boolean tick, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        long timeMillis = Util.getMeasuringTimeMs();
        int i = playerTickCounter.beginRenderTick(timeMillis);

        if (client.player != null) {
            MinecraftClientAccessor minecraftClientAccessor = ((MinecraftClientAccessor) client);
            minecraftClientAccessor.invokeHandleInputEvents(); // Fixes input at very low tickrates

            if (!changeSound) soundPitch = 1f;

            SoundManager clientSoundManager = client.getSoundManager();
            SoundManagerAccessor managerAccessor = ((SoundManagerAccessor)clientSoundManager);
            SoundSystem soundSystem = managerAccessor.getSoundSystem();
            ((SoundSystemExt)(soundSystem)).updateSoundPitch();

        }
        if (!client.isPaused()) {
            for (int j = 0; j < Math.min(maxClientTicks, i); ++j) {
                renderTick(client);
            }
        }
    }

    // Vanilla has a hard limit of 10 world ticks per render frame. This changes the limit to any number.
    @ModifyConstant(method = "render", constant = @Constant(intValue = 10))
    private int tickLimit(int i) {
        return maxClientTicks;
    }

    @Redirect(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;handleBlockBreaking(Z)V"))
    private void ignoreBlockBreakingHandler(MinecraftClient instance, boolean bl) { } // Ignores handleBlockBreaking call from handleInputEvents

}
