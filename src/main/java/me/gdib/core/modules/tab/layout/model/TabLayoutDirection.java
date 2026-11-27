package me.gdib.core.modules.tab.layout.model;

public enum TabLayoutDirection {

    COLUMNS,
    ROWS;

    public static TabLayoutDirection fromString(String value) {

        if (value == null || value.isBlank()) {
            return COLUMNS;
        }

        try {
            return TabLayoutDirection.valueOf(
                    value.trim().toUpperCase()
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid TAB layout direction: " + value
                    + ". Valid values: COLUMNS, ROWS.",
                    exception
            );
        }
    }
}
