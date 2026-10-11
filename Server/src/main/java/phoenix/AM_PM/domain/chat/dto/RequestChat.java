package phoenix.AM_PM.domain.chat.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;
import phoenix.AM_PM.domain.chat.entity.Chat;

import java.time.LocalDateTime;
import java.util.List;

@ToString
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@AllArgsConstructor
@NoArgsConstructor
public class RequestChat {
    private String message;
    private Integer projectId;
    private Integer userId;
}
