package com.yuri.filgueira.login_app_api.entities.vos;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

public class LoginResponseVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private UserVO user;
    private String accessToken;
    private String refreshToken;
    private Date expiresIn;
    private Date refreshTokenExpiresIn;

    public LoginResponseVO() {
    }

    public LoginResponseVO(UserVO user, String accessToken, String refreshToken, Date expiresIn, Date refreshTokenExpiresIn) {
        this.user = user;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.refreshTokenExpiresIn = refreshTokenExpiresIn;
    }

    public UserVO getUser() {
        return user;
    }

    public void setUser(UserVO user) {
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Date getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Date expiresIn) {
        this.expiresIn = expiresIn;
    }

    public Date getRefreshTokenExpiresIn() {
        return refreshTokenExpiresIn;
    }

    public void setRefreshTokenExpiresIn(Date refreshTokenExpiresIn) {
        this.refreshTokenExpiresIn = refreshTokenExpiresIn;
    }
}
