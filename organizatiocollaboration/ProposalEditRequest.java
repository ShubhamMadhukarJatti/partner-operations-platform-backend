package com.sharkdom.model.organizatiocollaboration;

import java.util.List;

public record ProposalEditRequest(List<ProposalEditDetails> senderBenefits,
                                  List<ProposalEditDetails> receiverBenefits) {
}