/*******************************************************************************************************
 *
 * IDisplayCreator.java, in msi.gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gama.common.interfaces;

import msi.gama.outputs.IDisplayOutput;
import msi.gama.outputs.display.NullDisplaySurface;

/**
 * The Interface IDisplayCreator.
 */
@FunctionalInterface
public interface IDisplayCreator {

	/**
	 * The Class DisplayDescription.
	 */
	public static class DisplayDescription implements IDisplayCreator, IGamlDescription {


		/** The original. */
		private final IDisplayCreator original;

		/** The plugin. */
		private final String name, plugin;

		/**
		 * Instantiates a new display description.
		 *
		 * @param original
		 *            the original
		 * @param name
		 *            the name
		 * @param plugin
		 *            the plugin
		 */
		public DisplayDescription(final IDisplayCreator original, final String name, 
				final String plugin) {
			this.original = original;
			this.name = name;
			this.plugin = plugin;
		}

		/**
		 * Method create()
		 *
		 * @see msi.gama.common.interfaces.IDisplayCreator#create(java.lang.Object[])
		 */
		@Override
		public IDisplaySurface create(final Object... args) {
			if (original != null) return original.create(args);
			return new NullDisplaySurface();
		}

		/**
		 * Creates the.
		 *
		 * @param output
		 *            the output
		 * @param args
		 *            the args
		 * @return the i display surface
		 */
		public IDisplaySurface create(final IDisplayOutput output, final Object... args) {
			final Object[] params = new Object[args.length + 1];
			params[0] = output;
			for (int i = 0; i < args.length; i++) { params[i + 1] = args[i]; }
			return create(params);
		}

		/**
		 * Method getName()
		 *
		 * @see msi.gama.common.interfaces.INamed#getName()
		 */
		@Override
		public String getName() { return name; }

		/**
		 * Method setName()
		 *
		 * @see msi.gama.common.interfaces.INamed#setName(java.lang.String)
		 */
		@Override
		public void setName(final String newName) {}

		/**
		 * Method serialize()
		 *
		 * @see msi.gama.common.interfaces.IGamlable#serialize(boolean)
		 */
		@Override
		public String serialize(final boolean includingBuiltIn) {
			return getName();
		}

		/**
		 * Method getTitle()
		 *
		 * @see msi.gama.common.interfaces.IGamlDescription#getTitle()
		 */
		@Override
		public String getTitle() { return "Display supported by " + getName() + ""; }

		/**
		 * Method getDefiningPlugin()
		 *
		 * @see msi.gama.common.interfaces.IGamlDescription#getDefiningPlugin()
		 */
		@Override
		public String getDefiningPlugin() { return plugin; }

	}

	/**
	 * Creates the.
	 *
	 * @param args
	 *            the args
	 * @return the i display surface
	 */
	IDisplaySurface create(Object... args);

}
