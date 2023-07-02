/*******************************************************************************************************
 *
 * TypeFieldExpression.java, in msi.gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gaml.expressions.operators;

import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.precompiler.GamlProperties;
import msi.gama.runtime.IScope;
import msi.gaml.descriptions.IDescription;
import msi.gaml.descriptions.OperatorProto;
import msi.gaml.expressions.IExpression;

/**
 * The Class TypeFieldExpression.
 */
public class TypeFieldExpression extends UnaryOperator {

	/**
	 * Instantiates a new type field expression.
	 *
	 * @param proto
	 *            the proto
	 * @param context
	 *            the context
	 * @param expr
	 *            the expr
	 */
	public TypeFieldExpression(final OperatorProto proto, final IDescription context, final IExpression expr) {
		super(proto, context, expr);
	}

	@Override
	public TypeFieldExpression resolveAgainst(final IScope scope) {
		return new TypeFieldExpression(prototype, null, child.resolveAgainst(scope));
	}

	@Override
	public String serialize(final boolean includingBuiltIn) {
		final StringBuilder sb = new StringBuilder();
		parenthesize(sb, child);
		sb.append(".").append(getName());
		return sb.toString();
	}

	@Override
	public String toString() {
		if (child == null) return prototype.signature.toString() + "." + getName();
		return child.serialize(false) + "." + getName();
	}

	@Override
	public Doc getDocumentation() {
		final StringBuilder sb = new StringBuilder(200);
		if (child != null) { sb.append("Defined on objects of type " + child.getGamlType().getTitle()); }
		final vars annot = prototype.getSupport().getAnnotation(vars.class);
		if (annot != null) {
			final variable[] allVars = annot.value();
			for (final variable v : allVars) {
				if (v.name().equals(getName()) && v.doc().length > 0) {
					sb.append("<br/>");
					sb.append(v.doc()[0].value());
				}
			}
		}
		return new RegularDoc(sb);
	}

	@Override
	public String getTitle() { return "field <b>" + getName() + "</b> of type " + getGamlType().getTitle(); }

	@Override
	public void collectMetaInformation(final GamlProperties meta) {
		meta.put(GamlProperties.ATTRIBUTES, getName());
	}

}
