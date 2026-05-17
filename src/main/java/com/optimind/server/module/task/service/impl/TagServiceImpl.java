package com.optimind.server.module.task.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.optimind.server.module.task.dto.TagDTO;
import com.optimind.server.module.task.service.TagService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {
    @Override
    public TagDTO.TagResponse addTag(TagDTO.CreateTagRequest request) {
        return null;
    }

    @Override
    public void deleteTag(UUID tagId) {
    }
}
