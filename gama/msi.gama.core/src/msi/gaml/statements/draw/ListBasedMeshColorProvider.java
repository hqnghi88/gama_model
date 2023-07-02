/*******************************************************************************************************
 *
 * ListBasedMeshColorProvider.java, in msi.gama.core, is part of the source code of the GAMA modeling and simulation
 * platform (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gaml.statements.draw;

import java.awt.Color;
import java.util.List;

/**
 * A simple implementation of the color provider that picks a color using the index of the cell being drawn (in a cyclic
 * manner so as to allow lists of colors with a smaller size than the field).
 *
 * @author drogoul
 *
 */
public class ListBasedMeshColorProvider implements IMeshColorProvider {

	/** The components. */
	private final double[] components;

	/** The size. */
	private final int size;

	/**
	 * Instantiates a new list based mesh color provider.
	 *
	 * @param colors
	 *            the colors
	 */
	public ListBasedMeshColorProvider(final List<? extends Color> colors) {
		this.size = colors.size();
		components = new double[size * 4];
		for (int i = 0; i < size; ++i) {
			Color color = colors.get(i);
			if (color != null) {
				components[i * 3] = color.getRed() / 255d;
				components[i * 3 + 1] = color.getGreen() / 255d;
				components[i * 3 + 2] = color.getBlue() / 255d;
				components[i * 3 + 3] = color.getAlpha() / 255d;
			}

		}
	}

	@Override
	public double[] getColor(final int index, final double elevation, final double min, final double max,
			final double[] rgb) {
		double[] result = rgb;
		if (result == null) { result = new double[4]; }
		int i = index % size;
		result[0] = components[i * 3];
		result[1] = components[i * 3 + 1];
		result[2] = components[i * 3 + 2];
		result[3] = 1d; // components[i * 3 + 3];
		return result;
	}

}
