package com.sharkdom.service.perks;

import com.sharkdom.entity.perks.CountType;
import com.sharkdom.entity.perks.PerkStatus;
import com.sharkdom.entity.perks.PerksEntity;
import com.sharkdom.repository.perks.PerksRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class PerksService {
    private final PerksRepository perksRepository;

    public PerksService(PerksRepository perksRepository) {
        this.perksRepository = perksRepository;
    }

    public PerksEntity save(PerksEntity perks) {
        return perksRepository.save(perks);
    }

    public Page<PerksEntity> getPerks(PerkStatus perkStatus, int size, int pageNumber) {
        return perksRepository.findAllByPerkStatus(perkStatus, PageRequest.of(pageNumber, size));
    }

    public PerksEntity updateCount(Long id, CountType countType) {
        var perk = perksRepository.findById(id).get();
        if (countType.equals(CountType.CLICK)) {
            perk.setClickedCount(perk.getClickedCount() + 1);
        } else {
            perk.setRedeemedCount(perk.getRedeemedCount() + 1);
        }
        return perksRepository.save(perk);
    }

}
