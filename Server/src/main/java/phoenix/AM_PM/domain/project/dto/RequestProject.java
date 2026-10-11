package phoenix.AM_PM.domain.project.dto;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@ToString
public class RequestProject {
    private LocalDate startDate;
    private LocalDate endDate;
    private String title;
    private String content;
}
