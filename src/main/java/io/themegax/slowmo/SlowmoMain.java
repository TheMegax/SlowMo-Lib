package io.themegax.slowmo;

import io.themegax.slowmo.config.SlowmoConfig;
import io.themegax.slowmo.ext.PlayerEntityExt;
import io.themegax.slowmo.mixin.common.MinecraftServerAccessor;
import io.themegax.slowmo.registry.CommandRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraft.util.math.MathHelper.clamp;

public class SlowmoMain implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger("SlowMo Lib");

	private static final String modID = "slowmo";
	public static final Identifier TICKRATE_PACKET_ID = new Identifier(modID, "tickrate_packet");
	public static final Identifier SERVER_TICKRATE_PACKET_ID = new Identifier(modID, "server_tickrate_packet");

	public static final float DEFAULT_TICKRATE = 20;
	public static final float MIN_TICKRATE = 0.1f;
	public static final float MAX_TICKRATE = 1000;

	public static float ticksPerSecond = 20;
	public static long millisecondsPerTick = 50L;

	public static int permissionLevel = 2;

	public static final GameRules.Key<DoubleRule> WORLD_TICK_SPEED =
			GameRuleRegistry.register("worldTickspeed",
					GameRules.Category.MISC,
					GameRuleFactory.createDoubleRule(DEFAULT_TICKRATE));

	@Override
	public void onInitialize() {
		new SlowmoConfig(modID).load();

		if (SlowmoConfig.tickrateCommands) {
			permissionLevel = SlowmoConfig.getPermissionLevel();
			CommandRegistry.init();
		}
		ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
		ServerEntityEvents.ENTITY_LOAD.register(this::onEntityLoad);

		//TODO LIST:
		// - Client particle spawning smoothing
		// - Mount ride fix
		// - Sound modifier intensity fix
		// - Fix item cooldown inconsistency on low server tickrates
		// - Fix thrown potions/arrows not rendering when too close
		// - Suggestion Provider

		LOGGER.info("SlowMo Lib Initialized");
	}

	public static String getModID() {
		return modID;
	}

	public static Logger getLogger() {
		return LOGGER;
	}

	private void onEntityLoad(Entity entity, ServerWorld serverWorld) {
		if (entity.isPlayer()) {
			float PLAYER_TICKS_PER_SECOND = ((PlayerEntityExt)entity).getPlayerTicks();
			updateClientTickrate(PLAYER_TICKS_PER_SECOND, (ServerPlayerEntity) entity);

			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeFloat(ticksPerSecond);
			ServerPlayNetworking.send((ServerPlayerEntity) entity, SERVER_TICKRATE_PACKET_ID, buf);
		}
	}


	private void onServerTick(MinecraftServer server) {
		float tickSpeedGamerule = (float) server.getGameRules().get(WORLD_TICK_SPEED).get();
		if (tickSpeedGamerule != ticksPerSecond) {
			updateServerTickrate(tickSpeedGamerule, server);
		}
	}

	public static void updateServerTickrate(float f, MinecraftServer minecraftServer) {
		f = clamp(f, MIN_TICKRATE, MAX_TICKRATE);

		if (ticksPerSecond != f) {
			millisecondsPerTick = (long) (1000/f);
			ticksPerSecond = f;

			((MinecraftServerAccessor)minecraftServer).setWaitingForNextTick(false);
			((MinecraftServerAccessor)minecraftServer).setTimeReference(Util.getMeasuringTimeMs()-1);
			((MinecraftServerAccessor)minecraftServer).setNextTickTimestamp(Util.getMeasuringTimeMs()+ millisecondsPerTick);

			// Lazy, but practical
			minecraftServer.getCommandManager().execute(
					minecraftServer.getCommandSource(), "/gamerule worldTickspeed " + f);

			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeFloat(f);

			for (ServerPlayerEntity player : PlayerLookup.all(minecraftServer)) {
				ServerPlayNetworking.send(player, SERVER_TICKRATE_PACKET_ID, buf);
			}

			LOGGER.info("Updated server tickrate to {} TPS", f);
		}
	}

	public static void updateClientTickrate(float f, ServerPlayerEntity player) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeFloat(f);

		((PlayerEntityExt)player).setPlayerTicks(f);
		ServerPlayNetworking.send(player, TICKRATE_PACKET_ID, buf);
	}
}
