// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;


import org.apache.commons.lang3.tuple.Pair;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.*;
import org.lfenergy.compas.sct.commons.scl.LDeviceActivation;
import org.lfenergy.compas.sct.commons.scl.ObjectReference;
import org.lfenergy.compas.sct.commons.scl.PrivateService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.lfenergy.compas.sct.commons.util.CommonConstants.*;

/**
 * A representation of the model object
 * <em><b>{@link org.lfenergy.compas.sct.commons.scl.ied.LN0Adapter LN0Adapter}</b></em>.
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
 *      <li>{@link LN0Adapter#getDAI <em>Returns the value of the <b>ResumedDataTemplate </b> containment reference By filter</em>}</li>
 *      <li>{@link LN0Adapter#getDAIValues(ResumedDataTemplate) <em>Returns <b>DAI (sGroup, value) </b> containment reference list By <b>ResumedDataTemplate </b> filter</em>}</li>
 *
 *      <li>{@link LN0Adapter#getDataSetByName(String) <em>Returns the value of the <b>TDataSet </b>object reference By the value of the <b>name </b>attribute </em>}</li>
 *
 *      <li>{@link LN0Adapter#getControlBlocks(List, TServiceType) <em>Returns the value of the <b>ControlBlock </b>containment reference list that match <b>datSet </b> value of given <b>TDataSet</b> </em>}</li>
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
 * @see org.lfenergy.compas.scl2007b4.model.TLN0
 * @see org.lfenergy.compas.sct.commons.scl.ied.AbstractLNAdapter
 */
public class LN0Adapter extends AbstractLNAdapter<LN0> {

    public static final DoTypeName MOD_DO_TYPE_NAME = new DoTypeName(MOD_DO_NAME);
    public static final DaTypeName STVAL_DA_TYPE_NAME = new DaTypeName(STVAL);
    public static final DoTypeName BEHAVIOUR_DO_TYPE_NAME = new DoTypeName(BEHAVIOUR_DO_NAME);
    public static final DaTypeName BEHAVIOUR_DA_TYPE_NAME = getDaTypeNameForBeh();
    /**
     * Constructor
     * @param parentAdapter Parent container reference
     * @param ln0 Current reference
     */
    public LN0Adapter(LDeviceAdapter parentAdapter, LN0 ln0) {
        super(parentAdapter,ln0);
    }

    /**
     * Check if node is child of the reference node
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
     * @return <em>LN0.class</em>
     */
    @Override
    protected Class<LN0> getElementClassType() {
        return LN0.class;
    }

    /**
     * Gets LNClass enum value of current LNO
     * @return LNClass value
     */
    public String getLNClass() {
        return TLLN0Enum.LLN_0.value();
    }

    /**
     * Gets LNInst value of current LN0
     * @return <em>""</em>
     */
    @Override
    public String getLNInst() {
        return "";
    }

    /**
     * Gets Prefix value of current LN0
     * @return <em>""</em>
     */
    @Override
    public String getPrefix() {
        return "";
    }

    /**
     * Gets Inputs node as an adapter
     * @return an InputsAdapter
     */
    public InputsAdapter getInputsAdapter(){
        return new InputsAdapter(this, currentElem.getInputs());
    }

    /** Checks if given attibrute corresponds to DataSet or ReportControl or SMVControl or GSEControl in current LN0
     * @param dataAttribute attribute to check
     * @return <em>Boolean</em> value of check result
     */
    @Override
    protected  boolean matchesDataAttributes(String dataAttribute){
        return  super.matchesDataAttributes(dataAttribute) ||
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
     * Construct ResumedDataTemplate object with DO and DA attributes
     *
     * @param doTypeName <em><b>DoTypeName </b> object</em>
     * @param daTypeName <em><b>DaTypeName </b> object</em>
     * @return ResumedDataTemplate
     */
    private ResumedDataTemplate createDaiFilter(DoTypeName doTypeName, DaTypeName daTypeName) {
        ResumedDataTemplate filter = new ResumedDataTemplate();
        filter.setLnClass(getLNClass());
        filter.setLnInst(getLNInst());
        filter.setPrefix(getPrefix());
        filter.setLnType(getLnType());
        filter.setDoName(doTypeName);
        filter.setDaName(daTypeName);
        return filter;
    }

    /**
     * Verify and update LDevice status in parent Node
     * @param iedNameLDeviceInstList pair of Ied name and LDevice inst attributes
     * @return Set of Errors
     */
    public Optional<SclReportItem> updateLDeviceStatus(List<Pair<String, String>> iedNameLDeviceInstList) {
        LDeviceActivation lDeviceActivation = new LDeviceActivation(iedNameLDeviceInstList);
        final String iedName = getParentAdapter().getParentAdapter().getName();
        final String ldInst = getParentAdapter().getInst();
        ResumedDataTemplate daiBehFilter = createDaiFilter(BEHAVIOUR_DO_TYPE_NAME, BEHAVIOUR_DA_TYPE_NAME);
            List<ResumedDataTemplate> daiBehList = getDAI(daiBehFilter, false);
        if (daiBehList.isEmpty()) {
            return Optional.of(buildFatalReportItem("The LDevice doesn't have a DO @name='Beh' OR its associated DA@fc='ST' AND DA@name='stVal'"));
        }
        Set<String> enumValues = getEnumValues(daiBehList.get(0).getDaName().getType());
        List<TCompasLDevice> compasLDevicePrivateList = PrivateService.extractCompasPrivates(getParentAdapter().getCurrentElem(), TCompasLDevice.class);
        if (compasLDevicePrivateList.isEmpty()) {
            return Optional.of(buildFatalReportItem("The LDevice doesn't have a Private compas:LDevice."));
        }
        if (!compasLDevicePrivateList.get(0).isSetLDeviceStatus()) {
            return Optional.of(buildFatalReportItem("The Private compas:LDevice doesn't have the attribute 'LDeviceStatus'"));
        }
        TCompasLDeviceStatus compasLDeviceStatus = compasLDevicePrivateList.get(0).getLDeviceStatus();
        Optional<ResumedDataTemplate> optionalModStVal = getDaiModStVal();
        if (optionalModStVal.isEmpty()) {
            return Optional.of(buildFatalReportItem("The LDevice doesn't have a DO @name='Mod'"));
        }
        ResumedDataTemplate newDaModToSetInLN0 = optionalModStVal.get();
        String initialValue = newDaModToSetInLN0.findFirstValue().orElse("");
        lDeviceActivation.checkLDeviceActivationStatus(iedName, ldInst, compasLDeviceStatus, enumValues);
        if(lDeviceActivation.isUpdatable()){
            if(!initialValue.equals(lDeviceActivation.getNewVal())) {
                newDaModToSetInLN0.setVal(lDeviceActivation.getNewVal());
                updateDAI(newDaModToSetInLN0);
            }
        }else {
            if(lDeviceActivation.getErrorMessage() != null) {
                return Optional.of(buildFatalReportItem(lDeviceActivation.getErrorMessage()));
            }
        }
        return Optional.empty();
    }

    public Optional<String> getLDeviceStatus() {
        return getDaiModStVal()
            .flatMap(ResumedDataTemplate::findFirstValue);
    }

    private Optional<ResumedDataTemplate> getDaiModStVal() {
        ResumedDataTemplate daiModFilter = createDaiFilter(MOD_DO_TYPE_NAME, STVAL_DA_TYPE_NAME);
        return getDAI(daiModFilter, false).stream()
            .findFirst();
    }

    private static DaTypeName getDaTypeNameForBeh() {
        DaTypeName daTypeNameBeh = new DaTypeName();
        daTypeNameBeh.setName(STVAL);
        daTypeNameBeh.setBType(TPredefinedBasicTypeEnum.ENUM);
        daTypeNameBeh.setFc(TFCEnum.ST);
        return daTypeNameBeh;
    }
}
