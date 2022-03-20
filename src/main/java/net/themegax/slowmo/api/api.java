package net.themegax.slowmo.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.themegax.slowmo.SlowmoMain;
import net.themegax.slowmo.ext.PlayerEntityExt;

import static net.themegax.slowmo.SlowmoMain.updateClientTickrate;
import static net.themegax.slowmo.SlowmoMain.updateServerTickrate;

public class api {
    public void setPlayerTickrate(float tps, PlayerEntity player) {
        updateClientTickrate(tps, (ServerPlayerEntity) player);
    }
    public float getPlayerTickrate(PlayerEntity player) {
        return ((PlayerEntityExt)player).getPlayerTicks();
    }

    public void setServerTickrate(float tps, MinecraftServer server) {
        updateServerTickrate(tps, server);
    }
    public float getServerTickrate() {
        return SlowmoMain.TICKS_PER_SECOND;
    }
}
