package pe.sag.routing.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.sag.routing.api.response.RestResponse;
import pe.sag.routing.core.model.User;
import pe.sag.routing.core.service.UserService;
import pe.sag.routing.data.parser.UserParser;
import pe.sag.routing.shared.dto.UserDto;

import javax.validation.Valid;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping
    protected ResponseEntity<?> register(@Valid @RequestBody UserDto request) {
        System.out.println(request);
        User user = userService.registerUser(request);
        RestResponse response = RestResponse.builder()
                .status(HttpStatus.OK)
                .message("Usuario registrado correctamente.")
                .payload(request)
                .build();
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @PostMapping(path = "/admin")
    protected ResponseEntity<?> saveAdmin() {
        return ResponseEntity
                .status(HttpStatus.I_AM_A_TEAPOT)
                .body(userService.createAdmin());
    }

    @GetMapping("/{email}")
    protected ResponseEntity<?> getByEmail(@PathVariable String email) throws IllegalAccessException {
        User user = userService.findByEmail(email);
        RestResponse response = new RestResponse(HttpStatus.OK, UserParser.toDto(user));
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
}
