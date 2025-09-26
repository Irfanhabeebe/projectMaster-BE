# Implementation Checklist & Testing Guide

## 📋 **Implementation Checklist**

### **Phase 1: Business Calendar Service ✅**
- [ ] **BusinessCalendarService.java**
  - [ ] `addBusinessDays(LocalDate startDate, int businessDays)`
  - [ ] `subtractBusinessDays(LocalDate endDate, int businessDays)`
  - [ ] `isBusinessDay(LocalDate date)`
  - [ ] `getBusinessDaysBetween(LocalDate startDate, LocalDate endDate)`
  - [ ] `getAustralianPublicHolidays(int year)`
  - [ ] `isPublicHoliday(LocalDate date)`

- [ ] **AustralianHolidayProvider.java**
  - [ ] Fixed holidays (New Year, Australia Day, Anzac Day, Christmas, Boxing Day)
  - [ ] Variable holidays (Good Friday, Easter Monday, Queen's Birthday)
  - [ ] State-specific holidays (Melbourne Cup - VIC, Foundation Day - WA)

- [ ] **Configuration**
  - [ ] Working days configuration (Mon-Fri default)
  - [ ] Work hours configuration (8 AM - 5 PM default)
  - [ ] Timezone support (Australia/Melbourne)

- [ ] **Unit Tests**
  - [ ] Test weekend exclusion
  - [ ] Test holiday exclusion
  - [ ] Test year boundary handling
  - [ ] Test leap year handling
  - [ ] Test negative business days
  - [ ] Test zero business days
  - [ ] Performance test for large date ranges

### **Phase 2: Core Schedule Calculator ✅**
- [ ] **ProjectScheduleCalculator.java**
  - [ ] `calculateProjectSchedule(UUID projectId)`
  - [ ] `recalculateFromDate(UUID projectId, LocalDate newStartDate)`
  - [ ] `recalculateFromChangedEntity(UUID entityId, DependencyEntityType type)`
  - [ ] `calculateStageSchedule(ProjectStage stage, Map<UUID, CalculatedDates> calculatedDates)`
  - [ ] `calculateTaskSchedule(ProjectTask task, Map<UUID, CalculatedDates> calculatedDates)`
  - [ ] `calculateStepSchedule(ProjectStep step, Map<UUID, CalculatedDates> calculatedDates)`

- [ ] **DTOs**
  - [ ] `ScheduleCalculationResult.java`
  - [ ] `CalculatedDates.java`
  - [ ] `SchedulingConflict.java`

- [ ] **Core Algorithm**
  - [ ] Topological sort for dependency order
  - [ ] Entity date calculation based on dependencies
  - [ ] Conflict detection and reporting
  - [ ] Database update with calculated dates

- [ ] **Integration Tests**
  - [ ] Simple linear dependency chain
  - [ ] Complex dependency networks
  - [ ] Circular dependency detection
  - [ ] Missing start date handling
  - [ ] Performance with large projects

### **Phase 3: Advanced Dependency Support ✅**
- [ ] **EnhancedDependencyResolver.java**
  - [ ] `calculateWithStartToStart(ProjectDependency dependency, LocalDate predecessorStartDate)`
  - [ ] `calculateWithFinishToFinish(ProjectDependency dependency, LocalDate predecessorEndDate)`
  - [ ] `calculateWithLagDays(LocalDate baseDate, int lagDays)`

- [ ] **Constraint Management**
  - [ ] `SchedulingConstraint.java` entity
  - [ ] Database migration for constraints table
  - [ ] Constraint validation logic
  - [ ] Constraint type support (MUST_START_ON, MUST_FINISH_BY, etc.)

- [ ] **Critical Path Integration**
  - [ ] Update critical path from schedule calculation
  - [ ] Slack calculation accuracy
  - [ ] Critical path visualization data

- [ ] **Advanced Tests**
  - [ ] START_TO_START dependency calculation
  - [ ] FINISH_TO_FINISH dependency calculation
  - [ ] Lag days with different dependency types
  - [ ] Constraint violation detection
  - [ ] Critical path accuracy verification

### **Phase 4: Schedule Update Engine ✅**
- [ ] **ScheduleUpdateService.java**
  - [ ] `updateScheduleOnDependencyChange(UUID projectId, List<UUID> changedDependencyIds)`
  - [ ] `updateScheduleOnDurationChange(UUID entityId, DependencyEntityType type, int oldDays, int newDays)`
  - [ ] `updateScheduleOnProgressUpdate(UUID entityId, DependencyEntityType type, LocalDate actualStartDate, LocalDate actualEndDate)`
  - [ ] `updateScheduleOnProjectStartChange(UUID projectId, LocalDate oldStartDate, LocalDate newStartDate)`

- [ ] **Impact Analysis**
  - [ ] `ScheduleImpactAnalysis.java` DTO
  - [ ] `ImpactedEntity.java` DTO
  - [ ] Impact calculation algorithms
  - [ ] Affected entity identification

- [ ] **Event System**
  - [ ] `ScheduleEventHandler.java`
  - [ ] Event listeners for various triggers
  - [ ] Async processing support
  - [ ] Event publishing

- [ ] **Real-time Tests**
  - [ ] Dependency change impact analysis
  - [ ] Duration change propagation
  - [ ] Progress update handling
  - [ ] Event system reliability
  - [ ] Performance under frequent updates

### **Phase 5: Calendar View API ✅**
- [ ] **Calendar DTOs**
  - [ ] `ProjectCalendarResponse.java`
  - [ ] `CalendarStage.java`
  - [ ] `CalendarTask.java`
  - [ ] `CalendarStep.java`
  - [ ] `CalendarMilestone.java`
  - [ ] `CalendarDependency.java`

- [ ] **ProjectCalendarController.java**
  - [ ] `GET /api/projects/{projectId}/calendar`
  - [ ] `GET /api/projects/{projectId}/calendar/gantt`
  - [ ] `POST /api/projects/{projectId}/calendar/recalculate`
  - [ ] `GET /api/projects/{projectId}/calendar/conflicts`

- [ ] **ProjectCalendarService.java**
  - [ ] Calendar data aggregation
  - [ ] Date range filtering
  - [ ] Status filtering
  - [ ] Resource allocation data

- [ ] **Export Features**
  - [ ] iCalendar export
  - [ ] MS Project export
  - [ ] Gantt chart data optimization

- [ ] **API Tests**
  - [ ] Calendar endpoint response validation
  - [ ] Date range filtering accuracy
  - [ ] Gantt chart data structure
  - [ ] Export functionality
  - [ ] Performance with large datasets

### **Phase 6: Integration & Performance ✅**
- [ ] **Integration Points**
  - [ ] Workflow engine integration
  - [ ] Assignment acceptance triggers
  - [ ] Progress update triggers
  - [ ] Dependency change triggers

- [ ] **Performance Optimization**
  - [ ] `ScheduleCalculationOptimizer.java`
  - [ ] Caching strategies
  - [ ] Incremental updates
  - [ ] Batch processing
  - [ ] Async processing

- [ ] **Monitoring & Metrics**
  - [ ] `SchedulingMetrics.java`
  - [ ] Performance monitoring
  - [ ] Accuracy tracking
  - [ ] Error rate monitoring

- [ ] **Production Readiness**
  - [ ] Error handling
  - [ ] Logging
  - [ ] Documentation
  - [ ] Deployment scripts

---

## 🧪 **Comprehensive Testing Strategy**

### **1. Unit Testing Scenarios**

#### **Business Calendar Service Tests**
```java
@ExtendWith(MockitoExtension.class)
class BusinessCalendarServiceTest {

    @Test
    @DisplayName("Should add business days excluding weekends")
    void testAddBusinessDays_ExcludingWeekends() {
        // Given: Friday January 5, 2024
        LocalDate friday = LocalDate.of(2024, 1, 5);
        
        // When: Add 1 business day
        LocalDate result = businessCalendarService.addBusinessDays(friday, 1);
        
        // Then: Should be Monday January 8, 2024
        assertEquals(LocalDate.of(2024, 1, 8), result);
    }

    @Test
    @DisplayName("Should add business days excluding Christmas Day")
    void testAddBusinessDays_ExcludingChristmas() {
        // Given: December 24, 2024 (Tuesday)
        LocalDate beforeChristmas = LocalDate.of(2024, 12, 24);
        
        // When: Add 1 business day
        LocalDate result = businessCalendarService.addBusinessDays(beforeChristmas, 1);
        
        // Then: Should be December 27, 2024 (Friday) - skipping Christmas Day
        assertEquals(LocalDate.of(2024, 12, 27), result);
    }

    @Test
    @DisplayName("Should handle Australia Day correctly")
    void testAustraliaDay_January26() {
        // Given: January 25, 2024 (Thursday)
        LocalDate beforeAustraliaDay = LocalDate.of(2024, 1, 25);
        
        // When: Add 1 business day
        LocalDate result = businessCalendarService.addBusinessDays(beforeAustraliaDay, 1);
        
        // Then: Should be January 29, 2024 (Monday) - skipping Friday Australia Day and weekend
        assertEquals(LocalDate.of(2024, 1, 29), result);
    }

    @Test
    @DisplayName("Should calculate business days between dates correctly")
    void testGetBusinessDaysBetween() {
        // Given: Monday to Friday (same week)
        LocalDate monday = LocalDate.of(2024, 1, 1);
        LocalDate friday = LocalDate.of(2024, 1, 5);
        
        // When: Calculate business days between
        int result = businessCalendarService.getBusinessDaysBetween(monday, friday);
        
        // Then: Should be 4 business days
        assertEquals(4, result);
    }

    @Test
    @DisplayName("Should handle leap year correctly")
    void testLeapYear_February29() {
        // Given: February 28, 2024 (leap year)
        LocalDate feb28 = LocalDate.of(2024, 2, 28);
        
        // When: Add 1 business day
        LocalDate result = businessCalendarService.addBusinessDays(feb28, 1);
        
        // Then: Should be February 29, 2024
        assertEquals(LocalDate.of(2024, 2, 29), result);
    }

    @Test
    @DisplayName("Should handle Melbourne Cup Day in Victoria")
    void testMelbourneCupDay_Victoria() {
        // Given: November 4, 2024 (Monday before Melbourne Cup Tuesday)
        LocalDate mondayBeforeCup = LocalDate.of(2024, 11, 4);
        
        // When: Add 1 business day in Victoria
        LocalDate result = businessCalendarService.addBusinessDays(mondayBeforeCup, 1, AustralianState.VIC);
        
        // Then: Should be November 6, 2024 (Wednesday) - skipping Melbourne Cup Tuesday
        assertEquals(LocalDate.of(2024, 11, 6), result);
    }

    @Test
    @DisplayName("Should handle negative business days")
    void testSubtractBusinessDays() {
        // Given: Wednesday January 10, 2024
        LocalDate wednesday = LocalDate.of(2024, 1, 10);
        
        // When: Subtract 3 business days
        LocalDate result = businessCalendarService.addBusinessDays(wednesday, -3);
        
        // Then: Should be Friday January 5, 2024
        assertEquals(LocalDate.of(2024, 1, 5), result);
    }

    @ParameterizedTest
    @CsvSource({
        "2024-01-01, true",  // New Year's Day
        "2024-01-26, true",  // Australia Day
        "2024-04-25, true",  // Anzac Day
        "2024-12-25, true",  // Christmas Day
        "2024-12-26, true",  // Boxing Day
        "2024-01-02, false", // Regular day
        "2024-06-15, false"  // Regular day
    })
    @DisplayName("Should identify public holidays correctly")
    void testIsPublicHoliday(LocalDate date, boolean expected) {
        assertEquals(expected, businessCalendarService.isPublicHoliday(date));
    }
}
```

#### **Schedule Calculator Tests**
```java
@ExtendWith(MockitoExtension.class)
class ProjectScheduleCalculatorTest {

    @Mock
    private BusinessCalendarService businessCalendarService;
    
    @Mock
    private ProjectDependencyRepository dependencyRepository;

    @InjectMocks
    private ProjectScheduleCalculator scheduleCalculator;

    @Test
    @DisplayName("Should calculate simple linear dependency chain")
    void testSimpleLinearDependencyChain() {
        // Given: Project with start date and simple A → B → C chain
        UUID projectId = UUID.randomUUID();
        LocalDate projectStartDate = LocalDate.of(2024, 3, 1);
        
        Project project = createTestProject(projectId, projectStartDate);
        ProjectTask taskA = createTestTask("Task A", 3); // 3 days
        ProjectTask taskB = createTestTask("Task B", 2); // 2 days
        ProjectTask taskC = createTestTask("Task C", 4); // 4 days
        
        List<ProjectDependency> dependencies = Arrays.asList(
            createDependency(taskB.getId(), taskA.getId(), FINISH_TO_START, 0),
            createDependency(taskC.getId(), taskB.getId(), FINISH_TO_START, 1) // 1 day lag
        );
        
        when(businessCalendarService.addBusinessDays(any(), anyInt()))
            .thenReturn(projectStartDate.plusDays(3))
            .thenReturn(projectStartDate.plusDays(5))
            .thenReturn(projectStartDate.plusDays(10));
        
        // When: Calculate schedule
        ScheduleCalculationResult result = scheduleCalculator.calculateProjectSchedule(projectId);
        
        // Then: Should calculate correct dates
        assertTrue(result.isSuccess());
        assertEquals(LocalDate.of(2024, 3, 1), result.getEntityDates().get(taskA.getId()).getStartDate());
        assertEquals(LocalDate.of(2024, 3, 4), result.getEntityDates().get(taskA.getId()).getEndDate());
        assertEquals(LocalDate.of(2024, 3, 5), result.getEntityDates().get(taskB.getId()).getStartDate());
        assertEquals(LocalDate.of(2024, 3, 6), result.getEntityDates().get(taskB.getId()).getEndDate());
        assertEquals(LocalDate.of(2024, 3, 8), result.getEntityDates().get(taskC.getId()).getStartDate()); // 1 day lag
        assertEquals(LocalDate.of(2024, 3, 11), result.getEntityDates().get(taskC.getId()).getEndDate());
    }

    @Test
    @DisplayName("Should handle parallel tasks correctly")
    void testParallelTasks() {
        // Given: Project with A → (B, C) parallel pattern
        UUID projectId = UUID.randomUUID();
        ProjectTask taskA = createTestTask("Task A", 2);
        ProjectTask taskB = createTestTask("Task B", 3);
        ProjectTask taskC = createTestTask("Task C", 4);
        
        List<ProjectDependency> dependencies = Arrays.asList(
            createDependency(taskB.getId(), taskA.getId(), FINISH_TO_START, 0),
            createDependency(taskC.getId(), taskA.getId(), FINISH_TO_START, 0)
        );
        
        // When: Calculate schedule
        ScheduleCalculationResult result = scheduleCalculator.calculateProjectSchedule(projectId);
        
        // Then: B and C should start on same date
        assertEquals(
            result.getEntityDates().get(taskB.getId()).getStartDate(),
            result.getEntityDates().get(taskC.getId()).getStartDate()
        );
    }

    @Test
    @DisplayName("Should detect circular dependencies")
    void testCircularDependencyDetection() {
        // Given: Project with circular dependency A → B → C → A
        UUID projectId = UUID.randomUUID();
        ProjectTask taskA = createTestTask("Task A", 2);
        ProjectTask taskB = createTestTask("Task B", 3);
        ProjectTask taskC = createTestTask("Task C", 1);
        
        List<ProjectDependency> dependencies = Arrays.asList(
            createDependency(taskB.getId(), taskA.getId(), FINISH_TO_START, 0),
            createDependency(taskC.getId(), taskB.getId(), FINISH_TO_START, 0),
            createDependency(taskA.getId(), taskC.getId(), FINISH_TO_START, 0) // Circular!
        );
        
        // When: Calculate schedule
        ScheduleCalculationResult result = scheduleCalculator.calculateProjectSchedule(projectId);
        
        // Then: Should detect circular dependency
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("circular dependency"));
    }

    @Test
    @DisplayName("Should handle START_TO_START dependencies")
    void testStartToStartDependency() {
        // Given: A START_TO_START B with 2-day lag
        UUID projectId = UUID.randomUUID();
        ProjectTask taskA = createTestTask("Task A", 5);
        ProjectTask taskB = createTestTask("Task B", 3);
        
        List<ProjectDependency> dependencies = Arrays.asList(
            createDependency(taskB.getId(), taskA.getId(), START_TO_START, 2)
        );
        
        // When: Calculate schedule
        ScheduleCalculationResult result = scheduleCalculator.calculateProjectSchedule(projectId);
        
        // Then: Task B should start 2 days after Task A starts
        LocalDate taskAStart = result.getEntityDates().get(taskA.getId()).getStartDate();
        LocalDate taskBStart = result.getEntityDates().get(taskB.getId()).getStartDate();
        assertEquals(2, ChronoUnit.DAYS.between(taskAStart, taskBStart));
    }

    @Test
    @DisplayName("Should handle FINISH_TO_FINISH dependencies")
    void testFinishToFinishDependency() {
        // Given: A FINISH_TO_FINISH B
        UUID projectId = UUID.randomUUID();
        ProjectTask taskA = createTestTask("Task A", 5);
        ProjectTask taskB = createTestTask("Task B", 3);
        
        List<ProjectDependency> dependencies = Arrays.asList(
            createDependency(taskB.getId(), taskA.getId(), FINISH_TO_FINISH, 0)
        );
        
        // When: Calculate schedule
        ScheduleCalculationResult result = scheduleCalculator.calculateProjectSchedule(projectId);
        
        // Then: Tasks should finish on same date
        LocalDate taskAEnd = result.getEntityDates().get(taskA.getId()).getEndDate();
        LocalDate taskBEnd = result.getEntityDates().get(taskB.getId()).getEndDate();
        assertEquals(taskAEnd, taskBEnd);
    }
}
```

### **2. Integration Testing Scenarios**

#### **Full Project Schedule Integration Test**
```java
@SpringBootTest
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
class ProjectScheduleIntegrationTest {

    @Autowired
    private ProjectScheduleCalculator scheduleCalculator;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private ProjectStageRepository stageRepository;
    
    @Autowired
    private ProjectTaskRepository taskRepository;
    
    @Autowired
    private ProjectStepRepository stepRepository;
    
    @Autowired
    private ProjectDependencyRepository dependencyRepository;

    @Test
    @DisplayName("Should calculate complete residential project schedule")
    void testCompleteResidentialProjectSchedule() {
        // Given: Complete residential project with all stages, tasks, steps
        UUID projectId = createCompleteResidentialProject();
        
        // When: Calculate full project schedule
        ScheduleCalculationResult result = scheduleCalculator.calculateProjectSchedule(projectId);
        
        // Then: Verify schedule accuracy
        assertTrue(result.isSuccess());
        assertNotNull(result.getProjectEndDate());
        
        // Verify foundation comes before framing
        LocalDate foundationEnd = getStageEndDate(result, "Foundation");
        LocalDate framingStart = getStageStartDate(result, "Framing");
        assertTrue(foundationEnd.isBefore(framingStart) || foundationEnd.equals(framingStart));
        
        // Verify services run parallel to external walls
        LocalDate servicesStart = getStageStartDate(result, "Services");
        LocalDate externalWallsStart = getStageStartDate(result, "External Walls");
        long daysBetween = ChronoUnit.DAYS.between(servicesStart, externalWallsStart);
        assertTrue(Math.abs(daysBetween) <= 3); // Within 3 days of each other
        
        // Verify total project duration is reasonable (3-6 months for single story)
        long totalDays = ChronoUnit.DAYS.between(result.getProjectStartDate(), result.getProjectEndDate());
        assertTrue(totalDays >= 90 && totalDays <= 180, "Project duration should be 3-6 months");
    }

    @Test
    @DisplayName("Should handle database updates correctly")
    void testDatabaseUpdates() {
        // Given: Project with calculated schedule
        UUID projectId = createSimpleTestProject();
        
        // When: Calculate schedule
        ScheduleCalculationResult result = scheduleCalculator.calculateProjectSchedule(projectId);
        
        // Then: Verify database was updated
        List<ProjectTask> tasks = taskRepository.findByProjectId(projectId);
        for (ProjectTask task : tasks) {
            CalculatedDates calculatedDates = result.getEntityDates().get(task.getId());
            if (calculatedDates != null) {
                assertEquals(calculatedDates.getStartDate(), task.getStartDate());
                assertEquals(calculatedDates.getEndDate(), task.getEndDate());
            }
        }
    }

    @Test
    @DisplayName("Should handle real dependency data correctly")
    void testWithRealDependencyData() {
        // Given: Project with dependencies from SQL insert statements
        UUID projectId = createProjectWithStandardDependencies();
        
        // When: Calculate schedule
        ScheduleCalculationResult result = scheduleCalculator.calculateProjectSchedule(projectId);
        
        // Then: Verify key construction sequences
        verifyFoundationSequence(result);
        verifyFramingSequence(result);
        verifyRoofingSequence(result);
        verifyFinishesSequence(result);
    }

    private void verifyFoundationSequence(ScheduleCalculationResult result) {
        // Site Survey → Building Footprint → Vegetation Removal → Site Grading → Foundation Trenches
        LocalDate surveyEnd = getStepEndDate(result, "Site Boundary Survey");
        LocalDate footprintStart = getStepStartDate(result, "Building Footprint Marking");
        assertTrue(footprintStart.equals(surveyEnd.plusDays(1)) || footprintStart.equals(surveyEnd));
        
        LocalDate vegetationEnd = getStepEndDate(result, "Vegetation Removal");
        LocalDate gradingStart = getStepStartDate(result, "Site Grading");
        assertTrue(gradingStart.equals(vegetationEnd.plusDays(1)) || gradingStart.equals(vegetationEnd));
    }
}
```

### **3. Performance Testing Scenarios**

#### **Large Project Performance Test**
```java
@SpringBootTest
class SchedulePerformanceTest {

    @Autowired
    private ProjectScheduleCalculator scheduleCalculator;

    @Test
    @DisplayName("Should calculate schedule for large project within 5 seconds")
    void testLargeProjectPerformance() {
        // Given: Project with 1000 tasks and 2000 dependencies
        UUID projectId = createLargeTestProject(1000, 2000);
        
        // When: Calculate schedule and measure time
        long startTime = System.currentTimeMillis();
        ScheduleCalculationResult result = scheduleCalculator.calculateProjectSchedule(projectId);
        long endTime = System.currentTimeMillis();
        
        // Then: Should complete within 5 seconds
        long durationMs = endTime - startTime;
        assertTrue(durationMs < 5000, "Large project calculation took " + durationMs + "ms");
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should handle concurrent schedule calculations")
    void testConcurrentCalculations() throws InterruptedException {
        // Given: Multiple projects
        List<UUID> projectIds = createMultipleTestProjects(10);
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Future<ScheduleCalculationResult>> futures = new ArrayList<>();
        
        // When: Calculate schedules concurrently
        for (UUID projectId : projectIds) {
            futures.add(executor.submit(() -> scheduleCalculator.calculateProjectSchedule(projectId)));
        }
        
        // Then: All should complete successfully
        for (Future<ScheduleCalculationResult> future : futures) {
            ScheduleCalculationResult result = future.get();
            assertTrue(result.isSuccess());
        }
        
        executor.shutdown();
    }

    @Test
    @DisplayName("Should maintain memory usage under 500MB for large projects")
    void testMemoryUsage() {
        // Given: Very large project
        UUID projectId = createLargeTestProject(5000, 10000);
        
        // When: Calculate schedule and monitor memory
        Runtime runtime = Runtime.getRuntime();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        
        ScheduleCalculationResult result = scheduleCalculator.calculateProjectSchedule(projectId);
        
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = memoryAfter - memoryBefore;
        
        // Then: Memory usage should be reasonable
        assertTrue(memoryUsed < 500_000_000, "Memory usage was " + (memoryUsed / 1_000_000) + "MB");
        assertTrue(result.isSuccess());
    }
}
```

### **4. End-to-End Testing Scenarios**

#### **Complete Workflow Integration Test**
```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SchedulingE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private WorkflowEngine workflowEngine;

    @Test
    @DisplayName("Should complete full project lifecycle with scheduling")
    void testCompleteProjectLifecycle() {
        // Given: New project created from standard template
        CreateProjectRequest projectRequest = createStandardProjectRequest();
        ProjectResponse project = projectService.createProject(UUID.randomUUID(), projectRequest);
        
        // When: Calculate initial schedule
        ResponseEntity<ApiResponse<ScheduleCalculationResult>> scheduleResponse = 
            restTemplate.postForEntity(
                "/api/projects/" + project.getId() + "/calendar/recalculate",
                null,
                new ParameterizedTypeReference<ApiResponse<ScheduleCalculationResult>>() {}
            );
        
        // Then: Schedule should be calculated
        assertTrue(scheduleResponse.getBody().isSuccess());
        ScheduleCalculationResult initialSchedule = scheduleResponse.getBody().getData();
        assertNotNull(initialSchedule.getProjectEndDate());
        
        // When: Progress some tasks and recalculate
        progressFirstFoundationTask(project.getId());
        
        ResponseEntity<ApiResponse<ScheduleCalculationResult>> updatedScheduleResponse = 
            restTemplate.postForEntity(
                "/api/projects/" + project.getId() + "/calendar/recalculate",
                null,
                new ParameterizedTypeReference<ApiResponse<ScheduleCalculationResult>>() {}
            );
        
        // Then: Schedule should be updated
        ScheduleCalculationResult updatedSchedule = updatedScheduleResponse.getBody().getData();
        
        // Verify dependent tasks start dates are updated
        verifyDependentTasksUpdated(initialSchedule, updatedSchedule);
        
        // When: Get calendar view
        ResponseEntity<ApiResponse<ProjectCalendarResponse>> calendarResponse = 
            restTemplate.getForEntity(
                "/api/projects/" + project.getId() + "/calendar",
                new ParameterizedTypeReference<ApiResponse<ProjectCalendarResponse>>() {}
            );
        
        // Then: Calendar should show updated schedule
        assertTrue(calendarResponse.getBody().isSuccess());
        ProjectCalendarResponse calendar = calendarResponse.getBody().getData();
        assertEquals(updatedSchedule.getProjectEndDate(), calendar.getProjectEndDate());
    }

    @Test
    @DisplayName("Should handle assignment acceptance triggering schedule updates")
    void testAssignmentTriggersScheduleUpdate() {
        // Given: Project with assignments
        UUID projectId = createProjectWithAssignments();
        UUID assignmentId = getFirstPendingAssignment(projectId);
        
        // When: Accept assignment
        acceptAssignment(assignmentId);
        
        // Then: Schedule should be automatically updated
        ProjectCalendarResponse calendar = getProjectCalendar(projectId);
        
        // Verify assigned step has scheduled dates
        CalendarStep assignedStep = findAssignedStep(calendar, assignmentId);
        assertNotNull(assignedStep.getStartDate());
        assertNotNull(assignedStep.getEndDate());
    }

    @Test
    @DisplayName("Should export calendar to iCalendar format")
    void testCalendarExport() {
        // Given: Project with calculated schedule
        UUID projectId = createProjectWithSchedule();
        
        // When: Export to iCalendar
        ResponseEntity<byte[]> response = restTemplate.getForEntity(
            "/api/projects/" + projectId + "/calendar/export/ical",
            byte[].class
        );
        
        // Then: Should return valid iCalendar data
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        String icalContent = new String(response.getBody());
        assertTrue(icalContent.contains("BEGIN:VCALENDAR"));
        assertTrue(icalContent.contains("BEGIN:VEVENT"));
        assertTrue(icalContent.contains("END:VCALENDAR"));
    }
}
```

### **5. Test Data Builders**

#### **Test Project Builder**
```java
@Component
public class TestProjectBuilder {
    
    public UUID createCompleteResidentialProject() {
        Project project = Project.builder()
            .name("Test Residential Project")
            .startDate(LocalDate.of(2024, 3, 1))
            .build();
        
        // Create stages
        ProjectStage foundation = createStage(project, "Foundation", 1);
        ProjectStage framing = createStage(project, "Framing", 2);
        ProjectStage roofing = createStage(project, "Roofing", 3);
        ProjectStage externalWalls = createStage(project, "External Walls", 4);
        ProjectStage services = createStage(project, "Services", 5);
        ProjectStage linings = createStage(project, "Internal Linings", 6);
        ProjectStage finishes = createStage(project, "Finishes", 7);
        
        // Create tasks and steps for each stage
        createFoundationTasks(foundation);
        createFramingTasks(framing);
        createRoofingTasks(roofing);
        createExternalWallsTasks(externalWalls);
        createServicesTasks(services);
        createLiningsTasks(linings);
        createFinishesTasks(finishes);
        
        // Create dependencies
        createStandardDependencies(project.getId());
        
        return project.getId();
    }
    
    private void createFoundationTasks(ProjectStage foundation) {
        ProjectTask sitePrepTask = createTask(foundation, "Site Preparation", 1, 5);
        ProjectTask foundationTask = createTask(foundation, "Foundation Work", 2, 8);
        ProjectTask drainageTask = createTask(foundation, "Drainage", 3, 3);
        
        // Create steps for each task
        createStep(sitePrepTask, "Site Boundary Survey", 1, 1);
        createStep(sitePrepTask, "Building Footprint Marking", 2, 1);
        createStep(sitePrepTask, "Vegetation Removal", 3, 2);
        createStep(sitePrepTask, "Site Grading", 4, 1);
        
        createStep(foundationTask, "Foundation Trenches", 1, 2);
        createStep(foundationTask, "Soil Compaction", 2, 1);
        createStep(foundationTask, "Reinforcement Installation", 3, 2);
        createStep(foundationTask, "Concrete Pouring", 4, 3);
        
        createStep(drainageTask, "Stormwater Pipes", 1, 2);
        createStep(drainageTask, "Drainage Testing", 2, 1);
    }
    
    // Similar methods for other stages...
}
```

#### **Test Dependency Builder**
```java
@Component
public class TestDependencyBuilder {
    
    public void createStandardDependencies(UUID projectId) {
        // Foundation sequence
        createDependency(projectId, "Building Footprint Marking", "Site Boundary Survey", FINISH_TO_START, 0);
        createDependency(projectId, "Vegetation Removal", "Building Footprint Marking", FINISH_TO_START, 0);
        createDependency(projectId, "Site Grading", "Vegetation Removal", FINISH_TO_START, 0);
        createDependency(projectId, "Foundation Trenches", "Site Grading", FINISH_TO_START, 1);
        
        // Framing sequence
        createDependency(projectId, "Floor Joists", "Drainage Testing", FINISH_TO_START, 3);
        createDependency(projectId, "Subfloor Installation", "Floor Joists", FINISH_TO_START, 0);
        createDependency(projectId, "Wall Studs", "Subfloor Installation", FINISH_TO_START, 0);
        
        // Parallel services
        createDependency(projectId, "Electrical Wiring", "Wall Bracing", FINISH_TO_START, 1);
        createDependency(projectId, "Plumbing Rough-in", "Wall Bracing", START_TO_START, 1);
        
        // Finishes
        createDependency(projectId, "Internal Painting", "Jointing & Finishing", FINISH_TO_START, 1);
        createDependency(projectId, "Floor Covering", "Jointing & Finishing", FINISH_TO_START, 2);
    }
    
    private void createDependency(UUID projectId, String dependentStepName, String dependsOnStepName, 
                                  DependencyType type, int lagDays) {
        UUID dependentStepId = findStepByName(projectId, dependentStepName);
        UUID dependsOnStepId = findStepByName(projectId, dependsOnStepName);
        
        ProjectDependency dependency = ProjectDependency.builder()
            .projectId(projectId)
            .dependentEntityId(dependentStepId)
            .dependentEntityType(DependencyEntityType.STEP)
            .dependsOnEntityId(dependsOnStepId)
            .dependsOnEntityType(DependencyEntityType.STEP)
            .dependencyType(type)
            .lagDays(lagDays)
            .status(DependencyStatus.PENDING)
            .build();
            
        dependencyRepository.save(dependency);
    }
}
```

### **6. Automated Test Execution**

#### **Test Automation Script**
```bash
#!/bin/bash
# run-scheduling-tests.sh

echo "🧪 Running Project Scheduling Test Suite"

# Phase 1: Unit Tests
echo "📅 Phase 1: Business Calendar Tests"
./mvnw test -Dtest=BusinessCalendarServiceTest

# Phase 2: Core Scheduling Tests  
echo "⚙️ Phase 2: Schedule Calculator Tests"
./mvnw test -Dtest=ProjectScheduleCalculatorTest

# Phase 3: Advanced Dependencies Tests
echo "🔗 Phase 3: Advanced Dependency Tests"
./mvnw test -Dtest=EnhancedDependencyResolverTest

# Phase 4: Update Engine Tests
echo "🔄 Phase 4: Schedule Update Tests"
./mvnw test -Dtest=ScheduleUpdateServiceTest

# Phase 5: Calendar API Tests
echo "📊 Phase 5: Calendar API Tests"
./mvnw test -Dtest=ProjectCalendarControllerTest

# Integration Tests
echo "🔗 Integration Tests"
./mvnw test -Dtest=ProjectScheduleIntegrationTest

# Performance Tests
echo "⚡ Performance Tests"
./mvnw test -Dtest=SchedulePerformanceTest

# End-to-End Tests
echo "🎯 End-to-End Tests"
./mvnw test -Dtest=SchedulingE2ETest

echo "✅ All Scheduling Tests Complete"
```

This comprehensive testing strategy ensures every aspect of the scheduling system is thoroughly validated before production deployment.
