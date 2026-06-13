package com.example.template.persistence.dao;

import com.example.template.domain.dto.PageResponse;
import com.example.template.domain.dto.TemplateDTO;
import com.example.template.persistence.entity.Template;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class TemplateRepository implements PanacheRepository<Template> {

    public Optional<Template> findDuplicate(Template template) {
        if (template.getDescription() == null) {
            return Optional.empty();
        }
        return find("description = ?1", template.getDescription()).firstResultOptional();
    }

    public PageResponse<TemplateDTO> findAllTemplates(int page, int size) {
        long total = count();
        int totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 0;
        int zeroBasedPage = Math.max(page, 0);

        List<TemplateDTO> content = getEntityManager()
                .createQuery(
                        "SELECT new com.example.template.domain.dto.TemplateDTO(t.id, t.description) FROM template t",
                        TemplateDTO.class)
                .setFirstResult(zeroBasedPage * size)
                .setMaxResults(size)
                .getResultList();

        return new PageResponse<>(content, total, totalPages, size, zeroBasedPage);
    }

    public void softDelete(Long id) {
        findByIdOptional(id).ifPresent(this::delete);
    }
}
