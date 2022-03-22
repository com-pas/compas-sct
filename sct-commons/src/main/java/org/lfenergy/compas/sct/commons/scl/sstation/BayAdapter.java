package org.lfenergy.compas.sct.commons.scl.sstation;

import org.lfenergy.compas.scl2007b4.model.TBay;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;

public class BayAdapter extends SclElementAdapter<VoltageLevelAdapter, TBay> {

    public BayAdapter(VoltageLevelAdapter parentAdapter){super(parentAdapter);}

    public BayAdapter(VoltageLevelAdapter parentAdapter, TBay currentElem){
        super (parentAdapter, currentElem);
    }

    public BayAdapter(VoltageLevelAdapter parentAdapter, String bayName) throws ScdException {
        super(parentAdapter);
        TBay tBay = parentAdapter.getCurrentElem().getBay()
                .stream()
                .filter(bay -> bay.getName().equals(bayName))
                .findFirst()
                .orElseThrow(() -> new ScdException("Unknown Bay name :" + bayName));
        setCurrentElem(tBay);
    }

    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getBay().contains(currentElem);
    }
}
