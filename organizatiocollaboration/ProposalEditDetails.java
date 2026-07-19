package com.sharkdom.model.organizatiocollaboration;

public record ProposalEditDetails(String benefit,
                           String description,
                           ProposalEditMode mode,
                           Long id,
                           Long parentId) {

}