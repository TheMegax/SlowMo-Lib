package io.themegax.slowmo.mixin.client;

import io.themegax.slowmo.ext.SoundSystemExt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.GameRenderer;
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

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;handleInputEvents()V"))
    private void ignoreInputCall(MinecraftClient instance) { } // Ignores handleInputEvents call from tick

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;tick()V"))
    private void ignoreGameRendererTick(GameRenderer instance) { } // Ignores GameRenderer.tick call from tick

    @Redirect(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;handleBlockBreaking(Z)V"))
    private void ignoreBlockBreakingHandler(MinecraftClient instance, boolean bl) { } // Ignores handleBlockBreaking call from handleInputEvents

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleManager;tick()V"))
    private void ignoreParticleManagerTick(ParticleManager instance) {} // Ignores particleManager.tick call from tick

    @Inject(method = "tick", at = @At("HEAD"))
    private void cooldownModify(CallbackInfo ci) { // Nullifies cooldown tickdown from tick() call
        MinecraftClient client = MinecraftClient.getInstance();
        int itemUseCooldown = ((MinecraftClientAccessor) client).getItemUseCooldown();
        if (itemUseCooldown > 0) {
            ((MinecraftClientAccessor) client).setItemUseCooldown(itemUseCooldown + 1);
        }
        int attackCooldown = ((MinecraftClientAccessor) client).getAttackCooldown();
        if (attackCooldown > 0) {
            ((MinecraftClientAccessor) client).setAttackCooldown(attackCooldown + 1);
        }
    }

    // Player tick render
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderTickCounter;beginRenderTick(J)I"))
    private void render(boolean tick, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        long timeMillis = Util.getMeasuringTimeMs();
        int i = playerTickCounter.beginRenderTick(timeMillis);

        if (client.player != null) {
            MinecraftClientAccessor minecraftClientAccessor = ((MinecraftClientAccessor) client);
            minecraftClientAccessor.invokeHandleInputEvents(); // Fixes inputs at very low tickrates

            if (i != 0 && CHANGE_SOUND && (SOUND_PITCH != SERVER_TICKS_PER_SECOND/20)) {
                float pitchDelta = (float) i/20;
                float pitchDistance = Math.abs(SERVER_TICKS_PER_SECOND/20 - SOUND_PITCH);

                if (SOUND_PITCH < SERVER_TICKS_PER_SECOND/20) {
                    SOUND_PITCH += pitchDelta;
                }
                else {
                    SOUND_PITCH -= pitchDelta;
                }

                if (pitchDistance < pitchDelta) {
                    SOUND_PITCH = SERVER_TICKS_PER_SECOND/20;
                }

                SoundManager clientSoundManager = client.getSoundManager();
                SoundManagerAccessor managerAccessor = ((SoundManagerAccessor)clientSoundManager);
                SoundSystem soundSystem = managerAccessor.getSoundSystem();
                ((SoundSystemExt)(soundSystem)).updateSoundPitch(SOUND_PITCH);
            }
            else if (!CHANGE_SOUND) {
                SOUND_PITCH = 1f;
                SoundManager clientSoundManager = client.getSoundManager();
                SoundManagerAccessor managerAccessor = ((SoundManagerAccessor)clientSoundManager);
                SoundSystem soundSystem = managerAccessor.getSoundSystem();
                ((SoundSystemExt)(soundSystem)).updateSoundPitch(SOUND_PITCH);
            }
        }
        if (!client.isPaused()) {
            for (int j = 0; j < Math.min(MAX_CLIENT_TICKS, i); ++j) {
                renderTick(client);
            }
        }
    }

    // Vanilla has a hard limit of 10 world ticks per render frame. This changes the limit to any number.
    @ModifyConstant(method = "render", constant = @Constant(intValue = 10))
    private int tickLimit(int i) {
        return MAX_CLIENT_TICKS;
    }

}
