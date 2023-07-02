/*******************************************************************************************************
 *
 * DifferentList.java, in msi.gama.core, is part of the source code of the
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

import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.population.IPopulation;
import msi.gama.metamodel.shape.IShape;
import msi.gama.runtime.IScope;
import msi.gama.util.GamaListFactory;
import msi.gama.util.IContainer;
import msi.gama.util.IList;
import msi.gaml.species.ISpecies;

/**
 * The Class DifferentList.
 */
public class DifferentList implements IAgentFilter {

	/** The agents. */
	final Set<IShape> agents;

	/**
	 * Instantiates a new different list.
	 *
	 * @param list the list
	 */
	public DifferentList(final IList<? extends IShape> list) {
		agents = new LinkedHashSet<>(list);
	}

	@Override
	public boolean accept(final IScope scope, final IShape source, final IShape a) {
		return a.getGeometry() != source.getGeometry() && !agents.contains(a);
	}

	@Override
	public IContainer<?, ? extends IAgent> getAgents(final IScope scope) {
		return GamaListFactory.EMPTY_LIST;
	}

	@Override
	public ISpecies getSpecies() {
		return null;
	}

	@Override
	public boolean hasAgentList() {
		return false;
	}

	@Override
	public IPopulation<? extends IAgent> getPopulation(final IScope scope) {
		return null;
	}

	@Override
	public void filter(final IScope scope, final IShape source, final Collection<? extends IShape> results) {
		agents.remove(source);
		results.removeAll(agents);
	}

}
