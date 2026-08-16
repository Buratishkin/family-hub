package org.buratishkin.familyhub.family.alias.controller;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.family.alias.api.AliasView;
import org.buratishkin.familyhub.family.alias.service.AliasCrudService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/aliases")
public class AliasInternalController {
    private final AliasCrudService aliasCrudService;

    @GetMapping("/families/{familyId}")
    public List<AliasView> findAliasesByFamilyId(@PathVariable Long familyId) {
        return aliasCrudService.findAliasesByFamilyId(familyId);
    }
}
