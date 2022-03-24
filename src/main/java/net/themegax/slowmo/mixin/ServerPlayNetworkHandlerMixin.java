package net.themegax.slowmo.mixin;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.themegax.slowmo.ext.PlayerEntityExt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.themegax.slowmo.SlowmoMain.TICKS_PER_SECOND;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    // Ticks missed player ticks
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;playerTick()V"))
    private void tick(CallbackInfo ci) {
        ServerPlayNetworkHandler networkHandler = ((ServerPlayNetworkHandler)(Object)this);
        ServerPlayerEntity player = networkHandler.getPlayer();

        float PLAYER_TICKS_PER_SECOND = ((PlayerEntityExt)player).getPlayerTicks();
        int tickLoops = (int) (PLAYER_TICKS_PER_SECOND/TICKS_PER_SECOND);
        for (int i = 0; i < tickLoops; i++) {
            player.playerTick();
        }
    }
}
