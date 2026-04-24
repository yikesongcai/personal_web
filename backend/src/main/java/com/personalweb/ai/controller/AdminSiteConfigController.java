package com.personalweb.ai.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.*;

import com.personalweb.ai.service.SiteConfigService;

@RestController
@RequestMapping("/api/admin/site-config")
public class AdminSiteConfigController {

    private final SiteConfigService siteConfigService;

    public AdminSiteConfigController(SiteConfigService siteConfigService) {
        this.siteConfigService = siteConfigService;
    }

    @GetMapping
    public Map<String, String> getAll() {
        return siteConfigService.getAll();
    }

    @PutMapping
    public Map<String, String> updateAll(@RequestBody Map<String, String> configs) {
        siteConfigService.batchSet(configs);
        return siteConfigService.getAll();
    }
}
