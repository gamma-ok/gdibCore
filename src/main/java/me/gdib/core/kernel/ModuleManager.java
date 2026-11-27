
package me.gdib.core.kernel;

import me.gdib.core.kernel.event.ModuleStateChangeEvent;
import me.gdib.core.common.event.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.HashMap;

public final class ModuleManager {

	private final Map<String, Module> modules;
	private final Map<String, ModuleState> states;
	private final CoreContext context;
	private final Map<String, ModuleContext> contexts;

	public ModuleManager(CoreContext context) {

		this.context = Objects.requireNonNull(context, "CoreContext cannot be null");

		this.modules = new LinkedHashMap<>();
		this.states = new LinkedHashMap<>();
		this.contexts = new HashMap<>();
	}

	public void registerModule(Module module) {

		Objects.requireNonNull(module, "Module cannot be null");

		ModuleDescriptor descriptor = Objects.requireNonNull(module.getDescriptor(),
				"Module descriptor cannot be null");

		String name = descriptor.getName();

		// Prevent duplicate names regardless of capitalization.
		for (String registeredName : modules.keySet()) {

			if (registeredName.equalsIgnoreCase(name)) {
				throw new IllegalStateException(
						"A module with the same name already exists: " + registeredName + " / " + name);
			}
		}

		ModuleContext moduleContext = new ModuleContext(context, descriptor);

		modules.put(name, module);
		states.put(name, ModuleState.UNLOADED);
		contexts.put(name, moduleContext);

		context.getPlugin().getLogger().info("Module registered: " + name);
	}

	public void enableModule(String name) throws Exception {

		Set<String> enabling = new LinkedHashSet<>();

		enableModule(name, enabling);
	}

	private void enableModule(String name, Set<String> enabling) throws Exception {

		Module module = modules.get(name);

		if (module == null) {
			throw new IllegalArgumentException("Module is not registered: " + name);
		}

		ModuleState currentState = states.get(name);

		if (currentState == ModuleState.ENABLED) {
			return;
		}

		if (currentState == ModuleState.LOADING || enabling.contains(name)) {

			throw new IllegalStateException("Circular module dependency detected: " + name);
		}

		enabling.add(name);

		changeState(name, ModuleState.LOADING);

		try {

			ModuleDescriptor descriptor = module.getDescriptor();

			// Enable dependencies first.
			for (String dependency : descriptor.getDependencies()) {

				if (!modules.containsKey(dependency)) {
					throw new IllegalStateException(
							"Missing dependency '" + dependency + "' required by module '" + name + "'");
				}

				enableModule(dependency, enabling);
			}

			// Enable current module.
			module.onEnable(contexts.get(name));

			changeState(name, ModuleState.ENABLED);

			context.getPlugin().getLogger().info("Module enabled: " + name);

		} catch (Exception exception) {

			// Clean up resources created during initialization.
			cleanupModuleResources(name);

			changeState(name, ModuleState.FAILED);

			context.getPlugin().getLogger().severe("Failed to enable module: " + name);

			throw exception;
		} finally {
			enabling.remove(name);
		}
	}

	public void disableModule(String name) {

		// Verify that the module exists.
		if (!modules.containsKey(name)) {
			throw new IllegalArgumentException("Module is not registered: " + name);
		}

		Set<String> processed = new LinkedHashSet<>();

		disableModule(name, processed);
	}

	private void disableModule(String name, Set<String> processed) {

		if (!processed.add(name)) {
			return;
		}

		// Disable active dependents first.
		for (Map.Entry<String, Module> entry : modules.entrySet()) {

			String dependentName = entry.getKey();

			if (states.get(dependentName) != ModuleState.ENABLED) {
				continue;
			}

			List<String> dependencies = entry.getValue().getDescriptor().getDependencies();

			if (dependencies.contains(name)) {
				disableModule(dependentName, processed);
			}
		}

		if (states.get(name) != ModuleState.ENABLED) {
			return;
		}

		try {

			ModuleContext moduleContext = contexts.get(name);

			modules.get(name).onDisable(moduleContext);

			changeState(name, ModuleState.DISABLED);

			context.getPlugin().getLogger().info("Module disabled: " + name);

		} catch (Exception exception) {

			changeState(name, ModuleState.FAILED);

			context.getPlugin().getLogger().severe("Failed to disable module: " + name);

			exception.printStackTrace();

		} finally {

			// Always clean up module resources.
			cleanupModuleResources(name);
		}
	}

	public void enableAll() {

		// Validate the dependency graph before enabling modules.
		validateDependencies();

		for (Map.Entry<String, Module> entry : modules.entrySet()) {

			String name = entry.getKey();

			if (!entry.getValue().getDescriptor().isEnabledByDefault()) {
				continue;
			}

			try {
				enableModule(name);

			} catch (Exception exception) {

				context.getPlugin().getLogger()
						.severe("Could not enable module " + name + ": " + exception.getMessage());
			}
		}
	}

	public void disableAll() {

		List<String> names = new ArrayList<>(modules.keySet());

		Collections.reverse(names);

		for (String name : names) {
			disableModule(name);
		}
	}

	public Optional<Module> getModule(String name) {
		return Optional.ofNullable(modules.get(name));
	}

	public ModuleState getState(String name) {

		if (!states.containsKey(name)) {
			throw new IllegalArgumentException("Module is not registered: " + name);
		}

		return states.get(name);
	}

	public Map<String, Module> getModules() {
		return Collections.unmodifiableMap(modules);
	}

	public int getModuleCount() {
		return modules.size();
	}

	public void validateDependencies() {

		Set<String> visited = new HashSet<>();
		Set<String> visiting = new LinkedHashSet<>();

		for (String name : modules.keySet()) {
			validateModule(name, visited, visiting);
		}

		context.getPlugin().getLogger().info("Module dependencies validated successfully.");
	}

	private void validateModule(String name, Set<String> visited, Set<String> visiting) {

		if (visited.contains(name)) {
			return;
		}

		if (!visiting.add(name)) {
			throw new IllegalStateException(
					"Circular module dependency detected: " + String.join(" -> ", visiting) + " -> " + name);
		}

		Module module = modules.get(name);

		if (module == null) {
			throw new IllegalStateException("Module is not registered: " + name);
		}

		for (String dependency : module.getDescriptor().getDependencies()) {

			if (!modules.containsKey(dependency)) {
				throw new IllegalStateException(
						"Missing dependency '" + dependency + "' required by module '" + name + "'");
			}

			validateModule(dependency, visited, visiting);
		}

		visiting.remove(name);
		visited.add(name);
	}

	private void cleanupModuleResources(String name) {

		ModuleContext moduleContext = contexts.get(name);

		if (moduleContext == null) {
			return;
		}

		// Remove event subscriptions.
		try {
			moduleContext.unsubscribeAll();

		} catch (Exception exception) {
			context.getPlugin().getLogger().severe("Failed to remove event subscriptions for module: " + name);

			exception.printStackTrace();
		}

		// Cancel scheduled tasks.
		try {
			moduleContext.cancelTasks();

		} catch (Exception exception) {
			context.getPlugin().getLogger().severe("Failed to cancel scheduled tasks for module: " + name);

			exception.printStackTrace();
		}
	}

	private void changeState(String name, ModuleState newState) {

		ModuleState previousState = states.get(name);

		if (previousState == newState) {
			return;
		}

		states.put(name, newState);

		context.getService(EventBus.class).publish(new ModuleStateChangeEvent(name, previousState, newState));
	}

	public boolean isEnabled(String name) {

		if (!states.containsKey(name)) {
			return false;
		}

		return states.get(name) == ModuleState.ENABLED;
	}

	public List<String> getModulesByState(ModuleState state) {

		Objects.requireNonNull(state, "Module state cannot be null");

		List<String> result = new ArrayList<>();

		for (Map.Entry<String, ModuleState> entry : states.entrySet()) {

			if (entry.getValue() == state) {
				result.add(entry.getKey());
			}
		}

		return Collections.unmodifiableList(result);
	}

	public int getEnabledModuleCount() {

		int count = 0;

		for (ModuleState state : states.values()) {

			if (state == ModuleState.ENABLED) {
				count++;
			}
		}

		return count;
	}

	public void unregisterModule(String name) {

		if (!modules.containsKey(name)) {
			throw new IllegalArgumentException("Module is not registered: " + name);
		}

		ModuleState state = states.get(name);

		if (state == ModuleState.ENABLED || state == ModuleState.LOADING) {

			throw new IllegalStateException("Cannot unregister an active module: " + name);
		}

		// Check whether another registered module depends on this one.
		for (Map.Entry<String, Module> entry : modules.entrySet()) {

			String dependentName = entry.getKey();

			if (dependentName.equals(name)) {
				continue;
			}

			List<String> dependencies = entry.getValue().getDescriptor().getDependencies();

			if (dependencies.contains(name)) {
				throw new IllegalStateException(
						"Cannot unregister module '" + name + "'. Module '" + dependentName + "' depends on it.");
			}
		}

		// Save and unload the module configuration.
		ModuleContext moduleContext = contexts.get(name);

		if (moduleContext != null) {
			moduleContext.getModuleConfig().unload();
		}

		// Remove module references.
		modules.remove(name);
		states.remove(name);
		contexts.remove(name);

		context.getPlugin().getLogger().info("Module unregistered: " + name);
	}
}
