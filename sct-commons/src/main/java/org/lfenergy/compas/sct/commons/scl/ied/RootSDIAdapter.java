// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.lfenergy.compas.scl2007b4.model.TDAI;
import org.lfenergy.compas.scl2007b4.model.TSDI;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.util.Utils;


/**
 * A representation of the model object
 * <em><b>{@link org.lfenergy.compas.sct.commons.scl.ied.RootSDIAdapter DOIAdapter}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ol>
 *   <li>Adapter</li>
 *    <ul>
 *      <li>{@link RootSDIAdapter#getStructuredDataAdapterByName(String) <em>Returns the value of the <b>Child Adapter </b>object reference</em>}</li>
 *      <li>{@link RootSDIAdapter#getDataAdapterByName(String) <em>Returns the value of the <b>Child Adapter </b>object reference By Name</em>}</li>
 *    </ul>
 *   <li>Principal functions</li>
 *    <ul>
 *      <li>{@link RootSDIAdapter#addDAI <em>Add <b>TDAI </b> under this object</em>}</li>
 *      <li>{@link RootSDIAdapter#addSDOI <em>Add <b>TSDI </b> under this object</em>}</li>
 *      <li>{@link RootSDIAdapter#addPrivate <em>Add <b>TPrivate </b> under this object</em>}</li>
 *    </ul>
 * </ol>
 *
 * @see org.lfenergy.compas.scl2007b4.model.TSDI
 */
public class RootSDIAdapter extends SclElementAdapter<DOIAdapter, TSDI> implements IDataParentAdapter{

    /**
     * Constructor
     * @param parentAdapter Parent container reference
     * @param currentElem Current reference
     */
    protected RootSDIAdapter(DOIAdapter parentAdapter, TSDI currentElem) {
        super(parentAdapter, currentElem);
    }

    /**
     * Check if node is child of the reference node
     * @return link parent child existence
     */
    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getSDIOrDAI().contains(currentElem);
    }

    @Override
    protected String elementXPath() {
        return String.format("SDI[%s]",
                Utils.xpathAttributeFilter("name", currentElem.isSetName() ? currentElem.getName() : null));
    }

    /**
     * Gets in current root SDI specific SDI by its name
     * @param name name of SDI to get
     * @return <em>SDIAdapter</em> object
     * @throws ScdException throws when DAI unknown in current root SDI
     */
    public SDIAdapter getStructuredDataAdapterByName(String name) throws ScdException {
        return currentElem.getSDIOrDAI()
                .stream()
                .filter(tUnNaming -> tUnNaming.getClass().equals(TSDI.class))
                .map(TSDI.class::cast)
                .filter(tsdi -> tsdi.getName().equals(name))
                .map(tsdi -> new SDIAdapter(this,tsdi))
                .findFirst()
                .orElseThrow(() -> new ScdException(
                        String.format("Unknown DAI (%s) in Root SDI (%s)", name, currentElem.getName())
                ));
    }

    /**
     * Gets in current root SDI specific DAI by its name
     * @param sName name of DAI to get
     * @return <em>DAIAdapter</em> object
     * @throws ScdException throws when DAI unknown in current root SDI
     */
    @Override
    public DAIAdapter getDataAdapterByName(String sName) throws ScdException {
        return currentElem.getSDIOrDAI()
                .stream()
                .filter(tUnNaming -> tUnNaming.getClass().equals(TDAI.class))
                .map(TDAI.class::cast)
                .filter(tdai -> tdai.getName().equals(sName))
                .map(tdai -> new DAIAdapter(this,tdai))
                .findFirst()
                .orElseThrow(() -> new ScdException(
                        String.format("Unknown DAI (%s) in Root SDI (%s)", sName, currentElem.getName())
                ));
    }

    /**
     * Adds in current root SDI a specific DAI
     * @param name name of DAI to add
     * @param isUpdatable updatability state of DAI
     * @return <em>DAIAdapter</em> object
     */
    @Override
    public DAIAdapter addDAI(String name, boolean isUpdatable) {
        TDAI tdai = new TDAI();
        tdai.setName(name);
        tdai.setValImport(isUpdatable);
        currentElem.getSDIOrDAI().add(tdai);
        return new DAIAdapter(this,tdai);
    }

    /**
     * Adds in current root SDI a specific SDOI
     * @param sdoName name of SDOI to add
     * @return <em>IDataParentAdapter</em> object as added SDOI
     */
    @Override
    public IDataParentAdapter addSDOI(String sdoName) {
        TSDI tsdi = new TSDI();
        tsdi.setName(sdoName);
        currentElem.getSDIOrDAI().add(tsdi);
        return new SDIAdapter(this,tsdi);
    }

    /**
     * A representation of the model object
     * <em><b>{@link org.lfenergy.compas.sct.commons.scl.ied.RootSDIAdapter.DAIAdapter RootSDIAdapter.DAIAdapter}</b></em>.
     * <p>
     * The following features are supported:
     * </p>
     * <ol>
     *   <li>Adapter</li>
     *    <ul>
     *      <li>{@link DAIAdapter#getStructuredDataAdapterByName(String) <em>Returns the value of the <b>Child Adapter </b>object reference</em>}</li>
     *      <li>{@link DAIAdapter#getDataAdapterByName(String) <em>Returns the value of the <b>Child Adapter </b>object reference By Name</em>}</li>
     *    </ul>
     *   <li>Principal functions</li>
     *    <ul>
     *      <li>{@link DAIAdapter#addDAI <em>Add <b>TDAI </b>under this object</em>}</li>
     *      <li>{@link DAIAdapter#addSDOI <em>Add <b>TSDI </b>under this object</em>}</li>
     *    </ul>
     *    </ul>
     * </ol>
     *
     * @see org.lfenergy.compas.scl2007b4.model.TDAI
     */
    public static class DAIAdapter extends AbstractDAIAdapter<RootSDIAdapter> {

        public DAIAdapter(RootSDIAdapter rootSDIAdapter, TDAI tdai) {
            super(rootSDIAdapter,tdai);
        }

        @Override
        protected boolean amChildElementRef() {
            return parentAdapter.getCurrentElem().getSDIOrDAI().contains(currentElem);
        }

        @Override
        protected String elementXPath() {
            return String.format("DAI[%s]",
                    Utils.xpathAttributeFilter("name", currentElem.isSetName() ? currentElem.getName() : null));
        }

    }
}
