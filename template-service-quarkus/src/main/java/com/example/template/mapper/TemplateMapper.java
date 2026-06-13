package com.example.template.mapper;

import com.example.template.domain.dto.TemplateDTO;
import com.example.template.persistence.entity.Template;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TemplateMapper {

    public TemplateDTO toDto(Template template) {
        if (template == null) {
            return null;
        }
        return new TemplateDTO(template.getId(), template.getDescription());
    }

    public Template toEntity(TemplateDTO dto) {
        if (dto == null) {
            return null;
        }
        Template template = new Template();
        template.setId(dto.getId());
        template.setDescription(dto.getDescription());
        return template;
    }
}
