package phoenix.AM_PM.domain.user.dto;

import lombok.Data;

@Data
public class LoginRequestDto {
	private String userId;
	private String password;
}
