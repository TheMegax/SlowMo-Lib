package io.themegax.slowmo.api;

import io.themegax.slowmo.ext.PlayerEntityExt;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import io.themegax.slowmo.SlowmoMain;

import static io.themegax.slowmo.SlowmoMain.updateClientTickrate;
import static io.themegax.slowmo.SlowmoMain.updateServerTickrate;

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
    public static float getServerTickrate() {
        return SlowmoMain.TICKS_PER_SECOND;
    }
}
