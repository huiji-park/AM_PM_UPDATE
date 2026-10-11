package phoenix.AM_PM.domain.members.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import phoenix.AM_PM.domain.members.dto.RequestMembers;
import phoenix.AM_PM.domain.members.dto.ResponseMembers;
import phoenix.AM_PM.domain.members.service.MembersService;
import phoenix.AM_PM.global.config.auth.MyUserDetails;
import phoenix.AM_PM.global.config.service.JwtService;

import java.util.List;



@RestController
public class MembersController {

    private final MembersService membersService;
    private JwtService jwtService;

    public MembersController(MembersService membersService) {
        this.membersService = membersService;
    }

    // 프로젝트 멤버 조회
    @GetMapping("/api/members/{project-id}")
    public ResponseEntity getMembers(@RequestHeader(value = "Authorization", required = false) String token,@PathVariable("project-id") Integer projectId) {
        List<ResponseMembers> responseMembersList = membersService.getMembers(projectId);
        return ResponseEntity.ok().body(responseMembersList);
    }
    // 프로젝트 멤버 추가
    @PutMapping("/api/members/{project-id}")
    public ResponseEntity addMembers(@AuthenticationPrincipal MyUserDetails userDetails,
                                     @PathVariable("project-id") Integer projectId,
                                     @RequestBody RequestMembers requestMembers) {
        if(!membersService.checkAuthorization(userDetails.getUser().getId(), projectId))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        try{
            membersService.addMembers(projectId, requestMembers);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception e){
            System.out.println(e);
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
    // 프로젝트 멤버 삭제
    @DeleteMapping("/api/members/{project-id}")
    public ResponseEntity removeMembers(@AuthenticationPrincipal MyUserDetails userDetails, @PathVariable("project-id") Integer projectId,
                                     @RequestBody RequestMembers requestMembers) {
        if(!membersService.checkAuthorization(userDetails.getUser().getId(), projectId))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        try{
            membersService.removeMembers(projectId, requestMembers);
        } catch (Exception e) {
            System.out.println(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.RESET_CONTENT);
    }
    // 프로젝트 대표 멤버 조회
    @GetMapping("/api/representative_member/{project-id}")
    public ResponseEntity getRepresentativeMember(@PathVariable("project-id") Integer projectId,
                                                 @AuthenticationPrincipal MyUserDetails userDetails) {
        ResponseMembers responseMembers = membersService.getRepresentativeMember(projectId);

        boolean result = (responseMembers.getUserId() == userDetails.getUser().getId()) ? true : false;
        System.out.println("result == >>>>>>>>>>>>>>>>>>>>>>" + result);
        return ResponseEntity.ok().body(result);
    }
    // 프로젝트 대표 멤버 변경
    @PutMapping("/api/representative_member/{project-id}")
    public ResponseEntity modifyRepresentativeMember(@PathVariable("project-id") Integer projectId,
                                                     @RequestBody RequestMembers requestMembers,
                                                     @AuthenticationPrincipal MyUserDetails userDetails) {
        System.out.println("맴버 변경" + userDetails.getUser());
        if(!membersService.checkAuthorization(userDetails.getUser().getId(), projectId))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        membersService.modifyRepresentativeMember(projectId, requestMembers);
        return new ResponseEntity(HttpStatus.RESET_CONTENT);
    }
}
