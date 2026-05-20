package com.campost.backend.domain.user.dto;

import com.campost.backend.domain.user.validation.UserProfileValidationRules;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "사용자 프로필 정보 수정 요청")
public record UserProfileUpdateRequest(
        @Schema(description = "학과 코드", example = "SW")
        @NotBlank(message = "학과를 선택해주세요.")
        @Pattern(
                regexp = UserProfileValidationRules.DEPARTMENT_CODE_PATTERN,
                message = UserProfileValidationRules.DEPARTMENT_CODE_MESSAGE
        )
        String department,

        @Schema(description = "학년", example = "3")
        @NotNull(message = "학년을 선택해주세요.")
        @Min(value = UserProfileValidationRules.MIN_GRADE, message = "학년은 1 이상이어야 합니다.")
        @Max(value = UserProfileValidationRules.MAX_GRADE, message = "학년은 6 이하이어야 합니다.")
        Integer grade,

        @Schema(description = "닉네임", example = "캠포스트유저")
        @NotBlank(message = "닉네임을 입력해주세요.")
        @Size(
                max = UserProfileValidationRules.MAX_NICKNAME_LENGTH,
                message = "닉네임은 {max}자 이하로 입력해주세요."
        )
        String nickname
) {
}
