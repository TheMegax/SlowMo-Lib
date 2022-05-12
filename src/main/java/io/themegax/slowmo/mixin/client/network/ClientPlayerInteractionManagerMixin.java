package io.themegax.slowmo.mixin.client.network;

import io.themegax.slowmo.ext.ClientPlayerInteractionManagerExt;
import net.minecraft.client.network.ClientPlayerInteractionManager;
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
