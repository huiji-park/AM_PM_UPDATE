package phoenix.AM_PM.domain.user.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveUserDto {
  private String userId;
  private String password;
  private String nickname;
  private String profileImg;
  private String email;
  private String roles;
}
