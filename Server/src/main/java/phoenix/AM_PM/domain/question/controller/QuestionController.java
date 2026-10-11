package phoenix.AM_PM.domain.question.controller;


import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import phoenix.AM_PM.domain.answer.service.AnswerService;
import phoenix.AM_PM.domain.question.dto.QuestionCreateDTO;
import phoenix.AM_PM.domain.question.dto.QuestionDTO;
import phoenix.AM_PM.domain.question.entity.Question;
import phoenix.AM_PM.domain.question.service.QuestionService;
import phoenix.AM_PM.domain.user.entity.User;
import phoenix.AM_PM.domain.user.service.UserService;
import phoenix.AM_PM.global.config.service.JwtService;

import java.util.List;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/question")
public class QuestionController {
    private final QuestionService questionService;
    private final UserService userService;
    private final JwtService jwtService;
    @GetMapping("")
    public List<Question> questionlist(@RequestHeader(value = "Authorization", required = false) String token){
        String name = jwtService.getId(token);

        if (userService.findbyUserId(jwtService.getId(token)).get().getRoles().equals("ROLE_ADMIN")){
            return questionService.findAll();
        }
        else {
            return questionService.getQuestionList(name);
        }

    }

    @GetMapping("/{question-id}")
    public QuestionDTO getQuestion(@PathVariable(name = "question-id") int id){
        return questionService.getQuestion(id);
    }

    @PostMapping("")
    public boolean create(@RequestHeader(name = "Authorization", required = false) String token, @RequestBody QuestionDTO questionDTO){
        String name = jwtService.getId(token);
        System.out.println(questionDTO);
        boolean result = false;

        try{
            questionService.create(questionDTO, name);
            result = true;
        } catch (Exception e) {
            System.out.println("오류: " + e.getMessage());
        }
        return result;
    }

    @PutMapping("")
    public Question update(@RequestBody QuestionDTO questionDTO){
        System.out.println(questionDTO);

        return questionService.update(questionDTO);
    }

    @DeleteMapping("/{question-id}")
    public void delete(@PathVariable(name = "question-id") int id){
        questionService.delete(id);
    }

}
