package com.umc.StudyFlexBE.controller;

import com.umc.StudyFlexBE.config.jwt.JwtTokenProvider;
import com.umc.StudyFlexBE.dto.request.NaverDto;
import com.umc.StudyFlexBE.dto.response.*;
import com.umc.StudyFlexBE.entity.Member;
import com.umc.StudyFlexBE.entity.MsgEntity;
import com.umc.StudyFlexBE.service.NaverService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/app/naver")
public class NaverController {

    private final NaverService naverService;
    private final JwtTokenProvider jwtTokenProvider;



    @GetMapping("/login")
    public ResponseEntity<?> getNaverLogin() {

        String naverUrl = naverService.getNaverLogin();
        Map<String, String> response = new HashMap<>();
        response.put("naverUrl", naverUrl);
        return ResponseEntity.ok(response);

    }



    @GetMapping("/callback")
    public BaseResponse<?> callback(HttpServletRequest request) {
        try {
            // 네이버 사용자 정보 가져옴
            NaverDto naverUser = naverService.getNaverInfo(request.getParameter("code"));
            // 사용자 등록 혹은 인증
            Member member = naverService.registerOrAuthenticate(naverUser);
            // 새 사용자 여부 확인
            boolean isNewUser = member.isNewUser();

            // Member의 정보로 Authentication 객체 생성
            List<GrantedAuthority> authorities = member.getRoles().stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    member.getEmail(), null, authorities);

            // 생성된 Authentication 객체를 사용하여 토큰 생성
            String token = "Bearer " + jwtTokenProvider.createToken(authentication);
            LoginRes naverLoginRes = new LoginRes(token, naverUser.getEmail(), isNewUser);

            return new BaseResponse<>(BaseResponseStatus.SUCCESS, naverLoginRes);
        } catch (Exception e) {
            return new BaseResponse<>(BaseResponseStatus.NAVER_LOGIN_FAILED);
        }
    }


    @PostMapping("/loginTemp")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        try {
            String code = payload.get("code");
            if (code == null) {
                throw new InvalidAuthorizationCodeException("Authorization code is not provided.");
            }


            NaverDto naverUser = naverService.getNaverInfo(code);
            Member member = naverService.registerOrAuthenticate(naverUser);

            List<GrantedAuthority> authorities = member.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority(role.toString()))
                    .collect(Collectors.toList());
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    member.getEmail(), null, authorities);

            String token = jwtTokenProvider.createToken(authentication);

            return ResponseEntity.ok(new MsgEntity("로그인 성공", token));
        } catch (InvalidAuthorizationCodeException | APICallException e) {
            return ResponseEntity.badRequest().body(new MsgEntity(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MsgEntity("서버 내부 오류", null));
        }
    }

}
