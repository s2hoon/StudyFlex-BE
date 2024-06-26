package com.umc.StudyFlexBE.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudyNoticesRes {
    private String title;
    private LocalDateTime createAt;
    private long noticeId;
}
