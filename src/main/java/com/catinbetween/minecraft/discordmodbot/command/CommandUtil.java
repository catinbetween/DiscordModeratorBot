package com.catinbetween.minecraft.discordmodbot.command;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;


public final class CommandUtil {

    private CommandUtil() {}

    public static RequiredArgumentBuilder<ServerCommandSource, EntitySelector> targetPlayerArgument() {
        return CommandManager.argument("target_player", EntityArgumentType.player());
    }

    public static ServerPlayerEntity getCommandTargetPlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        try {
            return EntityArgumentType.getPlayer(context, "target_player");
        } catch (IllegalArgumentException e) {
            return context.getSource().getPlayer();
        }
    }

}
