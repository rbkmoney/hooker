package com.rbkmoney.hooker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HooksLimit {
    private Map<String, Integer> perShop;
    private Integer perParty;
}
