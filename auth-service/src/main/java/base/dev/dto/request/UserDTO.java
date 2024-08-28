package base.dev.dto.request;

import java.util.List;
import java.util.Map;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @author ductn
 * @created 2023/05/22 - 11:11
 */

@Data
public class UserDTO {

    private String id;

    @NotEmpty
    private String username;
    private Boolean enabled;
    private Boolean emailVerified;
    private String fullName;
    private String email;
    private String password;
    private String accountType;
    private String phoneNumber;
    private String rfid;
    private String device;
    private List<String> realmRoles;
    private Map<String, List<String>> clientRoles;

}
