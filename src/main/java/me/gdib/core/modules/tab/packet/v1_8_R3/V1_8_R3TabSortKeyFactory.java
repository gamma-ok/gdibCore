package me.gdib.core.modules.tab.packet.v1_8_R3;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public final class V1_8_R3TabSortKeyFactory {

	private static final int MAX_POSITION = 80;

	private static final String TEAM_PREFIX = "g";

	/**
	 * Generates a unique, position-ordered team identifier for a specific viewer.
	 *
	 * Maximum length: 16 characters.
	 */
	public String createTeamName(UUID viewerId, int position) {
		Objects.requireNonNull(viewerId, "Viewer UUID cannot be null");

		validatePosition(position);

		String positionCode = String.format(Locale.ROOT, "%02d", position);

		String uniqueHash = generateHash(viewerId + ":gdib-tab:" + position);

		return TEAM_PREFIX + positionCode + uniqueHash.substring(0, 13);
	}

	/**
	 * Generates a deterministic identifier for a TAB entry.
	 */
	public String createEntryIdentifier(UUID viewerId, int position) {
		Objects.requireNonNull(viewerId, "Viewer UUID cannot be null");

		validatePosition(position);

		return "entry-" + generateHash(viewerId + ":gdib-entry:" + position).substring(0, 13);
	}

	private String generateHash(String value) {

		UUID hash = UUID.nameUUIDFromBytes(value.getBytes(StandardCharsets.UTF_8));

		return hash.toString().replace("-", "");
	}

	private void validatePosition(int position) {
		if (position < 1 || position > MAX_POSITION) {
			throw new IllegalArgumentException("TAB position must be between 1 and 80.");
		}
	}
}
