package com.example.template.service;

import com.example.template.domain.dto.PageResponse;
import com.example.template.domain.dto.TemplateDTO;
import com.example.template.mapper.TemplateMapper;
import com.example.template.persistence.dao.TemplateRepository;
import com.example.template.persistence.entity.Template;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TemplateServiceTest {

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private TemplateMapper templateMapper;

    @Spy
    @InjectMocks
    private TemplateService templateService;

    private TemplateDTO templateDTO;
    private Template template;

    @BeforeEach
    void setUp() {
        templateDTO = new TemplateDTO(1L, "Template 1");
        template = new Template();
        template.setId(1L);
        template.setDescription("Template 1");

        doReturn(templateDTO).when(templateService).convertToDto(any(Template.class));
        doReturn(template).when(templateService).convertToEntity(any(TemplateDTO.class));
    }

    @Test
    @DisplayName("Create Template - Success")
    void testCreateTemplateSuccess() {
        when(templateRepository.findDuplicate(any(Template.class))).thenReturn(Optional.empty());
        doNothing().when(templateRepository).persist(any(Template.class));

        Response response = templateService.createTemplate(templateDTO);

        assertEquals(201, response.getStatus());
        assertEquals(templateDTO, response.getEntity());
    }

    @Test
    @DisplayName("Create Template - Failure")
    void testCreateTemplateFailure() {
        when(templateRepository.findDuplicate(any(Template.class))).thenReturn(Optional.of(template));

        Response response = templateService.createTemplate(templateDTO);

        assertEquals(422, response.getStatus());
        assertNull(response.getEntity());
    }

    @Test
    @DisplayName("Update Template - Success")
    void testUpdateTemplateSuccess() {
        when(templateRepository.findByIdOptional(1L)).thenReturn(Optional.of(template));
        when(templateRepository.findDuplicate(any(Template.class))).thenReturn(Optional.empty());

        Response response = templateService.updateTemplate(1L, templateDTO);

        assertEquals(200, response.getStatus());
        assertEquals(templateDTO, response.getEntity());
    }

    @Test
    @DisplayName("Update Template - Failure")
    void testUpdateTemplateFailure() {
        when(templateRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        Response response = templateService.updateTemplate(1L, templateDTO);

        assertEquals(404, response.getStatus());
        assertNull(response.getEntity());
    }

    @Test
    @DisplayName("Get Template - Success")
    void testGetTemplateSuccess() {
        when(templateRepository.findByIdOptional(1L)).thenReturn(Optional.of(template));

        Response response = templateService.getTemplate(1L);

        assertEquals(200, response.getStatus());
        assertEquals(templateDTO, response.getEntity());
    }

    @Test
    @DisplayName("Get Template - Failure")
    void testGetTemplateFailure() {
        when(templateRepository.findByIdOptional(1L)).thenReturn(Optional.empty());

        Response response = templateService.getTemplate(1L);

        assertEquals(404, response.getStatus());
    }

    @Test
    @DisplayName("Get Templates - Non-empty page")
    void testGetTemplatesWhenPageContentIsNotEmpty() {
        List<TemplateDTO> templates = Collections.singletonList(templateDTO);
        PageResponse<TemplateDTO> page = new PageResponse<>(templates, 1, 1, 10, 0);
        when(templateRepository.findAllTemplates(anyInt(), anyInt())).thenReturn(page);

        PageResponse<TemplateDTO> result = (PageResponse<TemplateDTO>) templateService.getTemplates(1, 10).getEntity();

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("Get Templates - Empty page")
    void testGetTemplatesWhenPageContentIsEmpty() {
        PageResponse<TemplateDTO> page = new PageResponse<>(Collections.emptyList(), 0, 0, 10, 0);
        when(templateRepository.findAllTemplates(anyInt(), anyInt())).thenReturn(page);

        PageResponse<TemplateDTO> result = (PageResponse<TemplateDTO>) templateService.getTemplates(1, 10).getEntity();

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getContent().size());
    }

    @Test
    @DisplayName("Delete Template")
    void testDeleteTemplate() {
        doNothing().when(templateRepository).softDelete(1L);

        Response response = templateService.deleteTemplate(1L);

        verify(templateRepository, times(1)).softDelete(eq(1L));
        assertEquals(200, response.getStatus());
        assertEquals(Boolean.TRUE, response.getEntity());
    }
}
