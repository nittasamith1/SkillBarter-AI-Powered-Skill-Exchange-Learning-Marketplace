package com.skillbarter.tenant.service;

import com.skillbarter.common.exception.BusinessException;
import com.skillbarter.common.exception.ResourceNotFoundException;
import com.skillbarter.common.security.TenantContext;
import com.skillbarter.tenant.dto.CreateTenantRequest;
import com.skillbarter.tenant.dto.TenantResponse;
import com.skillbarter.tenant.entity.Tenant;
import com.skillbarter.tenant.repository.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TenantService {

    private static final Logger log = LoggerFactory.getLogger(TenantService.class);

    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public TenantResponse createTenant(CreateTenantRequest request) {
        if (tenantRepository.existsBySlug(request.slug())) {
            throw new BusinessException("DUPLICATE_SLUG",
                    "A tenant with slug '" + request.slug() + "' already exists");
        }

        Tenant tenant = new Tenant();
        tenant.setName(request.name());
        tenant.setSlug(request.slug());
        tenant.setStatus(Tenant.TenantStatus.ACTIVE);

        Tenant saved = tenantRepository.save(tenant);
        log.info("Created tenant: id={} slug={}", saved.getId(), saved.getSlug());
        return TenantResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public TenantResponse getTenantById(UUID id) {
        return tenantRepository.findById(id)
                .map(TenantResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", id));
    }

    @Transactional(readOnly = true)
    public TenantResponse getMyTenant() {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new ResourceNotFoundException("No tenant context available");
        }
        return tenantRepository.findById(tenantId)
                .map(TenantResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));
    }

    @Transactional(readOnly = true)
    public Tenant findBySlug(String slug) {
        return tenantRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tenant not found with slug: " + slug));
    }

    @Transactional(readOnly = true)
    public Tenant findById(UUID id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", id));
    }
}
