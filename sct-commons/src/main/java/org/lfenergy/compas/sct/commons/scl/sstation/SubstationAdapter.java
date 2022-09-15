// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.sstation;

import org.lfenergy.compas.scl2007b4.model.TSubstation;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * A representation of the model object
 * <em><b>{@link SubstationAdapter SubstationAdapter}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ol>
 *   <li>Adapter</li>
 *    <ul>
 *      <li>{@link SubstationAdapter#getVoltageLevelAdapter <em>Returns the value of the <b>VoltageLevelAdapter </b>reference object By Name</em>}</li>
 *      <li>{@link SubstationAdapter#streamVoltageLevelAdapters <em>Returns the value of the <b>VoltageLevelAdapter </b>reference object as stream</em>}</li>
 *    </ul>
 *   <li>Principal functions</li>
 *    <ul>
 *      <li>{@link SubstationAdapter#addPrivate <em>Add <b>TPrivate </b>under this object</em>}</li>
 *      <li>{@link SubstationAdapter#elementXPath <em>Returns the <b>XPATH </b>for this object</em>}</li>
 *    </ul>
 *   <p>
 *       <b>XPATH Example :</b>
 *       <pre>
 *           Substation[@name="SUBSTATION"]
 *       </pre>
 *   </p>
 * @see org.lfenergy.compas.scl2007b4.model.TVoltageLevel
 * @see org.lfenergy.compas.scl2007b4.model.TCompasICDHeader
 */
public class SubstationAdapter extends SclElementAdapter<SclRootAdapter, TSubstation> {

    /**
     * Constructor
     * @param parentAdapter Parent container reference
     */
    public SubstationAdapter(SclRootAdapter parentAdapter) {
        super(parentAdapter);
    }

    /**
     * Constructor
     * @param parentAdapter Parent container reference
     * @param currentElem Current reference
     */
    public SubstationAdapter(SclRootAdapter parentAdapter, TSubstation currentElem) {
        super(parentAdapter, currentElem);
    }

    /**
     * Constructor
     * @param parentAdapter Parent container reference
     * @param ssName Substation name reference
     */
    public SubstationAdapter(SclRootAdapter parentAdapter, String ssName) throws ScdException {
        super(parentAdapter);
        TSubstation tSubstation = parentAdapter.getCurrentElem().getSubstation()
                .stream()
                .filter(subst -> subst.getName().equals(ssName))
                .findFirst()
                .orElseThrow(() -> new ScdException("Unknown Substation name :" + ssName));
        setCurrentElem(tSubstation);
    }

    /**
     * Check if node is child of the reference node
     * @return link parent child existence
     */
    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getSubstation().contains(currentElem);
    }

    /**
     * Returns XPath path to current Substation
     * @return path to current Substation
     */
    @Override
    protected String elementXPath() {
        return String.format("Substation[%s]", Utils.xpathAttributeFilter("name", currentElem.isSetName() ? currentElem.getName() : null));
    }

    /**
     * Gets Voltage Level from current Substation
     * @param vLevelName name of Voltage Level to get
     * @return optional of <em>VoltageLevelAdapter</em> object
     */
    public Optional<VoltageLevelAdapter> getVoltageLevelAdapter(String vLevelName) {
        return currentElem.getVoltageLevel()
                .stream()
                .filter(tVoltageLevel -> tVoltageLevel.getName().equals(vLevelName))
                .map(tVoltageLevel -> new VoltageLevelAdapter(this, tVoltageLevel))
                .findFirst();
    }

    /**
     * Gets Voltage Level in Stream from current Substation
     * @return Stream of <em>VoltageLevelAdapter</em> object from Substation
     */
    public Stream<VoltageLevelAdapter> streamVoltageLevelAdapters() {
        if (!currentElem.isSetVoltageLevel()){
            return Stream.empty();
        }
        return currentElem.getVoltageLevel().stream().map(tVoltageLevel -> new VoltageLevelAdapter(this, tVoltageLevel));
    }

}
