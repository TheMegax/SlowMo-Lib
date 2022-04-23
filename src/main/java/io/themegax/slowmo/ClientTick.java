package io.themegax.slowmo;

import io.themegax.slowmo.ext.ClientPlayerInteractionManagerExt;
import io.themegax.slowmo.mixin.client.MinecraftClientAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SleepingChatScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.toast.TutorialToast;
import net.minecraft.client.tutorial.TutorialManager;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.profiler.Profiler;

// This class reeplaces MinecraftClient's "tick()" function
@Environment(EnvType.CLIENT)
public class ClientTick {
    public static void gameTick(MinecraftClient client) {
        Profiler profiler = client.getProfiler();
        MinecraftClientAccessor minecraftClientAccessor = ((MinecraftClientAccessor) client);
        
        profiler.push("gui");
        client.inGameHud.tick(client.isPaused());
        profiler.pop();
        client.gameRenderer.updateTargetedEntity(1.0F);
        client.getTutorialManager().tick(client.world, client.crosshairTarget);
        profiler.push("gameMode");
        if (!client.isPaused() && client.world != null) {
            client.interactionManager.tick();
        }

        if (client.currentScreen == null && client.player != null) {
            if (client.player.isDead() && !(client.currentScreen instanceof DeathScreen)) {
                client.setScreen((Screen)null);
            } else if (client.player.isSleeping() && client.world != null) {
                client.setScreen(new SleepingChatScreen());
            }
        } else {
            Screen var2 = client.currentScreen;
            if (var2 instanceof SleepingChatScreen) {
                SleepingChatScreen sleepingChatScreen = (SleepingChatScreen)var2;
                if (!client.player.isSleeping()) {
                    sleepingChatScreen.closeChatIfEmpty();
                }
            }
        }

        if (!client.options.debugEnabled) {
            client.inGameHud.resetDebugHudChunk();
        }

        if (client.world != null) {
            profiler.swap("levelRenderer");
            if (!client.isPaused()) {
                client.worldRenderer.tick();
            }

            profiler.swap("level");
            if (!client.isPaused()) {
                if (client.world.getLightningTicksLeft() > 0) {
                    client.world.setLightningTicksLeft(client.world.getLightningTicksLeft() - 1);
                }

                client.world.tickEntities();
            }
        } else if (client.gameRenderer.getShader() != null) {
            client.gameRenderer.disableShader();
        }

        if (client.world != null) {
            if (!client.isPaused()) {
                if (!client.options.joinedFirstServer && isConnectedToServer(client)) {
                    Text text = new TranslatableText("tutorial.socialInteractions.title");
                    Text text2 = new TranslatableText("tutorial.socialInteractions.description", new Object[]{TutorialManager.keyToText("socialInteractions")});
                    minecraftClientAccessor.setSocialInteractionsToast(new TutorialToast(net.minecraft.client.toast.TutorialToast.Type.SOCIAL_INTERACTIONS, text, text2, true));
                    client.getTutorialManager().add(minecraftClientAccessor.getSocialInteractionsToast(), 160);
                    client.options.joinedFirstServer = true;
                    client.options.write();
                }

                client.getTutorialManager().tick();

                try {
                    client.world.tick(() -> {
                        return true;
                    });
                } catch (Throwable var5) {
                    CrashReport crashReport = CrashReport.create(var5, "Exception in world tick");
                    if (client.world == null) {
                        CrashReportSection crashReportSection = crashReport.addElement("Affected level");
                        crashReportSection.add("Problem", "Level is null!");
                    } else {
                        client.world.addDetailsToCrashReport(crashReport);
                    }

                    throw new CrashException(crashReport);
                }
            }

            profiler.swap("animateTick");
            if (!client.isPaused() && client.world != null) {
                client.world.doRandomBlockDisplayTicks(client.player.getBlockX(), client.player.getBlockY(), client.player.getBlockZ());
            }
        } else if (minecraftClientAccessor.getIntegratedServerConnection() != null) {
            profiler.swap("pendingConnection");
            minecraftClientAccessor.getIntegratedServerConnection().tick();
        }
        profiler.pop();
    }
    public static void renderTick(MinecraftClient client) {
        ClientTickEvents.START_CLIENT_TICK.invoker().onStartTick(client);

        Profiler profiler = client.getProfiler();
        ClientPlayerEntity clientPlayer = client.player;
        MinecraftClientAccessor minecraftClientAccessor = ((MinecraftClientAccessor) client);

        int itemUseCooldown = ((MinecraftClientAccessor) client).getItemUseCooldown();
        if (itemUseCooldown > 0) {
            ((MinecraftClientAccessor) client).setItemUseCooldown(itemUseCooldown - 1);
        }

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
            Screen.wrapScreenError(() -> {
                Screen currentScreen = client.currentScreen;

                ScreenEvents.beforeTick(currentScreen).invoker().beforeTick(currentScreen);
                currentScreen.tick();
                ScreenEvents.afterTick(currentScreen).invoker().afterTick(currentScreen);

            }, "Ticking screen", client.currentScreen.getClass().getCanonicalName());
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

        ClientTickEvents.END_CLIENT_TICK.invoker().onEndTick(client);
    }

    private static boolean isConnectedToServer(MinecraftClient client) {
        return !client.isIntegratedServerRunning() || client.getServer() != null && client.getServer().isRemote();
    }
}
