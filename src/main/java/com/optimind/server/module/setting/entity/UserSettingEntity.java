package com.optimind.server.module.setting.entity;

import java.util.UUID;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_settings")
public class UserSettingEntity {

    @Id
    private UUID userId;

    @Builder.Default
    private boolean darkMode = false;

    // Timer settings matching client TimerSettings
    @Builder.Default
    private String mode = "pomodoro"; // "pomodoro" | "countdown"

    @Builder.Default
    private int focusDuration = 25;

    @Builder.Default
    private int breakDuration = 5;

    @Builder.Default
    private int longBreakDuration = 15;

    @Builder.Default
    private int cyclesBeforeLongBreak = 4;

    @Builder.Default
    private int totalCycles = 4;

    // Sound & Haptic settings
    @Builder.Default
    private boolean vibrate = true;

    @Builder.Default
    private boolean soundEnabled = true;

    @Builder.Default
    private int soundVolume = 80;

    @Builder.Default
    private String soundName = "classic";

    // Auto break settings
    @Builder.Default
    private boolean autoBreak = true;
}
