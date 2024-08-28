package base.dev.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.keycloak.representations.idm.GroupRepresentation;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomGroupRepresentation extends GroupRepresentation {

    protected List<GroupRepresentation> subGroups;
}
