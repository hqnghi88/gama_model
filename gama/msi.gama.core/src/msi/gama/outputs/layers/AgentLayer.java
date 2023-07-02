/*******************************************************************************************************
 *
 * AgentLayer.java, in msi.gama.core, is part of the source code of the GAMA modeling and simulation platform (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gama.outputs.layers;

import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.Set;

import msi.gama.common.interfaces.IDisplaySurface;
import msi.gama.common.interfaces.IGraphics;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.IShape;
import msi.gama.runtime.ExecutionResult;
import msi.gama.runtime.IScope;
import msi.gama.runtime.IScope.IGraphicsScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.Collector;
import msi.gama.util.GamaListFactory;
import msi.gama.util.GamaMapFactory;
import msi.gama.util.IList;
import msi.gama.util.IMap;
import msi.gaml.species.ISpecies;
import msi.gaml.statements.AspectStatement;
import msi.gaml.statements.IExecutable;
import msi.gaml.types.Types;

/**
 * Written by drogoul Modified on 23 août 2008
 *
 * @todo Description
 *
 */
public class AgentLayer extends AbstractLayer {

	/**
	 * Instantiates a new agent layer.
	 *
	 * @param layer
	 *            the layer
	 */
	public AgentLayer(final ILayerStatement layer) {
		super(layer);
	}

	/** The shapes. */
	protected final IMap<IAgent, Rectangle2D> shapes = GamaMapFactory.createUnordered();

	/** The Constant DUMMY_RECT. */
	protected static final Rectangle2D DUMMY_RECT = new Rectangle2D.Double();

	/**
	 * Fill shapes.
	 *
	 * @param scope
	 *            the scope
	 */
	@SuppressWarnings ("unchecked")
	protected void fillShapes(final IScope scope) {
		shapes.clear();
		final Object o = ((AgentLayerStatement) definition).getAgentsExpr().value(scope);
		Iterable<? extends IAgent> agents = Collections.EMPTY_LIST;
		if (o instanceof ISpecies) {
			agents = ((ISpecies) o).iterable(scope);
		} else if (o instanceof IList) { agents = (IList) o; }
		for (final IAgent a : agents) { shapes.put(a, DUMMY_RECT); }
	}

	@Override
	public void privateDraw(final IGraphicsScope scope, final IGraphics g) throws GamaRuntimeException {
		if (scope == null || scope.interrupted()) return;
		fillShapes(scope);
		final String aspectName = ((AgentLayerStatement) definition).getAspectName();

		shapes.entrySet().forEach(entry -> {
			final IAgent a = entry.getKey();
			IExecutable aspect = null;
			if (a != null) {
				if (a == scope.getGui().getHighlightedAgent()) {
					aspect = a.getSpecies().getAspect("highlighted");
				} else {
					aspect = ((AgentLayerStatement) definition).getAspect();
					if (aspect == null) { aspect = a.getSpecies().getAspect(aspectName); }
				}
				if (aspect == null) { aspect = AspectStatement.DEFAULT_ASPECT; }

				final ExecutionResult result = scope.execute(aspect, a, null);
				final Object r = result.getValue();
				if (r instanceof Rectangle2D) { entry.setValue((Rectangle2D) r); }
			}
		});

	}

	@Override
	public IList<? extends IAgent> getAgentsForMenu(final IScope scope) {
		// if (shapes.isEmpty()) { return getAgentsToDisplay(); }
		// Avoid recalculating the agents
		return GamaListFactory.wrap(Types.AGENT, shapes.keySet());
	}

	// public Collection<IAgent> getAgentsToDisplay() {
	// return ((AgentLayerStatement) definition).getAgentsToDisplay();
	// }

	@Override
	public Set<IAgent> collectAgentsAt(final int x, final int y, final IDisplaySurface g) {
		try (final Collector.AsOrderedSet<IAgent> selectedAgents = Collector.getOrderedSet()) {
			final Rectangle2D selection = new Rectangle2D.Double();
			selection.setFrameFromCenter(x, y, x + IDisplaySurface.SELECTION_SIZE / 2,
					y + IDisplaySurface.SELECTION_SIZE / 2);
			shapes.forEachPair((a, b) -> {
				if (b.intersects(selection)) { selectedAgents.add(a); }
				return true;
			});

			return selectedAgents.items();
		}
	}

	@Override
	public Rectangle2D focusOn(final IShape geometry, final IDisplaySurface s) {
		if (geometry instanceof IAgent) {
			final Rectangle2D r = shapes.get(geometry);
			if (r != null) return r;
		}
		return super.focusOn(geometry, s);
	}

	@Override
	public String getType() { return "Agents layer"; }

}
