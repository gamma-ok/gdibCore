package me.gdib.core.modules.tab.packet.v1_8_R3;

import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;

import com.mojang.authlib.GameProfile;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.PlayerInteractManager;
import net.minecraft.server.v1_8_R3.WorldServer;

public final class V1_8_R3FakePlayerFactory {

    /**
     * Creates a fake EntityPlayer using the viewer's world.
     *
     * @param viewer player who will receive the TAB entries
     * @param profile fake player profile
     * @return generated EntityPlayer
     */
    public EntityPlayer create(
            Player viewer,
            GameProfile profile
    ) {
        Objects.requireNonNull(
                viewer,
                "Viewer cannot be null"
        );

        Objects.requireNonNull(
                profile,
                "GameProfile cannot be null"
        );

        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException(
                    "Fake players must be created on the main thread."
            );
        }

        if (!viewer.isOnline()) {
            throw new IllegalArgumentException(
                    "Viewer must be online."
            );
        }

        MinecraftServer server = (
                (CraftServer) Bukkit.getServer()
        ).getServer();

        WorldServer world = (
                (CraftWorld) viewer.getWorld()
        ).getHandle();

        PlayerInteractManager interactManager =
                new PlayerInteractManager(world);

        return new EntityPlayer(
                server,
                world,
                profile,
                interactManager
        );
    }
}
