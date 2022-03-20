package net.themegax.slowmo;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderTickCounter;
import net.themegax.slowmo.mixin.client.MinecraftClientAccessor;

import static net.minecraft.util.math.MathHelper.clamp;
import static net.themegax.slowmo.SlowmoMain.*;

public class SlowmoClient implements ClientModInitializer {
    public static RenderTickCounter playerTickCounter = new RenderTickCounter(20f, 0L);
    public static float CLIENT_TICKS_PER_SECOND = 20;
    public static float SERVER_TICKS_PER_SECOND = 20;
    public static int MAX_CLIENT_TICKS = 100;

    @Override
    public void onInitializeClient() {
        // This code uses access wideners to access tickTime

        ClientTickEvents.START_CLIENT_TICK.register(e -> this.onClientTick());

        ClientPlayNetworking.registerGlobalReceiver(
                TICKRATE_PACKET_ID, (client, handler, buf, responseSender) -> {
            float ticks = buf.readFloat();
            client.execute(() -> updateClientTickrate(ticks));
        });

        ClientPlayNetworking.registerGlobalReceiver(
                SERVER_TICKRATE_PACKET_ID, (client, handler, buf, responseSender) -> {
            float ticks = buf.readFloat();
            client.execute(() -> updateServerTickrate(ticks));
        });
    }

    private void onClientTick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getServer() == null) {
            RenderTickCounter renderTickCounter = ((MinecraftClientAccessor)client).getRenderTickCounter();
            renderTickCounter.tickTime = 1000F / DEFAULT_TICKRATE;
            playerTickCounter.tickTime = 1000F / DEFAULT_TICKRATE;
            CLIENT_TICKS_PER_SECOND = DEFAULT_TICKRATE;
            SERVER_TICKS_PER_SECOND = DEFAULT_TICKRATE;
        }

        RenderTickCounter renderTickCounter = ((MinecraftClientAccessor)client).getRenderTickCounter();
        renderTickCounter.tickTime = 1000F / SERVER_TICKS_PER_SECOND;
    }


    public static void updateServerTickrate(float f) {
        SERVER_TICKS_PER_SECOND = f;
    }

    public static void updateClientTickrate(float f) {
        f = clamp(f, MIN_TICKRATE, MAX_TICKRATE);
        playerTickCounter.tickTime = 1000F / f;
        CLIENT_TICKS_PER_SECOND = f;
        LOGGER.info("Updated client tickrate to {} TPS", f);
    }
}
