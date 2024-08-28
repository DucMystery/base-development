package base.dev.dto.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class UserLogin {

    @NotEmpty
    private String username;
    @NotEmpty
    private String password;
}
