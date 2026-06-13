package com.example.template.service;

import com.example.template.domain.dto.PageResponse;
import com.example.template.domain.dto.TemplateDTO;
import com.example.template.mapper.TemplateMapper;
import com.example.template.persistence.dao.TemplateRepository;
import com.example.template.persistence.entity.Template;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import java.util.Optional;

@ApplicationScoped
public class TemplateService {

    @Inject
    TemplateRepository templateRepository;

    @Inject
    TemplateMapper templateMapper;

    protected TemplateDTO convertToDto(Template template) {
        return templateMapper.toDto(template);
    }

    protected Template convertToEntity(TemplateDTO templateDTO) {
        return templateMapper.toEntity(templateDTO);
    }

    @Transactional
    public Response createTemplate(TemplateDTO templateDTO) {
        Template template = convertToEntity(templateDTO);
        Optional<Template> duplicate = templateRepository.findDuplicate(template);
        if (duplicate.isPresent()) {
            return Response.status(422).build();
        }

        templateRepository.persist(template);
        return Response.status(Response.Status.CREATED).entity(convertToDto(template)).build();
    }

    @Transactional
    public Response updateTemplate(Long id, TemplateDTO templateDTO) {
        Optional<Template> existing = templateRepository.findByIdOptional(id);
        if (existing.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Template template = convertToEntity(templateDTO);
        Optional<Template> duplicate = templateRepository.findDuplicate(template);
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            return Response.status(422).build();
        }

        Template entity = existing.get();
        entity.setDescription(template.getDescription());
        return Response.ok(convertToDto(entity)).build();
    }

    public Response getTemplate(Long id) {
        return templateRepository.findByIdOptional(id)
                .map(t -> Response.ok(convertToDto(t)).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    public Response getTemplates(int page, int size) {
        PageResponse<TemplateDTO> result = templateRepository.findAllTemplates(page - 1, size);
        return Response.ok(result).build();
    }

    @Transactional
    public Response deleteTemplate(Long id) {
        templateRepository.softDelete(id);
        return Response.ok(true).build();
    }
}
