package com.catinbetween.minecraft.discordmodbot.command;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;


public final class CommandUtil {

    private CommandUtil() {}

    public static RequiredArgumentBuilder<CommandSourceStack, EntitySelector> targetPlayerArgument() {
        return Commands.argument("target_player", EntityArgument.player());
    }

    public static ServerPlayer getCommandTargetPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        try {
            return EntityArgument.getPlayer(context, "target_player");
        } catch (IllegalArgumentException e) {
            return context.getSource().getPlayer();
        }
    }

}
