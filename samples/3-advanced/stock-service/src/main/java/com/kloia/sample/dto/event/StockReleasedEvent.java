package com.kloia.sample.dto.event;

import com.kloia.eventapis.common.EventType;
import com.kloia.eventapis.common.PublishableEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockReleasedEvent extends PublishableEvent {
    private String orderId;
    private long numberOfItemsReleased;

    @Override
    public EventType getEventType() {
        return EventType.EVENT;
    }
}
