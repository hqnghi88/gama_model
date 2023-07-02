package gaml.additions.network;

import msi.gama.util.matrix.*;
import msi.gama.util.graph.*;
import msi.gama.metamodel.shape.*;
import java.lang.*;
import msi.gama.util.*;
import msi.gama.util.path.*;
import msi.gama.extensions.messaging.*;
import msi.gaml.compilation.*;
import msi.gama.util.file.*;
import msi.gaml.architecture.finite_state_machine.*;
import msi.gama.common.interfaces.*;
import msi.gaml.architecture.user.*;
import msi.gama.kernel.simulation.*;
import msi.gama.outputs.*;
import msi.gaml.species.*;
import msi.gaml.architecture.weighted_tasks.*;
import msi.gama.kernel.experiment.*;
import  msi.gama.metamodel.shape.*;
import msi.gaml.extensions.multi_criteria.*;
import msi.gaml.types.*;
import msi.gama.kernel.batch.*;
import msi.gama.kernel.root.*;
import java.util.*;
import msi.gaml.factories.*;
import msi.gaml.skills.*;
import msi.gama.util.tree.*;
import msi.gama.outputs.layers.*;
import msi.gama.runtime.*;
import msi.gaml.statements.test.*;
import msi.gama.metamodel.agent.*;
import msi.gaml.expressions.*;
import msi.gama.metamodel.topology.*;
import msi.gaml.statements.draw.*;
import msi.gaml.descriptions.*;
import msi.gaml.variables.*;
import msi.gaml.operators.*;
import msi.gama.runtime.exceptions.*;
import msi.gaml.statements.*;
import msi.gama.kernel.model.*;
import msi.gama.metamodel.population.*;
import msi.gama.outputs.layers.charts.*;
import msi.gaml.architecture.reflex.*;
import msi.gaml.operators.Random;
import msi.gaml.operators.Maths;
import msi.gaml.operators.Points;
import msi.gaml.operators.Spatial.Properties;
import msi.gaml.operators.System;
import static msi.gaml.operators.Cast.*;
import static msi.gaml.operators.Spatial.*;
import static msi.gama.common.interfaces.IKeyword.*;
@SuppressWarnings({ "rawtypes", "unchecked", "unused" })

public class GamlAdditions extends msi.gaml.compilation.AbstractGamlAdditions {
	public void initialize() throws SecurityException, NoSuchMethodException {
	initializeVars();
	initializeAction();
	initializeSkill();
}public void initializeVars()  {
_var(ummisco.gama.network.skills.NetworkSkill.class,desc(4,S("type","4","name","network_name")),null,null,null);
_var(ummisco.gama.network.skills.NetworkSkill.class,desc(5,S("type","5","name","network_groups")),null,null,null);
_var(ummisco.gama.network.skills.NetworkSkill.class,desc(5,S("type","5","name","network_server")),null,null,null);
}public void initializeAction() throws SecurityException, NoSuchMethodException {
_action(new GamaHelper("leave_group",ummisco.gama.network.skills.NetworkSkill.class,(s,a,t,v)->((ummisco.gama.network.skills.NetworkSkill) t).leaveTheGroup(s)),desc(PRIM,new Children(_arg("with_name",4,F)),NAME,"leave_group",TYPE,Ti(B),VIRTUAL,FALSE),ummisco.gama.network.skills.NetworkSkill.class.getMethod("leaveTheGroup",SC));
_action(new GamaHelper("has_more_message",ummisco.gama.network.skills.NetworkSkill.class,(s,a,t,v)->((ummisco.gama.network.skills.NetworkSkill) t).hasMoreMessage(s)),desc(PRIM,new Children(),NAME,"has_more_message",TYPE,Ti(B),VIRTUAL,FALSE),ummisco.gama.network.skills.NetworkSkill.class.getMethod("hasMoreMessage",SC));
_action(new GamaHelper("fetch_message",ummisco.gama.network.skills.NetworkSkill.class,(s,a,t,v)->((ummisco.gama.network.skills.NetworkSkill) t).fetchMessage(s)),desc(PRIM,new Children(),NAME,"fetch_message",TYPE,Ti(GamaMessage.class),VIRTUAL,FALSE),ummisco.gama.network.skills.NetworkSkill.class.getMethod("fetchMessage",SC));
_action(new GamaHelper("connect",ummisco.gama.network.skills.NetworkSkill.class,(s,a,t,v)->((ummisco.gama.network.skills.NetworkSkill) t).connectToServer(s)),desc(PRIM,new Children(_arg("protocol",4,T),_arg("port",1,T),_arg("raw",3,T),_arg("with_name",4,T),_arg("login",4,T),_arg("password",4,T),_arg("force_network_use",3,T),_arg("to",4,T),_arg("size_packet",1,T)),NAME,"connect",TYPE,Ti(B),VIRTUAL,FALSE),ummisco.gama.network.skills.NetworkSkill.class.getMethod("connectToServer",SC));
_action(new GamaHelper("join_group",ummisco.gama.network.skills.NetworkSkill.class,(s,a,t,v)->((ummisco.gama.network.skills.NetworkSkill) t).registerToGroup(s)),desc(PRIM,new Children(_arg("with_name",4,F)),NAME,"join_group",TYPE,Ti(B),VIRTUAL,FALSE),ummisco.gama.network.skills.NetworkSkill.class.getMethod("registerToGroup",SC));
_action(new GamaHelper("fetch_message_from_network",ummisco.gama.network.skills.NetworkSkill.class,(s,a,t,v)->((ummisco.gama.network.skills.NetworkSkill) t).fetchMessagesOfAgents(s)),desc(PRIM,new Children(),NAME,"fetch_message_from_network",TYPE,Ti(B),VIRTUAL,FALSE),ummisco.gama.network.skills.NetworkSkill.class.getMethod("fetchMessagesOfAgents",SC));
_action(new GamaHelper("execute",ummisco.gama.network.skills.NetworkSkill.class,(s,a,t,v)->((ummisco.gama.network.skills.NetworkSkill) t).systemExec(s)),desc(PRIM,new Children(_arg("command",4,T)),NAME,"execute",TYPE,Ti(S),VIRTUAL,FALSE),ummisco.gama.network.skills.NetworkSkill.class.getMethod("systemExec",SC));
}public void initializeSkill()  {
_skill("network",ummisco.gama.network.skills.NetworkSkill.class,AS);
}
}