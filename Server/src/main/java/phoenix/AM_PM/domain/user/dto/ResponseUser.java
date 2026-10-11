package phoenix.AM_PM.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import phoenix.AM_PM.domain.user.entity.User;

@AllArgsConstructor
@Getter
public class ResponseUser {
    private Integer userId;
    private String nickname;
    private String profileImg;
    private String email;

    public static ResponseUser from(User user) {
        return new ResponseUser(user.getId(),
                user.getNickname(),
                user.getProfileImg(),
                user.getEmail());
    }
}
