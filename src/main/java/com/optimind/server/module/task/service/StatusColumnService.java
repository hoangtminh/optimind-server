package com.optimind.server.module.task.service;

import java.util.UUID;

import com.optimind.server.module.task.dto.StatusColumnDTO;

public interface StatusColumnService {
    StatusColumnDTO.StatusColumnResponse addColumn(StatusColumnDTO.CreateStatusColumnRequest request);

    StatusColumnDTO.StatusColumnResponse editColumn(UUID columnId, StatusColumnDTO.UpdateStatusColumnRequest request);

    void deleteColumn(UUID columnId);
}
