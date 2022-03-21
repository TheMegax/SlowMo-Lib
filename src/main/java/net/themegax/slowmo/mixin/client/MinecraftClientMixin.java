package net.themegax.slowmo.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.themegax.slowmo.ClientTick.renderTick;
import static net.themegax.slowmo.SlowmoClient.MAX_CLIENT_TICKS;
import static net.themegax.slowmo.SlowmoClient.playerTickCounter;

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

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;handleInputEvents()V"), remap = false)
    private void ignoreInputCall(MinecraftClient instance) { } // Will ignore handleInputEvents call

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
