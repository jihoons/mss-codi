package com.mss.codi.service.event;

import jakarta.validation.constraints.NotNull;

public interface ProductChangeEventHandler {
    void handle(@NotNull ProductChangeEvent event);
}
