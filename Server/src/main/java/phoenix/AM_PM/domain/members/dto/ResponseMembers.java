package phoenix.AM_PM.domain.members.dto;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.Getter;
import phoenix.AM_PM.domain.members.entity.Members;
import phoenix.AM_PM.domain.members.entity.Roles;

@AllArgsConstructor
@Getter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ResponseMembers {
    private Integer userId;
    private String nickName;
    private Roles roles;

    public static ResponseMembers from(Members members) {
        return new ResponseMembers(members.getUser().getId(), members.getUser().getNickname(), members.getRoles());
    }
}
