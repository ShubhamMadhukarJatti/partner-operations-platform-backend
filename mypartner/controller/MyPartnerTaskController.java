package com.sharkdom.mypartner.controller;

import com.sharkdom.mypartner.dto.*;
import com.sharkdom.mypartner.dto.mypartnertask.CreateTaskDTO;
import com.sharkdom.mypartner.dto.mypartnertask.TaskResponseDTO;
import com.sharkdom.mypartner.dto.mypartnertask.UpdateTaskDTO;
import com.sharkdom.mypartner.dto.mypartnertask.UpdateTaskStatusDTO;
import com.sharkdom.mypartner.service.MyPartnerTaskService;
import com.sharkdom.util.SharkdomApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/my-partner/tasks")
@RequiredArgsConstructor
@Slf4j
public class MyPartnerTaskController {

    private final MyPartnerTaskService service;

    @Operation(summary = "Create a new partner task")
    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody CreateTaskDTO dto) {
        log.info("API request received: create task");

        TaskResponseDTO response = service.createTask(dto);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(true, "Task created successfully", response)
        );
    }

    @Operation(summary = "Update existing task")
    @PutMapping
    public ResponseEntity<?> updateTask(@RequestBody UpdateTaskDTO dto) {
        log.info("API request received: update task id: {}", dto.getTaskId());

        TaskResponseDTO response = service.updateTask(dto);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(true, "Task updated successfully", response)
        );
    }

    @Operation(summary = "Delete task by id")
    @DeleteMapping("/{taskId}")
    public ResponseEntity<?> deleteTask(@PathVariable Long taskId) {
        log.info("API request received: delete task id: {}", taskId);

        service.deleteTask(taskId);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(true, "Task deleted successfully", null)
        );
    }

    @Operation(summary = "Get all tasks by organizationId")
    @GetMapping("/organization/{myPartnerId}")
    public ResponseEntity<?> getTasksByOrganization(@PathVariable Long myPartnerId) {
        log.info("API request received: get tasks for organizationId: {}", myPartnerId);

        List<TaskResponseDTO> response = service.getTasksByOrganization(myPartnerId);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(true, "Tasks fetched successfully", response)
        );
    }

    @Operation(summary = "Update task status by taskId")
    @PutMapping("/status")
    public ResponseEntity<?> updateTaskStatus(@RequestBody UpdateTaskStatusDTO dto) {

        log.info("API request received: update task status for taskId: {}", dto.getTaskId());

        TaskResponseDTO response = service.updateTaskStatus(dto);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(true, "Task status updated successfully", response)
        );
    }
}