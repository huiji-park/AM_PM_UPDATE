package phoenix.AM_PM.domain.members.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import phoenix.AM_PM.domain.members.dto.RequestMembers;
import phoenix.AM_PM.domain.members.dto.ResponseMembers;
import phoenix.AM_PM.domain.members.entity.Members;
import phoenix.AM_PM.domain.members.entity.Roles;
import phoenix.AM_PM.domain.members.repository.MembersRepository;
import phoenix.AM_PM.domain.project.entity.Project;
import phoenix.AM_PM.domain.project.repository.ProjectRepository;
import phoenix.AM_PM.domain.user.repository.UserRepository;
import phoenix.AM_PM.global.exception.BusinessLogicException;
import phoenix.AM_PM.global.exception.ExceptionCode;

import java.util.ArrayList;
import java.util.List;

@Service
public class MembersService {
    private final MembersRepository membersRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public MembersService(MembersRepository membersRepository, ProjectRepository projectRepository, UserRepository userRepository) {
        this.membersRepository = membersRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public List<ResponseMembers> getMembers(Integer projectId) {
        List<Members> membersList = membersRepository.findAllByProjectId(projectId);
        List<ResponseMembers> responseMembersList = new ArrayList<>();
        membersList.forEach(members -> responseMembersList.add(ResponseMembers.from(members)));
        return responseMembersList;
    }

    @Transactional
    public void addMembers(Integer projectId, RequestMembers requestMembers) {
        Project project = projectRepository.findById(projectId).orElseThrow(()-> new BusinessLogicException(ExceptionCode.PROJECT_NOT_FOUND));

        for(Integer userId : requestMembers.getMembers()) {
            Members entity = new Members().builder()
                    .user(userRepository.findById(userId).orElseThrow(()-> new BusinessLogicException(ExceptionCode.USER_NOT_FOUND)))
                    .project(project)
                    .roles(Roles.member)
                    .build();
            membersRepository.save(entity);
        }
    }

    @Transactional
    public void removeMembers(Integer projectId, RequestMembers requestMembers) {
        Project project = projectRepository.findById(projectId).orElseThrow(()-> new BusinessLogicException(ExceptionCode.PROJECT_NOT_FOUND));
        for(Integer userId : requestMembers.getMembers()) {
            Members members = membersRepository.findAllByUserIdAndProjectId(userId, projectId).orElseThrow(()-> new BusinessLogicException(ExceptionCode.USER_NOT_FOUND));
            membersRepository.delete(members);
        }
    }

    public ResponseMembers getRepresentativeMember(Integer projectId) {
        Members members = membersRepository.findAllByProjectIdAndRoles(projectId, Roles.representative_member).orElseThrow(()-> new BusinessLogicException(ExceptionCode.USER_NOT_FOUND));
        return ResponseMembers.from(members);
    }

    @Transactional
    public void modifyRepresentativeMember(Integer projectId, RequestMembers requestMembers) {
        Project project = projectRepository.findById(projectId).orElseThrow(()-> new BusinessLogicException(ExceptionCode.PROJECT_NOT_FOUND));
        Members members = membersRepository.findAllByUserIdAndProjectId(requestMembers.getMembers().get(0), projectId).orElseThrow(() -> new BusinessLogicException(ExceptionCode.USER_NOT_FOUND));
        Members oldMembers = membersRepository.findAllByProjectIdAndRoles(projectId, Roles.representative_member).orElseThrow(() -> new BusinessLogicException(ExceptionCode.PROJECT_NOT_FOUND));

        oldMembers.updateRoles(Roles.member);
        members.updateRoles(Roles.representative_member);
    }

    public boolean checkAuthorization(Integer userId, Integer projectId) {
        Members members = membersRepository.findAllByUserIdAndProjectId(userId, projectId).orElseThrow(() -> new BusinessLogicException(ExceptionCode.PROJECT_NOT_FOUND));
        return members.getRoles().equals(Roles.representative_member);
    }
}
