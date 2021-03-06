package pe.sag.routing.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.sag.routing.api.request.LoginRequest;
import pe.sag.routing.api.request.UserDeleteRequest;
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
                    .status(HttpStatus.BAD_REQUEST)
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
    protected ResponseEntity<?> edit(@Valid @RequestBody UserDto request) {
        RestResponse response;
        if(request.getCode() == null){
            response = new RestResponse(HttpStatus.BAD_REQUEST, "Error por no ingresar codigo de usuario.");
            return ResponseEntity
                    .status(response.getStatus())
                    .body(response);
        }

        User user = userService.findByCode(request.getCode());
        if (user == null) {
            response = new RestResponse(HttpStatus.BAD_REQUEST, "Error al editar usuario: Usuario no encontrado.");
            return ResponseEntity
                    .status(response.getStatus())
                    .body(response);
        }
        User userEdited = userService.edit(user, request);

        if (userEdited != null) response = new RestResponse(HttpStatus.OK, "Usuario editado correctamente.", userEdited);
        else response = new RestResponse(HttpStatus.BAD_REQUEST, "Error al editar usuario.");
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @PostMapping(path = "/delete")
    protected ResponseEntity<?> delete(@RequestBody UserDeleteRequest request) {
        RestResponse response;
        if(request.code == null){
            response = new RestResponse(HttpStatus.BAD_REQUEST, "Error por no ingresar codigo de usuario.");
            return ResponseEntity
                    .status(response.getStatus())
                    .body(response);
        }

        User user = userService.findByCode(request.code);
        if (user == null) {
            response = new RestResponse(HttpStatus.BAD_REQUEST, "Error al eliminar usuario: Usuario no encontrado.");
            return ResponseEntity
                    .status(response.getStatus())
                    .body(response);
        }
        userService.deleteUser(user);
        response = new RestResponse(HttpStatus.OK, "Usuario eliminado correctamente.");
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
        if(user == null) response = new RestResponse(HttpStatus.BAD_REQUEST, "Usuario con username ingresado no existe.");
        else if(user.getPassword().compareTo(request.getPassword()) == 0){
            response = new RestResponse(HttpStatus.OK, "Ingreso permitido.",user);
        }
        else response = new RestResponse(HttpStatus.BAD_REQUEST, "Contrase??a incorrecta.");
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
}
