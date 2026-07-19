package com.sharkdom.dto;

import java.util.Date;

public interface OrganizationUpdateView {

    String getPrimaryEmail();

    String getName();

    Date getLastUpdatedTimestamp();
}