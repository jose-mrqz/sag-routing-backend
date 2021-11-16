package pe.sag.routing.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.sag.routing.api.request.LoginRequest;
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
        RestResponse response;
        if(userService.usernameIsUnique(request.getUsername())){
            User user = userService.registerUser(request);
            response = RestResponse.builder()
                    .status(HttpStatus.OK)
                    .message("Usuario registrado correctamente.")
                    .payload(user)
                    .build();
        }
        else{
            response = RestResponse.builder()
                    .status(HttpStatus.OK)
                    .message("Username ingresado ya existe.")
                    .build();
        }
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

    @PostMapping(path = "/edit")
    protected ResponseEntity<?> edit(@Valid @RequestBody UserDto request) throws IllegalAccessException {
        RestResponse response;
        if(request.getCode() == null){
            response = new RestResponse(HttpStatus.OK, "Error por no ingresar codigo de usuario.");
            return ResponseEntity
                    .status(response.getStatus())
                    .body(response);
        }

        User user = userService.findByCode(request.getCode());
        User userEdited = userService.edit(user, request);

        if (userEdited != null) response = new RestResponse(HttpStatus.OK, "Usuario editado correctamente.", userEdited);
        else response = new RestResponse(HttpStatus.OK, "Error al editar usuario.");
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @PostMapping(path = "/delete")
    protected ResponseEntity<?> delete(@Valid @RequestBody UserDto request) throws IllegalAccessException {
        RestResponse response;
        if(request.getCode() == null){
            response = new RestResponse(HttpStatus.OK, "Error por no ingresar codigo de usuario.");
            return ResponseEntity
                    .status(response.getStatus())
                    .body(response);
        }

        int count = userService.deleteByCode(request.getCode());
        if (count == 1) response = new RestResponse(HttpStatus.OK, "Usuario eliminado correctamente.");
        else response = new RestResponse(HttpStatus.OK, "Error al eliminar usuario.");
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @GetMapping
    public ResponseEntity<?> list() {
        RestResponse response = new RestResponse(HttpStatus.OK, userService.list());
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @GetMapping("/{code}")
    protected ResponseEntity<?> getByCode(@PathVariable String code) throws IllegalAccessException {
        User user = userService.findByCode(code);
        RestResponse response = new RestResponse(HttpStatus.OK, UserParser.toDto(user));
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @GetMapping("/roles")
    public ResponseEntity<?> listRoles() {
        RestResponse response = new RestResponse(HttpStatus.OK, userService.listRoles());
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @PostMapping(path = "/login")
    protected ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.findByUsername(request.getUsername());

        RestResponse response;
        if(user == null) response = new RestResponse(HttpStatus.OK, "Usuario con username ingresado no existe.");
        else if(user.getPassword().compareTo(request.getPassword()) == 0){
            response = new RestResponse(HttpStatus.OK, "Ingreso permitido.");
        }
        else response = new RestResponse(HttpStatus.OK, "Contraseña incorrecta.");
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
}
