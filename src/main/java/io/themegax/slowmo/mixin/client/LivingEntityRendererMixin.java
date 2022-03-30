package io.themegax.slowmo.mixin.client;

import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static io.themegax.slowmo.SlowmoClient.playerTickCounter;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin <T extends LivingEntity>{

    @Inject(method = "getAnimationProgress", at = @At("HEAD"), cancellable = true)
    private void getAnimationProgress(T entity, float tickDelta, CallbackInfoReturnable<Float> cir) {
        // Fixes animations when the player is desync from the server
        if (entity instanceof PlayerEntity) {
            cir.setReturnValue(entity.age + playerTickCounter.tickDelta);
        }
    }
}
