package com.aparttime.auth.service;

import com.aparttime.admin.domain.Admin;
import com.aparttime.auth.dto.response.ReissueResponse;
import com.aparttime.auth.dto.response.SecondaryTokenResponse;
import com.aparttime.auth.dto.response.SignupResponse;
import com.aparttime.auth.dto.result.LoginResult;
import com.aparttime.auth.dto.request.LoginRequest;
import com.aparttime.auth.dto.request.SignupRequest;
import com.aparttime.admin.repository.AdminRepository;
import com.aparttime.auth.dto.response.LoginResponse;
import com.aparttime.auth.dto.result.ReissueResult;
import com.aparttime.config.properties.JwtProperties;
import com.aparttime.exception.auth.DuplicateUsernameException;
import com.aparttime.exception.auth.InvalidPasswordException;
import com.aparttime.exception.jwt.EmptyRefreshTokenException;
import com.aparttime.exception.jwt.RefreshTokenNotFoundException;
import com.aparttime.exception.member.MemberNotFoundException;
import com.aparttime.redis.repository.RefreshTokenRepository;
import com.aparttime.jwt.JwtTokenProvider;
import com.aparttime.redis.repository.SecondaryTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SecondaryTokenRepository secondaryTokenRepository;

    public SignupResponse signup(
        SignupRequest request
    ) {

        if (adminRepository.findByUsername(request.username()).isPresent()) {
            throw new DuplicateUsernameException();
        }

        Admin admin = Admin.of(
            request.username(),
            passwordEncoder.encode(request.password())
        );

        Admin savedAdmin = adminRepository.save(admin);

        return SignupResponse.of(
            savedAdmin.getId(),
            savedAdmin.getUsername()
        );
    }

    public LoginResult login(
        LoginRequest request
    ) {
        Admin admin = adminRepository.findByUsername(request.username())
            .orElseThrow(MemberNotFoundException::new);

        if (!passwordEncoder.matches(request.password(), admin.getPassword())) {
            throw new InvalidPasswordException();
        }

        String accessToken = jwtTokenProvider.createAccessToken(admin.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(admin.getId());

        // save refresh token in Redis
        refreshTokenRepository.save(
            admin.getId(),
            refreshToken,
            jwtProperties.getRefreshTokenExpiration()
        );

        LoginResponse loginResponse = LoginResponse.of(
            admin.getId(),
            admin.getUsername(),
            accessToken
        );

        return LoginResult.of(loginResponse, refreshToken);
    }

    public void logout(
        Long memberId,
        String refreshToken
    ) {
        if (refreshToken == null) {
            return;
        }

        Long findMemberId = refreshTokenRepository.findMemberIdByRefreshToken(refreshToken);

        if (memberId.equals(findMemberId)) {
            refreshTokenRepository.deleteByRefreshToken(refreshToken);
        }

        // TODO: memberId와 findMemberId가 같지 않을 경우의 예외 처리 필요

    }

    public ReissueResult reissue(
        String refreshToken
    ) {

        if (refreshToken == null) {
            throw new EmptyRefreshTokenException();
        }

        jwtTokenProvider.validateRefreshToken(refreshToken);

        Long memberId = refreshTokenRepository.findMemberIdByRefreshToken(refreshToken);

        if (memberId == null) {
            throw new RefreshTokenNotFoundException();
        }

        Admin admin = adminRepository.findById(memberId)
            .orElseThrow(MemberNotFoundException::new);

        refreshTokenRepository.deleteByRefreshToken(refreshToken);

        // 재발급 로직
        // TODO: 나중에 이 부분 별개의 메서드로 리팩터링 필요 (로그인 로직에서도 재사용)

        String newAccessToken = jwtTokenProvider.createAccessToken(memberId);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(memberId);

        refreshTokenRepository.save(
            admin.getId(),
            newRefreshToken,
            jwtProperties.getRefreshTokenExpiration()
        );

        ReissueResponse reissueResponse = ReissueResponse.of(
            admin.getId(),
            admin.getUsername(),
            newAccessToken
        );

        return ReissueResult.of(reissueResponse, newRefreshToken);
    }

    public SecondaryTokenResponse issueSecondaryToken(
        Long memberId
    ) {
        String secondaryToken = jwtTokenProvider.createSecondaryToken(memberId);

        secondaryTokenRepository.save(
            memberId,
            secondaryToken,
            jwtProperties.getSecondaryTokenExpiration()
        );

        return SecondaryTokenResponse.of(
            memberId,
            secondaryToken
        );
    }

}
