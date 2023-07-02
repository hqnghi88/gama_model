/*******************************************************************************************************
 *
 * GisLayer.java, in msi.gama.core, is part of the source code of the
 * GAMA modeling and simulation platform (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package msi.gama.outputs.layers;

import java.awt.Color;
import java.util.List;

import msi.gama.common.interfaces.IGraphics;
import msi.gama.common.interfaces.IKeyword;
import msi.gama.common.preferences.GamaPreferences;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.IShape;
import msi.gama.runtime.IScope;
import msi.gama.runtime.IScope.IGraphicsScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaColor;
import msi.gama.util.file.GamaShapeFile;
import msi.gaml.expressions.IExpression;
import msi.gaml.operators.Cast;
import msi.gaml.statements.draw.DrawingAttributes;
import msi.gaml.statements.draw.ShapeDrawingAttributes;
import msi.gaml.types.IType;

/**
 * The Class GisLayer.
 */
public class GisLayer extends AbstractLayer {

	/** The color expression. */
	IExpression gisExpression, colorExpression;

	/**
	 * Instantiates a new gis layer.
	 *
	 * @param layer the layer
	 */
	public GisLayer(final ILayerStatement layer) {
		super(layer);
		gisExpression = layer.getFacet(IKeyword.GIS);
		colorExpression = layer.getFacet(IKeyword.COLOR);
	}

	@Override
	public void privateDraw(final IGraphicsScope scope, final IGraphics g) {
		final GamaColor color =
				colorExpression == null ? GamaColor.getInt(GamaPreferences.Displays.CORE_COLOR.getValue().getRGB())
						: Cast.asColor(scope, colorExpression.value(scope));
		final List<IShape> shapes = buildGisLayer(scope);
		if (shapes != null) {
			for (final IShape geom : shapes) {
				if (geom != null) {
					final DrawingAttributes attributes =
							new ShapeDrawingAttributes(geom, (IAgent) null, color, new GamaColor(Color.black));
					g.drawShape(geom.getInnerGeometry(), attributes);
				}
			}
		}
	}

	/**
	 * Builds the gis layer.
	 *
	 * @param scope the scope
	 * @return the list
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	public List<IShape> buildGisLayer(final IScope scope) throws GamaRuntimeException {
		final GamaShapeFile file = getShapeFile(scope);
		if (file == null) return null;
		return file.getContents(scope);
	}

	/**
	 * Gets the shape file.
	 *
	 * @param scope the scope
	 * @return the shape file
	 */
	private GamaShapeFile getShapeFile(final IScope scope) {
		if (gisExpression == null) return null;
		if (gisExpression.getGamlType().id() == IType.STRING) {
			final String fileName = Cast.asString(scope, gisExpression.value(scope));
			return new GamaShapeFile(scope, fileName);
		}
		final Object o = gisExpression.value(scope);
		if (o instanceof GamaShapeFile) return (GamaShapeFile) o;
		return null;
	}

	@Override
	public String getType() { return "Gis layer"; }

}
