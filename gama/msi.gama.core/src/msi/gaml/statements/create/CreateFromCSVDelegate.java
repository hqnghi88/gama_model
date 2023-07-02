/*******************************************************************************************************
 *
 * CreateFromCSVDelegate.java, in msi.gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gaml.statements.create;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import msi.gama.common.interfaces.ICreateDelegate;
import msi.gama.runtime.IScope;
import msi.gama.util.GamaMapFactory;
import msi.gama.util.IList;
import msi.gama.util.file.GamaCSVFile;
import msi.gama.util.matrix.IMatrix;
import msi.gaml.expressions.IExpression;
import msi.gaml.operators.Cast;
import msi.gaml.statements.Arguments;
import msi.gaml.statements.CreateStatement;
import msi.gaml.types.IType;
import msi.gaml.types.Types;

/**
 * Class CreateFromDatabaseDelegate.
 *
 * @author drogoul
 * @since 27 mai 2015
 *
 */
public class CreateFromCSVDelegate implements ICreateDelegate {

	/**
	 * Method acceptSource()
	 *
	 * @see msi.gama.common.interfaces.ICreateDelegate#acceptSource(IScope, java.lang.Object)
	 */
	@Override
	public boolean acceptSource(final IScope scope, final Object source) {
		return source instanceof GamaCSVFile;
	}

	/**
	 * Method createFrom() Method used to read initial values and attributes from a CSV values descring a synthetic
	 * population
	 *
	 * @author Alexis Drogoul
	 * @since 04-09-2012
	 * @see msi.gama.common.interfaces.ICreateDelegate#createFrom(msi.gama.runtime.IScope, java.util.List, int,
	 *      java.lang.Object)
	 */
	@SuppressWarnings ("rawtypes")
	@Override
	public boolean createFrom(final IScope scope, final List<Map<String, Object>> inits, final Integer max,
			final Object input, final Arguments init, final CreateStatement statement) {
		final GamaCSVFile source = (GamaCSVFile) input;
		final IExpression header = statement.getHeader();
		if (header != null) { source.forceHeader(Cast.asBool(scope, header.value(scope))); }
		final boolean hasHeader = source.hasHeader(scope);
		final IMatrix<?> mat = source.getContents(scope);
		if (mat == null || mat.isEmpty(scope)) return false;
		int rows = mat.getRows(scope);
		final int cols = mat.getCols(scope);
		rows = max == null ? rows : Math.min(rows, max);

		List<String> headers;
		if (hasHeader) {
			headers = source.getAttributes(scope);
		} else {
			headers = new ArrayList<>();
			for (int j = 0; j < cols; j++) { headers.add(String.valueOf(j)); }
		}
		for (int i = 0; i < rows; i++) {
			final Map<String, Object> map = GamaMapFactory.create(hasHeader ? Types.STRING : Types.INT, Types.NO_TYPE);
			final IList vals = mat.getRow(i);
			for (int j = 0; j < cols; j++) {
				// see issue #3786
				String s = clean(headers.get(j));
				Object v = vals.get(j);
				map.put(s, v);
			}
			// CSV attributes are mixed with the attributes of agents
			statement.fillWithUserInit(scope, map);
			inits.add(map);
		}
		return true;
	}

	/**
	 * Clean.
	 *
	 * @param text
	 *            the text
	 * @return the string
	 */
	private static String clean(String text) {
		// // strips off all non-ASCII characters
		// text = text.replaceAll("[^\\x00-\\x7F]", "");
		// erases all the ASCII control characters
		text = text.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
		// removes non-printable characters from Unicode
		text = text.replaceAll("\\p{C}", "");

		return text.trim();
	}

	/**
	 * Method fromFacetType()
	 *
	 * @see msi.gama.common.interfaces.ICreateDelegate#fromFacetType()
	 */
	@Override
	public IType<?> fromFacetType() {
		return Types.FILE;
	}

}
