package mm.projectV.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventImageResponse {
    private Long id;
    private Integer displayOrder;
    private String originalUrl;
    private String cardUrl;
    private String thumbUrl;
}