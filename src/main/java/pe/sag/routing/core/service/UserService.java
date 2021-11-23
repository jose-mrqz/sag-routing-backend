package pe.sag.routing.core.service;

import org.springframework.stereotype.Service;
import pe.sag.routing.core.model.Order;
import pe.sag.routing.data.parser.TruckParser;
import pe.sag.routing.shared.dto.TruckDto;
import pe.sag.routing.shared.util.enums.Role;
import pe.sag.routing.core.model.User;
import pe.sag.routing.data.parser.UserParser;
import pe.sag.routing.data.repository.UserRepository;
import pe.sag.routing.shared.dto.UserDto;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(UserDto userDto) {
        User user = UserParser.fromDto(userDto);

        User lastUser = findFirstByOrderByCodeDesc();
        String userCode = "US";
        if (lastUser == null) userCode = userCode.concat("01");
        else {
            int code = Integer.parseInt(lastUser.getCode().substring(2))+1;
            if(code < 10) userCode = userCode.concat("0");
            userCode = userCode.concat(String.valueOf(code));
        }
        user.setCode(userCode);

//        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public List<UserDto> list() {
        return userRepository.findAll().stream().map(UserParser::toDto).collect(Collectors.toList());
    }

    public User createAdmin() {
        User admin = User.builder()
                .code("US00")
                .firstName("admin")
                .lastName("test")
                .username("admin_test")
                .password("test")
                .roles(List.of(Role.ADMINISTRADOR))
                .active(true)
                .build();
        return userRepository.save(admin);
    }

    public User findFirstByOrderByCodeDesc() {
        Optional<User> lastUser = userRepository.findFirstByOrderByCodeDesc();
        return lastUser.orElse(null);
    }

    public User findByCode(String code) {
        return userRepository.findByCode(code).orElse(null);
    }

    public User edit(User user, UserDto request) {
        user.setUsername(request.getUsername());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        if(request.getPassword() != null) user.setPassword(request.getPassword());
        List<String> roles = List.of(request.getRole());
        user.setRoles(roles.stream().map(Role::valueOf).collect(Collectors.toList()));
        return userRepository.save(user);
    }

    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    public List<String> listRoles() {
        List<Role> roles = List.of(Role.ADMINISTRADOR, Role.PLANIFICADOR, Role.GERENTE);
        return roles.stream().map(Role::toString).collect(Collectors.toList());
    }

    public User findByUsername(String username){
        Optional<User> user = userRepository.findByUsername(username);
        return user.orElse(null);
    }

    public boolean usernameIsUnique(String username){
        List<User> users = userRepository.findAll();
        for(User u : users){
            if(u.getUsername().compareTo(username) == 0){
                return false;
            }
        }
        return true;
    }
}
