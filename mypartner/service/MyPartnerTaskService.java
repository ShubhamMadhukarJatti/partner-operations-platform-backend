package com.sharkdom.mypartner.service;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.mypartner.dto.*;
import com.sharkdom.mypartner.dto.mypartnertask.CreateTaskDTO;
import com.sharkdom.mypartner.dto.mypartnertask.TaskResponseDTO;
import com.sharkdom.mypartner.dto.mypartnertask.UpdateTaskDTO;
import com.sharkdom.mypartner.dto.mypartnertask.UpdateTaskStatusDTO;
import com.sharkdom.mypartner.entity.MyPartnerTask;
import com.sharkdom.mypartner.repository.MyPartnerTaskRepository;
import com.sharkdom.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyPartnerTaskService {

    private final MyPartnerTaskRepository repository;

    // CREATE
    public TaskResponseDTO createTask(CreateTaskDTO dto) {
        var organizationId = Util.getOrgIdFromToken();
        log.info("Creating task for organizationId: {}", organizationId);

        MyPartnerTask task = new MyPartnerTask();
        task.setTitle(dto.getTitle());
        task.setStatus(dto.getStatus());
        task.setStage(dto.getStage());
        task.setTargetType(dto.getTargetType());
        task.setStartDate(dto.getStartDate());
        task.setEndDate(dto.getEndDate());
        task.setOwnerId(dto.getOwnerId());
        task.setNote(dto.getNote());
        task.setOrganizationId(organizationId);
        task.setMyPartnerId(dto.getMyPartnerId());
        task.setUserName(dto.getUserName());

        MyPartnerTask saved = repository.save(task);

        log.info("Task created successfully with id: {}", saved.getId());
        return mapToDTO(saved);
    }

    // UPDATE
    public TaskResponseDTO updateTask(UpdateTaskDTO dto) {
        log.info("Updating task with id: {}", dto.getTaskId());

        MyPartnerTask task = repository.findById(dto.getTaskId())
                .orElseThrow(() -> new ServiceException(ErrorMessages.NOT_FOUND));

        if (dto.getTitle() != null) task.setTitle(dto.getTitle());
        if (dto.getStatus() != null) task.setStatus(dto.getStatus());
        if (dto.getStage() != null) task.setStage(dto.getStage());
        if (dto.getTargetType() != null) task.setTargetType(dto.getTargetType());
        if (dto.getStartDate() != null) task.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) task.setEndDate(dto.getEndDate());
        if (dto.getOwnerId() != null) task.setOwnerId(dto.getOwnerId());
        if (dto.getNote() != null) task.setNote(dto.getNote());
        if (dto.getMyPartnerId() != null) task.setMyPartnerId(dto.getMyPartnerId());
        if (dto.getUserName() != null) task.setUserName(dto.getUserName());

        MyPartnerTask updated = repository.save(task);

        log.info("Task updated successfully with id: {}", updated.getId());
        return mapToDTO(updated);
    }

    // DELETE
    public void deleteTask(Long taskId) {
        log.info("Deleting task with id: {}", taskId);

        MyPartnerTask task = repository.findById(taskId)
                .orElseThrow(() -> new ServiceException(ErrorMessages.NOT_FOUND));

        repository.delete(task);

        log.info("Task deleted successfully with id: {}", taskId);
    }

    // GET ALL BY ORG
    public List<TaskResponseDTO> getTasksByOrganization(Long myPartnerId) {
        Long organizationId=Util.getOrgIdFromToken();
        log.info("Fetching tasks for organizationId: {}", organizationId);

        List<MyPartnerTask> tasks = repository.findByOrganizationIdAndMyPartnerId(organizationId,myPartnerId);

        log.info("Total tasks found: {}", tasks.size());

        return tasks.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public TaskResponseDTO updateTaskStatus(UpdateTaskStatusDTO dto) {

        log.info("Updating status for taskId: {} to status: {}", dto.getTaskId(), dto.getStatus());

        MyPartnerTask task = repository.findById(dto.getTaskId())
                .orElseThrow(() -> {
                    log.error("Task not found with id: {}", dto.getTaskId());
                    return new ServiceException(ErrorMessages.NOT_FOUND);
                });

        task.setStatus(dto.getStatus());

        MyPartnerTask updatedTask = repository.save(task);

        log.info("Task status updated successfully for taskId: {}", updatedTask.getId());

        return mapToDTO(updatedTask);
    }

    // MAPPER
    private TaskResponseDTO mapToDTO(MyPartnerTask task) {
        TaskResponseDTO dto = new TaskResponseDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setStatus(task.getStatus());
        dto.setStage(task.getStage());
        dto.setTargetType(task.getTargetType());
        dto.setStartDate(task.getStartDate());
        dto.setEndDate(task.getEndDate());
        dto.setOwnerId(task.getOwnerId());
        dto.setNote(task.getNote());
        dto.setUserName(task.getUserName());
        dto.setMyPartnerId(task.getMyPartnerId());
        dto.setOrganizationId(task.getOrganizationId());
        return dto;
    }
}