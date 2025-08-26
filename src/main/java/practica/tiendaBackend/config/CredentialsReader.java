package practica.tiendaBackend.config;

import org.springframework.beans.factory.annotation.Value;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;

@Configuration
@PropertySources({
        @PropertySource("classpath:db-credentials.txt") //txt donde cargar
})

public class CredentialsReader {
    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @PostConstruct
    public void loadCredentialsFromTxt() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("db-credentials.txt")))
        )) {
            String userFromFile = reader.readLine();
            String passFromFile = reader.readLine();

            System.setProperty("spring.datasource.username", userFromFile);
            System.setProperty("spring.datasource.password", passFromFile);

            System.out.println("✅ Credenciales cargadas desde db-credentials.txt: " + userFromFile);

        } catch (Exception e) {
            throw new RuntimeException("❌ Error leyendo db-credentials.txt", e);
        }
    }
}
