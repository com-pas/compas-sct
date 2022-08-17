// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.lfenergy.compas.scl2007b4.model.TDAI;
import org.lfenergy.compas.scl2007b4.model.TSDI;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;

import java.util.Objects;

public class SDIAdapter extends SclElementAdapter<SclElementAdapter, TSDI> implements IDataParentAdapter {

    protected SDIAdapter(SclElementAdapter parentAdapter, TSDI currentElem) {
        super(parentAdapter, currentElem);
    }

    @Override
    protected boolean amChildElementRef() {
        if(parentAdapter.getClass().equals(RootSDIAdapter.class)){
            return RootSDIAdapter.class.cast(parentAdapter).getCurrentElem().getSDIOrDAI().contains(currentElem);
        }
        return SDIAdapter.class.cast(parentAdapter).getCurrentElem().getSDIOrDAI().contains(currentElem);
    }

    @Override
    public SDIAdapter getStructuredDataAdapterByName(String sName) throws ScdException {
        return currentElem.getSDIOrDAI()
                .stream()
                .filter(tUnNaming -> tUnNaming.getClass().equals(TSDI.class))
                .map(TSDI.class::cast)
                .filter(tsdi -> Objects.equals(tsdi.getName(),sName))
                .map(tsdi -> new SDIAdapter(this,tsdi))
                .findFirst()
                .orElseThrow(
                        () -> new ScdException(
                                String.format("Unknown SDI (%s) in SDI (%s)", sName, currentElem.getName())
                        )
                );
    }


    @Override
    public DAIAdapter getDataAdapterByName(String sName) throws ScdException {
        return currentElem.getSDIOrDAI()
            .stream()
            .filter(tUnNaming -> tUnNaming.getClass().equals(TDAI.class))
            .map(TDAI.class::cast)
            .filter(tdai -> Objects.equals(tdai.getName(),sName))
            .map(tdai -> new DAIAdapter(this,tdai))
            .findFirst()
            .orElseThrow(
                    () -> new ScdException(
                            String.format("Unknown DAI (%s) in SDI (%s)",sName, currentElem.getName())
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
    public SDIAdapter addSDOI(String sdoName) {
        TSDI tsdi = new TSDI();
        tsdi.setName(sdoName);
        currentElem.getSDIOrDAI().add(tsdi);
        return new SDIAdapter(this,tsdi);
    }

    public static class DAIAdapter extends AbstractDAIAdapter<SDIAdapter> {

        protected DAIAdapter(SDIAdapter parentAdapter, TDAI currentElem) {
            super(parentAdapter, currentElem);
        }

        @Override
        protected boolean amChildElementRef() {
            return parentAdapter.getCurrentElem().getSDIOrDAI().contains(currentElem);
        }

    }
}
