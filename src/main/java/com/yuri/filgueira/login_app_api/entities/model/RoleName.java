package com.yuri.filgueira.login_app_api.entities.model;

import java.io.Serial;
import java.io.Serializable;

public enum RoleName implements Serializable {

    ROLE_CUSTOMER,
    ROLE_ADMIN;

    @Serial
    private static final long serialVersionUID = 1L;
}
