package com.sharkdom.controller.partnerportalsnapshot;

import com.sharkdom.dto.SnapshotShareRequest;
import com.sharkdom.entity.partnerportalsnapshot.PartnerPortalSnapShot;
import com.sharkdom.service.partnerportalsnapshot.PartnerPortalSnapShotService;
import com.sharkdom.util.SharkdomApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/partner/portal")
public class PartnerPortalSnapShotController {

    @Autowired
    private PartnerPortalSnapShotService partnerPortalSnapShotService;

    @PostMapping("/share")
    public ResponseEntity<SharkdomApiResponse<Map<String, Object>>> shareSnapshot(
            @RequestBody SnapshotShareRequest request) {

        log.info("Received share request for email={}", request.getEmail());

        Map<String, Object> result =
                partnerPortalSnapShotService.sharePortalSnapshot(
                        request.getEmail(),
                        request.getAccess(),
                        request.getExternalPartnerCode()
                );

        SharkdomApiResponse<Map<String, Object>> response =
                new SharkdomApiResponse<>(true, "Snapshot shared successfully", result);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/snapshots/{receiverUserId}")
    public ResponseEntity<SharkdomApiResponse<Object>> getSnapshotsByReceiver(
            @PathVariable String receiverUserId) {
        log.info("Fetching partner portal snapshots for receiverUserId: {}", receiverUserId);
        Object snapshots = partnerPortalSnapShotService.getSnapshotsWithOrgName(receiverUserId);
        SharkdomApiResponse<Object> response =
                new SharkdomApiResponse<>(true, "Snapshots fetched successfully", snapshots);
        return ResponseEntity.ok(response);
    }

}
