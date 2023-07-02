/*******************************************************************************************************
 *
 * GamaDateType.java, in msi.gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gaml.types;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.type;
import msi.gama.precompiler.IConcept;
import msi.gama.precompiler.ISymbolKind;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaDate;
import msi.gama.util.IContainer;
import msi.gaml.operators.Cast;

/**
 * Written by Patrick Tallandier
 *
 * @todo Description
 *
 */
@SuppressWarnings ("unchecked")
@type (
		name = "date",
		id = IType.DATE,
		wraps = { GamaDate.class },
		kind = ISymbolKind.Variable.NUMBER,
		concept = { IConcept.TYPE, IConcept.DATE, IConcept.TIME },
		doc = { @doc ("GAML objects that represent a date") })
public class GamaDateType extends GamaType<GamaDate> {

	/** The Constant DEFAULT_ZONE. */
	public static final ZoneId DEFAULT_ZONE = Clock.systemDefaultZone().getZone();

	/** The Constant DEFAULT_OFFSET_IN_SECONDS. */
	public static final ZoneOffset DEFAULT_OFFSET_IN_SECONDS =
			Clock.systemDefaultZone().getZone().getRules().getOffset(Instant.now(Clock.systemDefaultZone()));

	/** The Constant EPOCH. */
	public static final GamaDate EPOCH = GamaDate.of(LocalDateTime.ofEpochSecond(0, 0, DEFAULT_OFFSET_IN_SECONDS));

	// public static final ZoneOffset DEFAULT_OFFSET_IN_SECONDS = ZoneOffset.UTC; // ofTotalSeconds(0);
	// public static final ZoneId DEFAULT_ZONE =
	// ZoneId.ofOffset("UTC", DEFAULT_OFFSET_IN_SECONDS);/* ZoneId.ofOffset("", DEFAULT_OFFSET_IN_SECONDS); */
	// // Clock.systemDefaultZone().getZone().getRules().getOffset(Instant.now(Clock.systemDefaultZone()));
	// public static final GamaDate EPOCH = /** GamaDate.of(LocalDateTime.of(1970, 1, 1, 0, 0)); **/
	// GamaDate.of(LocalDateTime.ofEpochSecond(0, 0, DEFAULT_OFFSET_IN_SECONDS));
	@doc ("Cast the argument into a date. If the argument is a date already, returns it, otherwise: if it is a container, casts its contents to integer numbers and tries to build a date from it (following the order 'year, month, day, hour, minute, second'); if it is a string, tries to decode it into a date using the format described in the preferences; otherwise cast the argument into a float number and interprets it as the number of milliseconds since the start of the simulation")
	@Override
	public GamaDate cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		return staticCast(scope, obj, param, copy);
	}

	/**
	 * Static cast.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @param param
	 *            the param
	 * @param copy
	 *            the copy
	 * @return the gama date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public static GamaDate staticCast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		if (obj == null) return null;
		if (obj instanceof GamaDate) {
			if (copy) return new GamaDate(scope, (GamaDate) obj);
			return (GamaDate) obj;
		}
		if (obj instanceof IContainer)
			return new GamaDate(scope, ((IContainer<?, ?>) obj).listValue(scope, Types.INT, false));
		if (obj instanceof String) return new GamaDate(scope, (String) obj);
		// If everything fails, we assume it is a duration in seconds since the starting date of the model
		final Double d = Cast.asFloat(scope, obj);
		return new GamaDate(scope, d);
	}

	@Override
	public GamaDate getDefault() { return null; }

	@Override
	public IType<?> getContentType() { return Types.get(FLOAT); }

	@Override
	public boolean canCastToConst() {
		return true;
	}

	@Override
	public boolean isCompoundType() { return true; }

}
