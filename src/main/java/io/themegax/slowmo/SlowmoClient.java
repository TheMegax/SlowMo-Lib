package io.themegax.slowmo;

import io.themegax.slowmo.config.SlowmoConfig;
import io.themegax.slowmo.mixin.client.MinecraftClientAccessor;
import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import ladysnake.satin.api.managed.uniform.Uniform1f;
import me.lortseam.completeconfig.gui.ConfigScreenBuilder;
import me.lortseam.completeconfig.gui.cloth.ClothConfigScreenBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.slf4j.Logger;

import static io.themegax.slowmo.SlowmoMain.*;
import static net.minecraft.util.math.MathHelper.clamp;

public class SlowmoClient implements ClientModInitializer {
    public static RenderTickCounter playerTickCounter = new RenderTickCounter(20f, 0L);
    public static float clientTicksPerSecond = 20;
    public static float serverTicksPerSecond = 20;
    public static int maxClientTicks = 100;
    public static boolean changeSound = true;

    public static float soundPitch = 1f;
    private static float prevPitch = 1f;
    private static float goalPitch = 1f;
    private static float startingPitch = 1f;

    private static float prevSaturation = 1f;
    private static float goalSaturation = 1f;
    private static float startingSaturation = 1f;

    private static long startSat;
    private static long startPit;

    private static final ManagedShaderEffect SATURATION_SHADER = ShaderEffectManager.getInstance()
            .manage(new Identifier(SlowmoMain.getModID(), "shaders/post/saturation.json"));
    private final Uniform1f shaderSaturation = SATURATION_SHADER.findUniform1f("Saturation");

    @Override
    public void onInitializeClient() {
        // This code uses access wideners to access tickTime

        ClientTickEvents.START_CLIENT_TICK.register(e -> this.onClientTick());

        if (FabricLoader.getInstance().isModLoaded("cloth-config")) {
            ConfigScreenBuilder.setMain(SlowmoMain.getModID(), new ClothConfigScreenBuilder());
        }
        changeSound = SlowmoConfig.changeSound;
        maxClientTicks = SlowmoConfig.getMaxClientTicks();

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

        ShaderEffectRenderCallback.EVENT.register(tickDelta -> {
            if (SlowmoConfig.colorSaturation) {
                shaderSaturation.set(getSaturationProgress());
                SATURATION_SHADER.render(tickDelta);
            }
            if (SlowmoConfig.changeSound) {
                soundPitch = getSoundPitchProgress();
            }
        });
    }

    private float getSaturationProgress() {
        float stepCount = Math.min(System.currentTimeMillis() - startSat, SlowmoConfig.getFadeTimeMillis()) / SlowmoConfig.getFadeTimeMillis();
        if (startingSaturation < goalSaturation) {
            return startingSaturation + stepCount*(goalSaturation - startingSaturation);
        }
        return startingSaturation - stepCount*(startingSaturation - goalSaturation);
    }

    private float getSoundPitchProgress() {
        float stepCount = Math.min(System.currentTimeMillis() - startPit, SlowmoConfig.getFadeTimeMillis()) / SlowmoConfig.getFadeTimeMillis();
        if (startingPitch < goalPitch) {
            return startingPitch + stepCount*(goalPitch - startingPitch);
        }
        return startingPitch - stepCount*(startingPitch - goalPitch);
    }

    private void onClientTick() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;

        if (client.getServer() == null) {
            RenderTickCounter renderTickCounter = ((MinecraftClientAccessor)client).getRenderTickCounter();
            renderTickCounter.tickTime = 1000F / DEFAULT_TICKRATE;
            playerTickCounter.tickTime = 1000F / DEFAULT_TICKRATE;
            clientTicksPerSecond = DEFAULT_TICKRATE;
            serverTicksPerSecond = DEFAULT_TICKRATE;
        }
        changeSound = SlowmoConfig.changeSound;

        goalSaturation = MathHelper.clamp(serverTicksPerSecond/DEFAULT_TICKRATE, 0.5f, 1.25f);
        if (SlowmoConfig.doClampPitch) {
            goalPitch = MathHelper.clamp(serverTicksPerSecond/DEFAULT_TICKRATE, 0.2f, 5f);
        }
        else goalPitch = serverTicksPerSecond/DEFAULT_TICKRATE;

        if (world != null && prevSaturation != goalSaturation) {
            startSat = System.currentTimeMillis();
            startingSaturation = prevSaturation;
        }

        if (prevPitch != goalPitch) {
            startPit = System.currentTimeMillis();
            startingPitch = prevPitch;
        }

        prevSaturation = goalSaturation;
        prevPitch = goalPitch;
    }

    public static void updateServerTickrate(float f) {
        if (serverTicksPerSecond != f) {
            serverTicksPerSecond = f;
            MinecraftClient client = MinecraftClient.getInstance();
            RenderTickCounter renderTickCounter = ((MinecraftClientAccessor)client).getRenderTickCounter();
            renderTickCounter.tickTime = 1000F / serverTicksPerSecond;
        }
    }

    public static void updateClientTickrate(float f) {
        f = clamp(f, MIN_TICKRATE, MAX_TICKRATE);
        if (clientTicksPerSecond != f) {
            playerTickCounter.tickTime = 1000F / f;
            clientTicksPerSecond = f;
            Logger LOGGER = SlowmoMain.getLogger();
            LOGGER.info("Updated client tickrate to {} TPS", f);
        }
    }
}
