package com.kloia.sample.dto.event;

import com.kloia.eventapis.common.EventType;
import com.kloia.eventapis.common.PublishableEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreatedEvent extends PublishableEvent {
    private String stockId;
    private int orderAmount;
    private String description;

    @Override
    public EventType getEventType() {
        return EventType.OP_SINGLE;
    }
}
