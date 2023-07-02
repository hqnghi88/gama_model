/*******************************************************************************************************
 *
 * AbstractGraphEdgeAgent.java, in msi.gama.core, is part of the source code of the GAMA modeling and simulation
 * platform (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gama.util.graph;

import msi.gama.common.interfaces.IKeyword;
import msi.gama.metamodel.agent.GamlAgent;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.population.IPopulation;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.species;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.types.GamaGeometryType;
import msi.gaml.types.IType;

// FIXME: Add all the necessary variables and actions ?
/**
 * The Class AbstractGraphEdgeAgent.
 */
// FIXME:
@species (
		name = "graph_edge",
		doc = @doc ("A species that represents an edge of a graph made of agents. The source and the target of the edge should be agents"))
@vars ({ @variable (
		name = IKeyword.SOURCE,
		type = IType.AGENT,
		doc = @doc ("The source agent of this edge")),
		@variable (
				name = IKeyword.TARGET,
				type = IType.AGENT,
				doc = @doc ("The target agent of this edge")) })
@doc ("A species that represents an edge of a graph made of agents. The source and the target of the edge should be agents")
public class AbstractGraphEdgeAgent extends GamlAgent {

	/**
	 * Instantiates a new abstract graph edge agent.
	 *
	 * @param s
	 *            the s
	 * @param index
	 *            the index
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public AbstractGraphEdgeAgent(final IPopulation<? extends IAgent> s, final int index) throws GamaRuntimeException {
		super(s, index);
	}

	@Override
	public Object _step_(final IScope scope) {
		// if ( scope.interrupted() || dead() ) { return null; }
		final IAgent s = (IAgent) getAttribute(IKeyword.SOURCE);
		final IAgent t = (IAgent) getAttribute(IKeyword.TARGET);
		if (s == null || t == null) return null;
		setGeometry(GamaGeometryType.buildLine(s.getLocation(scope), t.getLocation(scope)));
		return super._step_(scope);
	}

}
