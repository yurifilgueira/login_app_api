package com.yuri.filgueira.login_app_api.entities.vos;

import java.io.Serializable;

public record RequestUpdateUserVO(Long id, String name, String email, String newPassword, String confirmNewPassword) implements Serializable {
}
