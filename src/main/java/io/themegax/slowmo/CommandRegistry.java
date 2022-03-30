package io.themegax.slowmo;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.themegax.slowmo.ext.PlayerEntityExt;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;

import java.util.Collection;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static io.themegax.slowmo.SlowmoMain.*;

public class CommandRegistry {
    static TranslatableText clientText = new TranslatableText("command.slowmo.client");
    static TranslatableText serverText = new TranslatableText("command.slowmo.server");
    static TranslatableText allText = new TranslatableText("command.slowmo.all");



    public static void init() {

        CommandRegistrationCallback.EVENT.register(
                ((dispatcher, dedicated) -> redirectRegister(dispatcher))
        );

        CommandRegistrationCallback.EVENT.register(
                ((dispatcher, dedicated) -> registerMain(dispatcher))
        );

    }

    public static void redirectRegister(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> node = registerMain(dispatcher);
        dispatcher.register(literal("ticks")
                .redirect(node));
        dispatcher.register(literal("tps")
                .redirect(node));
        dispatcher.register(literal("tickrate")
                .redirect(node));
    }

    public static LiteralCommandNode<ServerCommandSource> registerMain(CommandDispatcher<ServerCommandSource> dispatcher) {
        return dispatcher.register(literal("slowmo")
                .requires(source -> source.hasPermissionLevel(PERMISSION_LEVEL))
                .then(literal("reset")
                        .executes(ctx -> {
                            for (ServerPlayerEntity player : PlayerLookup.all(ctx.getSource().getServer())) {
                                ((PlayerEntityExt)player).setPlayerTicks(DEFAULT_TICKRATE);
                                updateClientTickrate(DEFAULT_TICKRATE, player);
                            }
                            updateServerTickrate(DEFAULT_TICKRATE, ctx.getSource().getServer());
                            ctx.getSource().sendFeedback(new TranslatableText("command.slowmo.reset"), true);
                            return 1;
                        }))
                .then(argument("ticks per second", FloatArgumentType.floatArg(MIN_TICKRATE, MAX_TICKRATE))
                        .then(literal("all")
                                .executes(ctx -> { // Affects everyone and the server
                                    float tps = FloatArgumentType.getFloat(ctx, "ticks per second");
                                    for (ServerPlayerEntity player : PlayerLookup.all(ctx.getSource().getServer())) {
                                        ((PlayerEntityExt)player).setPlayerTicks(tps);
                                        updateClientTickrate(tps, player);
                                    }
                                    updateServerTickrate(tps, ctx.getSource().getServer());
                                    ctx.getSource().sendFeedback(new TranslatableText("command.slowmo.changed_ticks", allText, tps), true);
                                    return 0;
                                }))
                        .then(literal("server")
                                .executes(ctx -> { // Affects only the server
                                    float tps = FloatArgumentType.getFloat(ctx, "ticks per second");
                                    updateServerTickrate(tps, ctx.getSource().getServer());
                                    ctx.getSource().sendFeedback(new TranslatableText("command.slowmo.changed_ticks", serverText, tps), true);
                                    return 0;
                                }))
                        .then(literal("player")
                                .then(argument("name", EntityArgumentType.players())
                                        .executes(ctx -> { // Affects specific player(s)
                                            float tps = FloatArgumentType.getFloat(ctx, "ticks per second");
                                            Collection<ServerPlayerEntity> playerEntities = EntityArgumentType.getPlayers(ctx, "name");
                                            for (ServerPlayerEntity player : playerEntities) {
                                                ((PlayerEntityExt)player).setPlayerTicks(tps);
                                                updateClientTickrate(tps, player);
                                            }
                                            ctx.getSource().sendFeedback(new TranslatableText("command.slowmo.changed_ticks", clientText, tps), true);
                                            return 0;
                                        })))
                ));
    }}