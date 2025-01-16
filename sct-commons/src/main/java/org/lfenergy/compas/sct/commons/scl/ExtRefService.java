/*
 * // SPDX-FileCopyrightText: 2023 2024 RTE FRANCE
 * //
 * // SPDX-License-Identifier: Apache-2.0
 */

package org.lfenergy.compas.sct.commons.scl;

import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.scl2007b4.model.TInputs;
import org.lfenergy.compas.scl2007b4.model.TLDevice;
import org.lfenergy.compas.scl2007b4.model.TLLN0Enum;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class ExtRefService {

    /**
     * List all ExtRefs in this LDevice
     *
     * @return list of ExtRefs. List is modifiable.
     */
    public Stream<TExtRef> getExtRefs(TLDevice tlDevice) {
        return getInputs(tlDevice)
                .filter(TInputs::isSetExtRef)
                .stream()
                .flatMap(tInputs -> tInputs.getExtRef().stream());
    }

    /**
     * Debind ExtRef
     *
     * @param extRef to debind
     */
    public void clearExtRefBinding(TExtRef extRef) {
        extRef.setIedName(null);
        extRef.setLdInst(null);
        extRef.setPrefix(null);
        extRef.setLnInst(null);
        extRef.setDoName(null);
        extRef.setDaName(null);
        extRef.setServiceType(null);
        extRef.setSrcLDInst(null);
        extRef.setSrcPrefix(null);
        extRef.setSrcLNInst(null);
        extRef.setSrcCBName(null);
        extRef.unsetLnClass();
        extRef.unsetSrcLNClass();
    }

    /**
     * Checks if two ExtRefs fed by same Control Block
     *
     * @param t1 extref to compare
     * @param t2 extref to compare
     * @return true if the two ExtRef are fed by same Control Block, otherwise false
     */
    public boolean isExtRefFeedBySameControlBlock(TExtRef t1, TExtRef t2) {
        String srcLNClass1 = (t1.isSetSrcLNClass()) ? t1.getSrcLNClass().get(0) : TLLN0Enum.LLN_0.value();
        String srcLNClass2 = (t2.isSetSrcLNClass()) ? t2.getSrcLNClass().get(0) : TLLN0Enum.LLN_0.value();
        return Utils.equalsOrBothBlank(t1.getIedName(), t2.getIedName())
                && Utils.equalsOrBothBlank(t1.getSrcLDInst(), t2.getSrcLDInst())
                && srcLNClass1.equals(srcLNClass2)
                && Utils.equalsOrBothBlank(t1.getSrcLNInst(), t2.getSrcLNInst())
                && Utils.equalsOrBothBlank(t1.getSrcPrefix(), t2.getSrcPrefix())
                && Utils.equalsOrBothBlank(t1.getSrcCBName(), t2.getSrcCBName())
                && Objects.equals(t1.getServiceType(), t2.getServiceType());
    }

    /**
     * Remove ExtRef which are fed by same Control Block
     *
     * @return list ExtRefs without duplication
     */
    public List<TExtRef> filterDuplicatedExtRefs(List<TExtRef> tExtRefs) {
        List<TExtRef> filteredList = new ArrayList<>();
        tExtRefs.forEach(tExtRef -> {
            if (filteredList.stream().noneMatch(t -> isExtRefFeedBySameControlBlock(tExtRef, t)))
                filteredList.add(tExtRef);
        });
        return filteredList;
    }

    private Optional<TInputs> getInputs(TLDevice tlDevice){
        if (!tlDevice.isSetLN0() || !tlDevice.getLN0().isSetInputs()) {
            return Optional.empty();
        }
        return Optional.of(tlDevice.getLN0().getInputs());
    }

}
