package base.dev.controller;

import base.dev.dto.request.UserDTO;
import base.dev.dto.request.UserLogin;
import base.dev.service.KeycloakAdminService;

import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    KeycloakAdminService keycloakAdminService;

    @PostMapping("/login")
    public ResponseEntity<AccessTokenResponse> login(@RequestBody UserLogin userLogin, HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(keycloakAdminService.login(userLogin));
    }

    @PostMapping("/users")
    public ResponseEntity<UserRepresentation> createUser(@Valid @RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(keycloakAdminService.createUser(userDTO));
    }
}

