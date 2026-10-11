package phoenix.AM_PM.domain.user.controller;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import phoenix.AM_PM.domain.refrash.service.RefreshTokenService;
import org.springframework.web.bind.annotation.*;
import phoenix.AM_PM.domain.user.dto.*;
import phoenix.AM_PM.domain.user.entity.User;
import phoenix.AM_PM.domain.user.repository.UserRepository;
import phoenix.AM_PM.domain.user.service.UserService;
import phoenix.AM_PM.global.config.auth.MyUserDetails;
import phoenix.AM_PM.global.config.jwt.JwtProperties;
import phoenix.AM_PM.global.config.service.JwtServiceImpl;
import phoenix.AM_PM.global.upload.S3UploadService;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtServiceImpl jwtService;
    private final RefreshTokenService refreshTokenService;

    private final S3UploadService s3UploadService;

    private JwtProperties jwtProperties;

    // 로그인
    @PostMapping("/api/auth/local")
    public ResponseEntity<String> login(@RequestBody LoginRequestDto loginDto,
                                        HttpServletResponse res) {
        return new ResponseEntity<>("로그인 성공", HttpStatus.OK);
    }

    @GetMapping("/api/auth/google")
    public ResponseEntity<String> googlelogin(HttpServletResponse res) {
        System.out.println("google login");
        return new ResponseEntity<>("로그인 성공", HttpStatus.OK);
    }

    @GetMapping("/login/oauth2/code/google")
    public ResponseEntity<String> googlelogin2(HttpServletResponse res) {
        System.out.println("google login");
        return new ResponseEntity<>("로그인 성공", HttpStatus.OK);
    }

    // 회원 가입
    @PostMapping("/api/user")
    public ResponseEntity<String> join(@RequestBody SaveUserDto dto, HttpServletResponse res) {
        if (userService.save(dto)) {
            return new ResponseEntity<>("성공적으로 회원 가입이 진행되었습니다.", HttpStatus.CREATED);
        }
        return new ResponseEntity<>("회원가입 오류", HttpStatus.BAD_REQUEST);

    }


    // 로그아웃
    @DeleteMapping("/api/auth")
    public ResponseEntity<String> logout(@RequestHeader(value = "Authorization", required = false) String token, HttpServletResponse res) {
        System.out.println("로그아웃!!!!");
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        refreshTokenService.delete(jwtService.getId(token));
        header.add("Authorization", "delete");

        return new ResponseEntity<>("로그아웃 성공", header, HttpStatus.OK);
    }

    @GetMapping("/api/atoken")
    public ResponseEntity<String> test1(@RequestHeader(value = "Authorization", required = false) String token, HttpServletResponse res) {
        System.out.println("access token 인증");
        System.out.println("first check " + token);
        System.out.println("Get ID Test!! => " + jwtService.getId(token));
//    jwtService.getClaims(token);

        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add("Authorization", token);

        return new ResponseEntity<>("Test", header, HttpStatus.OK);
    }

    @GetMapping("/api/rtoken")
    public ResponseEntity<String> test2(@RequestHeader(value = "RefreshToken", required = false) String token, HttpServletResponse res) {
        System.out.println("토큰 재발급");
        System.out.println(jwtService.getId(token));
        System.out.println(jwtService.getClaims(token));
        return new ResponseEntity<>("토큰 재발급", HttpStatus.OK);
    }

    @GetMapping("/api/user/user_id/{userId}")
    public ResponseEntity<Boolean> checkUserId(@PathVariable("userId") String userId) {
        Boolean body;
        try {
            userService.findbyUserId(userId).get();
            body = false;
        } catch (Exception e) {
            body = true;
        }
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @GetMapping("/api/user/nickname")
    public ResponseEntity<List<User>> findUserId(@RequestParam("nickname") String nickname) {

        return new ResponseEntity<>(userService.findbynickname(nickname), HttpStatus.OK);
    }

    @GetMapping("/api/user/email/{email}")
    public ResponseEntity<Boolean> checkEmail(@PathVariable("email") String email) {
        Boolean body;
        try {
            userService.findbyEmail(email);
            body = false;
        } catch (Exception e) {
            body = true;
        }
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    // 회원 정보 조회 & 수정
    @GetMapping("/api/user")
    public ResponseEntity<?> getUserInfo(@AuthenticationPrincipal MyUserDetails userDetails) {
        String userId = userDetails.getUser().getUserId();
        Optional<User> userInfo = userService.findbyUserId(userId);

        if (userInfo.isPresent()) {
            MypageUserDto mypageUserDto = userInfo.map(user -> MypageUserDto.builder()
                            .userId(user.getUserId())
                            .nickname(user.getNickname())
                            .password(user.getPassword())
                            .profileImg(user.getProfileImg())
                            .email(user.getEmail())
                            .role(user.getRoles())
                            .build())
                    .orElse(null);

            return ResponseEntity.ok().body(mypageUserDto);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    @PutMapping("/api/user")
    public ResponseEntity<String> editUserInfo(@AuthenticationPrincipal MyUserDetails userDetails, @RequestBody EditUserDto dto) {
        String userId = userDetails.getUser().getUserId();
        Optional<User> userInfoOptional = userService.findbyUserId(userId);

        if (userInfoOptional.isPresent()) {
            User userInfo = userInfoOptional.get();
            userInfo.setEmail(dto.getEmail());
            userInfo.setNickname(dto.getNickname());
            userInfo.setProfileImg(dto.getProfileImg());
            userInfo.setPassword(bCryptPasswordEncoder.encode(dto.getPassword()));

            userRepository.save(userInfo);
            return ResponseEntity.ok("successful");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    @GetMapping("/api/user/{user-id}")
    public ResponseEntity getUserDetail(@PathVariable("user-id") Integer userId) {
        ResponseUser responseUser = userService.getUserDetail(userId);
        return ResponseEntity.ok(responseUser);
    }

    @PutMapping("/api/profile_img")
    public ResponseEntity<String> editUserProfile(@AuthenticationPrincipal MyUserDetails userDetails, @RequestParam("file") MultipartFile file) throws IOException {
        String userId = userDetails.getUser().getUserId();
        Optional<User> userInfoOptional = userService.findbyUserId(userId);
        if (userInfoOptional.isPresent()) {
            User userInfo = userInfoOptional.get();
            userInfo.setProfileImg(s3UploadService.saveFile(file));
            userRepository.save(userInfo);
            return ResponseEntity.ok("successful");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }
}
