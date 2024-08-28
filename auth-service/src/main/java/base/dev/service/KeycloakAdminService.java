package base.dev.service;

import base.dev.dto.request.UserDTO;
import base.dev.dto.request.UserLogin;
import base.dev.exception.InternalServiceException;
import base.dev.exception.UnAuthException;
import base.dev.mapper.UserMapper;
import base.dev.util.BaseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.*;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.util.Http;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

import static org.keycloak.admin.client.CreatedResponseUtil.getCreatedId;

@Service
public class KeycloakAdminService {
    @Value("${auth.keycloak.realm}")
    private String keycloakRealm;

    @Value("${auth.keycloak.resource}")
    private String keycloakClientId;


    @Value("${auth.keycloak.config.group}")
    private String keycloakGroup;

    @Autowired
    public Keycloak keycloakClient;

    @Autowired
    private AuthzClient authzClient;

    @Autowired
    private Http http;

    @Autowired
    private UserMapper userMapper;

    public AccessTokenResponse login(UserLogin userLogin){

        final AccessTokenResponse accessTokenResponse = this.http.<AccessTokenResponse>post(
                        this.authzClient.getServerConfiguration().getTokenEndpoint()).authentication()
                .oauth2ResourceOwnerPassword(userLogin.getUsername(), userLogin.getPassword(),
                        "openid email profile").response().json(AccessTokenResponse.class).execute();
        return accessTokenResponse;
    }

    public UserRepresentation createUser(UserDTO userDTO){

        Optional<GroupRepresentation> groupRepresentationOptional = this.getRealmResource()
                .groups().groups().stream()
                .filter(groupRepresentation1 -> keycloakGroup.equals(groupRepresentation1.getName()))
                .findFirst();

        if (groupRepresentationOptional.isPresent()){
            UserRepresentation userRepresentation = this.convertDTO(userDTO);

            try (Response response = this.getRealmResource().users().create(userRepresentation)){

                if (Response.Status.Family.SUCCESSFUL == response.getStatusInfo().getFamily()){
                    userRepresentation.setId(getCreatedId(response));
                    return userRepresentation;
                }else {
                    throw new InternalServiceException("Error create user with username: "+userDTO.getUsername());
                }
            }
        }
        throw new InternalServiceException("Group user not found");
    }

    public UserRepresentation convertDTO(UserDTO dto) {

        UserRepresentation userRepresentation = userMapper.toUserDTO(dto);
        if (dto.getPassword() != null){
            CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
            credentialRepresentation.setValue(dto.getPassword());
            credentialRepresentation.setTemporary(false);
            userRepresentation.setCredentials(Collections.singletonList(credentialRepresentation));
        }
        //Add user to group deepb
        userRepresentation.setGroups(Collections.singletonList(keycloakGroup));

        return userRepresentation;

    }

    public void initGroup() {
        GroupsResource groupsResource = this.getRealmResource().groups();

        // List all existing groups
        List<GroupRepresentation> existingGroups = groupsResource.groups();
        Set<String> existingGroupNames = existingGroups.stream()
                .map(GroupRepresentation::getName)
                .collect(Collectors.toSet());

        // Define the group name to be created
        String groupName = keycloakGroup;

        // Create the group if it does not already exist
        if (!existingGroupNames.contains(groupName)) {
            GroupRepresentation newGroup = new GroupRepresentation();
            newGroup.setName(groupName);
            groupsResource.add(newGroup);
        }
    }

    public void initRole() {
        RolesResource rolesResource = this.getRealmResource().roles();

        // List all existing roles
        List<RoleRepresentation> existingRoles = rolesResource.list();
        Set<String> existingRoleNames = existingRoles.stream()
                .map(RoleRepresentation::getName)
                .collect(Collectors.toSet());

        // Define roles to be created
        List<String> rolesToCreate = Arrays.asList("ROLE_ADMIN", "ROLE_USER");

        // Create roles if they do not already exist
        for (String roleName : rolesToCreate) {
            if (!existingRoleNames.contains(roleName)) {
                RoleRepresentation role = new RoleRepresentation();
                role.setName(roleName);
                rolesResource.create(role);
            }
        }
    }

    public void initClientRole() {
        ClientsResource clientsResource = this.getRealmResource().clients();
        List<ClientRepresentation> clientRepresentations = this.getRealmResource().clients()
                .findByClientId(keycloakClientId);

        if (clientRepresentations == null || clientRepresentations.isEmpty()) {
            ClientRepresentation clientRepresentation = new ClientRepresentation();
            clientRepresentation.setName("PERMISSION_EXPORT");
            Map<String, String> attributes = new HashMap<>();
            attributes.put("value_vi", "Xuất file");
            attributes.put("tree_en", "ADMINISTRATION*Company administration*List companies");
            attributes.put("value_en", "Export");
            attributes.put("level", "3");
            attributes.put("name_vi", "Danh sách công ty");
            attributes.put("tree_vi", "QUẢN TRỊ*Quản trị công ty*Danh sách công ty");
            clientRepresentation.setAttributes(attributes);

            clientRepresentation.setClientId(BaseUtil.generateRandomCode());
            clientsResource.create(clientRepresentation);
        }
    }

    public RealmResource getRealmResource(){
        return keycloakClient.realm(keycloakRealm);
    }
}
