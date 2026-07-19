package com.sharkdom.model.organizatiocollaboration;

import com.sharkdom.entity.credits.Credits;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Getter
@Setter
@AllArgsConstructor
public class OrgCollaborationWithCredits {
    Credits credits;
    Page<PartnerResponse> partners;
}
