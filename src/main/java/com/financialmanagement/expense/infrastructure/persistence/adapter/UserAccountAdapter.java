package com.financialmanagement.expense.infrastructure.persistence.adapter;

import com.financialmanagement.expense.application.dto.user.UserResponse;
import com.financialmanagement.expense.application.port.out.UserAccountPort;
import com.financialmanagement.expense.domain.model.UserRole;
import com.financialmanagement.expense.infrastructure.persistence.entity.UserEntity;
import com.financialmanagement.expense.infrastructure.persistence.mapper.UserEntityMapper;
import com.financialmanagement.expense.infrastructure.persistence.repository.UserJpaRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserAccountAdapter implements UserAccountPort {

    private final UserJpaRepository userJpaRepository;
    private final UserEntityMapper userEntityMapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponse> findByGoogleSub(String googleSub) {
        return userJpaRepository.findByGoogleSubAndDeletedAtIsNull(googleSub).map(userEntityMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponse> findById(UUID id) {
        return userJpaRepository.findByIdAndDeletedAtIsNull(id).map(userEntityMapper::toResponse);
    }

    @Override
    @Transactional
    public UserResponse upsertFromOAuth(String email, String googleSub, String displayName, String avatarUrl) {
        Optional<UserEntity> existing = userJpaRepository.findByGoogleSubAndDeletedAtIsNull(googleSub);
        if (existing.isPresent()) {
            UserEntity u = existing.get();
            u.setDisplayName(displayName);
            u.setAvatarUrl(avatarUrl);
            u.setEmail(email);
            return userEntityMapper.toResponse(userJpaRepository.save(u));
        }
        UserEntity u = new UserEntity();
        u.setEmail(email);
        u.setGoogleSub(googleSub);
        u.setDisplayName(displayName);
        u.setAvatarUrl(avatarUrl);
        u.setRole(UserRole.USER);
        return userEntityMapper.toResponse(userJpaRepository.save(u));
    }
}
