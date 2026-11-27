package me.gdib.core.common.cooldown;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class CooldownManager {

    private final Map<UUID, Map<String, Long>> cooldowns;

    public CooldownManager() {
        this.cooldowns = new HashMap<>();
    }

    public void setCooldown(
            UUID playerId,
            String key,
            long durationMillis) {

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

        long durationNanos = TimeUnit.MILLISECONDS.toNanos(
            durationMillis
        );

        long expiration = System.nanoTime() + durationNanos;

        cooldowns.computeIfAbsent(
            playerId,
            id -> new HashMap<>()
        ).put(key, expiration);
    }

    public boolean hasCooldown(UUID playerId, String key) {
        validateArguments(playerId, key);

        Map<String, Long> playerCooldowns = cooldowns.get(playerId);

        if (playerCooldowns == null) {
            return false;
        }

        Long expiration = playerCooldowns.get(key);

        if (expiration == null) {
            return false;
        }

        if (expiration - System.nanoTime() <= 0) {
            removeCooldown(playerId, key);
            return false;
        }

        return true;
    }

    public long getRemainingMillis(UUID playerId, String key) {
        if (!hasCooldown(playerId, key)) {
            return 0L;
        }

        long expiration = cooldowns.get(playerId).get(key);
        long remainingNanos = expiration - System.nanoTime();

        return TimeUnit.NANOSECONDS.toMillis(
            remainingNanos + 999999L
        );
    }

    public boolean removeCooldown(UUID playerId, String key) {
        validateArguments(playerId, key);

        Map<String, Long> playerCooldowns = cooldowns.get(playerId);

        if (playerCooldowns == null) {
            return false;
        }

        boolean removed = playerCooldowns.remove(key) != null;

        if (playerCooldowns.isEmpty()) {
            cooldowns.remove(playerId);
        }

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

    public int getCooldownCount(UUID playerId) {
        if (playerId == null) {
            throw new IllegalArgumentException(
                "Player UUID cannot be null"
            );
        }

        Map<String, Long> playerCooldowns = cooldowns.get(playerId);

        if (playerCooldowns == null) {
            return 0;
        }

        int activeCount = 0;
        Iterator<Map.Entry<String, Long>> iterator =
            playerCooldowns.entrySet().iterator();

        long now = System.nanoTime();

        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();

            if (entry.getValue() - now > 0) {
                activeCount++;
            } else {
                iterator.remove();
            }
        }

        if (playerCooldowns.isEmpty()) {
            cooldowns.remove(playerId);
        }

        return activeCount;
    }

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
