package phoenix.AM_PM.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import phoenix.AM_PM.domain.user.entity.User;
import phoenix.AM_PM.domain.user.repository.UserRepository;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest
public class UserRepositoryTest {

  @Autowired
  UserRepository userR;

  @Autowired
  PasswordEncoder passwordEncoder;

  @Test
  @Transactional
  @Rollback(value = false)
  public void save(){

    try {
      userR.save(User.builder()
          .userId("ampm1")
          .password(passwordEncoder.encode("1234"))
          .email("mmm@nae.com")
          .nickname("AM_PM")
          .roles("ROLE_ADMIN")
          .build());
      userR.save(User.builder()
          .userId("admin1")
          .password(passwordEncoder.encode("1234"))
          .email("admin123@nae.com")
          .nickname("불사조123")
          .roles("ROLE_ADMIN")
          .build());
    } catch (Exception e){
      System.out.println(e);
    }
    userR.findAll().stream().forEach(System.out::println);
  }

  @Test
  public void findUserId() {
    User user = userR.findByUserId("maeng").get();
    System.out.println("Test Find userId" + user);
  }

  @Test
  public void findEmail() {
    User user = userR.findByEmail("mmm@nae.com").get();
    System.out.println("Test Find Email" + user);
  }


}

