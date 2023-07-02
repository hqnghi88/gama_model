/*******************************************************************************************************
 *
 * GamaKmlExportType.java, in msi.gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gaml.types;

import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.type;
import msi.gama.precompiler.IConcept;
import msi.gama.precompiler.ISymbolKind;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;

/**
 * The Class GamaKmlExportType.
 */
@type (
		name = "kml",
		id = IType.KML,
		wraps = { GamaKmlExport.class },
		kind = ISymbolKind.Variable.REGULAR,
		concept = { IConcept.TYPE },
		doc = { @doc (
				value = "Type of variables that enables to store objects and to export them into a KML (Keyhole Markup Language) file") })
public class GamaKmlExportType extends GamaType<GamaKmlExport> {

	@Override
	public boolean canCastToConst() {
		return true;
	}

	@Override
	@doc ("Returns a kml exportation object if the argument is alrady of type kml, otherwise nil")
	public GamaKmlExport cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		if (obj instanceof GamaKmlExport) return (GamaKmlExport) obj;
		return null;
	}

	@Override
	public GamaKmlExport getDefault() { return new GamaKmlExport(); }

}
