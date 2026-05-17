package com.optimind.server.module.task.dto;

import java.util.UUID;

public class StatusColumnDTO {
    public record StatusColumnResponse(UUID id, String name, Integer index, boolean isDefault) {
    }

    public record CreateStatusColumnRequest(String name, Integer index) {
    }

    public record UpdateStatusColumnRequest(String name, Integer index) {
    }
}
