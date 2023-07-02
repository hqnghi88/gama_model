package gaml.additions.serialize;

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
	initializeOperator();
	initializeFile();
	initializeExperiment();
}public void initializeOperator() throws SecurityException, NoSuchMethodException {
_operator(S("save_simulation"),ummisco.gama.serializer.gaml.ReverseOperators.class.getMethod("saveSimulation",SC,S),AI,i,F,-13,-13,-13,-13,(s,o)->ummisco.gama.serializer.gaml.ReverseOperators.saveSimulation(s,((String)o[0])),F);
_operator(S("save_agent"),ummisco.gama.serializer.gaml.ReverseOperators.class.getMethod("saveAgent",SC,IA,S),AI,i,F,-13,-13,-13,-13,(s,o)->ummisco.gama.serializer.gaml.ReverseOperators.saveAgent(s,((IAgent)o[0]),((String)o[1])),F);
_operator(S("serialize_agent"),ummisco.gama.serializer.gaml.ReverseOperators.class.getMethod("serializeAgent",SC,IA),AI,S,F,-13,-13,-13,-13,(s,o)->ummisco.gama.serializer.gaml.ReverseOperators.serializeAgent(s,((IAgent)o[0])),F);
_operator(S("restore_simulation"),ummisco.gama.serializer.gaml.ReverseOperators.class.getMethod("unSerializeSimulationFromXML",SC,S),AI,i,F,-13,-13,-13,-13,(s,o)->ummisco.gama.serializer.gaml.ReverseOperators.unSerializeSimulationFromXML(s,((String)o[0])),F);
_operator(S("unserialize"),ummisco.gama.serializer.gaml.ReverseOperators.class.getMethod("unserialize",SC,S),AI,O,F,-13,-13,-13,-13,(s,o)->ummisco.gama.serializer.gaml.ReverseOperators.unserialize(s,((String)o[0])),F);
_operator(S("serialize"),ummisco.gama.serializer.gaml.ReverseOperators.class.getMethod("serialize",SC,O),AI,S,F,-13,-13,-13,-13,(s,o)->ummisco.gama.serializer.gaml.ReverseOperators.serialize(s,o[0]),F);
_operator(S("restore_simulation_from_file"),ummisco.gama.serializer.gaml.ReverseOperators.class.getMethod("unSerializeSimulationFromFile",SC,ummisco.gama.serializer.gaml.GamaSavedSimulationFile.class),AI,i,F,-13,-13,-13,-13,(s,o)->ummisco.gama.serializer.gaml.ReverseOperators.unSerializeSimulationFromFile(s,((ummisco.gama.serializer.gaml.GamaSavedSimulationFile)o[0])),F);
_operator(S("unserialize_network"),ummisco.gama.serializer.gaml.ReverseOperators.class.getMethod("unserializeNetwork",SC,S),AI,O,F,-13,-13,-13,-13,(s,o)->ummisco.gama.serializer.gaml.ReverseOperators.unserializeNetwork(s,((String)o[0])),F);
_operator(S("serialize_network"),ummisco.gama.serializer.gaml.ReverseOperators.class.getMethod("serializeNetwork",SC,O),AI,S,F,-13,-13,-13,-13,(s,o)->ummisco.gama.serializer.gaml.ReverseOperators.serializeNetwork(s,o[0]),F);
}public void initializeFile() throws SecurityException, NoSuchMethodException {
_file("saved_simulation",ummisco.gama.serializer.gaml.GamaSavedSimulationFile.class,(s,o)-> {return new ummisco.gama.serializer.gaml.GamaSavedSimulationFile(s,((String)o[0]));},5,1,4,S("gsim","gasim"));
_operator(S("is_saved_simulation"),null,I(0),B,true,3,0,0,0,(s,o)-> { return GamaFileType.verifyExtension("saved_simulation",(String)o[0]);}, false);
_operator(S("saved_simulation_file"),ummisco.gama.serializer.gaml.GamaSavedSimulationFile.class.getConstructor(SC,S),-13,I(0),GF,false,"saved_simulation",(s,o)-> {return new ummisco.gama.serializer.gaml.GamaSavedSimulationFile(s,((String)o[0]));});
_operator(S("saved_simulation_file"),ummisco.gama.serializer.gaml.GamaSavedSimulationFile.class.getConstructor(SC,S,LI),-13,I(0),GF,false,"saved_simulation",(s,o)-> {return new ummisco.gama.serializer.gaml.GamaSavedSimulationFile(s,((String)o[0]),((IList)o[1]));});
_operator(S("saved_simulation_file"),ummisco.gama.serializer.gaml.GamaSavedSimulationFile.class.getConstructor(SC,S,b),-13,I(0),GF,false,"saved_simulation",(s,o)-> {return new ummisco.gama.serializer.gaml.GamaSavedSimulationFile(s,((String)o[0]),asBool(s,o[1]));});
}public void initializeExperiment()  {
_experiment("memorize",(p, i)->new ummisco.gama.serializer.experiment.ExperimentBackwardAgent(p, i));
}
}