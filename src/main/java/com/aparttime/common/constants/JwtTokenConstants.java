package com.aparttime.common.constants;

public final class JwtTokenConstants {

    private JwtTokenConstants() {
        throw new UnsupportedOperationException("상수 클래스는 인스턴스화 할 수 없습니다.");
    }

    // HTTP Authorization 헤더 이름
    public static final String AUTHORIZATION_HEADER = "Authorization";

    // Bearer prefix
    public static final String BEARER_PREFIX = "Bearer ";
    public static final int BEGIN_INDEX = 7;

    // Bearer 문자열 길이
    public static final int BEARER_PREFIX_LENGTH = BEARER_PREFIX.length();

    public static final String TOKEN_TYPE = "type";
    public static final String ACCESS_TOKEN = "Access Token";
    public static final String REFRESH_TOKEN = "Refresh Token";
    public static final String SECONDARY_TOKEN = "Secondary Token";

}
