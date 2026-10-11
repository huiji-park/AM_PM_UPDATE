package phoenix.AM_PM.Member;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import phoenix.AM_PM.domain.members.entity.Members;
import phoenix.AM_PM.domain.members.entity.Roles;
import phoenix.AM_PM.domain.members.repository.MembersRepository;
import phoenix.AM_PM.domain.project.repository.ProjectRepository;
import phoenix.AM_PM.domain.user.repository.UserRepository;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
public class MemberRepositoryTest {
    @Autowired
    MembersRepository membersRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ProjectRepository projectRepository;

    @Test
    public void save() {
        Members entity = new Members().builder()
                .user(userRepository.findById(1).get())
                .project(projectRepository.findById(1).get())
                .roles(Roles.member)
                .build();
        membersRepository.save(entity);

        membersRepository.findAll().stream().forEach(System.out::println);
    }
}
