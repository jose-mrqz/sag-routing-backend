package pe.sag.routing.api.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import pe.sag.routing.shared.util.Format;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestResponse {
    private HttpStatus status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Format.DATE_TIME)
    private LocalDateTime timestamp = LocalDateTime.now();
    private String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object payload;

    public RestResponse(HttpStatus status, String message) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.message = message;
    }

    public RestResponse(HttpStatus status, Object payload) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.message = "Consulta realizada correctamente.";
        this.payload = payload;
    }

    public RestResponse(HttpStatus status, String message, Object payload) {
        this(status, message);
        this.payload = payload;
    }
}
