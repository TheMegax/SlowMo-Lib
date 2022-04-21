package io.themegax.slowmo;

import io.themegax.slowmo.ext.ClientPlayerInteractionManagerExt;
import io.themegax.slowmo.mixin.client.MinecraftClientAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SleepingChatScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.profiler.Profiler;

@Environment(EnvType.CLIENT)
public class ClientTick {
    public static void renderTick(MinecraftClient client) {
        Profiler profiler = client.getProfiler();
        ClientPlayerEntity clientPlayer = client.player;
        MinecraftClientAccessor minecraftClientAccessor = ((MinecraftClientAccessor) client);

        int itemUseCooldown = ((MinecraftClientAccessor) client).getItemUseCooldown();
        if (itemUseCooldown > 0) {
            ((MinecraftClientAccessor) client).setItemUseCooldown(itemUseCooldown - 1);
        }

        profiler.push("gui");
        client.inGameHud.tick(false);
        profiler.pop();
        client.gameRenderer.updateTargetedEntity(1.0f);
        client.getTutorialManager().tick(client.world, client.crosshairTarget);
        profiler.push("gameMode");
        if (client.world != null) {
            assert client.interactionManager != null;
            ((ClientPlayerInteractionManagerExt)client.interactionManager).desyncTick();
        }
        profiler.swap("textures");
        if (client.world != null) {
            client.getTextureManager().tick();
        }
        if (client.currentScreen == null && client.player != null) {
            if (client.player.isDead() && !(client.currentScreen instanceof DeathScreen)) {
                client.setScreen(null);
            } else if (client.player.isSleeping() && client.world != null) {
                client.setScreen(new SleepingChatScreen());
            }
        } else {
            Screen screen = client.currentScreen;
            if (screen instanceof SleepingChatScreen sleepingChatScreen) {
                assert client.player != null;
                if (!client.player.isSleeping()) {
                    sleepingChatScreen.closeChatIfEmpty();
                }
            }
        }
        if (client.currentScreen != null) {
            ((MinecraftClientAccessor) client).setAttackCooldown(1000);
        }
        if (client.currentScreen != null) {

            Screen.wrapScreenError(() -> client.currentScreen.tick(), "Ticking screen", client.currentScreen.getClass().getCanonicalName());
        }
        if (!client.options.debugEnabled) {
            client.inGameHud.resetDebugHudChunk();
        }

        if (minecraftClientAccessor.getOverlay() == null && (client.currentScreen == null || client.currentScreen.passEvents)) {
            minecraftClientAccessor.invokeHandleBlockBreaking(client.currentScreen == null && client.options.attackKey.isPressed() && client.mouse.isCursorLocked());

            int attackCooldown = ((MinecraftClientAccessor) client).getAttackCooldown();
            if (attackCooldown > 0) {
                ((MinecraftClientAccessor) client).setAttackCooldown(attackCooldown - 1);
            }
        }

        if (clientPlayer != null && !client.isPaused() && !clientPlayer.hasVehicle()) {
            clientPlayer.resetPosition();
            clientPlayer.age++;
            clientPlayer.tick();
        }

        if (client.world != null) {
            profiler.swap("gameRenderer");
            client.gameRenderer.tick();
            client.getMusicTracker().tick();
            profiler.swap("particles");
            client.particleManager.tick();
        }

        client.getSoundManager().tick(false);
        profiler.swap("keyboard");
        client.keyboard.pollDebugCrash();
        profiler.pop();

        client.getMusicTracker().tick();
    }
}
