package phoenix.AM_PM.domain.notice.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class NoticeDTO {
    private int projectId;
    private String title;
    private String content;
}
