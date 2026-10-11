package phoenix.AM_PM.domain.user.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MypageUserDto {
    private String userId;
    private String nickname;
    private String password;
    private String profileImg;
    private String email;
    private String role;
}
