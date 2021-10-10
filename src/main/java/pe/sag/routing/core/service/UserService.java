package pe.sag.routing.core.service;

import org.springframework.stereotype.Service;
import pe.sag.routing.shared.util.enums.Role;
import pe.sag.routing.core.model.User;
import pe.sag.routing.data.parser.UserParser;
import pe.sag.routing.data.repository.UserRepository;
import pe.sag.routing.shared.dto.UserDto;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(UserDto userDto) {
        User user = UserParser.fromDto(userDto);
//        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User createAdmin() {
        User admin = User.builder()
                .code("US00")
                .firstName("admin")
                .lastName("test")
                .email("test@test.com")
                .password("test")
                .roles(List.of(Role.ADMIN, Role.MANAGER))
                .active(true)
                .build();
        return userRepository.save(admin);
    }

    public User findByEmail(String email) throws IllegalAccessException {
        return userRepository.findByEmail(email).orElseThrow(IllegalAccessException::new);
    }

    public User findByCode(String code) throws IllegalAccessException {
        return userRepository.findByCode(code).orElseThrow(IllegalAccessException::new);
    }
}
