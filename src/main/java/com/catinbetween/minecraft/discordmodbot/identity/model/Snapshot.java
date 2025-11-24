package com.catinbetween.minecraft.discordmodbot.identity.model;


import java.sql.Timestamp;
import java.util.UUID;

public record Snapshot (
    Integer snapshotId,
    UUID minecraftUuid,
    String minecraftName,
    String discordId,
    Timestamp firstSeen,
    Timestamp lastSeen,
    boolean isCurrent
) {}
