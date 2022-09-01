// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.compas.sct.commons.scl.sstation;

import org.lfenergy.compas.scl2007b4.model.TVoltageLevel;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * A representation of the model object
 * <em><b>{@link VoltageLevelAdapter VoltageLevelAdapter}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ol>
 *   <li>Adapter</li>
 *    <ul>
 *      <li>{@link VoltageLevelAdapter#getBayAdapter(String) <em>Returns the value of the <b>BayAdapter </b>reference object By Name</em>}</li>
 *      <li>{@link VoltageLevelAdapter#streamBayAdapters <em>Returns the value of the <b>BayAdapter </b>reference object as stream</em>}</li>
 *    </ul>
 *   <li>Principal functions</li>
 *    <ul>
 *      <li>{@link VoltageLevelAdapter#addPrivate <em>Add <b>TPrivate </b>under this object</em>}</li>
 *      <li>{@link VoltageLevelAdapter#elementXPath <em>Returns the <b>XPATH </b>for this object</em>}</li>
 *    </ul>
 *   <p>
 *      <b>XPATH Example :</b>
 *      <pre>
 *          VoltageLevel[@name="VOLTAGE_LEVEL"]
 *      </pre>
 *   </p>
 *  @see org.lfenergy.compas.scl2007b4.model.TBay
 *  @see org.lfenergy.compas.scl2007b4.model.TCompasICDHeader
 *  @see <a href="https://github.com/com-pas/compas-sct/issues/124" target="_blank">Issue !124 (update LNode iedName)</a>
 */
public class VoltageLevelAdapter extends SclElementAdapter<SubstationAdapter, TVoltageLevel> {

    public VoltageLevelAdapter(SubstationAdapter parentAdapter) {super(parentAdapter);}

    public VoltageLevelAdapter(SubstationAdapter substationAdapter, TVoltageLevel currentElem){
        super(substationAdapter, currentElem);
    }

    public VoltageLevelAdapter(SubstationAdapter parentAdapter, String vLevelName) throws ScdException {
        super(parentAdapter);
        TVoltageLevel tVoltageLevel = parentAdapter.getCurrentElem().getVoltageLevel()
                .stream()
                .filter(vLevel -> vLevel.getName().equals(vLevelName))
                .findFirst()
                .orElseThrow(() -> new ScdException("Unknown VoltageLevel name :" + vLevelName));
        setCurrentElem(tVoltageLevel);
    }

    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getVoltageLevel().contains(currentElem);
    }

    @Override
    protected String elementXPath() {
        return String.format("VoltageLevel[%s]", Utils.xpathAttributeFilter("name", currentElem.isSetName() ? currentElem.getName() : null));
    }

    public Optional<BayAdapter> getBayAdapter(String bayName) {
        return currentElem.getBay()
                .stream()
                .filter(tBay -> tBay.getName().equals(bayName))
                .map(tBay -> new BayAdapter(this, tBay))
                .findFirst();
    }

    public Stream<BayAdapter> streamBayAdapters() {
        if (!currentElem.isSetBay()){
            return Stream.empty();
        }
        return currentElem.getBay().stream().map(tBay -> new BayAdapter(this, tBay));
    }

}
