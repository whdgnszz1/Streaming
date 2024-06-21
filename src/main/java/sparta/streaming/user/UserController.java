package sparta.streaming.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import sparta.streaming.domain.User;
import sparta.streaming.dto.ResponseMessage;
import sparta.streaming.dto.user.CreateUserRequestDto;
import sparta.streaming.dto.user.PutUserRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sparta.streaming.dto.user.UserCommonDto;
import sparta.streaming.user.provider.JwtProvider;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    @Autowired
    private UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ResponseMessage> createUser(@RequestBody CreateUserRequestDto createUserRequestDto) {

        // 아이디 중복체크
        if (userService.idCheck(createUserRequestDto.getEmail()).isPresent()) {
            ResponseMessage response = ResponseMessage.builder()
                    .statusCode(400)
                    .resultMessage("Email is already in use")
                    .build();
            return ResponseEntity.status(400).body(response);
        }

        // 비밀번호 검증
        String password = createUserRequestDto.getPassword();
        if (!isPasswordValid(password)) {
            ResponseMessage response = ResponseMessage.builder()
                    .statusCode(400)
                    .resultMessage("Password does not meet the security requirements")
                    .build();
            return ResponseEntity.status(400).body(response);
        }

        User user = new User();
        user.setEmail(createUserRequestDto.getEmail());
        user.setName(createUserRequestDto.getName());  // username 설정
        user.setType("web");  // username 설정
        user.setPassword(passwordEncoder.encode(createUserRequestDto.getPassword())); // 비밀번호 암호화

        User createdUser = userService.createUser(user);
        ResponseMessage response = ResponseMessage.builder()
                .data(createdUser)
                .statusCode(201)
                .resultMessage("User created successfully")
                .build();
        return ResponseEntity.status(201).body(response);
    }

    // 비밀번호 유효성 검사
    private boolean isPasswordValid(String password) {
        String passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*?_])[A-Za-z\\d!@#$%^&*?_]{8,16}$";
        return password.matches(passwordPattern);
    }




    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ResponseMessage> login(@RequestBody UserCommonDto userCommonDto) {
        Optional<User> userOptional = userService.idCheck(userCommonDto.getEmail());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(userCommonDto.getPassword(), user.getPassword())) {
                ResponseMessage response = ResponseMessage.builder()
                        .statusCode(200)
                        .resultMessage("Login successful")
                        .build();
                return ResponseEntity.ok(response);
            }
        }
        ResponseMessage response = ResponseMessage.builder()
                .statusCode(401)
                .resultMessage("Invalid email or password")
                .build();
        return ResponseEntity.status(401).body(response);
        }




    // 유저 전체 조회
    @GetMapping
    public ResponseEntity<ResponseMessage> getAllUsers() {
        List<User> users = userService.getAllUsers();
        ResponseMessage response = ResponseMessage.builder()
                .data(users)
                .statusCode(200)
                .resultMessage("Success")
                .build();
        return ResponseEntity.ok(response);
    }

    // id로 유저 찾기
    @GetMapping("/{id}")
    public ResponseEntity<ResponseMessage> getUserById(@PathVariable("id") Long id) {
        Optional<User> user = userService.getUserById(id);
        if (user.isPresent()) {
            ResponseMessage response = ResponseMessage.builder()
                    .data(user.get())
                    .statusCode(200)
                    .resultMessage("Success")
                    .build();
            return ResponseEntity.ok(response);
        } else {
            ResponseMessage response = ResponseMessage.builder()
                    .statusCode(404)
                    .resultMessage("User not found")
                    .build();
            return ResponseEntity.status(404).body(response);
        }
    }

    //회원 수정
    @PutMapping()
    public ResponseEntity<ResponseMessage> updateUser(@RequestBody PutUserRequestDto userDetails) {
        try {
            User updatedUser = userService.updateUser(userDetails);
            ResponseMessage response = ResponseMessage.builder()
                    .data(updatedUser)
                    .statusCode(200)
                    .resultMessage("User updated successfully")
                    .build();
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ResponseMessage response = ResponseMessage.builder()
                    .statusCode(404)
                    .resultMessage("User not found")
                    .detailMessage(e.getMessage())
                    .build();
            return ResponseEntity.status(404).body(response);
        }
    }

    // 회원삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseMessage> deleteUser(@PathVariable("id") Long id) {
        try {
            userService.deleteUser(id);
            ResponseMessage response = ResponseMessage.builder()
                    .statusCode(204)
                    .resultMessage("User deleted successfully")
                    .build();
            return ResponseEntity.status(204).body(response);
        } catch (RuntimeException e) {
            ResponseMessage response = ResponseMessage.builder()
                    .statusCode(404)
                    .resultMessage("User not found")
                    .detailMessage(e.getMessage())
                    .build();
            return ResponseEntity.status(404).body(response);
        }
    }
}