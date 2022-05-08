package io.themegax.slowmo.mixin.client;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {
    // Client players won't use the normal tick entity loop (see "ClientTick$renderTick()")

    @Inject(method = "tickEntity", at = @At("HEAD"), cancellable = true)
    private void tickEntity(Entity entity, CallbackInfo ci) {
        if (entity instanceof PlayerEntity) {
            ci.cancel();
        }
    }

    @Inject(method = "tickPassenger", at = @At("HEAD"), cancellable = true)
    private void tickPassenger(Entity entity, Entity passenger, CallbackInfo ci) {
        if (passenger instanceof PlayerEntity) {
            ci.cancel();
        }
    }
}
