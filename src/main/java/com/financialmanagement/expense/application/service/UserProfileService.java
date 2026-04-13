package com.financialmanagement.expense.application.service;

import com.financialmanagement.expense.application.dto.user.UserResponse;
import com.financialmanagement.expense.application.port.out.UserAccountPort;
import com.financialmanagement.expense.domain.exception.ResourceNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserAccountPort userAccountPort;

    @Transactional(readOnly = true)
    public UserResponse get(UUID userId) {
        return userAccountPort
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
