package gaml.additions.gaml;

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
	initializeFile();
}public void initializeVars()  {
_field(msi.gama.lang.gaml.resource.GamlFile.class,"experiments",(s, o)->((msi.gama.lang.gaml.resource.GamlFile)o[0]).getExperiments(s),5,msi.gama.lang.gaml.resource.GamlFile.class,5,4,0);
_field(msi.gama.lang.gaml.resource.GamlFile.class,"tags",(s, o)->((msi.gama.lang.gaml.resource.GamlFile)o[0]).getTags(s),5,msi.gama.lang.gaml.resource.GamlFile.class,5,4,0);
_field(msi.gama.lang.gaml.resource.GamlFile.class,"uses",(s, o)->((msi.gama.lang.gaml.resource.GamlFile)o[0]).getUses(s),5,msi.gama.lang.gaml.resource.GamlFile.class,5,4,0);
_field(msi.gama.lang.gaml.resource.GamlFile.class,"valid",(s, o)->((msi.gama.lang.gaml.resource.GamlFile)o[0]).isValid(s),3,msi.gama.lang.gaml.resource.GamlFile.class,3,0,0);
_field(msi.gama.lang.gaml.resource.GamlFile.class,"imports",(s, o)->((msi.gama.lang.gaml.resource.GamlFile)o[0]).getImports(s),5,msi.gama.lang.gaml.resource.GamlFile.class,5,4,0);
}public void initializeFile() throws SecurityException, NoSuchMethodException {
_file("gaml",msi.gama.lang.gaml.resource.GamlFile.class,(s,o)-> {return new msi.gama.lang.gaml.resource.GamlFile(s,((String)o[0]));},5,1,14,S("gaml","experiment"));
_operator(S("is_gaml"),null,I(0),B,true,3,0,0,0,(s,o)-> { return GamaFileType.verifyExtension("gaml",(String)o[0]);}, false);
_operator(S("gaml_file"),msi.gama.lang.gaml.resource.GamlFile.class.getConstructor(SC,S),-13,I(0),GF,false,"gaml",(s,o)-> {return new msi.gama.lang.gaml.resource.GamlFile(s,((String)o[0]));});
_operator(S("gaml_file"),msi.gama.lang.gaml.resource.GamlFile.class.getConstructor(SC,S,S,S),-13,I(0),GF,false,"gaml",(s,o)-> {return new msi.gama.lang.gaml.resource.GamlFile(s,((String)o[0]),((String)o[1]),((String)o[2]));});
}
}