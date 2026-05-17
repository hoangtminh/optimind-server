package com.optimind.server.module.task.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import com.optimind.server.module.task.dto.TagDTO;
import com.optimind.server.module.task.service.TagService;

@RestController
@RequestMapping("/api/project/tags")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class TagController {

    private final TagService tagService;

    @PostMapping
    public ResponseEntity<TagDTO.TagResponse> addTag(@RequestBody TagDTO.CreateTagRequest tagDto) {
        return ResponseEntity.ok(tagService.addTag(tagDto));
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<Void> deleteTag(@PathVariable UUID tagId) {
        tagService.deleteTag(tagId);
        return ResponseEntity.ok().build();
    }
}
