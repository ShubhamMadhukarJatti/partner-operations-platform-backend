package com.sharkdom.gtm.controller;

import com.sharkdom.gtm.dto.*;
import com.sharkdom.gtm.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<TaskResponseDTO>> createTask(@RequestBody CreateTaskRequestDTO dto) {
        log.info("TaskController :: createTask() called with title='{}', orgId={}, ownerId={}",
                dto.getTitle(), dto.getOrganizationId(), dto.getOwner());

        try {
            TaskResponseDTO response = taskService.createTask(dto);
            log.info("TaskController :: Task created successfully with ID={}", response.getId());

            return ResponseEntity.ok(
                    ApiResponse.<TaskResponseDTO>builder()
                            .status("SUCCESS")
                            .message("Task created successfully.")
                            .data(response)
                            .build()
            );
        } catch (Exception ex) {
            log.error("TaskController :: Failed to create task '{}', Error: {}", dto.getTitle(), ex.getMessage(), ex);
            return ResponseEntity.internalServerError().body(
                    ApiResponse.<TaskResponseDTO>builder()
                            .status("FAILED")
                            .message("Task creation failed: " + ex.getMessage())
                            .data(null)
                            .build()
            );
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponseDTO>> getTaskById(@PathVariable Long id) {
        log.info("TaskController :: getTaskById() called with ID={}", id);
        TaskResponseDTO task = taskService.getTaskById(id);

        if (task == null) {
            log.warn("TaskController :: Task not found for ID={}", id);
            return ResponseEntity.status(404).body(
                    ApiResponse.<TaskResponseDTO>builder()
                            .status("FAILED")
                            .message("Task not found with ID=" + id)
                            .data(null)
                            .build()
            );
        }

        log.info("TaskController :: Task fetched successfully for ID={}", id);
        return ResponseEntity.ok(
                ApiResponse.<TaskResponseDTO>builder()
                        .status("SUCCESS")
                        .message("Task fetched successfully.")
                        .data(task)
                        .build()
        );
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<Page<TaskResponseDTO>>> getAllTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("TaskController :: getAllTasks() called page={}, size={}, sortBy={}, sortDir={}",
                page, size, sortBy, sortDir);

        Page<TaskResponseDTO> taskPage = taskService.getTasks(page, size, sortBy, sortDir);
        return ResponseEntity.ok(
                ApiResponse.<Page<TaskResponseDTO>>builder()
                        .status("SUCCESS")
                        .message("Tasks fetched successfully.")
                        .data(taskPage)
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Long id) {
        log.info("TaskController :: deleteTask() called with ID={}", id);

        try {
            taskService.deleteTask(id);
            log.info("TaskController :: Task deleted successfully for ID={}", id);

            return ResponseEntity.ok(
                    ApiResponse.<Void>builder()
                            .status("SUCCESS")
                            .message("Task deleted successfully.")
                            .build()
            );

        } catch (RuntimeException ex) {
            log.warn("TaskController :: Task not found for ID={}, Error={}", id, ex.getMessage());
            return ResponseEntity.status(404).body(
                    ApiResponse.<Void>builder()
                            .status("FAILED")
                            .message("Task not found with ID=" + id)
                            .build()
            );

        } catch (Exception ex) {
            log.error("TaskController :: Failed to delete task ID={}, Error={}", id, ex.getMessage(), ex);
            return ResponseEntity.internalServerError().body(
                    ApiResponse.<Void>builder()
                            .status("FAILED")
                            .message("Task deletion failed: " + ex.getMessage())
                            .build()
            );
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponseDTO>> updateTask(
            @PathVariable Long id,
            @RequestBody UpdateTaskRequestDTO dto) {

        log.info("TaskController :: updateTask() called with ID={}", id);

        try {
            TaskResponseDTO updatedTask = taskService.updateTask(id, dto);

            return ResponseEntity.ok(
                    ApiResponse.<TaskResponseDTO>builder()
                            .status("SUCCESS")
                            .message("Task updated successfully.")
                            .data(updatedTask)
                            .build()
            );

        } catch (RuntimeException ex) {
            log.warn("TaskController :: {}", ex.getMessage());
            return ResponseEntity.status(404).body(
                    ApiResponse.<TaskResponseDTO>builder()
                            .status("FAILED")
                            .message(ex.getMessage())
                            .build()
            );

        } catch (Exception ex) {
            log.error("TaskController :: Error updating task: {}", ex.getMessage(), ex);
            return ResponseEntity.internalServerError().body(
                    ApiResponse.<TaskResponseDTO>builder()
                            .status("FAILED")
                            .message("Task update failed: " + ex.getMessage())
                            .build()
            );
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TaskResponseDTO>> updateTaskStatus(
            @PathVariable Long id,
            @RequestBody UpdateTaskStatusDTO dto) {

        log.info("TaskController :: updateTaskStatus() called for ID={} with status={}", id, dto.getStatus());

        try {
            TaskResponseDTO updatedTask = taskService.updateTaskStatus(id, dto.getStatus());

            return ResponseEntity.ok(
                    ApiResponse.<TaskResponseDTO>builder()
                            .status("SUCCESS")
                            .message("Task status updated successfully.")
                            .data(updatedTask)
                            .build()
            );

        } catch (RuntimeException ex) {
            return ResponseEntity.status(404).body(
                    ApiResponse.<TaskResponseDTO>builder()
                            .status("FAILED")
                            .message(ex.getMessage())
                            .build()
            );
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.<TaskResponseDTO>builder()
                            .status("FAILED")
                            .message("Task status update failed: " + ex.getMessage())
                            .build()
            );
        }
    }

    @GetMapping("/list/external/partner")
    public ResponseEntity<ApiResponse<Page<TaskResponseDTO>>> getTasksByPartnerAndOrg(
            @RequestParam String externalPartnerCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {

        log.info("TaskController :: getTasksByPartnerAndOrg() called externalPartnerId={}, page={}, size={}",
                externalPartnerCode, page, size);

        Page<TaskResponseDTO> taskPage =
                taskService.getTasksByPartnerAndOrg(externalPartnerCode, page, size, sortBy, sortDir);

        return ResponseEntity.ok(
                ApiResponse.<Page<TaskResponseDTO>>builder()
                        .status("SUCCESS")
                        .message("Tasks fetched successfully for the given partner & organization.")
                        .data(taskPage)
                        .build()
        );
    }

}