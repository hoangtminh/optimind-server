package com.optimind.server.module.task.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import com.optimind.server.module.task.dto.StatusColumnDTO;
import com.optimind.server.module.task.service.StatusColumnService;

@RestController
@RequestMapping("/api/project/columns")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class StatusColumnController {

    private final StatusColumnService statusColumnService;

    @PostMapping
    public ResponseEntity<StatusColumnDTO.StatusColumnResponse> addColumn(
            @RequestBody StatusColumnDTO.CreateStatusColumnRequest columnDto) {
        return ResponseEntity.ok(statusColumnService.addColumn(columnDto));
    }

    @PutMapping("/{columnId}")
    public ResponseEntity<StatusColumnDTO.StatusColumnResponse> editColumn(@PathVariable UUID columnId,
            @RequestBody StatusColumnDTO.UpdateStatusColumnRequest columnDto) {
        return ResponseEntity.ok(statusColumnService.editColumn(columnId, columnDto));
    }

    @DeleteMapping("/{columnId}")
    public ResponseEntity<Void> deleteColumn(@PathVariable UUID columnId) {
        statusColumnService.deleteColumn(columnId);
        return ResponseEntity.ok().build();
    }
}
