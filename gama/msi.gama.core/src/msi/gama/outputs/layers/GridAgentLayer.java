/*******************************************************************************************************
 *
 * GridAgentLayer.java, in msi.gama.core, is part of the source code of the GAMA modeling and simulation platform
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
import msi.gama.common.interfaces.IKeyword;
import msi.gama.common.interfaces.ILayer.IGridLayer;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.IShape;
import msi.gama.runtime.ExecutionResult;
import msi.gama.runtime.IScope.IGraphicsScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaColor;
import msi.gaml.operators.Cast;
import msi.gaml.statements.IExecutable;
import msi.gaml.statements.draw.DrawingAttributes;
import msi.gaml.statements.draw.ShapeDrawingAttributes;

/**
 * The Class GridAgentLayer.
 */
public class GridAgentLayer extends AgentLayer implements IGridLayer {

	/**
	 * Instantiates a new grid agent layer.
	 *
	 * @param layer
	 *            the layer
	 */
	public GridAgentLayer(final ILayerStatement layer) {
		super(layer);
	}

	@Override
	public GridLayerData createData() {
		return new GridLayerData(definition);
	}

	@Override
	public GridLayerData getData() { return (GridLayerData) super.getData(); }

	@Override
	public void privateDraw(final IGraphicsScope s, final IGraphics gr) throws GamaRuntimeException {
		final GamaColor borderColor = getData().drawLines() ? getData().getLineColor() : null;
		final IExecutable aspect = scope -> {
			IGraphicsScope sc = (IGraphicsScope) scope;
			final IAgent agent = sc.getAgent();
			final IGraphics g = sc.getGraphics();
			try {
				if (agent == sc.getGui().getHighlightedAgent()) { g.beginHighlight(); }
				final GamaColor color = Cast.asColor(sc, agent.getDirectVarValue(sc, IKeyword.COLOR));
				final IShape ag = agent.getGeometry();
				final IShape ag2 = ag.copy(sc);
				final DrawingAttributes attributes = new ShapeDrawingAttributes(ag2, agent, color, borderColor);
				return g.drawShape(ag2.getInnerGeometry(), attributes);
			} catch (final GamaRuntimeException e) {
				// cf. Issue 1052: exceptions are not thrown, just displayed
				e.printStackTrace();
			} finally {
				g.endHighlight();
			}
			return null;
		};

		for (final IAgent a : getData().getAgentsToDisplay()) {
			if (a != null) {
				final ExecutionResult result = s.execute(aspect, a, null);
				final Object r = result.getValue();
				if (r instanceof Rectangle2D) { shapes.put(a, (Rectangle2D) r); }
			}
		}

	}

	@Override
	public String getType() { return "Grid layer"; }
}
