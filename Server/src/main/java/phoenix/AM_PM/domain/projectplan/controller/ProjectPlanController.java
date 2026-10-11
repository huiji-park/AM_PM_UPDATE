package phoenix.AM_PM.domain.projectplan.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import phoenix.AM_PM.domain.projectplan.dto.ProjectPlanDTO;
import phoenix.AM_PM.domain.projectplan.service.ProjectPlanService;
import phoenix.AM_PM.global.upload.S3UploadService;


import java.io.IOException;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/plan")
public class ProjectPlanController {

    @Autowired
    private ProjectPlanService projectPlanService;

    @GetMapping("/project-plan/{projectId}")
    public ResponseEntity<ProjectPlanDTO> getProjectPlanByProjectId(@PathVariable("projectId") int projectId) {
        ProjectPlanDTO projectPlanDTO = projectPlanService.getProjectPlanByProjectId(projectId);
        return ResponseEntity.ok(projectPlanDTO);
    }

    @GetMapping("/etc-pages/{projectId}")
    public ResponseEntity<List<ProjectPlanDTO>> getEtcPagesByProjectId(@PathVariable("projectId") int projectId) {
        try {
            List<ProjectPlanDTO> pages = projectPlanService.getEtcPagesByProjectId(projectId);
            return ResponseEntity.ok(pages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{type}-example/{projectId}")
    public ResponseEntity<ProjectPlanDTO> getExample(@PathVariable("type") String type, @PathVariable("projectId") int projectId) {
        ProjectPlanDTO projectPlanDTO;
        switch (type.toLowerCase()) {
            case "srs":
                projectPlanDTO = projectPlanService.getSrsExampleByProjectId(projectId);
                break;
            case "erd":
                projectPlanDTO = projectPlanService.getErdExampleByProjectId(projectId);
                break;
            case "usecase":
                projectPlanDTO = projectPlanService.getUsecaseExampleByProjectId(projectId);
                break;
            case "ui":
                projectPlanDTO = projectPlanService.getUiExampleByProjectId(projectId);
                break;
            default:
                projectPlanDTO = projectPlanService.getCustomTypeExampleByProjectId(type, projectId);
                if (projectPlanDTO == null) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found for type: " + type);
                }
                break;
        }
        return ResponseEntity.ok(projectPlanDTO);
    }
/*
    @GetMapping("/{title}/{projectId}/{id}")
    public ResponseEntity<ProjectPlanDTO> getExample(@PathVariable("title") String title,
                                                     @PathVariable("projectId") int projectId
    {
        ProjectPlanDTO projectPlanDTO;
        switch (title.toLowerCase()) {
            case "srs":
                projectPlanDTO = projectPlanService.getSrsExampleByProjectId(projectId);
                break;
            case "erd":
                projectPlanDTO = projectPlanService.getErdExampleByProjectId(projectId);
                break;
            case "usecase":
                projectPlanDTO = projectPlanService.getUsecaseExampleByProjectId(projectId);
                break;
            case "ui":
                projectPlanDTO = projectPlanService.getUiExampleByProjectId(projectId);
                break;
            default:
                projectPlanDTO = projectPlanService.getExampleByProjectIdAndId(projectId);
                break;
        }
        return ResponseEntity.ok(projectPlanDTO);
    }
*/

/*
    @GetMapping("/{type}/{projectId}/{id}")
    public ResponseEntity<ProjectPlanDTO> getExample(@PathVariable("type") String type,
                                                     @PathVariable("projectId") int projectId,
                                                     @PathVariable("id") int id) {
        ProjectPlanDTO projectPlanDTO;
        switch (type.toLowerCase()) {
            case "srs":
                projectPlanDTO = projectPlanService.getSrsExampleByProjectId(projectId);
                break;
            case "erd":
                projectPlanDTO = projectPlanService.getErdExampleByProjectId(projectId);
                break;
            case "usecase":
                projectPlanDTO = projectPlanService.getUsecaseExampleByProjectId(projectId);
                break;
            case "ui":
                projectPlanDTO = projectPlanService.getUiExampleByProjectId(projectId);
                break;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid type");
        }
        return ResponseEntity.ok(projectPlanDTO);
    }
*/
    @GetMapping("/{id}")
    public ResponseEntity<?> getFindById(@PathVariable("id") int id) {
        try {
            ProjectPlanDTO projectPlan = projectPlanService.getFindById(id);
            return ResponseEntity.ok(projectPlan);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("id를 찾을 수 없습니다: " + e.getMessage());
        }
    }




    @PostMapping("/create-page")
    public ResponseEntity<?> createNewPage(@RequestBody ProjectPlanDTO projectPlanDTO) {
        try {
            ProjectPlanDTO newPage = projectPlanService.createNewPage(projectPlanDTO.getTitle(), projectPlanDTO.getProjectId());
            return ResponseEntity.ok(newPage);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("서버에서 오류가 발생했습니다.");
        }
    }




    @PostMapping("/upload-image/{id}/{projectId}")
    public ResponseEntity<?> uploadImage(
            @PathVariable("id") int id,
            @PathVariable("projectId") int projectId,
            @RequestParam("file") MultipartFile file) {
        try {
            projectPlanService.storeImage(id, projectId, file);
            return ResponseEntity.ok("이미지가 성공적으로 업로드 되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이미지 업로드 중 오류 발생: " + e.getMessage());
        }
    }



    @PostMapping("/user-{type}/{projectId}/{title}")
    public ResponseEntity<?> upload(@PathVariable("type") String type,
                                    @PathVariable("projectId") int projectId,
                                    @PathVariable("title") String title,
                                    @RequestParam("file") MultipartFile file) {
        try {
            switch (type.toLowerCase()) {
                case "srs":
                    projectPlanService.storeSrs(projectId, title, file);
                    break;
                case "erd":
                    projectPlanService.storeErd(projectId, title, file);
                    break;
                case "usecase":
                    projectPlanService.storeUsecase(projectId, title, file);
                    break;
                case "ui":
                    projectPlanService.storeUi(projectId, title, file);
                    break;
                default:
                    projectPlanService.storeCustomType(projectId, title, file, type);
                    break;
            }
            return ResponseEntity.ok(type.toUpperCase() + "가 성공적으로 업로드 되었습니다");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(type.toUpperCase() + " 업로드 중 오류 발생: " + e.getMessage());
        }
    }


    @PutMapping("/update-url/{projectId}/{title}")
    public ResponseEntity<?> updateSampleUrl(@PathVariable("projectId") int projectId,
                                             @PathVariable("title") String title,
                                             @RequestBody Map<String, String> request) {
        try {
            String newSampleUrl = request.get("newSampleUrl");
            ProjectPlanDTO updatedProjectPlan = projectPlanService.updateSampleUrl(projectId, title, newSampleUrl);
            return ResponseEntity.ok(updatedProjectPlan);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("샘플 URL 업데이트 중 오류 발생: " + e.getMessage());
        }
    }

    @PutMapping("/user-erd/{projectId}/{title}")
    public ResponseEntity<?> updateErd(@PathVariable int projectId, @PathVariable String title,
                                       @RequestParam("file") MultipartFile file) {
        projectPlanService.updateErd(projectId, title, file);
        return ResponseEntity.ok("ERD가 성공적으로 수정되었습니다.");
    }

    @PutMapping("/user-usecase/{projectId}/{title}")
    public ResponseEntity<?> updateUsecase(@PathVariable int projectId, @PathVariable String title,
                                           @RequestParam("file") MultipartFile file) {
        projectPlanService.updateUsecase(projectId, title, file);
        return ResponseEntity.ok("USECASE가 성공적으로 수정 되었습니다");
    }

    @PutMapping("/user-ui/{projectId}/{title}")
    public ResponseEntity<?> updateUi(@PathVariable int projectId, @PathVariable String title,
                                      @RequestParam("file") MultipartFile file) {
        projectPlanService.updateUi(projectId, title, file);
        return ResponseEntity.ok("UI가 성공적으로 수정 되었습니다.");
    }

/*
    @PostMapping("/create-page")
    public ResponseEntity<?> createNewPage(
            @RequestParam("projectId") int projectId,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("sampleUrl") String sampleUrl,
            @RequestParam("sampleImg") String sampleImg,
            @RequestParam("title") String title){
        try {
            ProjectPlanDTO projectPlanDTO = projectPlanService.createNewEtcPage(projectId, file, sampleUrl, sampleImg,title);
            return ResponseEntity.ok(projectPlanDTO);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("새 페이지를 만드는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }


    @GetMapping("/etc-pages/{projectId}")
    public ResponseEntity<List<ProjectPlanDTO>> getAllEtcPages(@PathVariable int projectId) {
        List<ProjectPlanDTO> etcPages = projectPlanService.getAllEtcPages(projectId);
        return ResponseEntity.ok(etcPages);
    }
*/

    @PutMapping("/update-etc/{id}")
    public ResponseEntity<?> updateEtcPage(@PathVariable int id,
                                           @RequestParam("sampleUrl") String newSampleUrl,
                                           @RequestParam("sampleImg") String newSampleImg) {
        try {
            ProjectPlanDTO updatedProjectPlanDTO = projectPlanService.updateEtcPage(id, newSampleUrl, newSampleImg);
            return ResponseEntity.ok(updatedProjectPlanDTO);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("\n" +
                    "페이지 업데이트 중 오류 발생: " + e.getMessage());
        }
    }

    @DeleteMapping("/{title}")
    public ResponseEntity<?> deleteProjectPlan(@PathVariable("title") String title) {
        projectPlanService.deleteProjectPlanByTitle(title);
        return ResponseEntity.ok().build();
    }

}
