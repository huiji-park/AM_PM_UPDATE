package phoenix.AM_PM.domain.project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import phoenix.AM_PM.domain.project.entity.Project;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
public class ResponseProject {
    private Integer id;
    private LocalDate startDate;
    private LocalDate endDate;
    private String title;
    private String content;

    public static ResponseProject of(Project project) {
        return new ResponseProject(project.getId(),
                project.getStartDate(),
                project.getEndDate(),
                project.getTitle(),
                project.getContent());
    }
}
