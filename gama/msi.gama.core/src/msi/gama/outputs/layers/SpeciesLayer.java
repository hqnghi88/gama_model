/*******************************************************************************************************
 *
 * SpeciesLayer.java, in msi.gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gama.outputs.layers;

import java.awt.geom.Rectangle2D;

import msi.gama.common.interfaces.IGraphics;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.agent.IMacroAgent;
import msi.gama.metamodel.population.IPopulation;
import msi.gama.runtime.ExecutionResult;
import msi.gama.runtime.IScope;
import msi.gama.runtime.IScope.IGraphicsScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaListFactory;
import msi.gama.util.IList;
import msi.gaml.species.ISpecies;
import msi.gaml.statements.AspectStatement;
import msi.gaml.statements.IExecutable;
import msi.gaml.types.Types;

/**
 * Written by drogoul Modified on 23 août 2008
 */

public class SpeciesLayer extends AgentLayer {

	/** The has micro species layers. */
	final boolean hasMicroSpeciesLayers;

	/**
	 * Instantiates a new species layer.
	 *
	 * @param layer
	 *            the layer
	 */
	public SpeciesLayer(final ILayerStatement layer) {
		super(layer);
		hasMicroSpeciesLayers = getDefinition().getMicroSpeciesLayers() != null;
	}

	@Override
	public SpeciesLayerStatement getDefinition() { return (SpeciesLayerStatement) super.getDefinition(); }

	@Override
	public IList<? extends IAgent> getAgentsForMenu(final IScope scope) {
		return GamaListFactory.createWithoutCasting(Types.AGENT, getDefinition().getSpecies().getPopulation(scope));
		// return
		// ImmutableSet.copyOf(scope.getSimulation().getMicroPopulation(getDefinition().getSpecies()).iterator());
	}

	@Override
	public String getType() { return "Species layer"; }

	@Override
	public void privateDraw(final IGraphicsScope scope, final IGraphics g) throws GamaRuntimeException {
		shapes.clear();
		final ISpecies species = getDefinition().getSpecies();
		final IMacroAgent world = scope.getSimulation();
		if (world != null && !world.dead()) {
			final IPopulation<? extends IAgent> microPop = world.getMicroPopulation(species);
			if (microPop != null) {
				IExecutable aspect = getDefinition().getAspect();
				if (aspect == null) { aspect = AspectStatement.DEFAULT_ASPECT; }
				drawPopulation(scope, g, aspect, microPop);
			}
		}
	}

	/**
	 * Draw population.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @param aspect
	 *            the aspect
	 * @param population
	 *            the population
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	private void drawPopulation(final IScope scope, final IGraphics g, final IExecutable aspect,
			final IPopulation<? extends IAgent> population) throws GamaRuntimeException {

		// draw the population. A copy of the population is made to avoid
		// concurrent modification exceptions
		for (final IAgent a : population.toArray()) {
			if (a == null || a.dead()) { continue; }
			ExecutionResult result = null;
			if (a == scope.getGui().getHighlightedAgent()) {
				IExecutable hAspect = population.getSpecies().getAspect("highlighted");
				if (hAspect == null) { hAspect = aspect; }
				result = scope.execute(hAspect, a, null);
			} else {
				result = scope.execute(aspect, a, null);
			}
			if (result == ExecutionResult.FAILED) { break; }
			if (result != null && result.getValue() instanceof Rectangle2D) {
				final Rectangle2D r = (Rectangle2D) result.getValue();
				shapes.put(a, r);
			}
			if (!(a instanceof IMacroAgent)) { continue; }
			IPopulation<? extends IAgent> microPop;
			// then recursively draw the micro-populations

			if (hasMicroSpeciesLayers) {
				for (final SpeciesLayerStatement ml : getDefinition().getMicroSpeciesLayers()) {
					if (a.dead()) { continue; }
					microPop = ((IMacroAgent) a).getMicroPopulation(ml.getSpecies());
					if (microPop != null && microPop.size() > 0) {
						IExecutable microAspect = ml.getAspect();
						if (microAspect == null) { microAspect = AspectStatement.DEFAULT_ASPECT; }
						drawPopulation(scope, g, microAspect, microPop);
					}
				}
			}
		}

	}

}
