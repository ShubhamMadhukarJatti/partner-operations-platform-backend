package com.sharkdom.model.ppi;

import com.sharkdom.entity.ppi.InternalQuestion_Ppi;
import com.sharkdom.entity.ppi.InternalResponse_Ppi;
import lombok.Data;

import java.util.List;

@Data
public class ViewDetailsResponse {
    private String firstName;
    private String lastName;
    private String email;
    private String mobileNo;
   private String question;
   private String response;


}
