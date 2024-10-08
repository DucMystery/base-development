package base.dev.service.job;

import base.dev.dto.request.UserDTO;
import base.dev.service.KeycloakAdminService;
import base.dev.util.BaseUtil;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

@Service
public class InitDataKeycloak {

    @Autowired
    private KeycloakAdminService keycloakAdminService;
    @PostConstruct
    public void init(){

        keycloakAdminService.initGroup();
        keycloakAdminService.initRole();
        keycloakAdminService.initClientRole();

        List<UserRepresentation> userRepresentations = keycloakAdminService.getAllUsers();
        if (BaseUtil.isEmpty(userRepresentations, null, 1)){
            UserDTO userDTO = new UserDTO();
            userDTO.setAccountType("admin");
            userDTO.setUsername("accountAdmin");
            userDTO.setEnabled(true);
            userDTO.setId(BaseUtil.generateRandomCode());
            userDTO.setEmail("tonhuduc1997@gmail.com");
            userDTO.setFullName("DucTN");
            userDTO.setPassword("123123");
            userDTO.setRealmRoles(Arrays.asList("ROLE_ADMIN", "ROLE_USER"));
            userDTO.setPhoneNumber("0986886886");
            keycloakAdminService.createUser(userDTO);
        }
    }
}
