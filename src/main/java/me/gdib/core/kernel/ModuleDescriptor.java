package me.gdib.core.kernel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ModuleDescriptor {

	private final String name;
	private final String description;
	private final List<String> dependencies;
	private final boolean enabledByDefault;

	public ModuleDescriptor(String name, String description, List<String> dependencies, boolean enabledByDefault) {

		this.name = validateName(name);

		this.description = Objects.requireNonNull(description, "Module description cannot be null");

		Objects.requireNonNull(dependencies, "Module dependencies cannot be null");

		List<String> validatedDependencies = new ArrayList<>();

		for (String dependency : dependencies) {

			String validatedName = validateName(dependency);

			if (validatedName.equalsIgnoreCase(this.name)) {
				throw new IllegalArgumentException("A module cannot depend on itself: " + this.name);
			}

			if (validatedDependencies.contains(validatedName)) {
				throw new IllegalArgumentException("Duplicate module dependency: " + validatedName);
			}

			validatedDependencies.add(validatedName);
		}

		this.dependencies = Collections.unmodifiableList(validatedDependencies);

		this.enabledByDefault = enabledByDefault;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public List<String> getDependencies() {
		return dependencies;
	}

	public boolean isEnabledByDefault() {
		return enabledByDefault;
	}

	private String validateName(String name) {

		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("Module name cannot be null or empty");
		}

		String validatedName = name.trim();

		if (!validatedName.matches("[a-zA-Z0-9_-]+")) {
			throw new IllegalArgumentException("Invalid module name: " + validatedName
					+ ". Only letters, numbers, underscores and hyphens are allowed.");
		}

		return validatedName;
	}
}
