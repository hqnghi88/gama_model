/*******************************************************************************************************
 *
 * UserFirstControlArchitecture.java, in msi.gama.core, is part of the source code of the
 * GAMA modeling and simulation platform (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package msi.gaml.architecture.user;

import msi.gama.common.interfaces.IKeyword;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.precompiler.IConcept;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;

// @vars({ @var(name = IKeyword.STATE, type = IType.STRING),
/**
 * The Class UserFirstControlArchitecture.
 */
// @var(name = IKeyword.STATES, type = IType.LIST, constant = true) })
@skill (
		name = IKeyword.USER_FIRST,
		concept = { IConcept.GUI, IConcept.ARCHITECTURE })
@doc("A control architecture, based on FSM, where the user is being given control before states / reflexes of the agent are executed. This skill extends the UserControlArchitecture skill and take all his actions and variables ")
public class UserFirstControlArchitecture extends UserControlArchitecture {

	@Override
	public Object executeOn(final IScope scope) throws GamaRuntimeException {
		executeCurrentState(scope);
		return executeReflexes(scope);
	}

	@Override
	public boolean init(final IScope scope) throws GamaRuntimeException {
		if (initPanel != null) {
			scope.execute(initPanel);
		}
		return super.init(scope);
	}
}