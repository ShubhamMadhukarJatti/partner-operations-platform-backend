package com.sharkdom.model.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class EmailVerify {
    EmailVerifyStatus status;
}
