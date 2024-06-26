package com.umc.StudyFlexBE.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudyNoticesInfoRes {
    private List<StudyNoticesRes> notices;
    private int itemSize;

}
