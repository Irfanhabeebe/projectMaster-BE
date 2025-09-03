package com.projectmaster.app.user.service;

import com.projectmaster.app.common.exception.EntityNotFoundException;
import com.projectmaster.app.common.exception.ProjectMasterException;
import com.projectmaster.app.company.dto.CompanyDto;
import com.projectmaster.app.company.entity.Company;
import com.projectmaster.app.company.repository.CompanyRepository;
import com.projectmaster.app.company.service.CompanyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyService companyService;

    private CompanyDto testCompanyDto;
    private Company testCompany;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        
        testCompanyDto = CompanyDto.builder()
                .name("Test Construction Co.")
                .address("123 Test St")
                .phone("+1-555-0123")
                .email("test@construction.com")
                .build();

        testCompany = Company.builder()
                .name("Test Construction Co.")
                .address("123 Test St")
                .phone("+1-555-0123")
                .email("test@construction.com")
                .active(true)
                .build();
        // Set ID manually since it's from BaseEntity
        testCompany.setId(testId);
    }

    @Test
    void shouldCreateCompanySuccessfully() {
        // Given
        when(companyRepository.existsByNameIgnoreCase(testCompanyDto.getName())).thenReturn(false);
        when(companyRepository.existsByEmailIgnoreCase(testCompanyDto.getEmail())).thenReturn(false);
        when(companyRepository.save(any(Company.class))).thenReturn(testCompany);

        // When
        CompanyDto result = companyService.createCompany(testCompanyDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(testCompanyDto.getName());
        assertThat(result.getEmail()).isEqualTo(testCompanyDto.getEmail());
        verify(companyRepository).save(any(Company.class));
    }

    @Test
    void shouldThrowExceptionWhenCompanyNameAlreadyExists() {
        // Given
        when(companyRepository.existsByNameIgnoreCase(testCompanyDto.getName())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> companyService.createCompany(testCompanyDto))
                .isInstanceOf(ProjectMasterException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void shouldGetCompanyByIdSuccessfully() {
        // Given
        when(companyRepository.findById(testId)).thenReturn(Optional.of(testCompany));

        // When
        CompanyDto result = companyService.getCompanyById(testId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testId);
        assertThat(result.getName()).isEqualTo(testCompany.getName());
    }

    @Test
    void shouldThrowExceptionWhenCompanyNotFound() {
        // Given
        when(companyRepository.findById(testId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> companyService.getCompanyById(testId))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldDeactivateCompanySuccessfully() {
        // Given
        when(companyRepository.findById(testId)).thenReturn(Optional.of(testCompany));

        // When
        companyService.deactivateCompany(testId);

        // Then
        assertThat(testCompany.getActive()).isFalse();
        verify(companyRepository).save(testCompany);
    }
}