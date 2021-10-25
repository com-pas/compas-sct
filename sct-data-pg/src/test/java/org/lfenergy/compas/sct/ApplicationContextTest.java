// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct;


import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

public class ApplicationContextTest {
    @Test
    public void contextLoads() {
        // do nothing
    }

    @SpringBootApplication
    @EnableJpaRepositories
    @EnableTransactionManagement
    @PropertySource("classpath:application.yml")
    public static class Context {
    }
}
