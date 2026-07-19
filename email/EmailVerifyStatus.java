package com.sharkdom.model.email;

public enum EmailVerifyStatus {
    VERIFICATION_SUCCESSFUL,
    CODE_EXPIRED,
    INVALID_CODE,
    CODE_ALREADY_USED,
    DOMAIN_NOT_MATCHED

}
