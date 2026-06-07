package com.optimind.server.module.setting.service;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.optimind.server.module.setting.dto.UserSettingDTO;
import com.optimind.server.module.setting.entity.UserSettingEntity;
import com.optimind.server.module.setting.repo.UserSettingRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserSettingServiceImpl implements UserSettingService {

    private final UserSettingRepository repository;

    @Override
    @Transactional
    public UserSettingDTO.Response getSettings(UUID userId) {
        UserSettingEntity entity = repository.findById(userId)
                .orElseGet(() -> {
                    UserSettingEntity defaultSettings = UserSettingEntity.builder()
                            .userId(userId)
                            .build();
                    return repository.save(defaultSettings);
                });
        return mapToResponse(entity);
    }

    @Override
    @Transactional
    public UserSettingDTO.Response updateSettings(UUID userId, UserSettingDTO.UpdateRequest request) {
        UserSettingEntity entity = repository.findById(userId)
                .orElseGet(() -> UserSettingEntity.builder().userId(userId).build());

        entity.setDarkMode(request.darkMode());
        entity.setMode(request.mode());
        entity.setFocusDuration(request.focusDuration());
        entity.setBreakDuration(request.breakDuration());
        entity.setLongBreakDuration(request.longBreakDuration());
        entity.setCyclesBeforeLongBreak(request.cyclesBeforeLongBreak());
        entity.setTotalCycles(request.totalCycles());
        entity.setVibrate(request.vibrate());
        entity.setSoundEnabled(request.soundEnabled());
        entity.setSoundVolume(request.soundVolume());
        entity.setSoundName(request.soundName());
        entity.setAutoBreak(request.autoBreak());

        UserSettingEntity saved = repository.save(entity);
        return mapToResponse(saved);
    }

    private UserSettingDTO.Response mapToResponse(UserSettingEntity entity) {
        return new UserSettingDTO.Response(
            entity.getUserId(),
            entity.isDarkMode(),
            entity.getMode(),
            entity.getFocusDuration(),
            entity.getBreakDuration(),
            entity.getLongBreakDuration(),
            entity.getCyclesBeforeLongBreak(),
            entity.getTotalCycles(),
            entity.isVibrate(),
            entity.isSoundEnabled(),
            entity.getSoundVolume(),
            entity.getSoundName(),
            entity.isAutoBreak()
        );
    }
}
