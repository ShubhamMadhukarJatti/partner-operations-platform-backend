package com.sharkdom.gtm.service;

import com.sharkdom.gtm.common.Status;
import com.sharkdom.gtm.dto.CreateTaskRequestDTO;
import com.sharkdom.gtm.dto.TaskResponseDTO;
import com.sharkdom.gtm.dto.UpdateTaskRequestDTO;
import com.sharkdom.gtm.entity.Task;
import com.sharkdom.gtm.repository.TaskRepository;
import com.sharkdom.repository.user.UserRepository;
import com.sharkdom.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    private final UserRepository userRepository;

    public TaskResponseDTO createTask(CreateTaskRequestDTO dto) {
        log.debug("TaskService :: Creating new task for orgId={}, title='{}'", dto.getOrganizationId(), dto.getTitle());
        Long orgIdFromToken = Util.getOrgIdFromToken();
        try {
            Task task = new Task();
            task.setTitle(dto.getTitle());
            task.setStatus(dto.getStatus());
            task.setStage(dto.getStage());
            task.setTargetType(dto.getTargetType());
            task.setStartDate(dto.getStartDate());
            task.setEndDate(dto.getEndDate());
            task.setOwnerId(dto.getOwner());
            task.setNote(dto.getNote());
            task.setExternalPartnerCode(dto.getExternalPartnerCode());
            task.setExternalPartnerId(dto.getExternalPartnerId());
            task.setOrganizationId(orgIdFromToken);
            task.setStartDate(Instant.now());
            task.setEndDate(Instant.now());
            Task saved = taskRepository.save(task);
            log.info("TaskService :: Task saved successfully with ID={} for organizationId={}",
                    saved.getId(), saved.getOrganizationId());

            return toResponseDTO1(saved);
        } catch (Exception e) {
            log.error("TaskService :: Error while creating task for orgId={}, title='{}'. Error: {}",
                    dto.getOrganizationId(), dto.getTitle(), e.getMessage(), e);
            throw e;
        }
    }


    public TaskResponseDTO getTaskById(Long id) {
        log.info("TaskService :: Fetching task by ID={}", id);

        Optional<Task> taskOpt = taskRepository.findById(id);
        if (taskOpt.isEmpty()) {
            log.warn("TaskService :: Task not found for ID={}", id);
            return null;
        }

        Task task = taskOpt.get();
        log.info("TaskService :: Task found with ID={}, title='{}'", task.getId(), task.getTitle());
        return toResponseDTO(task);
    }


    public Page<TaskResponseDTO> getTasks(int page, int size, String sortBy, String sortDir) {
        log.info("TaskService :: Fetching paginated task list page={}, size={}, sortBy={}, sortDir={}",
                page, size, sortBy, sortDir);

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Task> taskPage = taskRepository.findAll(pageable);

        List<TaskResponseDTO> taskList = taskPage.getContent().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());

        log.info("TaskService :: Fetched {} tasks out of total {}", taskList.size(), taskPage.getTotalElements());
        return new PageImpl<>(taskList, pageable, taskPage.getTotalElements());
    }

    public void deleteTask(Long id) {
        log.info("TaskService :: deleteTask() called with ID={}", id);

        var optionalTask = taskRepository.findById(id);
        if (optionalTask.isEmpty()) {
            log.warn("TaskService :: Task not found for ID={}", id);
            throw new RuntimeException("Task not found with ID=" + id);
        }

        taskRepository.deleteById(id);
        log.info("TaskService :: Task deleted successfully for ID={}", id);
    }

    public TaskResponseDTO updateTask(Long id, UpdateTaskRequestDTO dto) {
        log.info("TaskService :: updateTask() called with ID={}", id);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with ID=" + id));
        if (dto.getTitle() != null) task.setTitle(dto.getTitle());
        if (dto.getStatus() != null) task.setStatus(dto.getStatus());
        if (dto.getStage() != null) task.setStage(dto.getStage());
        if (dto.getTargetType() != null) task.setTargetType(dto.getTargetType());
        if (dto.getStartDate() != null) task.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) task.setEndDate(dto.getEndDate());
        if (dto.getOwner() != null) task.setOwnerId(dto.getOwner());
        if (dto.getNote() != null) task.setNote(dto.getNote());
        Task updated = taskRepository.save(task);
        log.info("TaskService :: Task updated successfully ID={}", updated.getId());
        return toResponseDTO(updated);
    }

    public TaskResponseDTO updateTaskStatus(Long id, Status newStatus) {
        log.info("TaskService :: updateTaskStatus() called for ID={} with status={}", id, newStatus);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with ID=" + id));
        task.setStatus(newStatus);
        Task saved = taskRepository.save(task);
        log.info("TaskService :: Task ID={} updated to status={}", id, newStatus);
        return toResponseDTO(saved);
    }

    public Page<TaskResponseDTO> getTasksByPartnerAndOrg(
            String externalPartnerCode,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Task> taskPage = taskRepository
                .findByExternalPartnerCodeAndOrganizationId(externalPartnerCode,Util.getOrgIdFromToken(), pageable);

        return taskPage.map(this::toResponseDTO);
    }

    private TaskResponseDTO toResponseDTO(Task task) {
        return TaskResponseDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .status(task.getStatus())
                .stage(task.getStage())
                .targetType(task.getTargetType())
                .startDate(task.getStartDate())
                .endDate(task.getEndDate())
                .owner(task.getOwnerId())
                .note(task.getNote())
                .externalPartnerCode(task.getExternalPartnerCode())
                .organizationId(task.getOrganizationId())
                .createdAt(task.getStartDate())
                .updatedAt(task.getEndDate())
                .username(userRepository.findByUserId(task.getOwnerId()).get().getName())
                .build();
    }

    private TaskResponseDTO toResponseDTO1(Task task) {
        return TaskResponseDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .status(task.getStatus())
                .stage(task.getStage())
                .targetType(task.getTargetType())
                .startDate(task.getStartDate())
                .endDate(task.getEndDate())
                .owner(task.getOwnerId())
                .note(task.getNote())
                .organizationId(task.getOrganizationId())
                .createdAt(task.getStartDate())
                .updatedAt(task.getEndDate())
                .build();
    }

}