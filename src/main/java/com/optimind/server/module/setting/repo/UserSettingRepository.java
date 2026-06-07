package com.optimind.server.module.setting.repo;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.optimind.server.module.setting.entity.UserSettingEntity;

@Repository
public interface UserSettingRepository extends JpaRepository<UserSettingEntity, UUID> {
}
