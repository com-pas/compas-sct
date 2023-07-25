// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
import org.lfenergy.compas.scl2007b4.model.TCompasLDeviceStatus;
import org.lfenergy.compas.sct.commons.util.LdeviceStatus;

import java.util.List;
import java.util.Set;

/**
 * Common class for all states that define if LDevice should be activated or not
 * regardless of the CompasLDeviceStatus Private, Enum Values of DO 'Beh' and if it's referenced in Substation...LNode or not
 */
@Getter
@Setter
public class LDeviceActivation {

    private final List<Pair<String, String>> iedNameLdInstList;
    private boolean isUpdatable;
    private String newVal;
    private String errorMessage;

    public LDeviceActivation(List<Pair<String, String>> iedNameLdInstList) {
        this.iedNameLdInstList = iedNameLdInstList;
    }

    /**
     * checks whether LDevice status is authorized to be activated or Not
     * @param iedName Ied name value which LDevice appear
     * @param ldInst LDevice inst value
     * @param compasLDeviceStatus Private value
     * @param enumValues enum values
     */
    public void checkLDeviceActivationStatus(String iedName, String ldInst, TCompasLDeviceStatus compasLDeviceStatus, Set<String> enumValues) {
        if (!enumValues.contains(LdeviceStatus.ON.getValue()) && !enumValues.contains(LdeviceStatus.OFF.getValue())) {
            errorMessage = "The LDevice cannot be activated or desactivated because its BehaviourKind Enum contains NOT 'on' AND NOT 'off'.";
        }
        if (!enumValues.contains(LdeviceStatus.ON.getValue()) && enumValues.contains(LdeviceStatus.OFF.getValue())) {
            if (isDeclaredInSubstation(iedName, ldInst)) {
                errorMessage = "The LDevice cannot be set to 'on' but has been selected into SSD.";
            } else {
                isUpdatable = true;
                newVal = LdeviceStatus.OFF.getValue();
            }
        }
        if(compasLDeviceStatus.equals(TCompasLDeviceStatus.ACTIVE) ||
                compasLDeviceStatus.equals(TCompasLDeviceStatus.UNTESTED)){
            checkAuthorisationToActivateLDevice(iedName, ldInst, enumValues);
        }
        if(compasLDeviceStatus.equals(TCompasLDeviceStatus.INACTIVE)){
            checkAuthorisationToDeactivateLDevice(iedName, ldInst, enumValues);
        }
    }

    /**
     * checks whether LDevice status is authorized to be activated when CompasLDeviceStatus Private is ACTIVE or UNTESTED
     * @param iedName Ied name value which contains LDevice
     * @param ldInst LDevice inst value
     * @param enumValues enum values
     */
    private void checkAuthorisationToActivateLDevice(String iedName, String ldInst, Set<String> enumValues) {
        if (!enumValues.contains(LdeviceStatus.OFF.getValue()) && enumValues.contains(LdeviceStatus.ON.getValue())) {
            if (isDeclaredInSubstation(iedName, ldInst)) {
                isUpdatable = true;
                newVal = LdeviceStatus.ON.getValue();
            } else {
                errorMessage = "The LDevice cannot be set to 'off' but has not been selected into SSD.";
            }
        }
        if (enumValues.contains(LdeviceStatus.ON.getValue()) && enumValues.contains(LdeviceStatus.OFF.getValue())) {
            isUpdatable = true;
            if (isDeclaredInSubstation(iedName, ldInst)) {
                newVal = LdeviceStatus.ON.getValue();
            } else {
                newVal = LdeviceStatus.OFF.getValue();
            }
        }

    }

    /**
     * checks whether LDevice Status is authorized to be deactivated when CompasLDeviceStatus Private is INACTIVE
     * @param iedName Ied name value which contains LDevice
     * @param ldInst LDevice inst value
     * @param enumValues enum values
     */
    private void checkAuthorisationToDeactivateLDevice(String iedName, String ldInst, Set<String> enumValues) {
        if (!enumValues.contains(LdeviceStatus.OFF.getValue()) && enumValues.contains(LdeviceStatus.ON.getValue())) {
            if (isDeclaredInSubstation(iedName, ldInst)) {
                errorMessage = "The LDevice is not qualified into STD but has been selected into SSD.";
            } else {
                errorMessage = "The LDevice cannot be set to 'off' but has not been selected into SSD.";
            }
        }
        if (enumValues.contains(LdeviceStatus.ON.getValue()) && enumValues.contains(LdeviceStatus.OFF.getValue())) {
            if (isDeclaredInSubstation(iedName, ldInst)) {
                errorMessage = "The LDevice is not qualified into STD but has been selected into SSD.";
            } else {
                isUpdatable = true;
                newVal = LdeviceStatus.OFF.getValue();
            }
        }
    }

    /**
     * checks whether a pair of IED name and LDevice inst are referenced in Substation...LNode list
     * @param iedName Ied name value
     * @param ldInst LDevice inst value
     * @return Returns whether a pair of IED name and LDevice inst are referenced in Substation...LNode list
     */
    private boolean isDeclaredInSubstation(String iedName, String ldInst){
        return iedNameLdInstList.contains(Pair.of(iedName, ldInst));
    }


}
