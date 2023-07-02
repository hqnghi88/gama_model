/*******************************************************************************************************
 *
 * GridLayer.java, in msi.gama.core, is part of the source code of the GAMA modeling and simulation platform (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gama.outputs.layers;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;
import java.util.Set;

import msi.gama.common.interfaces.IDisplaySurface;
import msi.gama.common.interfaces.IGraphics;
import msi.gama.common.interfaces.IImageProvider;
import msi.gama.common.interfaces.ILayer.IGridLayer;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.IShape;
import msi.gama.runtime.IScope;
import msi.gama.runtime.IScope.IGraphicsScope;
import msi.gama.util.Collector;
import msi.gama.util.GamaColor;
import msi.gama.util.IList;
import msi.gama.util.matrix.GamaField;
import msi.gama.util.matrix.IField;
import msi.gaml.statements.draw.MeshDrawingAttributes;

/**
 * The Class GridLayer.
 */
public class GridLayer extends AbstractLayer implements IGridLayer {

	/**
	 * Instantiates a new grid layer.
	 *
	 * @param layer
	 *            the layer
	 */
	public GridLayer(final ILayerStatement layer) {
		super(layer);
	}

	@Override
	public GridLayerData createData() {
		return new GridLayerData(definition);
	}

	@Override
	public GridLayerData getData() { return (GridLayerData) super.getData(); }

	@Override
	public Rectangle2D focusOn(final IShape geometry, final IDisplaySurface s) {
		final IAgent a = geometry.getAgent();
		if (a == null || a.getSpecies() != getData().getGrid().getCellSpecies()) return null;
		return super.focusOn(a, s);
	}

	@Override
	public void reloadOn(final IDisplaySurface surface) {
		super.reloadOn(surface);
		getData().reset();
	}

	@Override
	public void privateDraw(final IGraphicsScope scope, final IGraphics dg) {
		GamaColor lineColor = null;
		final GridLayerData data = getData();
		if (data.drawLines()) { lineColor = data.getLineColor(); }
		final double[] gridValueMatrix = data.getElevationMatrix(scope);
		final IImageProvider textureFile = data.textureFile();
		final MeshDrawingAttributes attributes = new MeshDrawingAttributes(getName(), gridValueMatrix == null);
		attributes.setGrayscaled(data.isGrayScaled());
		attributes.setEmpty(data.isWireframe());
		final BufferedImage image = data.getImage();
		if (textureFile != null) {
			attributes.setTextures(Arrays.asList(textureFile));
		} else if (image != null) {
			final int[] imageData = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
			System.arraycopy(data.getGrid().getDisplayData(), 0, imageData, 0, imageData.length);
			attributes.setTextures(Arrays.asList(image));
		}
		attributes.setTriangulated(data.isTriangulated());
		attributes.setWithText(data.isShowText());
		attributes.setBorder(lineColor);
		attributes.setXYDimension(data.getDimensions());
		attributes.setSmooth(data.isSmooth() ? 1 : 0);

		if (gridValueMatrix == null) {
			dg.drawImage(image, attributes);
		} else {
			dg.drawField(new GamaField(scope, (int) data.getDimensions().x, (int) data.getDimensions().y,
					gridValueMatrix, IField.NO_NO_DATA), attributes);
		}
	}

	@Override
	public Set<IAgent> collectAgentsAt(final int x, final int y, final IDisplaySurface g) {
		try (Collector.AsOrderedSet<IAgent> result = Collector.getOrderedSet()) {
			result.add(getData().getGrid().getAgentAt(getModelCoordinatesFrom(x, y, g)));
			return result.items();
		}
	}

	@Override
	public String getType() { return "Grid layer"; }

	@Override
	public IList<IAgent> getAgentsForMenu(final IScope scope) {
		return getData().getGrid().getAgents();
	}

}
