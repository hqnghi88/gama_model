/*******************************************************************************************************
 *
 * ReferenceGraph.java, in ummisco.gama.serialize, is part of the source code of the
 * GAMA modeling and simulation platform (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package ummisco.gama.serializer.gamaType.reference;

import java.util.ArrayList;

import msi.gama.kernel.simulation.SimulationAgent;
import msi.gama.util.IReference;
import msi.gama.util.IReference.AgentAttribute;
import msi.gama.util.graph.GamaGraph;
import msi.gaml.types.Types;
import ummisco.gama.serializer.gamaType.reduced.GamaGraphReducer;

/**
 * The Class ReferenceGraph.
 */
public class ReferenceGraph extends GamaGraph implements IReference {
//	IAgent agt;
/** The agt attr. */
//	String attributeName;
	ArrayList<AgentAttribute> agtAttr;
	
	/** The graph reducer. */
	GamaGraphReducer graphReducer;

	/**
	 * Instantiates a new reference graph.
	 *
	 * @param g the g
	 */
	public ReferenceGraph(GamaGraphReducer g) {
		super(null, Types.NO_TYPE, Types.NO_TYPE);
		agtAttr = new ArrayList<AgentAttribute>();		
		graphReducer = g;
	}	
	
//	public IAgent getAgt() {return agt;}
//	public String getAttributeName() {return attributeName;}
		
//	public void setAgentAndAttrName(IAgent _agt, String attrName) {
//		agt = _agt;
//		attributeName = attrName;
//	}

	@Override
	public Object constructReferencedObject(SimulationAgent sim) {

	//	graphReducer.setValuesGraphReducer((GamaMap)IReference.getObjectWithoutReference(graphReducer.getValuesGraphReducer(),sim));
	//	graphReducer.setEdgesWeightsGraphReducer((GamaMap)IReference.getObjectWithoutReference(graphReducer.getWeightsGraphReducer(),sim));
		
	//	Map<Object,Object> mapWithReferences = mapReducer.getValues();
	//	HashMap<Object,Object> mapWithoutReferences = new HashMap<>();
		
	//	for(Map.Entry<Object,Object> e : mapWithReferences.entrySet()) {
	//		mapWithoutReferences.put(
	//				IReference.getObjectWithoutReference(e.getKey(),sim), 
	//				IReference.getObjectWithoutReference(e.getValue(),sim));
	//	}
		
	//	mapReducer.setValues(mapWithoutReferences);

		graphReducer.unreferenceReducer(sim);
		return graphReducer.constructObject(sim.getScope());
	}	
		
	@Override
	public ArrayList<AgentAttribute> getAgentAttributes() {
		return agtAttr;
	}	
	
    public boolean equals(Object o) {
        if (o == this)
            return true;
        else
        	return false;
    }	
}
