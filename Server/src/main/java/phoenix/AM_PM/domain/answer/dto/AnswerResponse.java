package phoenix.AM_PM.domain.answer.dto;

import lombok.Getter;
import phoenix.AM_PM.domain.answer.entity.Answer;

@Getter
public class AnswerResponse {
    private final String title;
    private final String content;

    public AnswerResponse(Answer answer) {
        this.title = answer.getTitle();
        this.content = answer.getContent();
    }
}
