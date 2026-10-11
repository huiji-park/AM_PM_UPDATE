package phoenix.AM_PM.domain.answer.dto;

import lombok.*;
import phoenix.AM_PM.domain.answer.entity.Answer;
import phoenix.AM_PM.domain.question.entity.Question;
import phoenix.AM_PM.domain.user.entity.User;



import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@ToString
public class AddAnswerRequest {

    private String createdDate;
    private String title;
    private String content;
    private Integer questionId;
    private boolean status;

    public Answer toEntity() {
        return Answer.builder()
                .questionId(questionId)
                .createdDate(LocalDateTime.now())
                .title(title)
                .content(content)
                .status(true)
                .build();
    }
}
