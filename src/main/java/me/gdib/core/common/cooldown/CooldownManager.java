package me.gdib.core.common.cooldown;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public final class CooldownManager {

    private final ConcurrentMap<UUID, ConcurrentMap<String, Long>> cooldowns;

    public CooldownManager() {
        this.cooldowns = new ConcurrentHashMap<>();
    }

    // =========================================================
    // COOLDOWN MANAGEMENT
    // =========================================================

    public void setCooldown(
            UUID playerId,
            String key,
            long durationMillis
    ) {
        validateArguments(playerId, key);

        if (durationMillis < 0) {
            throw new IllegalArgumentException(
                    "Cooldown duration cannot be negative"
            );
        }

        if (durationMillis == 0) {
            removeCooldown(playerId, key);
            return;
        }

        long durationNanos = TimeUnit.MILLISECONDS.toNanos(durationMillis);
        long expiration = System.nanoTime() + durationNanos;

        cooldowns.computeIfAbsent(
                playerId,
                id -> new ConcurrentHashMap<>()
        ).put(key, expiration);
    }

    public boolean hasCooldown(UUID playerId, String key) {
        validateArguments(playerId, key);

        ConcurrentMap<String, Long> playerCooldowns =
                cooldowns.get(playerId);

        if (playerCooldowns == null) {
            return false;
        }

        Long expiration = playerCooldowns.get(key);

        if (expiration == null) {
            return false;
        }

        if (expiration - System.nanoTime() <= 0) {
            playerCooldowns.remove(key, expiration);
            removeEmptyPlayer(playerId, playerCooldowns);

            return false;
        }

        return true;
    }

    public long getRemainingMillis(UUID playerId, String key) {
        validateArguments(playerId, key);

        ConcurrentMap<String, Long> playerCooldowns =
                cooldowns.get(playerId);

        if (playerCooldowns == null) {
            return 0L;
        }

        Long expiration = playerCooldowns.get(key);

        if (expiration == null) {
            return 0L;
        }

        long remainingNanos = expiration - System.nanoTime();

        if (remainingNanos <= 0) {
            playerCooldowns.remove(key, expiration);
            removeEmptyPlayer(playerId, playerCooldowns);

            return 0L;
        }

        // Round up to avoid returning zero while time remains.
        long remainingMillis =
                remainingNanos / 1_000_000L;

        if (remainingNanos % 1_000_000L != 0) {
            remainingMillis++;
        }

        return remainingMillis;
    }

    public boolean removeCooldown(UUID playerId, String key) {
        validateArguments(playerId, key);

        ConcurrentMap<String, Long> playerCooldowns =
                cooldowns.get(playerId);

        if (playerCooldowns == null) {
            return false;
        }

        boolean removed = playerCooldowns.remove(key) != null;

        removeEmptyPlayer(playerId, playerCooldowns);

        return removed;
    }

    public void removePlayer(UUID playerId) {
        if (playerId == null) {
            throw new IllegalArgumentException(
                    "Player UUID cannot be null"
            );
        }

        cooldowns.remove(playerId);
    }

    public void clear() {
        cooldowns.clear();
    }

    // =========================================================
    // INFORMATION
    // =========================================================

    public int getCooldownCount(UUID playerId) {
        if (playerId == null) {
            throw new IllegalArgumentException(
                    "Player UUID cannot be null"
            );
        }

        ConcurrentMap<String, Long> playerCooldowns =
                cooldowns.get(playerId);

        if (playerCooldowns == null) {
            return 0;
        }

        int activeCount = 0;
        long now = System.nanoTime();

        Iterator<Map.Entry<String, Long>> iterator =
                playerCooldowns.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();

            if (entry.getValue() - now > 0) {
                activeCount++;
            } else {
                playerCooldowns.remove(
                        entry.getKey(),
                        entry.getValue()
                );
            }
        }

        removeEmptyPlayer(playerId, playerCooldowns);

        return activeCount;
    }

    // =========================================================
    // INTERNAL CLEANUP
    // =========================================================

    private void removeEmptyPlayer(
            UUID playerId,
            ConcurrentMap<String, Long> playerCooldowns
    ) {
        if (playerCooldowns.isEmpty()) {
            cooldowns.remove(playerId, playerCooldowns);
        }
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateArguments(UUID playerId, String key) {
        if (playerId == null) {
            throw new IllegalArgumentException(
                    "Player UUID cannot be null"
            );
        }

        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Cooldown key cannot be null or empty"
            );
        }
    }
}
