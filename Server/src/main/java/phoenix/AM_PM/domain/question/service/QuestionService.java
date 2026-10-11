package phoenix.AM_PM.domain.question.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import phoenix.AM_PM.domain.question.dto.QuestionDTO;
import phoenix.AM_PM.domain.question.entity.Question;
import phoenix.AM_PM.domain.question.repository.QuestionRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class QuestionService {

    private final QuestionRepository questionR;


    public List<Question> findAll(){
        return questionR.findAll();
    }
    //목록 가져오기
    public List<Question> getQuestionList(String userId){
        return questionR.findByUserId(userId);
    }
    //게시글 가져오기
    public QuestionDTO getQuestion(int id){
        Question question = questionR.findById(id).orElseThrow(() -> new RuntimeException("게시글 없음"));
        return QuestionDTO.builder()
                .userId(question.getUserId())
                .title(question.getTitle())
                .content(question.getContent())
                .createdDate(question.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")))
                .build();
    }
    //게시글 등록
    public Question create(QuestionDTO questionDTO, String name){
        Question question = Question.builder()
                .userId(name)
                .title(questionDTO.getTitle())
                .content(questionDTO.getContent())
                .createdDate(LocalDateTime.now())
                .projectId(0)
                .status(false)
                .build();
        return questionR.save(question);
    }
    //게시글 수정
    public Question update(QuestionDTO questionDTO){
        Question question = questionR.findById(questionDTO.getId()).orElseThrow(() -> new RuntimeException("게시글 x"));
        question.setTitle(questionDTO.getTitle());
        question.setContent(questionDTO.getContent());
        return questionR.save(question);
    }
    //게시글 삭제
    public void delete(int id){
        Question question = questionR.findById(id).orElseThrow(() -> new RuntimeException("게시글 x"));
        questionR.delete(question);
    }

    //Status 업데이트
    @Transactional
    public Question updateStatus(int id){
        Question question = questionR.findById(id).orElseThrow(() -> new RuntimeException("게시글 x"));
        question.setStatus(true);
        return questionR.save(question);
    }

    //Status 업데이트
    @Transactional
    public Question deleteStatus(int id){
        Question question = questionR.findById(id).orElseThrow(() -> new RuntimeException("게시글 x"));
        question.setStatus(false);
        return questionR.save(question);
    }
}
