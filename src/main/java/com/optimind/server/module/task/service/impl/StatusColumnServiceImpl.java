package com.optimind.server.module.task.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.optimind.server.module.task.dto.StatusColumnDTO;
import com.optimind.server.module.task.service.StatusColumnService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatusColumnServiceImpl implements StatusColumnService {
    @Override
    public StatusColumnDTO.StatusColumnResponse addColumn(StatusColumnDTO.CreateStatusColumnRequest request) {
        return null;
    }

    @Override
    public StatusColumnDTO.StatusColumnResponse editColumn(UUID columnId,
            StatusColumnDTO.UpdateStatusColumnRequest request) {
        return null;
    }

    @Override
    public void deleteColumn(UUID columnId) {
    }
}
