// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;


public class DOIAdapter extends SclElementAdapter<AbstractLNAdapter<? extends TAnyLN>, TDOI> implements IDataParentAdapter {

    protected DOIAdapter(AbstractLNAdapter<? extends TAnyLN> parentAdapter, TDOI currentElem) {
        super(parentAdapter, currentElem);
    }

    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getDOI().contains(currentElem);
    }

    @Override
    public RootSDIAdapter getStructuredDataAdapterByName(String sName) throws ScdException {
        return currentElem.getSDIOrDAI()
                .stream()
                .filter(tUnNaming -> tUnNaming.getClass().equals(TSDI.class))
                .map(TSDI.class::cast)
                .filter(tsdi -> tsdi.getName().equals(sName))
                .map(tsdi -> new RootSDIAdapter(this,tsdi))
                .findFirst()
                .orElseThrow(
                    ()-> new ScdException(
                            String.format("Unknown SDI (%s) in DOI (%s)", sName, currentElem.getName())
                    )
                );
    }

    @Override
    protected void addPrivate(TPrivate tPrivate) {
        currentElem.getPrivate().add(tPrivate);
    }

    @Override
    public DAIAdapter getDataAdapterByName(String daName) throws ScdException {
        return  currentElem.getSDIOrDAI()
                .stream()
                .filter(tUnNaming -> tUnNaming.getClass().equals(TDAI.class))
                .map(TDAI.class::cast)
                .filter(tdai -> tdai.getName().equals(daName))
                .map(tdai -> new DAIAdapter(this,tdai))
                .findFirst()
                .orElseThrow(
                    ()-> new ScdException(
                            String.format("Unknown DAI (%s) in DOI (%s)", daName, currentElem.getName())
                    )
                );
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
    public RootSDIAdapter addSDOI(String sdoName) {
        TSDI tsdi = new TSDI();
        tsdi.setName(sdoName);
        currentElem.getSDIOrDAI().add(tsdi);
        return new RootSDIAdapter(this,tsdi);
    }

    public static class DAIAdapter extends AbstractDAIAdapter<DOIAdapter> {

        protected DAIAdapter(DOIAdapter parentAdapter, TDAI currentElem) {
            super(parentAdapter, currentElem);
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
