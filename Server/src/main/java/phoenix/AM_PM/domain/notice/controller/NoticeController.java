package phoenix.AM_PM.domain.notice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import phoenix.AM_PM.domain.notice.dto.NoticeDTO;
import phoenix.AM_PM.domain.notice.entity.Notice;
import phoenix.AM_PM.domain.notice.repository.NoticeRepository;
import phoenix.AM_PM.global.config.auth.MyUserDetails;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notice")
public class NoticeController {

    private final NoticeRepository noticeRepository;

    @PostMapping
    public ResponseEntity<String> addNotice(@AuthenticationPrincipal MyUserDetails userDetails, @RequestBody NoticeDTO dto) {
        try {
            String userId = userDetails.getUser().getUserId();
            if (userId == null) {
                throw new RuntimeException("User ID is null");
            }
            Notice noticeEntity = Notice.builder()
                    .projectId(dto.getProjectId())
                    .title(dto.getTitle())
                    .content(dto.getContent())
                    .userId(userId)
                    .build();
            noticeRepository.save(noticeEntity);
            return ResponseEntity.ok("successful");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to add notice: " + e.getMessage());
        }
    }

    @DeleteMapping("/{noticeId}")
    public ResponseEntity<String> deleteNotice(@PathVariable(name = "noticeId") String noticeId) {
        try {
            System.out.println(noticeId);
            noticeRepository.deleteById(noticeId);
            return ResponseEntity.ok("successful");
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Notice not found");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Delete failed");
        }
    }

    @PutMapping("/{noticeId}")
    public ResponseEntity<String> editNotice(@PathVariable(name = "noticeId") int noticeId, @RequestBody NoticeDTO dto) {
        try {
            Notice targetNotice = noticeRepository.findById(noticeId);

            if (targetNotice == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Notice not found for id: " + noticeId);
            }

            targetNotice.setTitle(dto.getTitle());
            targetNotice.setContent(dto.getContent());
            noticeRepository.save(targetNotice);
            return ResponseEntity.ok("successful");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<List<Notice>> getNotice(@PathVariable(name = "projectId") int projectId) {
        List<Notice> notices = noticeRepository.findByProjectId(projectId);
        return ResponseEntity.ok(notices);
    }
}
