package com.yuri.filgueira.login_app_api.entities.vos;

import com.yuri.filgueira.login_app_api.entities.model.Role;

import java.util.Set;

public record RegisterAccountCredentialsVO(String name, String email, String password, Set<Role> roles) {
}
