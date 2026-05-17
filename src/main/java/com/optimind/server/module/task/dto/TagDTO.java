package com.optimind.server.module.task.dto;

import java.util.UUID;

public class TagDTO {
    public record TagResponse(UUID id, String name, String color) {
    }

    public record CreateTagRequest(String name, String color) {
    }
}
