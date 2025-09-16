package practica.gimnasioBackend.dto;

import java.util.List;

import lombok.*;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private List<String> roles;
}
