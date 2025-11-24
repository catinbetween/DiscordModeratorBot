package com.catinbetween.minecraft.discordmodbot.utils;

import com.catinbetween.minecraft.discordmodbot.identity.model.Snapshot;
import com.hypherionmc.craterlib.core.event.annot.CraterEventListener;
import com.hypherionmc.sdlink.api.accounts.MinecraftAccount;
import com.hypherionmc.sdlink.api.events.SDLinkReadyEvent;
import com.hypherionmc.sdlink.api.events.VerificationEvent;
import com.hypherionmc.sdlink.core.database.SDLinkAccount;
import com.hypherionmc.sdlink.core.managers.DatabaseManager;
import lombok.extern.log4j.Log4j2;
import net.minecraft.server.network.ServerPlayerEntity;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Event listener that syncs SDLink account data to the historical identity database
 */
@Log4j2
public class SDLinkEventHandler {

    private final DatabaseManager SDLinkDatabaseManager;
    private final IdentityDatabaseManager identityDatabaseManager;

    public SDLinkEventHandler(DatabaseManager SDLinkDatabaseManager, IdentityDatabaseManager identityDatabaseManager) {
        this.SDLinkDatabaseManager = SDLinkDatabaseManager;
        this.identityDatabaseManager = identityDatabaseManager;
    }


    /**
     * Fired when SDLink bot is fully connected and ready
     * Use this to perform initial sync of all verified accounts
     */
    @CraterEventListener
    public void onSdlinkReady(SDLinkReadyEvent event) {
        log.info("SDLink is ready, starting initial sync...");

        try {
            List<SDLinkAccount> allAccounts = SDLinkDatabaseManager.findAll(SDLinkAccount.class);
            int synced = 0;
            int skipped = 0;

            for (SDLinkAccount account : allAccounts) {
                // Only sync fully verified accounts (must have Discord ID)
                if (isFullyVerified(account)) {
                    try {
                        syncAccount(account);
                        synced++;
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid UUID format: {}", account.getUuid());
                        skipped++;
                    } catch (SQLException e) {
                        log.warn("Failed to sync account {}: {}", account.getUuid(), e.getMessage());
                        skipped++;
                    }
                } else {
                    skipped++; // Incomplete verification or missing data
                }
            }

            log.info("Initial sync completed: {} accounts synced, {} skipped", synced, skipped);

        } catch (Exception e) {
            log.error("Initial sync failed", e);
        }
    }

     /* Fired when a player completes the verification process
     * This is the primary sync trigger
     */
    @CraterEventListener
    public void onPlayerVerified(VerificationEvent.PlayerVerified event) {
        MinecraftAccount minecraftAccount = event.getAccount();
        UUID uuid = minecraftAccount.getUuid();

        log.debug("Player verified: {} ({})", minecraftAccount.getUsername(), uuid);


        try {
            SDLinkAccount account = DatabaseManager.INSTANCE.findById(uuid.toString(), SDLinkAccount.class);

            if (isFullyVerified(account)) {
                syncAccount(account);
                log.debug("Successfully synced verified player: {}", minecraftAccount.getUsername());
            } else {
                log.warn("Player verified but account data incomplete: {}", uuid);
            }

        } catch (SQLException e) {
            log.error("Failed to sync verified player {}", uuid, e);
        } catch (Exception e) {
            log.error("Unexpected error syncing verified player {}", uuid, e);
        }
    }

    /**
     * Fired when a player is unverified (unlinked)
     * Mark their current link snapshots as no longer active
     */
    @CraterEventListener
    public void onPlayerUnverified(VerificationEvent.PlayerUnverified event) {
        MinecraftAccount minecraftAccount = event.getAccount();
        UUID uuid = minecraftAccount.getUuid();

        log.debug("Player unverified: {} ({})", minecraftAccount.getUsername(), uuid);

            try {
                identityDatabaseManager.markSnapshotsAsOldByUuid(uuid);
                log.debug("Marked snapshots as old for unverified player: {}", minecraftAccount.getUsername());
            } catch (SQLException e) {
                log.error("Failed to mark snapshots as old for {}", uuid, e);
            }
    }
    /**
     * Handles an event when a player joins the server
     * Use this to catch Minecraft username changes
     * */
    public void onPlayerJoin(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        try {
            SDLinkAccount account = DatabaseManager.INSTANCE.findById(player.getUuid().toString(), SDLinkAccount.class);
            if (isFullyVerified(account)) {
                syncAccount(account);
               log.debug("Synced player on join: {}", player.getName().getString());
            }

        } catch (SQLException e) {
            log.warn("Failed to sync player on join: {}", uuid, e);
        } catch (Exception e) {
            log.warn("Unexpected error syncing player on join: {}", uuid, e);
        }
    }


    /**
     * Check if an SDLinkAccount is fully verified and has all required data
     */
    private boolean isFullyVerified(SDLinkAccount account) {
        if (account == null) {
            return false;
        }
        // Must have Discord ID
        if (account.getDiscordID() == null || account.getDiscordID().isEmpty()) {
            return false;
        }
        // Must have valid UUID
        if (account.getUuid() == null || account.getUuid().isEmpty()) {
            return false;
        }
        // Must have Minecraft name
        String mcName = account.getUsername();
        return mcName != null && !mcName.isEmpty();
    }

    /**
     * Sync an SDLinkAccount to the historical database
     * Handles identity profile assignment and snapshot creation
     */
    private void syncAccount(SDLinkAccount account) throws SQLException {
        UUID uuid = UUID.fromString(account.getUuid());
        String mcName = account.getInGameName();
        String discordId = account.getDiscordID();

        // Check if this exact link state already exists
        Optional<Integer> existingSnapshotId = identityDatabaseManager.getCurrentSnapshot(uuid, discordId);

        if (existingSnapshotId.isPresent()) {
            // Link state unchanged, just update last_seen
            identityDatabaseManager.updateLastSeen(uuid, discordId);
            return;
        }

        // Link state has changed - need to create new snapshot
        // Mark old snapshots as no longer current
        Optional<Snapshot> oldUuidSnapshot = identityDatabaseManager.getCurrentSnapshotByUuid(uuid);
        Optional<Snapshot> oldDiscordSnapshot = identityDatabaseManager.getCurrentSnapshotByDiscord(discordId);

        if (oldUuidSnapshot.isPresent()) {
            identityDatabaseManager.markSnapshotAsOld(oldUuidSnapshot.get().snapshotId());
        }

        if (oldDiscordSnapshot.isPresent() &&
                !oldDiscordSnapshot.get().snapshotId().equals(oldUuidSnapshot.map(Snapshot::snapshotId).orElse(null))) {
            identityDatabaseManager.markSnapshotAsOld(oldDiscordSnapshot.get().snapshotId());
        }
        // Create new snapshot
        Optional<Integer> newSnapshotIdWrapper = identityDatabaseManager.createSnapshot(uuid, mcName, discordId);

        if (newSnapshotIdWrapper.isEmpty()) {
            throw new RuntimeException("Failed to create new snapshot for minecraft account: " + uuid);
        }

        // Determine which identity profile this belongs to
        Optional<Integer> identityProfileByUuidWrapper = identityDatabaseManager.findIdentityProfileByUuid(uuid);
        Optional<Integer> identityProfileByDiscordWrapper = identityDatabaseManager.findIdentityProfileByDiscordId(discordId);

        Optional<Integer> targetIdentityProfileIdWrapper;

        if (identityProfileByUuidWrapper.isPresent() && identityProfileByDiscordWrapper.isPresent()) {
            Integer identityProfileByUuid = identityProfileByUuidWrapper.get();
            Integer identityProfileByDiscord = identityProfileByDiscordWrapper.get();
            if (identityProfileByUuid.equals(identityProfileByDiscord)) {
                targetIdentityProfileIdWrapper = identityProfileByUuidWrapper;
            } else {
                identityDatabaseManager.mergeIdentityProfiles(identityProfileByUuid, identityProfileByDiscord);
                targetIdentityProfileIdWrapper = identityProfileByUuidWrapper;
                log.debug("Merged identity profiles {} and {} for player {}", identityProfileByUuid, identityProfileByDiscord, mcName);
            }
        } else if (identityProfileByUuidWrapper.isPresent()) {
            targetIdentityProfileIdWrapper = identityProfileByUuidWrapper;
        } else if (identityProfileByDiscordWrapper.isPresent()) {
            targetIdentityProfileIdWrapper = identityProfileByDiscordWrapper;
        } else {
            // Neither has an identity profile - create new one
            targetIdentityProfileIdWrapper = identityDatabaseManager.createIdentityProfile();
        }
        if (targetIdentityProfileIdWrapper.isEmpty()) {
            throw new RuntimeException("Failed to determine or create identity profile for account: " + uuid);
        }
        log.debug("Created new identity profile {} for player {}", targetIdentityProfileIdWrapper.get(), mcName);

        // Step 4: Add snapshot to identity profile
        identityDatabaseManager.addSnapshotToIdentityProfile(targetIdentityProfileIdWrapper.get(), newSnapshotIdWrapper.get());
        log.debug("Created snapshot {} in identity profile {} for {} ({}) <-> {}", newSnapshotIdWrapper, targetIdentityProfileIdWrapper, mcName, uuid, discordId);
    }
}
