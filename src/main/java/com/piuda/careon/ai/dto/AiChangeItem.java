package com.piuda.careon.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AiChangeItem {

    private String title;

    private String description;

    private String type; // increase, decrease, normal
}