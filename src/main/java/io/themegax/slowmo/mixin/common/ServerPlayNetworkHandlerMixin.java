package io.themegax.slowmo.mixin.common;

import io.themegax.slowmo.ext.PlayerEntityExt;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static io.themegax.slowmo.SlowmoMain.ticksPerSecond;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    // Ticks or skips player ticks
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;playerTick()V"))
    private void playerTickCall(ServerPlayerEntity player) {
        float PLAYER_TICKS_PER_SECOND = ((PlayerEntityExt) player).getPlayerTicks();
        float TICK_DELTA = ((PlayerEntityExt) player).getTickDelta();

        float tickFloat = PLAYER_TICKS_PER_SECOND / ticksPerSecond;

        TICK_DELTA += tickFloat;
        int deltaInt = (int) TICK_DELTA;
        for (int i = 0; i < deltaInt; i++) {
            player.playerTick();
        }
        TICK_DELTA -= deltaInt;

        ((PlayerEntityExt) player).setTickDelta(TICK_DELTA);
    }
}
