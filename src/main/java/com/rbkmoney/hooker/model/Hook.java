package com.rbkmoney.hooker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Created by jeckep on 13.04.17.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Hook {
    public long id;
    public String partyId;
    public Set<EventType> eventTypes;
    public String url;
    public String pubKey;
    private boolean enabled;
}
