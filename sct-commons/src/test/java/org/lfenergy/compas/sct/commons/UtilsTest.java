// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    @Test
    void getField_should_find_field_declared_class() {
        // Given : nothing
        // When
        Field result = Utils.getField(ChildClass.class, "childField");
        // Then
        assertNotNull(result);
        assertEquals("childField", result.getName());
    }

    @Test
    void getField_should_look_in_parent_classes_to_find_field() {
        // Given : nothing
        // When
        Field result = Utils.getField(ChildClass.class, "parentField");
        // Then
        assertNotNull(result);
        assertEquals("parentField", result.getName());
    }

    @Test
    void getField_should_return_null_when_field_not_found() {
        // Given : nothing
        // When
        Field result = Utils.getField(ChildClass.class, "non existent field");
        // Then
        assertNull(result);
    }

    @Test
    void setField_should_set_declaredField() {
        // Given
        ChildClass child = new ChildClass();
        // When
        Utils.setField(child, "childField", "changed");
        // Then
        assertEquals("changed", child.getChildField());
    }

    @Test
    void setField_should_set_declaredField_with_value_null() {
        // Given
        ChildClass child = new ChildClass();
        // When
        Utils.setField(child, "childField", null);
        // Then
        assertNull(child.getChildField());
    }

    @Test
    void setField_should_set_value_when_field_declared_on_parent_class() {
        // Given
        ChildClass child = new ChildClass();
        // When
        Utils.setField(child, "parentField", "changed");
        // Then
        assertEquals("changed", child.getParentField());
    }

    @Test
    void setField_should_do_nothing_when_field_not_found() {
        // Given
        ChildClass child = new ChildClass();
        // When & Then
        assertDoesNotThrow(() ->
            Utils.setField(child, "non existent field", "changed"));
    }

    @Test
    void setField_should_do_nothing_when_value_types_mismatch() {
        // Given
        ChildClass child = new ChildClass();
        // When & Then
        assertDoesNotThrow(() ->
            Utils.setField(child, "childField", Boolean.TRUE));
        assertEquals("CHILD", child.getChildField());
    }

    @Test
    void entering_should_return_text() {
        // Given : method name
        // When
        String entering = Utils.entering();
        // Then
        assertEquals(">>> Entering: ::entering_should_return_text", entering);
    }

    @Test
    void leaving_should_return_text() {
        // Given : method name
        // When
        String leaving = Utils.leaving();
        // Then
        assertEquals("<<< Leaving: ::leaving_should_return_text", leaving);
    }

    @Test
    void leaving_should_return_text_with_time() {
        // Given : method name
        // When
        String leaving = Utils.leaving(System.nanoTime());
        // Then
        assertTrue(leaving.matches("<<< Leaving: ::leaving_should_return_text_with_time - Timer duration: .* sec."), leaving);
    }

    @Test
    void leaving_should_return_text_with_invalid_time() {
        // Given : method name
        // When
        String leaving = Utils.leaving(-1L);
        // Then
        assertEquals("<<< Leaving: ::leaving_should_return_text_with_invalid_time", leaving);
    }

    private static class ParentClass {
        private String parentField = "PARENT";

        public String getParentField() {
            return parentField;
        }
    }

    private static class ChildClass extends ParentClass {
        private String childField = "CHILD";

        public String getChildField() {
            return childField;
        }
    }
}
