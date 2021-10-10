package pe.sag.routing.data.parser;

import pe.sag.routing.core.model.User;
import pe.sag.routing.shared.dto.UserDto;
import pe.sag.routing.shared.util.enums.Role;

import java.util.List;
import java.util.stream.Collectors;

public class UserParser {
    public static User fromDto(UserDto userDto) {
        User user = User.builder()
                .code(userDto.getCode())
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .email(userDto.getEmail())
                .password(userDto.getPassword())
                .roles(userDto.getRoles().stream().map(Role::valueOf).collect(Collectors.toList()))
                .active(true)
                .build();
        return user;
    }

    public static UserDto toDto(User user) {
        UserDto userDto = BaseParser.parse(user, UserDto.class);
        userDto.setRoles(user.getRoles().stream().map(Role::toString).collect(Collectors.toList()));
        return userDto;
    }
}
