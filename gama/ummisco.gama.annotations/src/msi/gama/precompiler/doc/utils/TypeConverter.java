/*******************************************************************************************************
 *
 * TypeConverter.java, in ummisco.gama.annotations, is part of the source code of the
 * GAMA modeling and simulation platform (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package msi.gama.precompiler.doc.utils;

import java.util.HashMap;

import msi.gama.precompiler.IOperatorCategory;

/**
 * The Class TypeConverter.
 */
public class TypeConverter {

	/** The Constant properNameTypeMap. */
	public static final HashMap<String, String> properNameTypeMap = new HashMap<>();
	
	/** The special cases proper name type map. */
	HashMap<String, String> specialCasesProperNameTypeMap;
	
	/** The proper category name map. */
	HashMap<String, String> properCategoryNameMap;
	
	/** The Constant typeStringFromIType. */
	public static final HashMap<Integer, String> typeStringFromIType = new HashMap<>();
	
	/** The symbol kind string from I symbol kind. */
	HashMap<Integer, String> symbolKindStringFromISymbolKind;

	/**
	 * Instantiates a new type converter.
	 */
	public TypeConverter() {
		properNameTypeMap.putAll(initProperNameTypeMap());
		specialCasesProperNameTypeMap = initSpecialCasesProperNameTypeMap();
		properCategoryNameMap = initProperNameCategoriesMap();
		typeStringFromIType.putAll(initNameTypeFromIType());
		symbolKindStringFromISymbolKind = initSymbolKindStringFromISymbolKind();
	}

	/**
	 * Register type.
	 *
	 * @param className the class name
	 * @param type the type
	 * @param id the id
	 */
	public static void registerType(final String className, final String type, final Integer id) {
		if (!properNameTypeMap.containsKey(className)) {
			properNameTypeMap.put(className, type);
		}
		if (!typeStringFromIType.containsKey(id)) {
			typeStringFromIType.put(id, type);
		}
	}

	/**
	 * Inits the symbol kind string from I symbol kind.
	 *
	 * @return the hash map
	 */
	private HashMap<Integer, String> initSymbolKindStringFromISymbolKind() {
		final HashMap<Integer, String> hm = new HashMap<>();
		hm.put(0, "Species");
		hm.put(1, "Model");
		hm.put(2, "Single statement");
		hm.put(3, "Behavior");
		hm.put(4, "Parameter");
		hm.put(5, "Output");
		hm.put(6, "Layer");
		hm.put(7, "Skill");
		hm.put(8, "Batch section");
		hm.put(9, "Batch method");
		hm.put(10, "Environment");
		hm.put(11, "Sequence of statements or action");
		hm.put(13, "Experiment");
		hm.put(14, "Abstract section");
		hm.put(101, "Variable (number)");
		hm.put(102, "Variable (container)");
		hm.put(103, "Variable (signal)");
		hm.put(104, "Variable (regular)");
		return hm;
	}

	/**
	 * Inits the proper name type map.
	 *
	 * @return the hash map
	 */
	private HashMap<String, String> initProperNameTypeMap() {
		final HashMap<String, String> hm = new HashMap<>();

		hm.put("java.lang.Integer", "int");
		hm.put("java.lang.Double", "float");
		hm.put("java.lang.Long", "float");
		hm.put("double", "float");
		hm.put("java.lang.Number", "float");
		hm.put("boolean", "bool");
		hm.put("java.lang.Boolean", "bool");
		hm.put("msi.gama.util.GamaPair", "pair");
		hm.put("java.lang.String", "string");

		// Matrix
		hm.put("msi.gama.util.matrix.IMatrix", "matrix");
		hm.put("msi.gama.util.matrix.GamaMatrix", "matrix");
		hm.put("msi.gama.util.matrix.GamaIntMatrix", "matrix<int>");
		hm.put("msi.gama.util.matrix.GamaFloatMatrix", "matrix<float>");

		// Files
		hm.put("msi.gama.util.file.IGamaFile", "file");
		hm.put("msi.gama.util.file.GamaFile", "file");
		hm.put("msi.gama.jogl.files.Gama3DSFile", "file");
		hm.put("msi.gama.jogl.files.GamaObjFile", "file");
		hm.put("ummisco.gama.serializer.gaml.GamaSavedSimulationFile", "file");

		// Colors
		hm.put("msi.gama.util.GamaColor", "rgb");
		hm.put("msi.gaml.operators.Colors.GamaPalette","list<rgb>");
		hm.put("msi.gaml.operators.Colors.GamaGradient","map<rgb,float>");	
		hm.put("msi.gaml.operators.Colors.GamaScale","map<float,rgb>");

		// List
		hm.put("msi.gama.util.IList", "list");
		hm.put("msi.gama.util.GamaList", "list");
		hm.put("msi.gama.util.IList", "list");
		hm.put("java.util.List", "list");
		hm.put("msi.gama.util.GamaDateInterval", "list");

		hm.put("java.lang.Object", "unknown");
		hm.put("T", "unknown");
		hm.put("?", "unknown");
		hm.put("msi.gama.util.tree.GamaNode", "unknown");
		hm.put("? extends java.lang.Comparable", "unknown");
		hm.put("msi.gaml.types.IType", "any GAML type");
		hm.put("msi.gaml.expressions.IExpression", "any expression");

		hm.put("msi.gama.metamodel.agent.IAgent", "agent");
		hm.put("msi.gama.kernel.experiment.IExperimentAgent", "agent");
		hm.put("msi.gama.metamodel.shape.IShape", "geometry");
		hm.put("msi.gama.metamodel.shape.GamaShape", "geometry");
		hm.put("? extends msi.gama.metamodel.shape.IShape", "geometry");

		hm.put("msi.gama.metamodel.shape.GamaPoint", "point");
		hm.put("msi.gama.metamodel.shape.GamaPoint", "point");
		hm.put("msi.gama.util.graph.IGraph", "graph");
		hm.put("msi.gama.util.graph.GamaGraph", "graph");
		hm.put("msi.gama.metamodel.topology.ITopology", "topology");
		hm.put("msi.gama.util.GamaMap", "map");
		hm.put("msi.gama.util.IMap", "map");
		hm.put("msi.gaml.species.ISpecies", "species");

		hm.put("msi.gama.util.IContainer", "container");
		hm.put("msi.gama.util.IContainer<?,java.lang.Double>", "container<float>");

		hm.put("java.util.Map", "map");

		hm.put("msi.gama.util.GamaFont", "font");
		hm.put("msi.gama.util.GamaRegression", "regression");
		hm.put("msi.gama.util.GamaDate", "date");
		hm.put("msi.gama.util.GamaMaterial", "material");

		// BDI
		hm.put("msi.gaml.architecture.simplebdi.Predicate", "predicate");
		hm.put("msi.gaml.architecture.simplebdi.BDIPlan", "BDIPlan");
		hm.put("msi.gaml.architecture.simplebdi.Emotion", "emotion");
		hm.put("msi.gaml.architecture.simplebdi.MentalState", "mental_state");
		hm.put("msi.gaml.architecture.simplebdi.SocialLink", "social_link");

		// FIPA
		hm.put("msi.gaml.extensions.fipa.Conversation", "conversation");
		
		hm.put("msi.gaml.extensions.fipa.Message", "message");	
		hm.put("msi.gama.extensions.messaging.GamaMessage", "message");
		hm.put("msi.gaml.extensions.fipa.FIPAMessage", "message");

		hm.put("msi.gama.util.IPath", "path");
		hm.put("msi.gama.util.path.IPath", "path");
		hm.put("msi.gama.util.path.GamaSpatialPath", "path");

		hm.put("msi.gama.util.IContainer.Addressable", "container");

		hm.put("msi.gaml.types.GamaKmlExport", "kml");
		
		hm.put("msi.gama.kernel.experiment.IParameter", "unknown");
		
		hm.put("msi.gama.util.matrix.GamaField", "field");
		hm.put("msi.gama.util.matrix.IField","field");

		hm.put("msi.gaml.descriptions.ActionDescription", "action");
		
		return hm;
	}

	/**
	 * Inits the special cases proper name type map.
	 *
	 * @return the hash map
	 */
	private HashMap<String, String> initSpecialCasesProperNameTypeMap() {
		final HashMap<String, String> sphm = new HashMap<>();
		sphm.put(
				"msi.gama.util.IAddressableContainer<java.lang.Integer,msi.gama.metamodel.agent.IAgent,java.lang.Integer,msi.gama.metamodel.agent.IAgent>",
				"list<agent>");
		sphm.put("msi.gama.util.IContainer<KeyType,ValueType>.Addressable<KeyType,ValueType>",
				"container<KeyType,ValueType>");
		return sphm;
	}

	/**
	 * Inits the name type from I type.
	 *
	 * @return the hash map
	 */
	// FROM IType.java
	private HashMap<Integer, String> initNameTypeFromIType() {
		final HashMap<Integer, String> hm = new HashMap<>();
		hm.put(0, "any type"); // NONE
		hm.put(1, "int");
		hm.put(2, "float");
		hm.put(3, "boolean");
		hm.put(4, "string");
		hm.put(5, "list");
		hm.put(6, "rgb");
		hm.put(7, "point");
		hm.put(8, "matrix");
		hm.put(9, "pair");
		hm.put(10, "map");
		hm.put(11, "agent");
		hm.put(12, "file");
		hm.put(13, "geometry");
		hm.put(14, "species");
		hm.put(15, "graph");
		hm.put(16, "container");
		hm.put(17, "path");
		hm.put(18, "topology");
		hm.put(19, "font");
		hm.put(20, "image");
		hm.put(21, "regression");
		hm.put(23, "date");
		hm.put(24, "message");
		hm.put(25, "material");
		hm.put(29, "kml");
		hm.put(30, "directory");
		hm.put(31, "field");
		hm.put(50, "available_types");
		hm.put(99, "message");
		hm.put(100, "species_types");

		hm.put(-27, "agent");		// a simulation agent
		hm.put(-29, "agent");		// a host agent
		hm.put(-199, "agent");		// agent / linked road
		hm.put(-200, "a label");
		hm.put(-201, "an identifier");
		hm.put(-202, "a datatype identifier");
		hm.put(-203, "a new identifier");
		hm.put(-204, "a new identifier");
		return hm;
	}

	/**
	 * Inits the proper name categories map.
	 *
	 * @return the hash map
	 */
	private HashMap<String, String> initProperNameCategoriesMap() {
		final HashMap<String, String> hm = new HashMap<>();
		hm.put("Cast", IOperatorCategory.CASTING);
		hm.put("Colors", IOperatorCategory.COLOR);
		hm.put("DrivingOperators", IOperatorCategory.DRIVING);
		hm.put("Comparison", IOperatorCategory.COMPARISON);
		hm.put("IContainer", IOperatorCategory.CONTAINER);
		hm.put("Containers", IOperatorCategory.CONTAINER);
		hm.put("GamaMap", IOperatorCategory.CONTAINER);
		hm.put("IMap", IOperatorCategory.CONTAINER);
		hm.put("Files", IOperatorCategory.FILE);
		hm.put("GamaFileType", IOperatorCategory.FILE);
		hm.put("MessageType", IOperatorCategory.FIPA);
		hm.put("ConversationType", IOperatorCategory.FIPA);
		hm.put("Graphs", IOperatorCategory.GRAPH);
		hm.put("GraphsGraphstream", IOperatorCategory.GRAPH);
		hm.put("Logic", IOperatorCategory.LOGIC);
		hm.put("Maths", IOperatorCategory.ARITHMETIC);
		hm.put("GamaFloatMatrix", IOperatorCategory.MATRIX);
		hm.put("GamaIntMatrix", IOperatorCategory.MATRIX);
		hm.put("GamaMatrix", IOperatorCategory.MATRIX);
		hm.put("GamaObjectMatrix", IOperatorCategory.MATRIX);
		hm.put("IMatrix", IOperatorCategory.MATRIX);
		hm.put("SingleEquationStatement", IOperatorCategory.EDP);
		hm.put("Creation", IOperatorCategory.SPATIAL);
		hm.put("Operators", IOperatorCategory.SPATIAL);
		hm.put("Points", IOperatorCategory.SPATIAL);
		hm.put("Properties", IOperatorCategory.SPATIAL);
		hm.put("Punctal", IOperatorCategory.SPATIAL);
		hm.put("Queries", IOperatorCategory.SPATIAL);
		hm.put("ThreeD", IOperatorCategory.SPATIAL);
		hm.put("Statistics", IOperatorCategory.SPATIAL);
		hm.put("Transformations", IOperatorCategory.SPATIAL);
		hm.put("Relations", IOperatorCategory.SPATIAL);
		hm.put("Random", IOperatorCategory.RANDOM);
		hm.put("Stats", IOperatorCategory.STATISTICAL);
		hm.put("Strings", IOperatorCategory.STRING);
		hm.put("System", IOperatorCategory.SYSTEM);
		hm.put("Types", IOperatorCategory.TYPE);
		hm.put("WaterLevel", IOperatorCategory.WATER);
		return hm;
	}

	/**
	 * Gets the proper type.
	 *
	 * @param rawName the raw name
	 * @return the proper type
	 */
	public String getProperType(final String rawName) {
		if (specialCasesProperNameTypeMap.containsKey(rawName)) { return specialCasesProperNameTypeMap.get(rawName); }

		// Get only the first <
		final String[] splitByLeftBracket = rawName.split("<", 2);

		// Stop criteria: no bracket
		if (splitByLeftBracket.length == 1) {
			if (properNameTypeMap.containsKey(splitByLeftBracket[0])) {
				return properNameTypeMap.get(splitByLeftBracket[0]);
			} else {
				return splitByLeftBracket[0];
			}
		} else if (splitByLeftBracket.length == 2) {
			final String leftElement = getProperType(splitByLeftBracket[0]);

			final String lastString = splitByLeftBracket[1];
			splitByLeftBracket[1] = lastString.substring(0, lastString.length() - 1);

			// Get only the first ","
			final int comaIndex = findCentralComa(splitByLeftBracket[1]);
			if (comaIndex > 0) {
				return leftElement + "<" + getProperType(splitByLeftBracket[1].substring(0, comaIndex)) + ","
						+ getProperType(splitByLeftBracket[1].substring(comaIndex + 1)) + ">";
			} else {
				return leftElement + "<" + getProperType(splitByLeftBracket[1]) + ">";
			}

			// String[] splitByComa = splitByLeftBracket[1].split(",",2);

			// if(splitByComa.length > 1) {
			// return leftElement + "<" + getProperType(splitByComa[0]) + "," + getProperType(splitByComa[1]) + ">";
			// } else {
			// return leftElement + "<" + getProperType(splitByLeftBracket[1]) + ">";
			// }
			// return leftElement + "<" + getProperType(splitByLeftBracket[1]) + ">";
		} else {
			throw new IllegalArgumentException("getProperType has a not appropriate input");
		}

	}

	/**
	 * Find central coma.
	 *
	 * @param s the s
	 * @return the int
	 */
	public static int findCentralComa(final String s) {
		int foundIndex = 0;

		if (s.contains(",")) {
			foundIndex = s.indexOf(",", 0);

			do {
				final String sLeft = s.substring(0, foundIndex);

				if (sLeft.lastIndexOf("<") == -1 && sLeft.lastIndexOf(">") == -1) {
					return foundIndex;
				} else if (sLeft.lastIndexOf(">") > sLeft.lastIndexOf("<")) { return foundIndex; }

				foundIndex = s.indexOf(",", foundIndex + 1);

			} while (foundIndex >= 0);
			return -1;
		}
		return -1;
	}

	/**
	 * Gets the proper operator name.
	 *
	 * @param opName the op name
	 * @return the proper operator name
	 */
	public String getProperOperatorName(final String opName) {
		// if("*".equals(opName)) return "`*`";
		return opName;
	}

	/**
	 * Gets the proper category.
	 *
	 * @param rawName the raw name
	 * @return the proper category
	 */
	public String getProperCategory(final String rawName) {
		if (properCategoryNameMap.containsKey(rawName)) {
			return properCategoryNameMap.get(rawName);
		} else {
			return rawName;
		}
	}

	/**
	 * Gets the type string.
	 *
	 * @param i the i
	 * @return the type string
	 */
	public String getTypeString(final Integer i) {
		if (typeStringFromIType.containsKey(i)) {
			return typeStringFromIType.get(i);
		} else {
			return "" + i;
		}
	}

	/**
	 * Gets the type string.
	 *
	 * @param types the types
	 * @return the type string
	 */
	public String getTypeString(final int[] types) {
		final StringBuilder s = new StringBuilder(30);
		s.append(types.length < 2 ? "" : "any type in [");
		for (int i = 0; i < types.length; i++) {
			s.append(getTypeString(types[i]));

			if (i != types.length - 1) {
				s.append(", ");
			}
		}
		if (types.length >= 2) {
			s.append("]");
		}
		return s.toString();
	}

	/**
	 * Gets the symbol kind string from I symbol kind.
	 *
	 * @param i the i
	 * @return the symbol kind string from I symbol kind
	 */
	public String getSymbolKindStringFromISymbolKind(final Integer i) {
		if (symbolKindStringFromISymbolKind.containsKey(i)) {
			return symbolKindStringFromISymbolKind.get(i);
		} else {
			return "" + i;
		}
	}
}
