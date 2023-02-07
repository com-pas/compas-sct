// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import com.opencsv.bean.CsvBindByPosition;
import lombok.*;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CsvUtilsTest {

    private static final Tuple ROW_1 = Tuple.tuple("cel1x1", "cel1x2", "cel1x3");
    private static final Tuple ROW_2 = Tuple.tuple("cel2x1", "cel2x2", "cel2x3");

    @Test
    void parseRows_should_parse_rows() {
        //Given
        StringReader csvReader = new StringReader("""
            cel1x1;cel1x2;cel1x3
            cel2x1;cel2x2;cel2x3
            """);
        //When
        List<Row> rows = CsvUtils.parseRows(csvReader, Row.class);
        //Then
        assertThat(rows).extracting(Row::getCol1, Row::getCol2, Row::getCol3)
            .containsExactly(
                ROW_1,
                ROW_2
            );
    }

    @Test
    void parseRows_should_ignore_empty_lines() {
        //Given
        StringReader csvReader = new StringReader("""
            cel1x1;cel1x2;cel1x3
            
            cel2x1;cel2x2;cel2x3
            """);
        //When
        List<Row> rows = CsvUtils.parseRows(csvReader, Row.class);
        //Then
        assertThat(rows).extracting(Row::getCol1, Row::getCol2, Row::getCol3)
            .containsExactly(
                ROW_1,
                ROW_2
            );
    }

    @Test
    void parseRows_should_treat_empty_string_as_null() {
        //Given
        StringReader csvReader = new StringReader("""
            ;cel1x2;cel1x3
            cel2x1;;cel2x3
            cel3x1;cel3x2;
            """);
        //When
        List<Row> rows = CsvUtils.parseRows(csvReader, Row.class);
        //Then
        assertThat(rows).extracting(Row::getCol1, Row::getCol2, Row::getCol3)
            .containsExactly(
                Tuple.tuple(null, "cel1x2", "cel1x3"),
                Tuple.tuple("cel2x1", null, "cel2x3"),
                Tuple.tuple("cel3x1", "cel3x2", null)
            );
    }

    @Test
    void parseRows_should_ignore_comment_lines() {
        //Given
        StringReader csvReader = new StringReader("""
            cel1x1;cel1x2;cel1x3
            
               # other comment line with indentation
            line with # in the middle should not be ignored;a;b
            """);
        //When
        List<Row> rows = CsvUtils.parseRows(csvReader, Row.class);
        //Then
        assertThat(rows).extracting(Row::getCol1, Row::getCol2, Row::getCol3)
            .containsExactly(
                ROW_1,
                Tuple.tuple("line with # in the middle should not be ignored", "a", "b")
            );
    }

    @Test
    void parseRows_with_resource_path_should_parse_rows() {
        //Given
        String resourcePath = "csvutils/csv_utils_test_file.csv";
        //When
        List<Row> rows = CsvUtils.parseRows(resourcePath, StandardCharsets.UTF_8, Row.class);
        //Then
        assertThat(rows).extracting(Row::getCol1, Row::getCol2, Row::getCol3)
            .containsExactly(
                ROW_1,
                ROW_2
            );
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @EqualsAndHashCode
    public static class Row {
        @CsvBindByPosition(position = 0)
        private String col1;
        @CsvBindByPosition(position = 1)
        private String col2;
        @CsvBindByPosition(position = 2)
        private String col3;
    }

}
