package io.themegax.slowmo.mixin.common;

import io.themegax.slowmo.ext.PlayerEntityExt;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.EntityList;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import static io.themegax.slowmo.SlowmoMain.TICKS_PER_SECOND;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World {
    float oddTicks = 0f;

    protected ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, RegistryEntry<DimensionType> registryEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed) {
        super(properties, registryRef, registryEntry, profiler, isClient, debugWorld, seed);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/EntityList;forEach(Ljava/util/function/Consumer;)V"), cancellable = true)
    private void tick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        // Fixes block breaking speed. I couldn't figure out how to mixin into lambda foreach expresions
        ServerWorld serverWorld = ((ServerWorld)(Object)this);
        ServerWorldAccessor serverWorldAccessor = ((ServerWorldAccessor)(serverWorld));
        WorldAccessor worldAccessor = ((WorldAccessor)(serverWorld));

        EntityList entityList = serverWorldAccessor.getEntityList();

        entityList.forEach(entity -> {
            if (entity instanceof PlayerEntity) {
                float PLAYER_TICKS_PER_SECOND = ((PlayerEntityExt)entity).getPlayerTicks();

                float tickSpeed = (PLAYER_TICKS_PER_SECOND/TICKS_PER_SECOND);
                int tickLoops = (int) tickSpeed;
                oddTicks += tickSpeed - tickLoops;
                for (int i = 0; i < tickLoops + (int) oddTicks; i++) {
                    tickPlayerAsync(entity);
                }
                return;
            }

            if (entity.isRemoved()) {
                return;
            }
            if (serverWorldAccessor.invokeShouldCancelSpawn(entity)) {
                entity.discard();
                return;
            }
            entity.checkDespawn();
            if (!serverWorldAccessor.getChunkManager().threadedAnvilChunkStorage.getTicketManager().shouldTickEntities(entity.getChunkPos().toLong())) {
                return;
            }
            Entity entity2 = entity.getVehicle();
            if (entity2 != null) {
                if (entity2.isRemoved() || !entity2.hasPassenger(entity)) {
                    entity.stopRiding();
                } else {
                    return;
                }
            }
            serverWorld.tickEntity(serverWorld::tickEntity, entity);
        });
        worldAccessor.invokeTickBlockEntities();
        serverWorldAccessor.getEntityManager().tick();
        ci.cancel();
    }

    public void tickPlayerAsync(Entity entity) {
        ServerWorld serverWorld = ((ServerWorld)(Object)this);
        ServerWorldAccessor serverWorldAccessor = ((ServerWorldAccessor)(serverWorld));

        if (entity.isRemoved()) {
            return;
        }
        if (serverWorldAccessor.invokeShouldCancelSpawn(entity)) {
            entity.discard();
            return;
        }
        entity.checkDespawn();
        if (!serverWorldAccessor.getChunkManager().threadedAnvilChunkStorage.getTicketManager().shouldTickEntities(entity.getChunkPos().toLong())) {
            return;
        }
        Entity entity2 = entity.getVehicle();
        if (entity2 != null) {
            if (entity2.isRemoved() || !entity2.hasPassenger(entity)) {
                entity.stopRiding();
            } else {
                return;
            }
        }
        serverWorld.tickEntity(serverWorld::tickEntity, entity);
    }
}
