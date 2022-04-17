package io.themegax.slowmo.mixin.common;

import io.themegax.slowmo.SlowmoConfig;
import io.themegax.slowmo.ext.PlayerEntityExt;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
    @Inject(method = "copyFrom", at = @At("RETURN"))
    public void copyFrom(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        if(SlowmoConfig.keepTickrateOnDeath) {
            ServerPlayerEntity serverPlayer = ((ServerPlayerEntity)(Object)this);
            float PLAYER_TICKS_PER_SECOND = ((PlayerEntityExt)oldPlayer).getPlayerTicks();
            ((PlayerEntityExt)serverPlayer).setPlayerTicks(PLAYER_TICKS_PER_SECOND);
        }
    }
}
