package me.gdib.core.modules.tab.packet.v1_8_R3;

import java.util.Objects;

import org.bukkit.ChatColor;

import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;

public final class V1_8_R3TabDisplayNameFactory {

	/**
	 * Creates an NMS display name from formatted text.
	 *
	 * @param text original text with optional color codes
	 * @return formatted NMS chat component
	 */
	public IChatBaseComponent create(String text) {

		Objects.requireNonNull(text, "Display name cannot be null");

		String formattedText = ChatColor.translateAlternateColorCodes('&', text);

		return new ChatComponentText(formattedText);
	}

	/**
	 * Creates an empty display name.
	 */
	public IChatBaseComponent createEmpty() {
		return new ChatComponentText("");
	}
}
