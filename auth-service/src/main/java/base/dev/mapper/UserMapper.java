package base.dev.mapper;

import base.dev.constant.MapperConstants;
import base.dev.dto.request.UserDTO;
import base.dev.dto.response.UserResponseDTO;
import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author ductn
 * @created 2023/05/26 - 16:58
 */
@Component
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Named(MapperConstants.TO_USER_RESPONSE)
    UserResponseDTO toUserResponse(UserRepresentation userRepresentation);

    @Named(MapperConstants.TO_USER_DTO)
    UserRepresentation toUserDTO(UserDTO dto);

    @Named(MapperConstants.TO_USER_RESPONSES)
    @IterableMapping(qualifiedByName = MapperConstants.TO_USER_RESPONSE)
    List<UserResponseDTO> toUserResponses(List<UserRepresentation> userRepresentationList);


}
