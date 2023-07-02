/*******************************************************************************************************
 *
 * GridMooreNeighborhood.java, in msi.gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gama.metamodel.topology.grid;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Written by drogoul Modified on 8 mars 2011
 *
 * @todo Description
 *
 */
public class GridMooreNeighborhood extends GridNeighborhood {

	/**
	 * Instantiates a new grid moore neighborhood.
	 *
	 * @param gamaSpatialMatrix
	 *            the gama spatial matrix
	 */
	public GridMooreNeighborhood(final GamaSpatialMatrix gamaSpatialMatrix) {
		super(gamaSpatialMatrix);
	}

	@Override
	protected Set<Integer> getNeighborsAtRadius(final int placeIndex, final int radius) {
		final int y = placeIndex / this.matrix.numCols;
		final int x = placeIndex - y * this.matrix.numCols;
		final Set<Integer> v = new LinkedHashSet<>(radius + 1 * radius + 1);
		int p;
		for (int i = 1 - radius; i < radius; i++) {
			p = this.matrix.getPlaceIndexAt(x + i, y - radius);
			if (p != -1) { v.add(p); }
			p = this.matrix.getPlaceIndexAt(x - i, y + radius);
			if (p != -1) { v.add(p); }
		}
		for (int i = -radius; i < radius + 1; i++) {
			p = this.matrix.getPlaceIndexAt(x - radius, y - i);
			if (p != -1) { v.add(p); }
			p = this.matrix.getPlaceIndexAt(x + radius, y + i);
			if (p != -1) { v.add(p); }
		}
		return v;
	}

	@Override
	public boolean isVN() { return false; }

	/*
	 * (non-Javadoc)
	 *
	 * @see msi.gama.metamodel.topology.grid.INeighborhood#clear()
	 */
	@Override
	public void clear() {}
}