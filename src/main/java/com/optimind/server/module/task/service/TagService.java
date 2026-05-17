package com.optimind.server.module.task.service;

import java.util.UUID;

import com.optimind.server.module.task.dto.TagDTO;

public interface TagService {
    TagDTO.TagResponse addTag(TagDTO.CreateTagRequest request);

    void deleteTag(UUID tagId);
}
