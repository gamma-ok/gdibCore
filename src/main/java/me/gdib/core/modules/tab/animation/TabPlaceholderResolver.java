package me.gdib.core.modules.tab.animation;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TabPlaceholderResolver {

    private static final Pattern ANIMATION_PATTERN =
            Pattern.compile("%animation:([a-zA-Z0-9_-]+)%");

    private final TabAnimationManager animationManager;

    public TabPlaceholderResolver(TabAnimationManager animationManager) {
        this.animationManager = Objects.requireNonNull(
                animationManager,
                "Animation manager cannot be null"
        );
    }

    public String resolve(String text) {
        Objects.requireNonNull(text, "Text cannot be null");

        Matcher matcher = ANIMATION_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String animationName = matcher.group(1);

            String currentText =
                    animationManager.getCurrentText(animationName);

            if (currentText == null) {
                currentText = matcher.group(0);
            }

            matcher.appendReplacement(
                    result,
                    Matcher.quoteReplacement(currentText)
            );
        }

        matcher.appendTail(result);

        return result.toString();
    }

    public boolean containsAnimation(String text) {
        Objects.requireNonNull(text, "Text cannot be null");

        return ANIMATION_PATTERN.matcher(text).find();
    }
}
