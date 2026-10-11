package phoenix.AM_PM.domain.projectplan.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ProjectPlanDTO {
    private int id;
    private int projectId;
    private String title;
    private String filePath;
    private String sampleUrl;
    private String sampleImg;
}

