package io.themegax.slowmo.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.toast.TutorialToast;
import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {
    @Accessor
    RenderTickCounter getRenderTickCounter();

    @Accessor
    Overlay getOverlay();

    @Accessor
    int getAttackCooldown();

    @Accessor
    void setAttackCooldown(int i);

    @Accessor
    float getPausedTickDelta();

    @Accessor
    int getItemUseCooldown();

    @Accessor
    void setItemUseCooldown(int i);

    @Accessor
    void setSocialInteractionsToast(TutorialToast toast);

    @Accessor
    TutorialToast getSocialInteractionsToast();

    @Accessor
    ClientConnection getIntegratedServerConnection();

    @Invoker("handleInputEvents")
    void invokeHandleInputEvents();

    @Invoker("handleBlockBreaking")
    void invokeHandleBlockBreaking(boolean bl);
}
