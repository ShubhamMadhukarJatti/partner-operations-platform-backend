package com.sharkdom.service.configuration;

import com.sharkdom.entity.configuration.Configuration;
import com.sharkdom.repository.configuration.ConfigurationRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConfigurationService {
    @Autowired
    private ConfigurationRepository configurationRepo;

    @Transactional
    public Configuration saveConfiguration(Configuration configuration) {
        return configurationRepo.save(configuration);
    }

    public List<Configuration> findAllActiveByTypeAndKeyIn(String type, boolean webApplicable, boolean appApplicable, List<String> keys) {
        return configurationRepo.findAllActiveByTypeAndWebApplicableAndAppApplicableAndKeyIn(type, webApplicable, appApplicable, keys);
    }

    public List<Configuration> findAllActiveByType(String type) {
        return configurationRepo.findAllByTypeAndActiveIsTrue(type);
    }

    public List<Configuration> findAllByType(String type) {
        return configurationRepo.findAllByType(type);
    }

    public Map<String, List<Configuration>> findAllByTypes(String type) {
        var types = type.split(",");
        List<Configuration> configurations = configurationRepo.findAllByTypeIn(types);
        Map<String, List<Configuration>> result = new HashMap<>();

        // Initialize the map with empty lists for all types
        for (String t : types) {
            result.put(t, new ArrayList<>());
        }
        configurations.forEach(config -> result.get(config.getType()).add(config));

        return result;
    }

    public Page<Configuration> findAll(int page, int size) {
        return configurationRepo.findAll(PageRequest.of(page, size));
    }

    public List<Configuration> findAllActive() {
        return configurationRepo.findByActiveTrue();
    }

    public Page<Configuration> searchByPartialValue(String configType, String partialValue, int page, int size) {

        partialValue = partialValue.replaceAll(" ", "%").replaceAll("-", "%");
        partialValue = "%" + partialValue + "%";

        return configurationRepo.findAllByTypeAndValueLikeIgnoreCase(configType, partialValue, PageRequest.of(page, size));
        // return configurationRepo.findAllByTypeAndValueContainingIgnoreCase(configType, partialValue, PageRequest.of(page, size));
    }
}
