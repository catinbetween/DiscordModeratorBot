package com.finni.discordmodbot.command;

import net.kyori.adventure.text.Component;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class CustomConsoleSender implements CommandSender {
    private final CommandSender sender;

    private String message;


    public String getMessage(){
        return message;
    }

    public CustomConsoleSender(CommandSender sender) {
        this.sender = sender;
        this.message = "";
    }

    @Override
    public void sendMessage(String message) {
        if (!message.isEmpty())
            //event.reply(message);
            this.message += message + "\n";
    }

    @Override
    public void sendMessage(String[] messages) {
        List<String> queue = Arrays.stream(messages).toList();
        if (queue.size() > 0)
            sender.sendMessage(queue.toArray( new String[0] ));

    }

    @Override
    public void sendMessage(@Nullable UUID sender, @NotNull String message) {
        this.sender.sendMessage(sender, message);
    }

    @Override
    public void sendMessage(@Nullable UUID sender, @NotNull String... messages) {
        this.sender.sendMessage(sender, messages);
    }

    @Override
    public @NotNull Server getServer() {
        return sender.getServer();
    }

    @Override
    public @NotNull String getName() {
        return sender.getName();
    }

    @NotNull
    @Override
    public Spigot spigot() {
        return sender.spigot();
    }

    @Override
    public @NotNull Component name() {
        return sender.name();
    }

    @Override
    public boolean isPermissionSet( @NotNull String name) {
        return sender.isPermissionSet(name);
    }

    @Override
    public boolean isPermissionSet( @NotNull Permission perm) {
        return sender.isPermissionSet(perm);
    }

    @Override
    public boolean hasPermission( @NotNull String name) {
        return sender.hasPermission(name);
    }

    @Override
    public boolean hasPermission( @NotNull Permission perm) {
        return sender.hasPermission(perm);
    }

    @Override
    public @NotNull PermissionAttachment addAttachment( @NotNull Plugin plugin, @NotNull String name, boolean value) {
        return sender.addAttachment(plugin, name, value);
    }

    @Override
    public @NotNull PermissionAttachment addAttachment( @NotNull Plugin plugin) {
        return sender.addAttachment(plugin);
    }

    @Override
    public PermissionAttachment addAttachment( @NotNull Plugin plugin, @NotNull String name, boolean value, int ticks) {
        return sender.addAttachment(plugin, name, value, ticks);
    }

    @Override
    public PermissionAttachment addAttachment( @NotNull Plugin plugin, int ticks) {
        return sender.addAttachment(plugin, ticks);
    }

    @Override
    public void removeAttachment( @NotNull PermissionAttachment attachment) {
        sender.removeAttachment(attachment);
    }

    @Override
    public void recalculatePermissions() {
        sender.recalculatePermissions();
    }

    @Override
    public @NotNull Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return sender.getEffectivePermissions();
    }

    @Override
    public boolean isOp() {
        return sender.isOp();
    }

    @Override
    public void setOp(boolean value) {
        sender.setOp(value);
    }
}