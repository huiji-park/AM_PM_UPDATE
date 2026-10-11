package phoenix.AM_PM.domain.answer.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import phoenix.AM_PM.domain.answer.entity.Answer;

import java.util.Optional;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Integer> {

//    @Query("SELECT a from Answer a where a.question.id=:bulletinId and a.id>0 order by a.id ASC")
//    public List<Answer> getAnswer(@Param("bulletinId") int bulletinId);
//    List<Answer> findAllByBulletinId(int bulletinId);
//    List<Answer> findAllByUserId(String userId);

    public Optional<Answer> findByQuestionId(Integer questionId);
    public void deleteByQuestionId(Integer questionId);
}
