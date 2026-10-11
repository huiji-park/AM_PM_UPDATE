//package phoenix.AM_PM.answer;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Order;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import phoenix.AM_PM.domain.answer.entity.Answer;
//import phoenix.AM_PM.domain.answer.repository.AnswerRepository;
//
//import java.util.List;
//
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@DataJpaTest
//public class AnswerRepositoryTest {
//
//    @Autowired
//    private AnswerRepository answerR;
//
//    @BeforeEach
//    public void setup() {
//        System.out.println("=".repeat(80));
//    }


//
//    @Order(1)
//    //@Test
//    void saveBoard(){
//        Answer answer = Answer.builder().id(1).build();
//        answerR.save(answer);
//    }
//
//    @Order(2)
//    @Test
//    public void testFindAll() {
//        List<Answer> answer = answerR.findAll();
//        answer.forEach(System.out::println);
//    }
//
//    @Order(3)
//    //@Test
//    void answerDelete(){
//        answerR.delete(Answer.builder().bulletinId("").build());
//    }
//
//}
