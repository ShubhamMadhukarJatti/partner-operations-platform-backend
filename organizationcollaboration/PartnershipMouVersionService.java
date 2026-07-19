package com.sharkdom.service.organizationcollaboration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ResourceNotFoundException;
import com.sharkdom.entity.organizationcollaboration.PartnershipMouVersion;
import com.sharkdom.repository.organizationcollaboration.PartnershipMouVersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class PartnershipMouVersionService {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PartnershipMouVersionRepository partnershipMouVersionRepository;


    @Transactional
    public PartnershipMouVersion create(PartnershipMouVersion partnershipMouVersion) {
        return partnershipMouVersionRepository.save(partnershipMouVersion);
    }


    @Transactional
    public PartnershipMouVersion update(PartnershipMouVersion updated) throws Exception {
        findById(updated.getId());
        return partnershipMouVersionRepository.save(updated);
    }

    @Transactional
    public PartnershipMouVersion createNewVersion(PartnershipMouVersion partnershipMouVersionData) {
        partnershipMouVersionData.setVersion(partnershipMouVersionRepository
                .findTopByOrganizationCollaborationIdOrderByVersionDesc(partnershipMouVersionData.getOrganizationCollaborationId()).get().getVersion() + 1);
        partnershipMouVersionData.setId(null);
        return partnershipMouVersionRepository.save(partnershipMouVersionData);
    }

    private PartnershipMouVersion findById(long id) {
        return partnershipMouVersionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH36, id));
    }

    public Page<PartnershipMouVersion> getAllByOrganizationCollaborationId(long organizationCollaborationId, int page, int size) {
        return partnershipMouVersionRepository.getAllByOrganizationCollaborationId(organizationCollaborationId, PageRequest.of(page, size));
    }

    @Transactional
    public PartnershipMouVersion patchByMOUVersionId(long id, JsonPatch patch) throws Exception {
        PartnershipMouVersion partnershipMouVersion = findById(id);
        PartnershipMouVersion partnershipMouVersionPatched = applyPatchToOrganizationCollaboration(patch, partnershipMouVersion);
        return partnershipMouVersionRepository.save(partnershipMouVersionPatched);
    }

    private PartnershipMouVersion applyPatchToOrganizationCollaboration(JsonPatch patch, PartnershipMouVersion targetUser)

            throws JsonPatchException, JsonProcessingException {
        JsonNode patched = patch.apply(objectMapper.convertValue(targetUser, JsonNode.class));
        return objectMapper.treeToValue(patched, PartnershipMouVersion.class);
    }


}
