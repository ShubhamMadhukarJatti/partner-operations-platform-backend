package com.sharkdom.service.ppi;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "t_azure_afd_route_logs")
@Getter
@Setter
public class AzureAfdRouteLog extends BaseEntity {

    @Column(name = "custom_domain_name")
    private String customDomainName;

    @Column(name = "response_json", columnDefinition = "TEXT")
    private String responseJson;
}