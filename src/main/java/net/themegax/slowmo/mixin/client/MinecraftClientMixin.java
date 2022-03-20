package net.themegax.slowmo.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.themegax.slowmo.SlowmoClient.MAX_CLIENT_TICKS;
import static net.themegax.slowmo.SlowmoClient.playerTickCounter;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    float pausedTickDelta = 0;
    boolean paused = false;
    boolean pausedCondition = false;

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;render(FJZ)V"))
    private float tickDelta(float f) {
        return playerTickCounter.tickDelta;
    }

    @ModifyVariable(method = "render", at = @At(value = "STORE"), ordinal = 1)
    private boolean paused(boolean bl) {
        pausedCondition = bl;
        return bl;
    }

    // Player tick render
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderTickCounter;beginRenderTick(J)I"))
    private void render(boolean tick, CallbackInfo ci) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        MinecraftClientAccessor minecraftClientAccessor = ((MinecraftClientAccessor) minecraftClient);
        long timeMillis = Util.getMeasuringTimeMs();
        int i = playerTickCounter.beginRenderTick(timeMillis);
        if (!paused) {
            ClientPlayerEntity clientPlayer = minecraftClient.player;
            if (clientPlayer != null) {
                for (int j = 0; j < Math.min(MAX_CLIENT_TICKS, i); ++j) {
                    clientPlayer.resetPosition();
                    clientPlayer.age++;
                    clientPlayer.tick();

                    // If a tick is scheduled to run, don't double tick
                    RenderTickCounter renderTickCounter = ((MinecraftClientAccessor)minecraftClient).getRenderTickCounter();
                    float prevTimeMillis = renderTickCounter.prevTimeMillis;
                    float lastFrameDuration = (timeMillis - prevTimeMillis) / renderTickCounter.tickTime;
                    int k = (int) (renderTickCounter.tickDelta + lastFrameDuration);
                    if (k < 1) { // TODO this is much faster than it needs to be!
                        int itemUseCooldown = ((MinecraftClientAccessor) minecraftClient).getItemUseCooldown();
                        if (itemUseCooldown > 0) {
                            ((MinecraftClientAccessor) minecraftClient).setItemUseCooldown(itemUseCooldown-1);
                        }
                        minecraftClient.getMusicTracker().tick();
                        if (minecraftClient.interactionManager != null)
                            minecraftClient.interactionManager.tick();
                        minecraftClient.inGameHud.tick(false);
                        minecraftClient.gameRenderer.tick();
                        minecraftClient.particleManager.tick();
                        if (minecraftClientAccessor.getOverlay() == null && (minecraftClient.currentScreen == null || minecraftClient.currentScreen.passEvents)) {
                            minecraftClientAccessor.invokeHandleInputEvents();
                            int attackCooldown = ((MinecraftClientAccessor) minecraftClient).getAttackCooldown();
                            if (attackCooldown > 0) {
                                ((MinecraftClientAccessor) minecraftClient).setAttackCooldown(attackCooldown-1);
                            }
                        }
                    }
                }
            }
        }
        //FIXME paused behavior doesn't work properly
        if (paused != pausedCondition) {
            if (paused) {
                pausedTickDelta = playerTickCounter.tickDelta;
            } else {
                playerTickCounter.tickDelta = pausedTickDelta;
            }
            paused = pausedCondition;
        }
    }

    // Vanilla has a hard limit of 10 world ticks per render frame. This changes the limit to any number.
    @ModifyConstant(method = "render", constant = @Constant(intValue = 10))
    private int tickLimit(int i) {
        return MAX_CLIENT_TICKS;
    }
}
