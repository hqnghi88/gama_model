/*******************************************************************************************************
 *
 * In.java, in msi.gama.core, is part of the source code of the
 * GAMA modeling and simulation platform (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package msi.gama.metamodel.topology.filter;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.collect.Iterables;

import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.population.IPopulation;
import msi.gama.metamodel.population.IPopulationSet;
import msi.gama.metamodel.shape.IShape;
import msi.gama.metamodel.topology.graph.ISpatialGraph;
import msi.gama.runtime.IScope;
import msi.gama.util.GamaListFactory;
import msi.gama.util.IContainer;
import msi.gama.util.IList;
import msi.gaml.operators.Cast;
import msi.gaml.species.ISpecies;
import msi.gaml.types.Types;

/**
 * The Class In.
 */
@SuppressWarnings ({ "rawtypes" })
public abstract class In implements IAgentFilter {

	/**
	 * List.
	 *
	 * @param scope the scope
	 * @param targets the targets
	 * @return the i agent filter
	 */
	public static IAgentFilter list(final IScope scope, final IContainer<?, ? extends IShape> targets) {
		if (targets.isEmpty(scope)) { return null; }
		if (targets instanceof IPopulationSet) { return (IPopulationSet) targets; }
		final ISpecies species = targets.getGamlType().getContentType().isAgentType()
				? Cast.asSpecies(scope, targets.getGamlType().getContentType().getSpeciesName()) : null;
		return new InList(targets.listValue(scope, Types.NO_TYPE, false), species);
	}

	/**
	 * Edges of.
	 *
	 * @param graph the graph
	 * @return the i agent filter
	 */
	public static IAgentFilter edgesOf(final ISpatialGraph graph) {
		return graph;
	}

	/**
	 * The Class InList.
	 */
	private static class InList extends In {

		/** The agents. */
		final Set<IShape> agents;
		
		/** The species. */
		ISpecies species;

		/**
		 * Instantiates a new in list.
		 *
		 * @param list the list
		 * @param species the species
		 */
		InList(final IList<? extends IShape> list, final ISpecies species) {
			agents = new LinkedHashSet<>(list);
			this.species = species;
		}

		@Override
		public boolean accept(final IScope scope, final IShape source, final IShape a) {
			return (source == null || a.getGeometry() != source.getGeometry()) && agents.contains(a);
		}

		@Override
		public boolean hasAgentList() {
			return true;
		}

		@Override
		public IContainer<?, ? extends IAgent> getAgents(final IScope scope) {
			return GamaListFactory.createWithoutCasting(Types.AGENT, Iterables.filter(agents, IAgent.class));
		}

		@Override
		public ISpecies getSpecies() {
			return species;
		}

		@Override
		public IPopulation<? extends IAgent> getPopulation(final IScope scope) {
			if (species == null) { return null; }
			return scope.getSimulation().getPopulationFor(species);
		}

		@Override
		public void filter(final IScope scope, final IShape source, final Collection<? extends IShape> results) {
			agents.remove(source);
			results.retainAll(agents);
		}

	}

}
