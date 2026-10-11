package phoenix.AM_PM.domain.task.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class EditTaskDTO {
    private LocalDateTime beginDate;
    private LocalDateTime endDate;
    private String content;
}
