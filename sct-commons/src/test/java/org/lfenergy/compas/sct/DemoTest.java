// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct;


import org.junit.jupiter.api.Test;

import java.util.List;

public class DemoTest {

    @Test
    void demo() {
        // Given
        List<List<String>> lists = List.of(
                List.of("0A", "0B"),
                List.of("1A", "1B")
        );
        // When
        lists.stream()
                .filter(strings -> {
                    System.out.println("filter 1er niveau " + strings);
                    return strings != null;
                })
                .flatMap(strings -> {
                    System.out.println("flatmap " + strings);
                    return strings.stream()
                            .filter(s -> {
                                System.out.println("filter 2eme niveau " + s );
                                return "0A".equals(s);
                            })
                            .map(s -> {
                                System.out.println("map " + s);
                                return "trouvé " + s;
                            });
                })
                .findFirst()
                .ifPresentOrElse(s -> System.out.println("present " + s), () -> System.out.println("else"));
    }
}
