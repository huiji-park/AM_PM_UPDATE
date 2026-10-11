package phoenix.AM_PM.domain.question.dto;

import lombok.*;

import java.io.Serializable;

@Data
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class QuestionCreateDTO implements Serializable {
    private String title;
    private String content;
}
