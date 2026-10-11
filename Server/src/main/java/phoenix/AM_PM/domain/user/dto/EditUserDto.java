package phoenix.AM_PM.domain.user.dto;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class EditUserDto {
    private String password;
    private String confirmPassword;
    private String nickname;
    private String profileImg;
    private String email;
}
