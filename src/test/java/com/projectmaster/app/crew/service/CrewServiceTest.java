package com.projectmaster.app.crew.service;

import com.projectmaster.app.crew.dto.CreateCrewRequest;
import com.projectmaster.app.crew.entity.Crew;
import com.projectmaster.app.crew.repository.CrewRepository;
import com.projectmaster.app.company.entity.Company;
import com.projectmaster.app.company.repository.CompanyRepository;
import com.projectmaster.app.user.dto.UserDto;
import com.projectmaster.app.user.entity.User;
import com.projectmaster.app.user.repository.UserRepository;
import com.projectmaster.app.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrewServiceTest {

    @Mock
    private CrewRepository crewRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CrewService crewService;

    private Company testCompany;
    private User testUser;
    private UserDto testUserDto;
    private CreateCrewRequest createCrewRequest;

    @BeforeEach
    void setUp() {
        testCompany = Company.builder()
                .name("Test Company")
                .build();
        testCompany.setId(UUID.randomUUID());

        testUser = User.builder()
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();
        testUser.setId(UUID.randomUUID());

        testUserDto = UserDto.builder()
                .id(testUser.getId())
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        createCrewRequest = CreateCrewRequest.builder()
                .companyId(testCompany.getId())
                .employeeId("EMP001")
                .firstName("John")
                .lastName("Doe")
                .email("test@example.com")
                .phone("1234567890")
                .hireDate(LocalDate.now())
                .position("Carpenter")
                .department("Construction")
                .build();
    }

    @Test
    void createCrew_Success() {
        // Arrange
        when(companyRepository.findById(testCompany.getId())).thenReturn(Optional.of(testCompany));
        when(crewRepository.existsByEmployeeId("EMP001")).thenReturn(false);
        when(crewRepository.existsByEmailIgnoreCase("test@example.com")).thenReturn(false);
        when(userService.createUser(any(), any())).thenReturn(testUserDto);
        when(userRepository.findById(testUserDto.getId())).thenReturn(Optional.of(testUser));
        when(crewRepository.save(any(Crew.class))).thenAnswer(invocation -> {
            Crew crew = invocation.getArgument(0);
            crew.setId(UUID.randomUUID());
            return crew;
        });

        // Act
        var result = crewService.createCrew(createCrewRequest);

        // Assert
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("EMP001", result.getEmployeeId());
        assertEquals(testCompany.getId(), result.getCompanyId());
        assertEquals(testUser.getId(), result.getUserId());
        assertEquals("Carpenter", result.getPosition());
        assertEquals("Construction", result.getDepartment());

        // Verify interactions
        verify(companyRepository).findById(testCompany.getId());
        verify(crewRepository).existsByEmployeeId("EMP001");
        verify(crewRepository).existsByEmailIgnoreCase("test@example.com");
        verify(userService).createUser(any(), any());
        verify(userRepository).findById(testUserDto.getId());
        verify(crewRepository).save(any(Crew.class));
    }

    @Test
    void createCrew_CompanyNotFound() {
        // Arrange
        when(companyRepository.findById(testCompany.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(Exception.class, () -> crewService.createCrew(createCrewRequest));
        verify(companyRepository).findById(testCompany.getId());
        verifyNoInteractions(crewRepository, userService, userRepository);
    }

    @Test
    void createCrew_EmployeeIdAlreadyExists() {
        // Arrange
        when(companyRepository.findById(testCompany.getId())).thenReturn(Optional.of(testCompany));
        when(crewRepository.existsByEmployeeId("EMP001")).thenReturn(true);

        // Act & Assert
        assertThrows(Exception.class, () -> crewService.createCrew(createCrewRequest));
        verify(companyRepository).findById(testCompany.getId());
        verify(crewRepository).existsByEmployeeId("EMP001");
        verifyNoInteractions(userService, userRepository);
    }

    @Test
    void createCrew_EmailAlreadyExists() {
        // Arrange
        when(companyRepository.findById(testCompany.getId())).thenReturn(Optional.of(testCompany));
        when(crewRepository.existsByEmployeeId("EMP001")).thenReturn(false);
        when(crewRepository.existsByEmailIgnoreCase("test@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(Exception.class, () -> crewService.createCrew(createCrewRequest));
        verify(companyRepository).findById(testCompany.getId());
        verify(crewRepository).existsByEmployeeId("EMP001");
        verify(crewRepository).existsByEmailIgnoreCase("test@example.com");
        verifyNoInteractions(userService, userRepository);
    }
}
