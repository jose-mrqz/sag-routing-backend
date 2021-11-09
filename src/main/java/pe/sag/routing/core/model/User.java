package pe.sag.routing.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import pe.sag.routing.shared.util.enums.Role;

import java.util.List;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    private String _id;
    @Indexed(unique = true)
    private String code;
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private List<Role> roles;
    private boolean active = true;
}
