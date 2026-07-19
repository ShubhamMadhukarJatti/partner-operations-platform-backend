package com.sharkdom.emailagent.dto;

import lombok.Data;
import java.util.List;

@Data
public class EmailResponse {
    private List<EmailDraft> drafts;
}