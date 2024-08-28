package base.dev.dto.response;

import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * @author ductn
 * @created 2023/05/26 - 17:04
 */
@Data
public class UserResponseDTO {

    private String id;
    private String origin;
    private Long createdTimestamp;
    private String username;
    private Boolean enabled;
    private Boolean emailVerified;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;
    private String accountType;
    private String email;
    private String serviceAccountClientId;
    private List<String> realmRoles;
    private Map<String, List<String>> clientRoles;

    private List<String> groups;
}
