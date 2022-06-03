// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.apache.commons.lang3.tuple.Pair;
import org.lfenergy.compas.sct.commons.exception.ScdException;

import java.util.List;

public interface IDataParentAdapter extends IDataAdapter {
    <S extends IDataParentAdapter> S getStructuredDataAdapterByName(String sName) throws ScdException;
    <S extends AbstractDAIAdapter> S getDataAdapterByName(String sName) throws ScdException;
    <S extends AbstractDAIAdapter> S addDAI(String name,boolean isUpdatable);
    <S extends IDataParentAdapter> S addSDOI(String sdoNme) ;

    /**
     * Search for the closest match for a list of structured data name (SDIs names = list of SDOs or BDAs names)
     * @param sNames list of either SDO or BDA names
     * @param fromIndex position in the list (0 to list size minus one)
     * @param isBdaNames true if list of BDA names, false otherwise
     * @return a pair of SCL data object Adapter and depth of the list (an index of the list)
     */
    default Pair<? extends IDataAdapter,Integer> findDeepestMatch(List<String> sNames, int fromIndex, boolean isBdaNames){
        int index = -1;
        int sz = sNames.size();
        IDataAdapter diAdapter = null;
        IDataAdapter currSdiAdapter = this;
        for(int currIndex = fromIndex; currIndex < sz; currIndex++){
            try {
                if(currIndex == sz-1 && isBdaNames){
                    currSdiAdapter = ((IDataParentAdapter) currSdiAdapter).getDataAdapterByName(
                            sNames.get(currIndex)
                    );
                } else {
                    currSdiAdapter = ((IDataParentAdapter) currSdiAdapter).getStructuredDataAdapterByName(
                            sNames.get(currIndex)
                    );
                }
                diAdapter = currSdiAdapter;
                index = currIndex;
            } catch (ScdException e) {
                // partial match was found if index >= 0 or unknown data object names
                break;
            }
        }
        return Pair.of(diAdapter,index);
    }
}
