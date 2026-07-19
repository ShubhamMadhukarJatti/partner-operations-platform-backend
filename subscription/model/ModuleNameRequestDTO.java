package com.sharkdom.subscription.model;

import com.sharkdom.subscription.entity.ModuleName;
import lombok.Data;

import java.util.List;

@Data
public class ModuleNameRequestDTO {

    private List<ModuleName> moduleNames;
}