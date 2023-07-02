/*******************************************************************************************************
 *
 * GenericLightDefinition.java, in msi.gama.core, is part of the source code of the GAMA modeling and simulation
 * platform (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gama.outputs.layers.properties;

import msi.gama.util.GamaColor;

/**
 * The Class AmbientLightDefinition.
 */
public class GenericLightDefinition implements ILightDefinition {

	/** The name. */
	final String name;

	/** The id. */
	final int id;

	/** The intensity. */
	final GamaColor intensity;

	/**
	 * Instantiates a new generic light definition.
	 *
	 * @param name
	 *            the name.
	 * @param id
	 *            the id.
	 */
	public GenericLightDefinition(final String name, final int id, final int intensity) {
		this(name, id, new GamaColor(intensity, intensity, intensity, 255));
	}

	/**
	 * Instantiates a new generic light definition.
	 *
	 * @param name
	 *            the name.
	 * @param id
	 *            the id.
	 * @param intensity
	 *            the intensity.
	 */
	public GenericLightDefinition(final String name, final int id, final GamaColor intensity) {
		this.name = name;
		this.id = id;
		this.intensity = intensity;
	}

	@Override
	public String getName() { return name; }

	@Override
	public int getId() { return id; }

	@Override
	public GamaColor getIntensity() { return intensity; }

}
