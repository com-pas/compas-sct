// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import java.util.List;

public record SubNetworkTypeDTO(String subnetworkName, String subnetworkType, List<String> accessPointNames) {
}
