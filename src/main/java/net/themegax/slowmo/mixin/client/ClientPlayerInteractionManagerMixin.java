package net.themegax.slowmo.mixin.client;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.themegax.slowmo.SlowmoMain;
import net.themegax.slowmo.ext.ClientPlayerInteractionManagerExt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin implements ClientPlayerInteractionManagerExt {
    public void desyncTick() {
        ClientPlayerInteractionManager interactionManager = ((ClientPlayerInteractionManager)(Object)this);
        ClientPlayerInteractionManagerAccessor interactionManagerAccessor = ((ClientPlayerInteractionManagerAccessor) interactionManager);

        interactionManagerAccessor.invokeSyncSelectedSlot();
        if (interactionManagerAccessor.getNetworkHandler().getConnection().isOpen()) {
            interactionManagerAccessor.getNetworkHandler().getConnection().tick();
        } else {
            interactionManagerAccessor.getNetworkHandler().getConnection().handleDisconnection();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void tick(CallbackInfo ci) {
        ci.cancel();
    }
}
