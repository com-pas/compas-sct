// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ln;


import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.*;
import org.lfenergy.compas.sct.commons.scl.ObjectReference;
import org.lfenergy.compas.sct.commons.scl.ied.InputsAdapter;
import org.lfenergy.compas.sct.commons.scl.ldevice.LDeviceAdapter;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.lfenergy.compas.sct.commons.util.CommonConstants.*;

/**
 * A representation of the model object
 * <em><b>{@link LN0Adapter LN0Adapter}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ol>
 *   <li>Adapter</li>
 *    <ul>
 *      <li>{@link LN0Adapter#getDataTypeTemplateAdapter <em>Returns the value of the <b>DataTypeTemplateAdapter </b>reference object</em>}</li>
 *    </ul>
 *   <li>Principal functions</li>
 *    <ul>
 *      <li>{@link LN0Adapter#getLNInst <em>Returns the value of the <b>inst </b>attribute</em>}</li>
 *      <li>{@link LN0Adapter#getLNClass <em>Returns the value of the <b>lnClass </b>attribute</em>}</li>
 *      <li>{@link LN0Adapter#getLnType <em>Returns the value of the <b>lnTYpe </b>attribute</em>}</li>
 *      <li>{@link LN0Adapter#getLNodeName <em>Returns the logical node name <b>LNName = prefix + lnClass + lnInst</b></em>}</li>
 *
 *      <li>{@link LN0Adapter#getExtRefs() <em>Returns the value of the <b>TExtRef </b>containment reference list</em>}</li>
 *      <li>{@link LN0Adapter#getExtRefs(ExtRefSignalInfo) <em>Returns the value of the <b>TExtRef </b>containment reference list By <b>ExtRefSignalInfo <b></b></b></em>}</li>
 *      <li>{@link LN0Adapter#isExtRefExist(ExtRefSignalInfo) <em></b>throws exception when specified signal not found in LN target</b></em>}</li>
 *
 *      <li>{@link LN0Adapter#getDAI <em>Returns the value of the <b>DataAttributeRef </b> containment reference By filter</em>}</li>
 *      <li>{@link LN0Adapter#getDAIValues(DataAttributeRef) <em>Returns <b>DAI (sGroup, value) </b> containment reference list By <b>DataAttributeRef </b> filter</em>}</li>
 *
 *      <li>{@link LN0Adapter#addPrivate <em>Add <b>TPrivate </b>under this object</em>}</li>
 *      <li>{@link LN0Adapter#removeAllControlBlocksAndDatasets() <em>Remove all <b>ControlBlock</b></em>}</li>
 *    </ul>
 *   <li>Checklist functions</li>
 *    <ul>
 *      <li>{@link LN0Adapter#matches(ObjectReference) <em>Check whether the section <b>DataName </b> of given <b>ObjectReference </b> match current LN0Adapter DataName</em>}</li>
 *      <li>{@link LN0Adapter#matchesDataAttributes(String) <em>Check whether the section <b>DataName </b> of given <b>ObjectReference </b> match current LNAdapter DataName Excluding DataName from DataTypeTemplat</em>}</li>
 *    </ul>
 * </ol>
 * <br/>
 *  <pre>
 *      <b>ObjectReference</b>: LDName/LNName.DataName[.DataName[…]].DataAttributeName[.DAComponentName[ ….]]
 *      <b>LDName</b> = "name" attribute of IEDName element + "inst" attribute of LDevice element
 *      <b>LNName</b> = "prefix" + "lnClass" + "lnInst"
 *  </pre>
 *
 * @see org.lfenergy.compas.scl2007b4.model.TLN0
 * @see AbstractLNAdapter
 */
public class LN0Adapter extends AbstractLNAdapter<LN0> {

    private static final Pattern LDEFP_DIGITAL_CHANNEL_PATTERN = Pattern.compile("DYN_LDEPF_DIGITAL CHANNEL \\d+_\\d+_BOOLEAN");

    /**
     * Constructor
     *
     * @param parentAdapter Parent container reference
     * @param ln0           Current reference
     */
    public LN0Adapter(LDeviceAdapter parentAdapter, LN0 ln0) {
        super(parentAdapter, ln0);
    }

    /**
     * Check if node is child of the reference node
     *
     * @return link parent child existence
     */
    @Override
    protected boolean amChildElementRef() {
        return currentElem == parentAdapter.getCurrentElem().getLN0();
    }

    @Override
    protected String elementXPath() {
        return "LN0";
    }


    /**
     * Gets current LN0 class type
     *
     * @return <em>LN0.class</em>
     */
    @Override
    protected Class<LN0> getElementClassType() {
        return LN0.class;
    }

    /**
     * Gets LNClass enum value of current LNO
     *
     * @return LNClass value
     */
    public String getLNClass() {
        return TLLN0Enum.LLN_0.value();
    }

    /**
     * Gets LNInst value of current LN0
     *
     * @return <em>""</em>
     */
    @Override
    public String getLNInst() {
        return "";
    }

    /**
     * Gets Prefix value of current LN0
     *
     * @return <em>""</em>
     */
    @Override
    public String getPrefix() {
        return "";
    }

    /**
     * Gets Inputs node as an adapter
     *
     * @return an InputsAdapter
     */
    public InputsAdapter getInputsAdapter() {
        return new InputsAdapter(this, currentElem.getInputs());
    }

    /**
     * Checks if given attibrute corresponds to DataSet or ReportControl or SMVControl or GSEControl in current LN0
     *
     * @param dataAttribute attribute to check
     * @return <em>Boolean</em> value of check result
     */
    @Override
    protected boolean matchesDataAttributes(String dataAttribute) {
        return super.matchesDataAttributes(dataAttribute) ||
                currentElem.getSampledValueControl().stream().anyMatch(smp -> smp.getName().equals(dataAttribute)) ||
                currentElem.getGSEControl().stream().anyMatch(gse -> gse.getName().equals(dataAttribute));
    }

    /**
     * Remove all SMVControl and GSEControl of current LN0
     */
    @Override
    public void removeAllControlBlocksAndDatasets() {
        super.removeAllControlBlocksAndDatasets();
        currentElem.unsetGSEControl();
        currentElem.unsetSampledValueControl();
    }

    /**
     * Update DAIs of DO InRef in all LN0 of the SCD using matching ExtRef information.
     *
     * @return A list of SclReport Objects that contain errors
     */
    public List<SclReportItem> updateDoInRef() {
        return getDOIAdapters().stream()
                .filter(doiAdapter -> doiAdapter.getCurrentElem().isSetName()
                        && doiAdapter.getCurrentElem().getName().startsWith(INREF_PREFIX)
                        && doiAdapter.findDataAdapterByName(PURPOSE_DA_NAME).isPresent())
                .map(doiAdapter -> doiAdapter.getDataAdapterByName(PURPOSE_DA_NAME).getCurrentElem().getVal().stream()
                        .findFirst()
                        .map(tVal -> doiAdapter.updateDaiFromExtRef(getExtRefsBoundToInRef(tVal.getValue())))
                        .orElse(List.of(SclReportItem.warning(getXPath(), "The DOI %s can't be bound with an ExtRef".formatted(getXPath()))))
                )
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private List<TExtRef> getExtRefsBoundToInRef(String desc) {
        List<TExtRef> boundExtRefs = getBoundExtRefsByDesc(desc);
        // Special case for LDEPF DIGITAL CHANNEL of type BOOLEAN RSR-1048
        if (boundExtRefs.isEmpty() && LDEFP_DIGITAL_CHANNEL_PATTERN.matcher(desc).matches()) {
            String descWithoutType = desc.substring(0, desc.lastIndexOf("_"));
            return getBoundExtRefsByDesc(descWithoutType);
        }
        return boundExtRefs;
    }

    private List<TExtRef> getBoundExtRefsByDesc(String desc) {
        return getExtRefs().stream()
                .filter(tExtRef -> tExtRef.isSetIedName() && tExtRef.isSetLdInst() && tExtRef.isSetLnClass() && tExtRef.isSetDoName() &&
                        tExtRef.isSetDesc() && tExtRef.getDesc().contains(desc))
                .toList();
    }
}
