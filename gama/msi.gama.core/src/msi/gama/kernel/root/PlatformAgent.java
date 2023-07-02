/*******************************************************************************************************
 *
 * PlatformAgent.java, in msi.gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gama.kernel.root;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.resources.ResourcesPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import msi.gama.common.interfaces.IKeyword;
import msi.gama.common.preferences.GamaPreferences;
import msi.gama.common.preferences.Pref;
import msi.gama.common.util.RandomUtils;
import msi.gama.kernel.experiment.IExperimentAgent;
import msi.gama.kernel.experiment.ITopLevelAgent;
import msi.gama.kernel.simulation.SimulationAgent;
import msi.gama.kernel.simulation.SimulationClock;
import msi.gama.metamodel.agent.GamlAgent;
import msi.gama.metamodel.population.GamaPopulation;
import msi.gama.metamodel.population.IPopulation;
import msi.gama.metamodel.topology.ITopology;
import msi.gama.metamodel.topology.continuous.AmorphousTopology;
import msi.gama.outputs.IOutputManager;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.getter;
import msi.gama.precompiler.GamlAnnotations.species;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.runtime.ExecutionScope;
import msi.gama.runtime.GAMA;
import msi.gama.runtime.IScope;
import msi.gama.runtime.MemoryUtils;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaColor;
import msi.gama.util.GamaMapFactory;
import msi.gama.util.IList;
import msi.gaml.compilation.kernel.GamaMetaModel;
import msi.gaml.expressions.IExpression;
import msi.gaml.operators.Containers;
import msi.gaml.species.ISpecies;
import msi.gaml.statements.IExecutable;
import msi.gaml.types.IType;
import msi.gaml.types.Types;
import one.util.streamex.StreamEx;

/**
 * The Class PlatformAgent.
 */
@species (
		name = IKeyword.PLATFORM,
		internal = true,
		doc = { @doc ("The species of the unique platform agent, called 'gama'") })
@vars ({ @variable (
		name = PlatformAgent.MACHINE_TIME,
		type = IType.FLOAT,
		doc = @doc (
				value = "Returns the current system time in milliseconds (i.e. number of milliseconds since UNIX epoch day)",
				comment = "The return value is a float number")),
		@variable (
				name = PlatformAgent.WORKSPACE_PATH,
				type = IType.STRING,
				constant = true,
				doc = @doc (
						value = "Contains the absolute path to the workspace of GAMA. Can be used to list all the projects and files present in the platform",
						comment = "Always terminated with a trailing separator",
						see = { "workspace" })),
		@variable (
				name = "version",
				type = IType.STRING,
				constant = true,
				doc = @doc (
						value = "Returns the version of the current GAMA installation")),
		@variable (
				name = "plugins",
				type = IType.LIST,
				of = IType.STRING,
				constant = true,
				doc = @doc (
						value = "Lists all the plugins present in this installation of GAMA")),
		@variable (
				name = "free_memory",
				type = IType.INT,
				constant = false,
				doc = @doc (
						value = "Returns the free memory available to GAMA in bytes")),
		@variable (
				name = "max_memory",
				type = IType.INT,
				constant = false,
				doc = @doc (
						value = "Returns the maximum amount of memory available to GAMA in bytes")),
		@variable (
				name = "workspace",
				type = IType.FILE,
				constant = true,
				doc = @doc (
						value = "A folder representing the workspace of GAMA. Can be used to list all the projects and files present in the platform",
						see = { "workspace_path" })), })
public class PlatformAgent extends GamlAgent implements ITopLevelAgent, IExpression {

	static {
		// DEBUG.ON();
	}

	/** The Constant WORKSPACE_PATH. */
	public static final String WORKSPACE_PATH = "workspace_path";

	/** The Constant MACHINE_TIME. */
	public static final String MACHINE_TIME = "machine_time";

	/** The polling. */
	private final Timer polling = new Timer();

	/** The prefs to restore. */
	Map<String, Object> prefsToRestore = GamaMapFactory.create();

	/** The basic scope. */
	final IScope basicScope;

	/** The current task. */
	private TimerTask currentTask;

	/**
	 * Instantiates a new platform agent.
	 */
	public PlatformAgent() {
		this(new GamaPopulation<PlatformAgent>(null,
				GamaMetaModel.INSTANCE.getAbstractModelSpecies().getMicroSpecies(IKeyword.PLATFORM)), 0);
	}

	/**
	 * Instantiates a new platform agent.
	 *
	 * @param pop
	 *            the pop
	 * @param index
	 *            the index
	 */
	public PlatformAgent(final IPopulation<PlatformAgent> pop, final int index) {
		super(pop, index);
		basicScope = new ExecutionScope(this, "Gama platform scope");
		if (GamaPreferences.Runtime.CORE_MEMORY_POLLING.getValue()) { startPollingMemory(); }
		GamaPreferences.Runtime.CORE_MEMORY_POLLING.onChange(newValue -> {
			if (newValue) {
				startPollingMemory();
			} else {
				stopPollingMemory();
			}
		});
		GamaPreferences.Runtime.CORE_MEMORY_FREQUENCY.onChange(newValue -> {
			stopPollingMemory();
			startPollingMemory();
		});
	}

	/**
	 * Start polling memory.
	 */
	private void startPollingMemory() {
		if (currentTask == null) {
			currentTask = new TimerTask() {
				@Override
				public void run() {
					if (MemoryUtils.memoryIsLow()) {
						final IExperimentAgent agent = getExperiment();
						if (agent != null) {
							final long mb = (long) (MemoryUtils.availableMemory() / 1000000d);
							final GamaRuntimeException e = GamaRuntimeException.warning("Memory is low (" + mb
									+ " megabytes). You should close the experiment, exit GAMA and give it more memory",
									agent.getScope());
							GAMA.reportError(basicScope, e, false);
						}
					}
				}
			};
		}
		polling.scheduleAtFixedRate(currentTask, 0,
				(long) 1000 * GamaPreferences.Runtime.CORE_MEMORY_FREQUENCY.getValue());
	}

	/**
	 * Stop polling memory.
	 */
	private void stopPollingMemory() {
		if (currentTask != null) {
			currentTask.cancel();
			currentTask = null;
		}
	}

	@Override
	public Object primDie(final IScope scope) {
		stopPollingMemory();
		polling.cancel();
		GAMA.closeAllExperiments(false, true);
		scope.getGui().exit();
		return null;
	}

	@Override
	public boolean isContextIndependant() { return false; }

	@Override
	public ITopology getTopology() { return new AmorphousTopology(); }

	@Override
	public String getName() { return "gama"; }

	@Override
	public String serialize(final boolean includingBuiltIn) {
		return "gama";
	}

	@Override
	public ISpecies getSpecies() { return getPopulation().getSpecies(); }

	@Override
	public SimulationClock getClock() { return new SimulationClock(getScope()); }

	@Override
	public IScope getScope() { return basicScope; }

	@Override
	public GamaColor getColor() { return GamaColor.colors.get("gamaorange"); }

	@Override
	public RandomUtils getRandomGenerator() { return new RandomUtils(); }

	@Override
	public IOutputManager getOutputManager() {
		if (getExperiment() != null) return getExperiment().getOutputManager();
		return null;
	}

	@Override
	public void postEndAction(final IExecutable executable) {}

	@Override
	public void postDisposeAction(final IExecutable executable) {}

	@Override
	public void postOneShotAction(final IExecutable executable) {}

	@Override
	public void executeAction(final IExecutable executable) {}

	@Override
	public boolean isOnUserHold() { return false; }

	@Override
	public void setOnUserHold(final boolean state) {}

	@Override
	public SimulationAgent getSimulation() { return GAMA.getSimulation(); }

	@Override
	public IExperimentAgent getExperiment() {
		if (GAMA.getExperiment() != null) return GAMA.getExperiment().getAgent();
		return null;
	}

	/**
	 * Gets the workspace path.
	 *
	 * @return the workspace path
	 */
	@getter (
			value = WORKSPACE_PATH,
			initializer = true)
	public String getWorkspacePath() {
		return ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
		// Patrick : previous version: does not work well on windows (/C:/....)
		// final URL url = Platform.getInstanceLocation().getURL();
		// return url.getPath();
	}

	/**
	 * Gets the plugins list.
	 *
	 * @return the plugins list
	 */
	@SuppressWarnings ("unchecked")
	@getter (
			value = "plugins",
			initializer = true)
	public IList<String> getPluginsList() {
		final BundleContext bc = FrameworkUtil.getBundle(getClass()).getBundleContext();
		return StreamEx.of(bc.getBundles()).map(Bundle::getSymbolicName).toCollection(Containers.listOf(Types.STRING));
	}

	/**
	 * Gets the version.
	 *
	 * @return the version
	 */
	@getter (
			value = "version",
			initializer = true)
	public String getVersion() {
		final BundleContext bc = FrameworkUtil.getBundle(getClass()).getBundleContext();
		return bc.getBundle().getVersion().toString();
	}

	/**
	 * Gets the available memory.
	 *
	 * @return the available memory
	 */
	@getter (
			value = "free_memory",
			initializer = true)
	public long getAvailableMemory() {
		final long allocatedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		return Runtime.getRuntime().maxMemory() - allocatedMemory;
	}

	/**
	 * Gets the max memory.
	 *
	 * @return the max memory
	 */
	@getter (
			value = "max_memory",
			initializer = true)
	public long getMaxMemory() { return Runtime.getRuntime().maxMemory(); }

	/**
	 * Gets the machine time.
	 *
	 * @return the machine time
	 */
	@getter (PlatformAgent.MACHINE_TIME)
	public Double getMachineTime() { return (double) System.currentTimeMillis(); }

	@Override
	public String getTitle() { return "gama platform agent"; }

	@Override
	public Doc getDocumentation() {
		return new ConstantDoc("The unique instance of the platform species. Used to access GAMA platform properties.");
	}

	@Override
	public String getDefiningPlugin() { return "msi.gama.core"; }

	@Override
	public Object value(final IScope scope) throws GamaRuntimeException {
		return this;
	}

	@Override
	public boolean isConst() { return false; }

	@Override
	public String literalValue() {
		return IKeyword.GAMA;
	}

	@Override
	public IExpression resolveAgainst(final IScope scope) {
		return this;
	}

	@Override
	public boolean shouldBeParenthesized() {
		return false;
	}

	@Override
	public IType<?> getGamlType() { return Types.get(IKeyword.PLATFORM); }

	/**
	 * Save pref to restore.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public void savePrefToRestore(final String key, final Object value) {
		// DEBUG.OUT("Save preference to restor" + key + " with value " + value);
		// In order to not restore a previous value if it has already been set
		prefsToRestore.putIfAbsent(key, value);
	}

	/**
	 * Restore prefs.
	 */
	public void restorePrefs() {
		// DEBUG.OUT("Restoring preferences" + prefsToRestore);
		prefsToRestore.forEach((key, value) -> {
			Pref<?> p = GamaPreferences.get(key);
			if (p != null) { p.setValue(basicScope, value); }
		});

	}
}
