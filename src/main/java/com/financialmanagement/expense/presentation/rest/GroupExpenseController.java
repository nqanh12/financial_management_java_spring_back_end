package com.financialmanagement.expense.presentation.rest;

import com.financialmanagement.expense.application.dto.group.AddGroupMemberRequest;
import com.financialmanagement.expense.application.dto.group.CreateExpenseGroupRequest;
import com.financialmanagement.expense.application.dto.group.CreateSharedExpenseRequest;
import com.financialmanagement.expense.application.dto.group.ExpenseGroupResponse;
import com.financialmanagement.expense.application.dto.group.SharedExpenseResponse;
import com.financialmanagement.expense.application.service.GroupExpenseService;
import com.financialmanagement.expense.infrastructure.security.JwtUserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Group expenses")
@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupExpenseController {

    private final GroupExpenseService groupExpenseService;

    @PostMapping
    public ResponseEntity<ExpenseGroupResponse> create(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @Valid @RequestBody CreateExpenseGroupRequest request) {
        ExpenseGroupResponse g = groupExpenseService.createGroup(principal.userId(), request);
        return ResponseEntity.created(URI.create("/api/v1/groups/" + g.id())).body(g);
    }

    @GetMapping
    public List<ExpenseGroupResponse> list(@AuthenticationPrincipal JwtUserPrincipal principal) {
        return groupExpenseService.listGroups(principal.userId());
    }

    @GetMapping("/{groupId}")
    public ExpenseGroupResponse get(
            @AuthenticationPrincipal JwtUserPrincipal principal, @PathVariable UUID groupId) {
        return groupExpenseService.getGroup(principal.userId(), groupId);
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<Void> addMember(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable UUID groupId,
            @Valid @RequestBody AddGroupMemberRequest request) {
        groupExpenseService.addMember(principal.userId(), groupId, request.userId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{groupId}/shared-expenses")
    public List<SharedExpenseResponse> listShared(
            @AuthenticationPrincipal JwtUserPrincipal principal, @PathVariable UUID groupId) {
        return groupExpenseService.listShared(principal.userId(), groupId);
    }

    @PostMapping("/{groupId}/shared-expenses")
    public ResponseEntity<SharedExpenseResponse> createShared(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable UUID groupId,
            @Valid @RequestBody CreateSharedExpenseRequest request) {
        SharedExpenseResponse s = groupExpenseService.createShared(principal.userId(), groupId, request);
        return ResponseEntity.created(URI.create("/api/v1/groups/shared-expenses/" + s.id())).body(s);
    }

    @GetMapping("/shared-expenses/{sharedId}")
    public SharedExpenseResponse getShared(
            @AuthenticationPrincipal JwtUserPrincipal principal, @PathVariable UUID sharedId) {
        return groupExpenseService.getShared(principal.userId(), sharedId);
    }
}
