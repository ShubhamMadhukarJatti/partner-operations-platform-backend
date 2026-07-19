package com.sharkdom.dto;

import com.sharkdom.enums.partnerportalsnapshot.PartnerPortalSnapshotAccess;
import lombok.Data;
import java.util.List;
@Data
public class SnapshotShareRequest {

    private String email;

    private PartnerPortalSnapshotAccess access;

    private String externalPartnerCode;

}