package com.optimind.server.module.setting.dto;

import java.util.UUID;

public class UserSettingDTO {
    public record Response(
        UUID userId,
        boolean darkMode,
        String mode,
        int focusDuration,
        int breakDuration,
        int longBreakDuration,
        int cyclesBeforeLongBreak,
        int totalCycles,
        boolean vibrate,
        boolean soundEnabled,
        int soundVolume,
        String soundName,
        boolean autoBreak
    ) {}

    public record UpdateRequest(
        boolean darkMode,
        String mode,
        int focusDuration,
        int breakDuration,
        int longBreakDuration,
        int cyclesBeforeLongBreak,
        int totalCycles,
        boolean vibrate,
        boolean soundEnabled,
        int soundVolume,
        String soundName,
        boolean autoBreak
    ) {}
}
