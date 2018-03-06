package com.kloia.sample.dto.event;

import com.kloia.eventapis.common.PublishedEvent;
import lombok.Data;

@Data
public class ReserveStockEvent extends PublishedEvent {
    private String stockId;
    private long numberOfItemsSold;

}
