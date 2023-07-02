/*******************************************************************************************************
 *
 * GamaGeoJsonFile.java, in msi.gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gama.util.file;

import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geojson.feature.FeatureJSON;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryType;

import msi.gama.metamodel.shape.IShape;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.file;
import msi.gama.precompiler.GamlAnnotations.no_test;
import msi.gama.precompiler.GamlAnnotations.operator;
import msi.gama.precompiler.IConcept;
import msi.gama.precompiler.IOperatorCategory;
import msi.gama.runtime.GAMA;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaListFactory;
import msi.gama.util.IList;
import msi.gaml.expressions.IExpression;
import msi.gaml.statements.save.GeoJSonSaver;
import msi.gaml.types.IType;
import msi.gaml.types.Types;

/**
 * The Class GamaGeoJsonFile.
 */
@file (
		name = "geojson",
		extensions = { "json", "geojson", "geo.json" },
		buffer_type = IType.LIST,
		buffer_content = IType.GEOMETRY,
		buffer_index = IType.INT,
		concept = { IConcept.GIS, IConcept.FILE },
		doc = @doc ("Represents geospatial files written using the GeoJSON format. The internal representation is a list of geometries"))
public class GamaGeoJsonFile extends GamaGisFile {

	/**
	 * Instantiates a new gama geo json file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@doc (
			value = "This file constructor allows to read a geojson file (https://geojson.org/)",
			examples = { @example (
					value = "file f <- geojson_file(\"file.json\");",
					isExecutable = false) })
	public GamaGeoJsonFile(final IScope scope, final String pathName) throws GamaRuntimeException {
		super(scope, pathName, (Integer) null);
	}

	/**
	 * Instantiates a new gama geo json file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param code
	 *            the code
	 */
	@doc (
			value = "This file constructor allows to read a geojson file and specifying the coordinates system code, as an int",
			examples = { @example (
					value = "file f <- geojson_file(\"file.json\", 32648);",
					isExecutable = false) })
	public GamaGeoJsonFile(final IScope scope, final String pathName, final Integer code) {
		super(scope, pathName, code);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Instantiates a new gama geo json file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param code
	 *            the code
	 */
	@doc (
			value = "This file constructor allows to read a geojson file and specifying the coordinates system code (epg,...,), as a string",
			examples = { @example (
					value = "file f <- geojson_file(\"file.json\", \"EPSG:32648\");",
					isExecutable = false) })
	public GamaGeoJsonFile(final IScope scope, final String pathName, final String code) {
		super(scope, pathName, code);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Instantiates a new gama geo json file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param withZ
	 *            the with Z
	 */
	@doc (
			value = "This file constructor allows to read a geojson file and take a potential z value (not taken in account by default)",
			examples = { @example (
					value = "file f <- geojson_file(\"file.json\", true);",
					isExecutable = false) })
	public GamaGeoJsonFile(final IScope scope, final String pathName, final boolean withZ) {
		super(scope, pathName, (Integer) null, withZ);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Instantiates a new gama geo json file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param code
	 *            the code
	 * @param withZ
	 *            the with Z
	 */
	@doc (
			value = "This file constructor allows to read a geojson file, specifying the coordinates system code, as an int and take a potential z value (not taken in account by default)",
			examples = { @example (
					value = "file f <- geojson_file(\"file.json\",32648, true);",
					isExecutable = false) })
	public GamaGeoJsonFile(final IScope scope, final String pathName, final Integer code, final boolean withZ) {
		super(scope, pathName, code, withZ);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Instantiates a new gama geo json file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param code
	 *            the code
	 * @param withZ
	 *            the with Z
	 */
	@doc (
			value = "This file constructor allows to read a geojson file, specifying the coordinates system code (epg,...,), as a string and take a potential z value (not taken in account by default",
			examples = { @example (
					value = "file f <- geojson_file(\"file.json\", \"EPSG:32648\",true);",
					isExecutable = false) })
	public GamaGeoJsonFile(final IScope scope, final String pathName, final String code, final boolean withZ) {
		super(scope, pathName, code, withZ);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void fillBuffer(final IScope scope) throws GamaRuntimeException {
		if (getBuffer() != null) return;
		setBuffer(GamaListFactory.<IShape> create(Types.GEOMETRY));
		readShapes(scope);
	}

	@Override
	public IList<String> getAttributes(final IScope scope) {
		final Map<String, String> attributes = new LinkedHashMap<>();
		final SimpleFeatureCollection store = getFeatureCollection(scope);
		final java.util.List<AttributeDescriptor> att_list = store.getSchema().getAttributeDescriptors();
		for (final AttributeDescriptor desc : att_list) {
			String type;
			if (desc.getType() instanceof GeometryType) {
				type = "geometry";
			} else {
				type = Types.get(desc.getType().getBinding()).toString();
			}
			attributes.put(desc.getName().getLocalPart(), type);
		}

		return GamaListFactory.wrap(Types.STRING, attributes.keySet());
	}

	@Override
	protected SimpleFeatureCollection getFeatureCollection(final IScope scope) {
		try (FileReader fileReader = new FileReader(getFile(scope))) {
			final FeatureJSON fJSON = new FeatureJSON();
			return (SimpleFeatureCollection) fJSON.readFeatureCollection(fileReader);
		} catch (final Exception e) {
			GAMA.reportError(scope, GamaRuntimeException.create(e, scope), true);
		}
		return null;
	}

	/**
	 * To geojson.
	 *
	 * @param val
	 *            the val
	 * @return the string
	 */
	@operator (
			value = "to_geojson",
			category = { IOperatorCategory.CASTING },
			concept = { IConcept.CAST })
	@doc (
			value = "returns geojson of species with crs",
			examples = { @example (
					value = "to_geojson(boat,\"EPSG:4326\",[\"color\"])",
					equals = "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[100.51155642068785,3.514781609095577E-4,0.0]},\"properties\":{},\"id\":\"0\"}]}") },
			see = {})
	@no_test
	public static String toGeoJSon(final IScope scope, final IExpression spec, final String epsgCode,
			final IExpression attributesFacet) {

		final GeoJSonSaver gjsoner = new GeoJSonSaver();
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			gjsoner.save(scope, spec, baos, epsgCode, attributesFacet);
			return baos.toString(StandardCharsets.UTF_8);

		} catch (final GamaRuntimeException e) {
			throw e;
		} catch (final Throwable e) {
			throw GamaRuntimeException.create(e, scope);
		}
	}

}
