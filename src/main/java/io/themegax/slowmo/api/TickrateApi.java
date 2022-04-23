package io.themegax.slowmo.api;

import io.themegax.slowmo.ext.MinecraftServerExt;
import io.themegax.slowmo.ext.PlayerEntityExt;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import static io.themegax.slowmo.SlowmoMain.updateClientTickrate;
import static io.themegax.slowmo.SlowmoMain.updateServerTickrate;

@SuppressWarnings("unused")
public class TickrateApi {
    public static void setPlayerTickrate(float tps, PlayerEntity player) {
        updateClientTickrate(tps, (ServerPlayerEntity) player);
    }
    public static float getPlayerTickrate(PlayerEntity player) {
        return ((PlayerEntityExt)player).getPlayerTicks();
    }

    public static void setServerTickrate(float tps, MinecraftServer server) {
        updateServerTickrate(tps, server);
    }
    public static float getServerTickrate(MinecraftServer server) {
        return ((MinecraftServerExt) server).getServerTickrate();
    }
}
