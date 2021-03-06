package pe.sag.routing.data.parser;

import pe.sag.routing.core.model.User;
import pe.sag.routing.shared.dto.UserDto;
import pe.sag.routing.shared.util.enums.Role;

import java.util.List;
import java.util.stream.Collectors;

public class UserParser {
    public static User fromDto(UserDto userDto) {
        List<String> roles = List.of(userDto.getRole());
        User user = User.builder()
                .code(userDto.getCode())
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .username(userDto.getUsername())
                .password(userDto.getPassword())
                .roles(roles.stream().map(Role::valueOf).collect(Collectors.toList()))
                .active(true)
                .build();
        return user;
    }

    public static UserDto toDto(User user) {
        UserDto userDto = BaseParser.parse(user, UserDto.class);
        userDto.setRole(user.getRoles().stream().map(Role::toString).collect(Collectors.toList()).get(0));
        return userDto;
    }
}
