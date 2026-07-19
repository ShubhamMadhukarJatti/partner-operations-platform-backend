package com.sharkdom.model.otp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OneTimePassword {

    String firstNumber;

    String secondNumber;

    String thirdNumber;

    String fourthNumber;

}
