/*******************************************************************************************************
 *
 * GamaPopulation.java, in msi.gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gama.metamodel.population;

import static com.google.common.collect.Iterators.transform;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.EMPTY_MAP;
import static msi.gama.common.interfaces.IKeyword.CELL_HEIGHT;
import static msi.gama.common.interfaces.IKeyword.CELL_WIDTH;
import static msi.gama.common.interfaces.IKeyword.EDGE_SPECIES;
import static msi.gama.common.interfaces.IKeyword.EXPERIMENT;
import static msi.gama.common.interfaces.IKeyword.FILE;
import static msi.gama.common.interfaces.IKeyword.FILES;
import static msi.gama.common.interfaces.IKeyword.LOCATION;
import static msi.gama.common.interfaces.IKeyword.MIRRORS;
import static msi.gama.common.interfaces.IKeyword.NEIGHBORS;
import static msi.gama.common.interfaces.IKeyword.NEIGHBOURS;
import static msi.gama.common.interfaces.IKeyword.SHAPE;
import static msi.gama.common.interfaces.IKeyword.TARGET;
import static msi.gama.common.interfaces.IKeyword.WIDTH;
import static msi.gaml.descriptions.VariableDescription.INIT_DEPENDENCIES_FACETS;
import static msi.gaml.descriptions.VariableDescription.UPDATE_DEPENDENCIES_FACETS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.jgrapht.graph.DirectedAcyclicGraph;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

import msi.gama.common.geometry.Envelope3D;
import msi.gama.common.interfaces.IKeyword;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.agent.IMacroAgent;
import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.metamodel.shape.GamaShape;
import msi.gama.metamodel.shape.IShape;
import msi.gama.metamodel.topology.ITopology;
import msi.gama.metamodel.topology.continuous.ContinuousTopology;
import msi.gama.metamodel.topology.filter.IAgentFilter;
import msi.gama.metamodel.topology.filter.In;
import msi.gama.metamodel.topology.graph.GamaSpatialGraph;
import msi.gama.metamodel.topology.graph.GraphTopology;
import msi.gama.metamodel.topology.grid.GridTopology;
import msi.gama.runtime.FlowStatus;
import msi.gama.runtime.GAMA;
import msi.gama.runtime.IScope;
import msi.gama.runtime.benchmark.StopWatch;
import msi.gama.runtime.concurrent.GamaExecutorService;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaList;
import msi.gama.util.GamaListFactory;
import msi.gama.util.GamaMapFactory;
import msi.gama.util.IContainer;
import msi.gama.util.IList;
import msi.gama.util.file.GamaGridFile;
import msi.gama.util.graph.AbstractGraphNodeAgent;
import msi.gaml.compilation.IAgentConstructor;
import msi.gaml.descriptions.ActionDescription;
import msi.gaml.descriptions.TypeDescription;
import msi.gaml.descriptions.VariableDescription;
import msi.gaml.expressions.IExpression;
import msi.gaml.operators.Cast;
import msi.gaml.species.ISpecies;
import msi.gaml.statements.IExecutable;
import msi.gaml.statements.RemoteSequence;
import msi.gaml.types.GamaTopologyType;
import msi.gaml.types.IType;
import msi.gaml.types.Types;
import msi.gaml.variables.IVariable;
import ummisco.gama.dev.utils.DEBUG;

/**
 * Written by drogoul Modified on 6 sept. 2010
 *
 * @todo Description
 *
 */
public class GamaPopulation<T extends IAgent> extends GamaList<T> implements IPopulation<T> {

	static {
		DEBUG.ON();
	}

	/**
	 * The agent hosting this population which is considered as the direct macro-agent.
	 */
	protected IMacroAgent host;

	/**
	 * The object describing how the agents of this population are spatially organized
	 */
	protected ITopology topology;

	/** The species. */
	protected final ISpecies species;

	/** The updatable vars. */
	protected final IVariable[] orderedVars, updatableVars;

	/** The current agent index. */
	protected int currentAgentIndex;

	/** The hash code. */
	private final int hashCode;

	/** The is step overriden. */
	private final boolean isInitOverriden, isStepOverriden;

	/** The mirror management. */
	private final MirrorPopulationManagement mirrorManagement;

	/**
	 * Listeners, created in a lazy way
	 */
	private LinkedList<IPopulation.Listener> listeners = null;

	/** The ordered var names. */
	public final LinkedHashSet<String> orderedVarNames = new LinkedHashSet<>();

	/** The Constant isLiving. */
	public final static IPopulation.IsLiving isLiving = new IPopulation.IsLiving();

	/**
	 * The Class MirrorPopulationManagement.
	 */
	class MirrorPopulationManagement implements IExecutable {

		/** The list of target agents. */
		final IExpression listOfTargetAgents;

		/**
		 * Instantiates a new mirror population management.
		 *
		 * @param exp
		 *            the exp
		 */
		MirrorPopulationManagement(final IExpression exp) {
			listOfTargetAgents = exp;
		}

		@Override
		public Object executeOn(final IScope scope) throws GamaRuntimeException {
			final IPopulation<T> pop = GamaPopulation.this;
			final Set<IAgent> targets = new HashSet<IAgent>(Cast.asList(scope, listOfTargetAgents.value(scope)));
			final List<IAgent> toKill = new ArrayList<>();
			for (final IAgent agent : pop.iterable(scope)) {
				final IAgent target = Cast.asAgent(scope, agent.getAttribute(TARGET));
				if (targets.contains(target)) {
					targets.remove(target);
				} else {
					toKill.add(agent);
				}
			}
			for (final IAgent agent : toKill) { agent.dispose(); }
			final List<Map<String, Object>> attributes = new ArrayList<>();
			for (final IAgent target : targets) {
				final Map<String, Object> att = GamaMapFactory.createUnordered();
				att.put(TARGET, target);
				attributes.add(att);
			}
			return pop.createAgents(scope, targets.size(), attributes, false, true, null);
		}

	}

	/**
	 * Try add.
	 *
	 * @param graph
	 *            the graph
	 * @param v
	 *            the v
	 * @param existing
	 *            the existing
	 */
	private void tryAdd(final DirectedAcyclicGraph<String, Object> graph, final String v, final String existing) {
		graph.addVertex(v);
		try {
			graph.addEdge(v, existing);
		} catch (IllegalArgumentException e) {
			// AD Revision in Aug 2021 for Issue #3068: edge is not added if it creates a cycle
		}
	}

	/**
	 * Order attributes.
	 *
	 * @param ecd
	 *            the ecd
	 * @param keep
	 *            the keep
	 * @param facetsToConsider
	 *            the facets to consider
	 * @return the i variable[]
	 */
	public IVariable[] orderAttributes(final TypeDescription ecd, final Predicate<VariableDescription> keep,
			final Set<String> facetsToConsider) {
		// AD Revised in Aug 2019 for Issue #2869: keep constraints between superspecies and subspecies
		// AD Revised in Aug 2021 for Issue #3068: do not introduce cycles (which might exist in update blocks) in order
		// to obtain a correct topological order
		final DirectedAcyclicGraph<String, Object> graph = new VariableOrderingGraph();

		// if (this instanceof SimulationPopulation) { DEBUG.OUT("Species to order: " +
		// DEBUG.TO_STRING(Iterables.toArray(Iterables.transform(subs, v->v.getName()), String.class))); }
		ecd.visitAllAttributes(d -> {
			VariableDescription var = (VariableDescription) d;
			if (keep.apply(var)) {
				String name = var.getName();
				graph.addVertex(name);
				for (final VariableDescription dep : var.getDependencies(facetsToConsider, false, true)) {
					if (keep.apply(dep)) { tryAdd(graph, dep.getName(), name); }
				}
				// Adding a constraint between the shape of the macrospecies and the populations of microspecies
				if (var.isSyntheticSpeciesContainer()) {
					// SpeciesDescription sd = var.getGamlType().getSpecies();
					// if (sd != null && graph.containsVertex(sd.getParentName())) {
					// tryAdd(graph, sd.getParentName(), name);
					// }
					tryAdd(graph, SHAPE, name);
				}
			}
			return true;
		});

		// AD Revised in Dec 2022 for Issue #3526: the order in which the sub-species is declared in the variables is
		// kept if possible, so that 'agents' returns the agents in the same order
		final List<VariableDescription> subs = new ArrayList<>();
		ecd.visitAllAttributes(d -> {
			VariableDescription var = (VariableDescription) d;
			if (var.isSyntheticSpeciesContainer()) { subs.add(var); }
			return true;
		});
		for (int i = 0; i < subs.size() - 1; i++) {
			VariableDescription vs = subs.get(i);
			if (keep.apply(vs)) {
				VariableDescription vt = subs.get(i + 1);
				if (keep.apply(vt)) {
					String source = vs.getName();
					String target = vt.getName();
					if (!graph.containsEdge(target, source)) { tryAdd(graph, source, target); }
				}
			}
		}
		// End revision
		// if (this instanceof SimulationPopulation) {
		// DEBUG.OUT("After ordering: " + DEBUG.TO_STRING(Iterators.toArray(
		// Iterators.filter(graph.iterator(),
		// s -> ((VariableDescription) getVar(s).getDescription()).isSyntheticSpeciesContainer()),
		// String.class)));
		// }
		return Iterators.toArray(transform(graph.iterator(), s -> getVar(s)), IVariable.class);

	}

	/**
	 * Instantiates a new gama population.
	 *
	 * @param host
	 *            the agent hosting this population which is considered as the direct macro-agent.
	 * @param species
	 *            the species
	 */
	public GamaPopulation(final IMacroAgent host, final ISpecies species) {
		super(0, host == null ? Types.get(EXPERIMENT)
				: host.getModel().getDescription().getTypeNamed(species.getName()));
		this.host = host;
		this.species = species;
		final TypeDescription ecd = species.getDescription();
		orderedVars = orderAttributes(ecd, Predicates.alwaysTrue(), INIT_DEPENDENCIES_FACETS);
		for (IVariable v : orderedVars) { orderedVarNames.add(v.getName()); }
		updatableVars = orderAttributes(ecd, VariableDescription::isUpdatable, UPDATE_DEPENDENCIES_FACETS);
		if (species.isMirror() && host != null) {
			mirrorManagement = new MirrorPopulationManagement(species.getFacet(MIRRORS));
		} else {
			mirrorManagement = null;
		}

		/*
		 * PATRICK TAILLANDIER: the problem of having the host here is that depending on the simulation the hashcode
		 * will be different... and this hashcode is very important for the manipultion of GamaMap thus, having two
		 * different hashcodes depending on the simulation makes ensure the repication of simulation So I remove the
		 * host for the moment.
		 */
		/*
		 * AD: Reverting this as different populations in different hosts should not have the same hash code ! See
		 * discussion in https://github.com/gama-platform/gama/issues/3339
		 */
		hashCode = Objects.hash(getSpecies(), getHost());
		final boolean[] result = { false, false };
		species.getDescription().visitChildren(d -> {
			if (d instanceof ActionDescription && !d.isBuiltIn()) {
				final String name = d.getName();
				if (ISpecies.initActionName.equals(name)) {
					result[0] = true;
				} else if (ISpecies.stepActionName.equals(name)) { result[1] = true; }
			}
			return true;
		});
		isInitOverriden = result[0];
		isStepOverriden = result[1];

	}

	@Override
	public boolean step(final IScope scope) throws GamaRuntimeException {
		final IExpression frequencyExp = species.getFrequency();
		if (frequencyExp != null) {
			final int frequency = Cast.asInt(scope, frequencyExp.value(scope));
			final int step = scope.getClock().getCycle();
			if (frequency == 0 || step % frequency != 0) return true;
		}
		if (mirrorManagement != null) { mirrorManagement.executeOn(scope); }
		getSpecies().getArchitecture().preStep(scope, this);
		return stepAgents(scope);

	}

	/**
	 * Step agents.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if successful
	 */
	protected boolean stepAgents(final IScope scope) {
		return GamaExecutorService.step(scope, this, getSpecies());
	}

	// @Override
	// public StreamEx<T> stream(final IScope scope) {
	// return super.stream(scope);
	// }

	/**
	 * Take copy into account and always creates a list (necessary for #2254)
	 */
	@Override
	public IList<T> listValue(final IScope scope, final IType contentsType, final boolean copy) {
		if (copy) return GamaListFactory.create(scope, contentsType, this);
		return this;
	}

	/**
	 * Explicity copy (necessary for #2254)
	 */
	@Override
	public IList<T> copy(final IScope scope) {
		return listValue(scope, getGamlType().getContentType(), true);
	}

	@Override
	public void updateVariables(final IScope scope, final IAgent a) {
		for (final IVariable v : updatableVars) {
			try (StopWatch w = GAMA.benchmark(scope, v)) {
				scope.setCurrentSymbol(v);
				scope.setAgentVarValue(a, v.getName(), v.getUpdatedValue(scope));
			}
		}
	}

	@Override
	public boolean init(final IScope scope) {
		// See #2933
		if (mirrorManagement != null) { mirrorManagement.executeOn(scope); }
		return true;
		// // Do whatever the population has to do at the first step ?
	}

	@Override
	public void createVariablesFor(final IScope scope, final T agent) throws GamaRuntimeException {
		for (final IVariable var : orderedVars) { var.initializeWith(scope, agent, null); }
	}

	@Override
	public T getAgent(final Integer index) {
		return Iterables.find(this, each -> each.getIndex() == index, null);
	}

	@Override
	public int compareTo(final IPopulation<T> o) {
		return getName().compareTo(o.getName());
	}

	@Override
	public ITopology getTopology() { return topology; }

	@Override
	public String getName() { return species.getName(); }

	@Override
	public boolean isGrid() { return species.isGrid(); }

	@Override
	public ISpecies getSpecies() { return species; }

	@SuppressWarnings ("unchecked")
	@Override
	public Iterable<T> iterable(final IScope scope) {
		return (Iterable<T>) getAgents(scope);
	}

	@Override
	public void dispose() {
		killMembers();
		clear();
		final IScope scope = getHost() == null ? GAMA.getRuntimeScope() : getHost().getScope();
		firePopulationCleared(scope);
		if (topology != null) {
			topology.dispose();
			topology = null;
		}
	}

	@SuppressWarnings ("unchecked")
	@Override
	public T[] toArray() {
		return (T[]) super.toArray(new IAgent[0]);
	}

	/**
	 * Special case for creating agents directly from geometries
	 *
	 * @param scope
	 * @param number
	 * @param initialValues
	 * @param geometries
	 * @return
	 */
	@Override
	public IList<T> createAgents(final IScope scope, final IContainer<?, ? extends IShape> geometries) {
		final int number = geometries.length(scope);
		if (number == 0) return GamaListFactory.EMPTY_LIST;
		final IList<T> list = GamaListFactory.create(getGamlType().getContentType(), number);
		final IAgentConstructor<T> constr = species.getDescription().getAgentConstructor();
		for (final IShape geom : geometries.iterable(scope)) {
			// WARNING Should be redefined somehow
			final T a = constr.createOneAgent(this, currentAgentIndex++);
			// final int ind = currentAgentIndex++;
			// a.setIndex(ind);
			a.setGeometry(geom);
			list.add(a);
		}
		/* agents. */addAll(list);

		for (final IAgent a : list) {
			a.schedule(scope);
			// a.scheduleAndExecute(null);
		}
		// AD May 2021: adds the execution of the sequence *before* firing listeners (otherwise, improperly initialized
		// agents were "notified"
		createVariablesFor(scope, list, EMPTY_LIST);
		// if (sequence != null && !sequence.isEmpty()) {
		// for (final IAgent a : list) { if (!scope.execute(sequence, a, null).passed()) { break; } }
		// }
		fireAgentsAdded(scope, list);
		return list;

	}

	@Override
	public T createAgentAt(final IScope scope, final int index, final Map<String, Object> initialValues,
			final boolean isRestored, final boolean toBeScheduled) throws GamaRuntimeException {

		final List<Map<String, Object>> mapInitialValues = new ArrayList<>();
		mapInitialValues.add(initialValues);

		// TODO : think to another solution... it is ugly
		final int tempIndexAgt = currentAgentIndex;

		currentAgentIndex = index;
		final IList<T> listAgt = createAgents(scope, 1, mapInitialValues, isRestored, toBeScheduled, null);
		currentAgentIndex = tempIndexAgt;

		return listAgt.firstValue(scope);
	}

	@Override
	public IList<T> createAgents(final IScope scope, final int number,
			final List<? extends Map<String, Object>> initialValues, final boolean isRestored,
			final boolean toBeScheduled, final RemoteSequence sequence) throws GamaRuntimeException {
		if (number == 0) return GamaListFactory.EMPTY_LIST;
		final IList<T> list = GamaListFactory.create(getGamlType().getContentType(), number);
		final IAgentConstructor<T> constr = species.getDescription().getAgentConstructor();
		for (int i = 0; i < number; i++) {
			@SuppressWarnings ("unchecked") final T a = constr.createOneAgent(this, currentAgentIndex++);
			// final int ind = currentAgentIndex++;
			// a.setIndex(ind);
			// Try to grab the location earlier
			if (initialValues != null && !initialValues.isEmpty()) {
				final Map<String, Object> init = initialValues.get(i);
				if (init.containsKey(SHAPE)) {
					final Object val = init.get(SHAPE);
					if (val instanceof GamaPoint) {
						a.setGeometry(new GamaShape((GamaPoint) val));
					} else {
						a.setGeometry((IShape) val);
					}
					init.remove(SHAPE);
				} else if (init.containsKey(LOCATION)) {
					a.setLocation(scope, (GamaPoint) init.get(LOCATION));
					init.remove(LOCATION);
				}
			}
			list.add(a);
		}

		createVariablesFor(scope, list, initialValues);
		// #3626 ??
		addAll(list);
		if (!isRestored) {
			for (final IAgent a : list) {
				// if agent is restored (on the capture or release); then don't
				// need to run the "init" reflex
				a.schedule(scope);
				// a.scheduleAndExecute(sequence);
			}
			// AD May 2021: adds the execution of the sequence *before* firing listeners (otherwise, improperly
			// initialized
			// agents were "notified"
			if (sequence != null && !sequence.isEmpty()) {
				for (final IAgent a : list) {

					if (!scope.execute(sequence, a, null).passed()
							|| scope.getAndClearBreakStatus() == FlowStatus.BREAK) {
						break;
					}
				}
			}

		}
		fireAgentsAdded(scope, list);
		return list;
	}

	/**
	 * Creates the variables for.
	 *
	 * @param scope
	 *            the scope
	 * @param agents
	 *            the agents
	 * @param initialValues
	 *            the initial values
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@SuppressWarnings ("null")
	public void createVariablesFor(final IScope scope, final List<T> agents,
			final List<? extends Map<String, Object>> initialValues) throws GamaRuntimeException {
		if (agents == null || agents.isEmpty()) return;
		final boolean empty = initialValues == null || initialValues.isEmpty();
		Map<String, Object> inits;
		for (int i = 0, n = agents.size(); i < n; i++) {
			final IAgent a = agents.get(i);
			inits = empty ? EMPTY_MAP : initialValues.get(i);
			for (final IVariable var : orderedVars) {
				final Object initGet =
						empty || !allowVarInitToBeOverridenByExternalInit(var) ? null : inits.get(var.getName());
				var.initializeWith(scope, a, initGet);
			}
			// Added to fix #3266 -- saves the values of the "extra" attributes found in the files
			if (!empty) {
				inits.forEach((name, v) -> { if (!orderedVarNames.contains(name)) { a.setAttribute(name, v); } });
			}
		}
	}

	/**
	 * Allow var init to be overriden by external init.
	 *
	 * @param var
	 *            the var
	 * @return true, if successful
	 *
	 */
	protected boolean allowVarInitToBeOverridenByExternalInit(final IVariable var) {
		return true;
	}

	@Override
	public void initializeFor(final IScope scope) throws GamaRuntimeException {
		dispose();
		computeTopology(scope);
		if (topology != null) { topology.initialize(scope, this); }
	}

	@Override
	public boolean hasVar(final String n) {
		return species.getVar(n) != null;
	}

	@Override
	public boolean hasAspect(final String default1) {
		return species.hasAspect(default1);
	}

	@Override
	public IExecutable getAspect(final String default1) {
		return species.getAspect(default1);
	}

	@Override
	public Collection<String> getAspectNames() { return species.getAspectNames(); }

	@Override
	public IVariable getVar(final String s) {
		return species.getVar(s);
	}

	@Override
	public boolean hasUpdatableVariables() {
		return updatableVars.length > 0;
	}

	@SuppressWarnings ("unchecked")
	@Override
	public T getAgent(final IScope scope, final GamaPoint coord) {
		final IAgentFilter filter = In.list(scope, this);
		if (filter == null) return null;

		return topology == null ? null : (T) topology.getAgentClosestTo(scope, coord, filter);
	}

	/**
	 * Initializes the appropriate topology.
	 *
	 * @param scope
	 * @return
	 * @throws GamaRuntimeException
	 */
	protected void computeTopology(final IScope scope) throws GamaRuntimeException {
		final IExpression expr = species.getFacet(IKeyword.TOPOLOGY);
		final boolean fixed = species.isGraph() || species.isGrid();
		if (expr != null) {
			if (!fixed) {
				topology = GamaTopologyType.staticCast(scope, scope.evaluate(expr, host).getValue(), false);
				return;
			}
			throw GamaRuntimeException.warning(
					"Impossible to assign a topology to " + species.getName() + " as it already defines one.", scope);
		}
		if (species.isGrid()) {
			topology = buildGridTopology(scope, species, getHost());
		} else if (species.isGraph()) {
			final IExpression spec = species.getFacet(EDGE_SPECIES);
			final String edgeName = spec == null ? "base_edge" : spec.literalValue();
			final ISpecies edgeSpecies = scope.getModel().getSpecies(edgeName);
			final IType<?> edgeType = scope.getType(edgeName);
			final IType<?> nodeType = getGamlType().getContentType();
			// TODO Specifier directed quelque part dans l'espece
			final GamaSpatialGraph g = new GamaSpatialGraph(GamaListFactory.EMPTY_LIST, false, false, false,
					new AbstractGraphNodeAgent.NodeRelation(), edgeSpecies, scope, nodeType, edgeType);
			this.addListener(g);
			g.postRefreshManagementAction(scope);
			topology = new GraphTopology(scope, this.getHost(), g);
		} else {
			topology = new ContinuousTopology(scope, this.getHost());
		}

	}

	/**
	 * Builds the grid topology.
	 *
	 * @param scope
	 *            the scope
	 * @param species
	 *            the species
	 * @param host
	 *            the host
	 * @return the i topology
	 */
	public static ITopology buildGridTopology(final IScope scope, final ISpecies species, final IAgent host) {
		IExpression exp = species.getFacet(WIDTH);
		final Envelope3D env = scope.getSimulation().getGeometry().getEnvelope();
		final int rows = exp == null
				? species.hasFacet(CELL_WIDTH)
						? (int) (env.getWidth() / Cast.asFloat(scope, species.getFacet(CELL_WIDTH).value(scope))) : 100
				: Cast.asInt(scope, exp.value(scope));
		exp = species.getFacet(IKeyword.HEIGHT);
		final int columns = exp == null ? species.hasFacet(CELL_HEIGHT)
				? (int) (env.getHeight() / Cast.asFloat(scope, species.getFacet(CELL_HEIGHT).value(scope))) : 100
				: Cast.asInt(scope, exp.value(scope));

		final boolean isTorus = host.getTopology().isTorus();
		exp = species.getFacet("use_individual_shapes");
		final boolean useIndividualShapes = exp == null || Cast.asBool(scope, exp.value(scope));
		exp = species.getFacet("use_neighbors_cache");
		final boolean useNeighborsCache = exp == null || Cast.asBool(scope, exp.value(scope));
		exp = species.getFacet("horizontal_orientation");
		final boolean horizontalOrientation = exp == null || Cast.asBool(scope, exp.value(scope));

		exp = species.getFacet("optimizer");
		final String optimizer = exp == null ? "" : Cast.asString(scope, exp.value(scope));

		exp = species.getFacet(NEIGHBORS);
		if (exp == null) { exp = species.getFacet(NEIGHBOURS); }
		final boolean usesVN = exp == null || Cast.asInt(scope, exp.value(scope)) == 4;
		final boolean isHexagon = exp != null && Cast.asInt(scope, exp.value(scope)) == 6;
		exp = species.getFacet(FILES);
		IList<GamaGridFile> files = null;
		if (exp != null) { files = Cast.asList(scope, exp.value(scope)); }
		GridTopology result;
		if (files != null && !files.isEmpty()) {
			result = new GridTopology(scope, host, files, isTorus, usesVN, useIndividualShapes, useNeighborsCache,
					optimizer);
		} else {
			exp = species.getFacet(FILE);
			final GamaGridFile file = (GamaGridFile) (exp != null ? exp.value(scope) : null);
			if (file == null) {
				result = new GridTopology(scope, host, rows, columns, isTorus, usesVN, isHexagon, horizontalOrientation,
						useIndividualShapes, useNeighborsCache, optimizer);
			} else {
				result = new GridTopology(scope, host, file, isTorus, usesVN, useIndividualShapes, useNeighborsCache,
						optimizer);
			}

		}
		// Reverts the modification of the world envelope (see #1953 and #1939)
		//
		// final Envelope3D env =
		// result.getPlaces().getEnvironmentFrame().getEnvelope();
		// final Envelope3D world = host.getEnvelope();
		// final Envelope3D newEnvelope = new Envelope3D(0,
		// Math.max(env.getWidth(), world.getWidth()), 0,
		// Math.max(env.getHeight(), world.getHeight()), 0,
		// Math.max(env.getDepth(), world.getDepth()));
		// host.setGeometry(new GamaShape(newEnvelope));
		return result;
	}

	@Override
	public IMacroAgent getHost() { return host; }

	@Override
	public void setHost(final IMacroAgent agt) { host = agt; }
	//
	// @Override
	// public Iterator<T> iterator() {
	// return super.iterator();
	// }

	@Override
	public final boolean equals(final Object o) {
		return o == this;
	}

	@Override
	public final int hashCode() {
		return hashCode;
	}

	@Override
	public void killMembers() throws GamaRuntimeException {
		final T[] ag = toArray();
		for (final IAgent a : ag) { if (a != null) { a.dispose(); } }
		this.clear();
	}

	@Override
	public String toString() {
		return "Population of " + species.getName();
	}

	@Override
	public void addValue(final IScope scope, final T value) {
		fireAgentAdded(scope, value);
		add(value);
	}

	@Override
	public void addValueAtIndex(final IScope scope, final Object index, final T value) {
		fireAgentAdded(scope, value);
		super.addValueAtIndex(scope, index, value);
	}

	@Override
	public void addValues(final IScope scope, final IContainer values) {
		for (final T o : (java.lang.Iterable<T>) values.iterable(scope)) { addValue(scope, o); }
	}

	@Override
	public void removeValue(final IScope scope, final Object value) {
		if (value instanceof IAgent && super.remove(value)) {
			if (topology != null) { topology.removeAgent((IAgent) value); }
			fireAgentRemoved(scope, (IAgent) value);
		}
	}

	@Override
	public void removeValues(final IScope scope, final IContainer values) {
		for (final Object o : values.iterable(scope)) { removeValue(scope, o); }
	}

	@Override
	public void removeAllOccurrencesOfValue(final IScope scope, final Object value) {
		removeValue(scope, value);
	}

	@Override
	public boolean remove(final Object a) {
		removeValue(null, a);
		return true;
	}

	@Override
	public boolean contains(final IScope scope, final Object o) {
		if (!(o instanceof IAgent)) return false;
		return ((IAgent) o).getPopulation() == this;
	}

	/**
	 * Checks for listeners.
	 *
	 * @return true, if successful
	 */
	private boolean hasListeners() {
		return listeners != null && !listeners.isEmpty();
	}

	@Override
	public void addListener(final IPopulation.Listener listener) {
		if (listeners == null) { listeners = new LinkedList<>(); }
		if (!listeners.contains(listener)) { listeners.add(listener); }
	}

	@Override
	public void removeListener(final IPopulation.Listener listener) {
		if (listeners == null) return;
		listeners.remove(listener);
	}

	/**
	 * Fire agent added.
	 *
	 * @param scope
	 *            the scope
	 * @param agent
	 *            the agent
	 */
	protected void fireAgentAdded(final IScope scope, final IAgent agent) {
		if (!hasListeners()) return;
		try {
			for (final IPopulation.Listener l : listeners) { l.notifyAgentAdded(scope, this, agent); }
		} catch (final RuntimeException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Fire agents added.
	 *
	 * @param <T>
	 *            the generic type
	 * @param scope
	 *            the scope
	 * @param container
	 *            the container
	 */
	protected <T extends IAgent> void fireAgentsAdded(final IScope scope, final IList<T> container) {
		if (!hasListeners()) return;
		// create list
		final Collection<T> agents = new LinkedList<>(container);
		// send event
		try {
			for (final IPopulation.Listener l : listeners) { l.notifyAgentsAdded(scope, this, agents); }
		} catch (final RuntimeException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Fire agent removed.
	 *
	 * @param scope
	 *            the scope
	 * @param agent
	 *            the agent
	 */
	protected void fireAgentRemoved(final IScope scope, final IAgent agent) {
		if (!hasListeners()) return;
		try {
			for (final IPopulation.Listener l : listeners) { l.notifyAgentRemoved(scope, this, agent); }
		} catch (final RuntimeException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Fire population cleared.
	 *
	 * @param scope
	 *            the scope
	 */
	protected void firePopulationCleared(final IScope scope) {
		if (!hasListeners()) return;
		// send event
		try {
			for (final IPopulation.Listener l : listeners) { l.notifyPopulationCleared(scope, this); }
		} catch (final RuntimeException e) {
			e.printStackTrace();
		}
	}

	// Filter methods

	/**
	 * Method getAgents()
	 *
	 * @see msi.gama.metamodel.topology.filter.IAgentFilter#getAgents()
	 */
	@Override
	public IContainer<?, ? extends IAgent> getAgents(final IScope scope) {
		return GamaListFactory.create(scope, getGamlType().getContentType(), GamaPopulation.allLivingAgents(this));
	}

	@Override
	public boolean hasAgentList() {
		return true;
	}

	/**
	 * Method accept()
	 *
	 * @see msi.gama.metamodel.topology.filter.IAgentFilter#accept(msi.gama.runtime.IScope,
	 *      msi.gama.metamodel.shape.IShape, msi.gama.metamodel.shape.IShape)
	 */
	@Override
	public boolean accept(final IScope scope, final IShape source, final IShape a) {
		final IAgent agent = a.getAgent();
		if (agent == null || agent.getPopulation() != this || agent.dead()) return false;
		final IAgent as = source.getAgent();
		if (agent == as) return false;
		// }
		return true;
	}

	/**
	 * Method filter()
	 *
	 * @see msi.gama.metamodel.topology.filter.IAgentFilter#filter(msi.gama.runtime.IScope,
	 *      msi.gama.metamodel.shape.IShape, java.util.Collection)
	 */
	@Override
	public void filter(final IScope scope, final IShape source, final Collection<? extends IShape> results) {
		final IAgent sourceAgent = source == null ? null : source.getAgent();
		results.remove(sourceAgent);
		final Predicate<IShape> toRemove = each -> {
			final IAgent a = each.getAgent();
			return a == null || a.dead()
					|| a.getPopulation() != this
							&& (a.getPopulation().getGamlType().getContentType() != this.getGamlType().getContentType()
									|| !this.contains(a));
		};
		results.removeIf(toRemove);
	}

	@Override
	public Collection<? extends IPopulation<? extends IAgent>> getPopulations(final IScope scope) {
		return Collections.singleton(this);
	}

	@Override
	public T getFromIndicesList(final IScope scope, final IList indices) throws GamaRuntimeException {
		if (indices == null) return null;
		final int size = indices.size();
		return switch (size) {
			case 0 -> null;
			case 1 -> super.getFromIndicesList(scope, indices);
			case 2 -> this.getAgent(scope,
					new GamaPoint(Cast.asFloat(scope, indices.get(0)), Cast.asFloat(scope, indices.get(1))));
			default -> throw GamaRuntimeException.error("Populations cannot be accessed with 3 or more indexes", scope);
		};

	}

	/**
	 * @param actionScope
	 * @param iterable
	 * @return
	 */
	public static <T extends IAgent> Iterable<T> allLivingAgents(final Iterable<T> iterable) {
		return Iterables.filter(iterable, isLiving);
	}

	/**
	 * Method isInitOverriden()
	 *
	 * @see msi.gaml.species.ISpecies#isInitOverriden()
	 */
	@Override
	public boolean isInitOverriden() { return isInitOverriden; }

	/**
	 * Method isStepOverriden()
	 *
	 * @see msi.gaml.species.ISpecies#isStepOverriden()
	 */
	@Override
	public boolean isStepOverriden() { return isStepOverriden; }

}