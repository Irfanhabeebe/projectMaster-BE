package com.projectmaster.app.workflow.service;

import com.projectmaster.app.workflow.entity.StandardWorkflowDependency;
import com.projectmaster.app.workflow.entity.StandardDependencyEntityType;
import com.projectmaster.app.workflow.entity.DependencyType;
import com.projectmaster.app.workflow.repository.StandardWorkflowDependencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class StandardWorkflowDependencyService {
    
    private final StandardWorkflowDependencyRepository standardWorkflowDependencyRepository;
    
    /**
     * Add a dependency to a standard workflow template
     */
    @Transactional
    public void addStandardWorkflowDependency(UUID standardWorkflowTemplateId, 
                                            StandardWorkflowDependencyRequest request) {
        
        StandardWorkflowDependency dependency = StandardWorkflowDependency.builder()
            .standardWorkflowTemplateId(standardWorkflowTemplateId)
            .dependentEntityType(request.getDependentEntityType())
            .dependentEntityId(request.getDependentEntityId())
            .dependsOnEntityType(request.getDependsOnEntityType())
            .dependsOnEntityId(request.getDependsOnEntityId())
            .dependencyType(request.getDependencyType())
            .lagDays(request.getLagDays())
            .build();
        
        standardWorkflowDependencyRepository.save(dependency);
        
        log.info("Added standard workflow dependency: {} {} depends on {} {}", 
                request.getDependentEntityType(), request.getDependentEntityId(),
                request.getDependsOnEntityType(), request.getDependsOnEntityId());
    }
    
    /**
     * Get all dependencies for a standard workflow template
     */
    public List<StandardWorkflowDependency> getStandardWorkflowDependencies(UUID standardWorkflowTemplateId) {
        return standardWorkflowDependencyRepository.findByStandardWorkflowTemplateId(standardWorkflowTemplateId);
    }
    
    /**
     * Get dependencies for a specific entity in a standard workflow
     */
    public List<StandardWorkflowDependency> getEntityDependencies(UUID entityId, 
                                                                StandardDependencyEntityType entityType) {
        return standardWorkflowDependencyRepository
            .findByDependentEntityTypeAndDependentEntityId(entityType, entityId);
    }
    
    /**
     * Remove a dependency from a standard workflow template
     */
    @Transactional
    public void removeStandardWorkflowDependency(UUID dependencyId) {
        standardWorkflowDependencyRepository.deleteById(dependencyId);
        log.info("Removed standard workflow dependency: {}", dependencyId);
    }
    
    /**
     * Update a dependency in a standard workflow template
     */
    @Transactional
    public void updateStandardWorkflowDependency(UUID dependencyId, 
                                               StandardWorkflowDependencyRequest request) {
        
        StandardWorkflowDependency dependency = standardWorkflowDependencyRepository.findById(dependencyId)
            .orElseThrow(() -> new RuntimeException("Standard workflow dependency not found"));
        
        dependency.setDependentEntityType(request.getDependentEntityType());
        dependency.setDependentEntityId(request.getDependentEntityId());
        dependency.setDependsOnEntityType(request.getDependsOnEntityType());
        dependency.setDependsOnEntityId(request.getDependsOnEntityId());
        dependency.setDependencyType(request.getDependencyType());
        dependency.setLagDays(request.getLagDays());
        
        standardWorkflowDependencyRepository.save(dependency);
        
        log.info("Updated standard workflow dependency: {}", dependencyId);
    }
    
    // DTOs
    public static class StandardWorkflowDependencyRequest {
        private StandardDependencyEntityType dependentEntityType;
        private UUID dependentEntityId;
        private StandardDependencyEntityType dependsOnEntityType;
        private UUID dependsOnEntityId;
        private DependencyType dependencyType;
        private Integer lagDays;
        
        // Getters and setters
        public StandardDependencyEntityType getDependentEntityType() { return dependentEntityType; }
        public void setDependentEntityType(StandardDependencyEntityType dependentEntityType) { this.dependentEntityType = dependentEntityType; }
        
        public UUID getDependentEntityId() { return dependentEntityId; }
        public void setDependentEntityId(UUID dependentEntityId) { this.dependentEntityId = dependentEntityId; }
        
        public StandardDependencyEntityType getDependsOnEntityType() { return dependsOnEntityType; }
        public void setDependsOnEntityType(StandardDependencyEntityType dependsOnEntityType) { this.dependsOnEntityType = dependsOnEntityType; }
        
        public UUID getDependsOnEntityId() { return dependsOnEntityId; }
        public void setDependsOnEntityId(UUID dependsOnEntityId) { this.dependsOnEntityId = dependsOnEntityId; }
        
        public DependencyType getDependencyType() { return dependencyType; }
        public void setDependencyType(DependencyType dependencyType) { this.dependencyType = dependencyType; }
        
        public Integer getLagDays() { return lagDays; }
        public void setLagDays(Integer lagDays) { this.lagDays = lagDays; }
    }
}
