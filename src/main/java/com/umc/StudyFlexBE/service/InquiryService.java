package com.umc.StudyFlexBE.service;

import com.umc.StudyFlexBE.dto.request.Inquiry.InquiryAnswerRequestDto;
import com.umc.StudyFlexBE.dto.request.Inquiry.InquiryUploadRequestDto;
import com.umc.StudyFlexBE.dto.response.BaseException;
import com.umc.StudyFlexBE.dto.response.BaseResponseStatus;
import com.umc.StudyFlexBE.dto.response.Inquiry.InquiryAnswerResponseDto;
import com.umc.StudyFlexBE.dto.response.Inquiry.InquiryListResponseDto;
import com.umc.StudyFlexBE.dto.response.Inquiry.InquiryResponseDto;
import com.umc.StudyFlexBE.entity.Inquiry;
import com.umc.StudyFlexBE.entity.InquiryAnswer;
import com.umc.StudyFlexBE.entity.Member;
import com.umc.StudyFlexBE.repository.InquiryAnswerRepository;
import com.umc.StudyFlexBE.repository.InquiryRepository;
import com.umc.StudyFlexBE.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InquiryService {
    private final InquiryRepository inquiryRepository;
    private final MemberRepository memberRepository;
    private final InquiryAnswerRepository inquiryAnswerRepository;

    @Autowired
    public InquiryService(InquiryRepository inquiryRepository, MemberRepository memberRepository, InquiryAnswerRepository inquiryAnswerRepository) {
        this.inquiryRepository = inquiryRepository;
        this.memberRepository = memberRepository;
        this.inquiryAnswerRepository = inquiryAnswerRepository;
    }

    public Inquiry createInquiry(String email, InquiryUploadRequestDto request) {
        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            throw new BaseException(BaseResponseStatus.NO_SUCH_EMAIL);
        }
        Inquiry inquiry = new Inquiry();
        inquiry.setMember_id(member);
        inquiry.setTitle(request.getTitle());
        inquiry.setContent(request.getContent());
        inquiry.setIs_answered(false);
        inquiry.setCreated_at(new Timestamp(System.currentTimeMillis()));
        inquiry.setUpdated_at(new Timestamp(System.currentTimeMillis()));
        inquiry.setIs_opened(true);
        inquiry.setView(0);

        return inquiryRepository.save(inquiry);
    }

    public InquiryListResponseDto getInquiryList(int page, int itemSize) {
        Pageable pageable = PageRequest.of(page, itemSize);
        Page<Inquiry> inquiryPage = inquiryRepository.findAll(pageable);

        List<InquiryListResponseDto.InquirySummary> inquirySummaries = inquiryPage.getContent().stream()
                .map(inquiry -> new InquiryListResponseDto.InquirySummary(
                        inquiry.getId(),
                        inquiry.getTitle(),
                        inquiry.getMember_id().getName(),
                        inquiry.getView(),
                        inquiry.getIs_opened()
                ))
                .collect(Collectors.toList());

        return new InquiryListResponseDto(
                page,
                itemSize,
                inquiryPage.getTotalPages(),
                inquirySummaries
        );
    }

    public InquiryResponseDto getInquiryDetail(Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new RuntimeException("Inquiry not found"));

        inquiry.setView(inquiry.getView() + 1);
        inquiryRepository.save(inquiry);

        InquiryResponseDto.AnswerResponse answerResponse = null;
        boolean isAnswered = inquiryAnswerRepository.findByInquiry(inquiry).isPresent();
        if (isAnswered) {
            InquiryAnswer answer = inquiryAnswerRepository.findByInquiry(inquiry).get();
            answerResponse = new InquiryResponseDto.AnswerResponse(
                    answer.getId(),
                    answer.getMember_id().getName(),
                    answer.getContent(),
                    answer.getCreated_at(),
                    answer.getUpdated_at()
            );
        }

        return new InquiryResponseDto(
                inquiry.getId(),
                inquiry.getView(),
                inquiry.getTitle(),
                inquiry.getIs_opened(),
                inquiry.getMember_id().getName(),
                inquiry.getContent(),
                inquiry.getCreated_at(),
                inquiry.getUpdated_at(),
                isAnswered,
                answerResponse
        );
    }

    public InquiryListResponseDto searchInquiries(String searchTerm, int page, int itemSize) {
        Pageable pageable = PageRequest.of(page, itemSize);
        Page<Inquiry> inquiryPage = inquiryRepository.findByTitleOrContentContaining(searchTerm, pageable);

        List<InquiryListResponseDto.InquirySummary> inquirySummaries = inquiryPage.getContent().stream()
                .map(inquiry -> new InquiryListResponseDto.InquirySummary(
                        inquiry.getId(),
                        inquiry.getTitle(),
                        inquiry.getMember_id().getName(),
                        inquiry.getView(),
                        inquiry.getIs_opened()
                ))
                .collect(Collectors.toList());

        return new InquiryListResponseDto(
                page,
                itemSize,
                inquiryPage.getTotalPages(),
                inquirySummaries
        );
    }

    public InquiryAnswerResponseDto postAnswer(Long inquiryId, InquiryAnswerRequestDto request, String email) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new RuntimeException("Inquiry not found"));

        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            throw new BaseException(BaseResponseStatus.NO_SUCH_EMAIL);
        }

        InquiryAnswer answer = new InquiryAnswer();
        answer.setInquiry(inquiry);
        answer.setMember_id(member);
        answer.setContent(request.getContent());
        answer.setCreated_at(new Timestamp(System.currentTimeMillis()));
        answer.setUpdated_at(new Timestamp(System.currentTimeMillis()));

        InquiryAnswer savedAnswer = inquiryAnswerRepository.save(answer);


        inquiry.setIs_answered(true);
        inquiryRepository.save(inquiry);

        return new InquiryAnswerResponseDto(inquiryId, savedAnswer.getId());
    }

}

