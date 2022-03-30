package io.themegax.slowmo.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.themegax.slowmo.SlowmoClient.playerTickCounter;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Inject(method = "renderEntity", at = @At("HEAD"), cancellable = true)
    private void renderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        if (entity instanceof PlayerEntity) {
            // Players use playerTickCounter's tickDelta to render instead of server tickDelta
            renderEntityMixin(entity, cameraX, cameraY, cameraZ, playerTickCounter.tickDelta, matrices, vertexConsumers);
            ci.cancel();
        }
    }

    public void renderEntityMixin(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        MinecraftClient client = MinecraftClient.getInstance();
        EntityRenderDispatcher entityRenderDispatcher = client.getEntityRenderDispatcher();
        double d = MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX());
        double e = MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY());
        double f = MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ());
        float g = MathHelper.lerp(tickDelta, entity.prevYaw, entity.getYaw());
        entityRenderDispatcher.render(entity, d - cameraX, e - cameraY, f - cameraZ, g, tickDelta, matrices, vertexConsumers, entityRenderDispatcher.getLight(entity, tickDelta));
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleManager;renderParticles(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/client/render/Camera;F)V"))
    private float tickDelta(float f) {
        return playerTickCounter.tickDelta;
    }
}
