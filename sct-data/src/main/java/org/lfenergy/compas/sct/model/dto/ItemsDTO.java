package org.lfenergy.compas.sct.model.dto;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Getter
public class ItemsDTO<T> {
    private Set<T> items = new HashSet<>();
}
