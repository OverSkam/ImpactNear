package mm.projectV.mapper;

import lombok.AllArgsConstructor;
import mm.projectV.dto.EventImageResponse;
import mm.projectV.model.EventImage;
import mm.projectV.service.EventImageService;
import mm.projectV.service.R2StorageService;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class EventImageMapper {
    private final R2StorageService r2;

    public EventImageResponse toResponse(EventImage image) {
        Long eventId = image.getEvent().getId();
        String uuid = image.getImageUuid();
        return new EventImageResponse(
                image.getId(),
                image.getDisplayOrder(),
                r2.publicUrl(EventImageService.keyFor(eventId, uuid, "original")),
                r2.publicUrl(EventImageService.keyFor(eventId, uuid, "card")),
                r2.publicUrl(EventImageService.keyFor(eventId, uuid, "thumb"))
        );
    }
}