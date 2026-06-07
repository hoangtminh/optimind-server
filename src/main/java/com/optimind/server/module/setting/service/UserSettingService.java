package com.optimind.server.module.setting.service;

import java.util.UUID;
import com.optimind.server.module.setting.dto.UserSettingDTO;

public interface UserSettingService {
    UserSettingDTO.Response getSettings(UUID userId);
    UserSettingDTO.Response updateSettings(UUID userId, UserSettingDTO.UpdateRequest request);
}
