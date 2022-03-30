package io.themegax.slowmo.mixin.common;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.EntityList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerWorld.class)
public interface ServerWorldAccessor {
    @Accessor
    EntityList getEntityList();

    @Accessor
    ServerChunkManager getChunkManager();

    @Accessor
    ServerEntityManager<Entity> getEntityManager();

    @Invoker("shouldCancelSpawn")
    boolean invokeShouldCancelSpawn(Entity entity);

}
