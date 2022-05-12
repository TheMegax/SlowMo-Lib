package io.themegax.slowmo.mixin.common.player;

import io.themegax.slowmo.config.SlowmoConfig;
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
        ServerPlayerEntity serverPlayer = ((ServerPlayerEntity)(Object)this);
        if(SlowmoConfig.keepTickrateOnDeath) {
            float PLAYER_TICKS_PER_SECOND = ((PlayerEntityExt)oldPlayer).getPlayerTicks();
            ((PlayerEntityExt)serverPlayer).setPlayerTicks(PLAYER_TICKS_PER_SECOND);
        }
        float TICK_DELTA = ((PlayerEntityExt)oldPlayer).getTickDelta();
        ((PlayerEntityExt)serverPlayer).setTickDelta(TICK_DELTA);

        float ODD_TICKS = ((PlayerEntityExt)oldPlayer).getOddTicks();
        ((PlayerEntityExt)serverPlayer).setOddTicks(ODD_TICKS);
    }
}
