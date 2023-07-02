/*******************************************************************************************************
 *
 * GamaDirectoryType.java, in msi.gama.core, is part of the source code of the
 * GAMA modeling and simulation platform (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package msi.gaml.types;

import msi.gama.common.interfaces.IKeyword;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.type;
import msi.gama.precompiler.IConcept;
import msi.gama.precompiler.ISymbolKind;
import msi.gama.util.file.GamaFolderFile;

/**
 * Written by taillandier Modified on 10 Apr. 2021
 *
 */
@type (
		name = IKeyword.DIRECTORY,
		id = IType.DIRECTORY,
		wraps = { GamaFolderFile.class },
		kind = ISymbolKind.Variable.CONTAINER,
		concept = { IConcept.TYPE, IConcept.FILE },
		doc = @doc ("specific type of for folder"))
public class GamaDirectoryType extends GamaFileType {


}