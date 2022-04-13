// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.lfenergy.compas.scl2007b4.model.TDAI;
import org.lfenergy.compas.scl2007b4.model.TPrivate;
import org.lfenergy.compas.scl2007b4.model.TSDI;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;


public class RootSDIAdapter extends SclElementAdapter<DOIAdapter, TSDI> implements IDataParentAdapter{

    protected RootSDIAdapter(DOIAdapter parentAdapter, TSDI currentElem) {
        super(parentAdapter, currentElem);
    }

    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getSDIOrDAI().contains(currentElem);
    }

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

    @Override
    public DAIAdapter addDAI(String name, boolean isUpdatable) {
        TDAI tdai = new TDAI();
        tdai.setName(name);
        tdai.setValImport(isUpdatable);
        currentElem.getSDIOrDAI().add(tdai);
        return new DAIAdapter(this,tdai);
    }

    @Override
    public IDataParentAdapter addSDOI(String sdoName) {
        TSDI tsdi = new TSDI();
        tsdi.setName(sdoName);
        currentElem.getSDIOrDAI().add(tsdi);
        return new SDIAdapter(this,tsdi);
    }

    @Override
    protected void addPrivate(TPrivate tPrivate) {
        currentElem.getPrivate().add(tPrivate);
    }

    public static class DAIAdapter extends AbstractDAIAdapter<RootSDIAdapter> {

        public DAIAdapter(RootSDIAdapter rootSDIAdapter, TDAI tdai) {
            super(rootSDIAdapter,tdai);
        }

        @Override
        protected boolean amChildElementRef() {
            return parentAdapter.getCurrentElem().getSDIOrDAI().contains(currentElem);
        }

        @Override
        protected void addPrivate(TPrivate tPrivate) {
            currentElem.getPrivate().add(tPrivate);
        }
    }
}
