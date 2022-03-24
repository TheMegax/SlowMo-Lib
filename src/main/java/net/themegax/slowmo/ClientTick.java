package net.themegax.slowmo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SleepingChatScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.profiler.Profiler;
import net.themegax.slowmo.ext.ClientPlayerInteractionManagerExt;
import net.themegax.slowmo.mixin.client.MinecraftClientAccessor;

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

        if (clientPlayer != null && !client.isPaused()) {
            clientPlayer.resetPosition();
            clientPlayer.age++;
            clientPlayer.tick();
        }

        if (client.world != null) {
            profiler.swap("gameRenderer");
            client.gameRenderer.tick();
            client.getMusicTracker().tick();
        }

        profiler.swap("particles");
        client.particleManager.tick();

        client.getSoundManager().tick(false);
        profiler.swap("keyboard");
        client.keyboard.pollDebugCrash();
        profiler.pop();

        client.getMusicTracker().tick();
    }
    /*
    public static void worldTick(MinecraftClient client) {
        ClientWorld world = client.world;
        ClientPlayerEntity player = client.player;
        Profiler profiler = client.getProfiler();
        
        profiler.push("gui");
        client.inGameHud.tick(client.isPaused());
        profiler.pop();
        client.gameRenderer.updateTargetedEntity(1.0f);
        client.getTutorialManager().tick(world, client.crosshairTarget);
        profiler.push("gameMode");
        if (!client.isPaused() && world != null) {
            client.interactionManager.tick();
        }
        profiler.swap("textures");
        if (world != null) {
            client.getTextureManager().tick();
        }
        if (client.currentScreen == null && client.player != null) {
            if (client.player.isDead() && !(client.currentScreen instanceof DeathScreen)) {
                client.setScreen(null);
            } else if (client.player.isSleeping() && world != null) {
                client.setScreen(new SleepingChatScreen());
            }
        } else {
            Screen screen = client.currentScreen;
            if (screen instanceof SleepingChatScreen) {
                SleepingChatScreen sleepingChatScreen = (SleepingChatScreen)screen;
                if (!client.player.isSleeping()) {
                    sleepingChatScreen.closeChatIfEmpty();
                }
            }
        }

        if (client.currentScreen != null) {
            Screen.wrapScreenError(() -> client.currentScreen.tick(), "Ticking screen", client.currentScreen.getClass().getCanonicalName());
        }
        if (!client.options.debugEnabled) {
            client.inGameHud.resetDebugHudChunk();
        }
        if (world != null) {
            profiler.swap("gameRenderer");
            if (!client.isPaused()) {
                client.gameRenderer.tick();
            }
            profiler.swap("levelRenderer");
            if (!client.isPaused()) {
                client.worldRenderer.tick();
            }
            profiler.swap("level");
            if (!client.isPaused()) {
                if (client.world.getLightningTicksLeft() > 0) {
                    client.world.setLightningTicksLeft(client.world.getLightningTicksLeft() - 1);
                }
                world.tickEntities();
            }
        } else if (client.gameRenderer.getShader() != null) {
            client.gameRenderer.disableShader();
        }
        if (world != null) {
            if (!client.isPaused()) {
                if (!client.options.joinedFirstServer && ((MinecraftClientAccessor) client).invokeIsConnectedToServer()) {
                    TranslatableText text = new TranslatableText("tutorial.socialInteractions.title");
                    TranslatableText text2 = new TranslatableText("tutorial.socialInteractions.description", TutorialManager.keyToText("socialInteractions"));
                    ((MinecraftClientAccessor) client).setSocialInteractionsToast(new TutorialToast(TutorialToast.Type.SOCIAL_INTERACTIONS, text, text2, true));
                    client.getTutorialManager().add(((MinecraftClientAccessor) client).getSocialInteractionsToast() , 160);
                    client.options.joinedFirstServer = true;
                    client.options.write();
                }
                client.getTutorialManager().tick();
                try {
                    world.tick(() -> true);
                }
                catch (Throwable throwable) {
                    CrashReport crashReport = CrashReport.create(throwable, "Exception in world tick");
                    if (world == null) {
                        CrashReportSection crashReportSection = crashReport.addElement("Affected level");
                        crashReportSection.add("Problem", "Level is null!");
                    } else {
                        client.world.addDetailsToCrashReport(crashReport);
                    }
                    throw new CrashException(crashReport);
                }
            }
            profiler.swap("animateTick");
            if (!client.isPaused() && world != null) {
                world.doRandomBlockDisplayTicks(client.player.getBlockX(), client.player.getBlockY(), client.player.getBlockZ());
            }
            profiler.swap("particles");
            if (!client.isPaused()) {
                client.particleManager.tick();
            }
        } else if (((MinecraftClientAccessor) client).getIntegratedServerConnection() != null) {
            profiler.swap("pendingConnection");
            ((MinecraftClientAccessor) client).getIntegratedServerConnection().tick();
        }
        profiler.swap("keyboard");
        client.keyboard.pollDebugCrash();
        profiler.pop();
    }
     */
}
