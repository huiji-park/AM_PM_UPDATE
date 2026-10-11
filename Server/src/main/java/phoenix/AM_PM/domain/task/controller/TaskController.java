package phoenix.AM_PM.domain.task.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import phoenix.AM_PM.domain.project.entity.Project;
import phoenix.AM_PM.domain.project.repository.ProjectRepository;
import phoenix.AM_PM.domain.task.dto.AddTaskDTO;
import phoenix.AM_PM.domain.task.dto.EditTaskDTO;
import phoenix.AM_PM.domain.task.entity.Task;
import phoenix.AM_PM.domain.task.repository.TaskRepository;
import phoenix.AM_PM.domain.user.entity.User;
import phoenix.AM_PM.domain.user.repository.UserRepository;
import phoenix.AM_PM.global.config.auth.MyUserDetails;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TaskController {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @GetMapping("/task/{projectId}")
    public ResponseEntity<List<Task>> getTasks(@PathVariable(name = "projectId") int projectId) {
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping("/task")
    public ResponseEntity<String> addTask(@AuthenticationPrincipal MyUserDetails userDetails, @RequestBody AddTaskDTO dto) {
        try {
            String userId = userDetails.getUser().getUserId();
            if (userId == null) {
                throw new RuntimeException("User ID is null");
            }
            Random random = new Random();
            int randomNumber = random.nextInt(5);
            String[] bgList = {"#02343F", "#331B3F", "#0A174E", "#07553B", "#50586C"};
            String[] fontColorList = {"#F0EDCC", "#ACC7B4", "#F5D042", "#CED46A", "#DCE2F0"};
            Optional<Project> projectOptional = projectRepository.findById(dto.getProjectId());
            Optional<User> UserOptional = userRepository.findByUserId(userId);
            if (UserOptional.isPresent()) {
                User user = UserOptional.get();
                if (projectOptional.isPresent()) {
                    Project projectInfo = projectOptional.get();
                    Task taskEntity = Task.builder()
                            .projectId(dto.getProjectId())
                            .Nickname(user.getNickname())
                            .content(dto.getContent())
                            .beginDate(projectInfo.getStartDate().atStartOfDay())
                            .endDate(projectInfo.getEndDate().atStartOfDay())
                            .backgroundColor(bgList[randomNumber])
                            .frontColor(fontColorList[randomNumber])
                            .build();
                    taskRepository.save(taskEntity);
                    return ResponseEntity.ok("successful");
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("project not found");
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("user not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to add notice: " + e.getMessage());
        }
    }

    @DeleteMapping("/task/{taskId}")
    public ResponseEntity<String> deleteTask(@PathVariable(name = "taskId") String taskId) {
        try {
            System.out.println(taskId);
            taskRepository.deleteById(taskId);
            return ResponseEntity.ok("successful");
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Notice not found");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Delete failed");
        }
    }

    @PutMapping("/task/{taskId}")
    public ResponseEntity<String> editTask(@PathVariable(name = "taskId") String taskId, @RequestBody EditTaskDTO dto) {
        try {
            Optional<Task> taskOptional = taskRepository.findById(taskId);
            if (taskOptional.isPresent()) {
                Task TaskInfo = taskOptional.get();
                TaskInfo.setBeginDate(dto.getBeginDate());
                TaskInfo.setEndDate(dto.getEndDate());
                TaskInfo.setContent(dto.getContent());
                taskRepository.save(TaskInfo);
                return ResponseEntity.ok("successful");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("task not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }
}
