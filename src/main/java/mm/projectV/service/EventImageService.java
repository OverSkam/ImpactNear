package mm.projectV.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.projectV.dto.EventImageResponse;
import mm.projectV.exception.InvalidRequestException;
import mm.projectV.exception.NotFoundException;
import mm.projectV.mapper.EventImageMapper;
import mm.projectV.model.Event;
import mm.projectV.model.EventImage;
import mm.projectV.model.User;
import mm.projectV.repository.EventImageRepository;
import mm.projectV.repository.EventRepository;
import mm.projectV.util.ImageProcessor;
import mm.projectV.util.ImageProcessor.ProcessedImage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventImageService {

    private static final int MAX_IMAGES_PER_EVENT = 5;
    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024; // 5 MB
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final EventRepository eventRepository;
    private final EventImageRepository eventImageRepository;
    private final R2StorageService r2;
    private final ImageProcessor imageProcessor;
    private final EventImageMapper eventImageMapper;

    @Transactional
    public EventImageResponse upload(User user, Long eventId, MultipartFile file) {
        validateFile(file);

        Event event = eventRepository.findByUserIdAndId(user.getId(), eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        long existing = eventImageRepository.countByEventId(eventId);
        if (existing >= MAX_IMAGES_PER_EVENT) {
            throw new InvalidRequestException("Maximum of " + MAX_IMAGES_PER_EVENT + " images per event");
        }

        ProcessedImage processed;
        try {
            processed = imageProcessor.process(file.getBytes());
        } catch (IOException e) {
            throw new InvalidRequestException("Invalid image file");
        }

        String uuid = UUID.randomUUID().toString();
        r2.upload(keyFor(eventId, uuid, "original"), processed.getOriginal(), ImageProcessor.JPEG_CONTENT_TYPE);
        r2.upload(keyFor(eventId, uuid, "card"), processed.getCard(), ImageProcessor.JPEG_CONTENT_TYPE);
        r2.upload(keyFor(eventId, uuid, "thumb"), processed.getThumb(), ImageProcessor.JPEG_CONTENT_TYPE);

        int nextOrder = nextDisplayOrder(eventId);

        EventImage entity = new EventImage();
        entity.setEvent(event);
        entity.setImageUuid(uuid);
        entity.setDisplayOrder(nextOrder);
        entity.setContentType(ImageProcessor.JPEG_CONTENT_TYPE);
        entity.setSizeBytes(processed.getOriginal().length);
        entity.setWidth(processed.getSourceWidth());
        entity.setHeight(processed.getSourceHeight());

        EventImage saved = eventImageRepository.save(entity);
        log.info("Uploaded image {} for event {} by user {}", saved.getId(), eventId, user.getId());
        return eventImageMapper.toResponse(saved);
    }

    @Transactional
    public void delete(User user, Long eventId, Long imageId) {
        eventRepository.findByUserIdAndId(user.getId(), eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        EventImage image = eventImageRepository.findByIdAndEventId(imageId, eventId)
                .orElseThrow(() -> new NotFoundException("Image not found"));

        String uuid = image.getImageUuid();
        eventImageRepository.delete(image);

        r2.delete(keyFor(eventId, uuid, "original"));
        r2.delete(keyFor(eventId, uuid, "card"));
        r2.delete(keyFor(eventId, uuid, "thumb"));

        log.info("Deleted image {} from event {} by user {}", imageId, eventId, user.getId());
    }

    public static String keyFor(Long eventId, String uuid, String size) {
        return "events/" + eventId + "/" + uuid + "/" + size + ".jpg";
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidRequestException("File is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new InvalidRequestException("File too large; max 5 MB");
        }
        String type = file.getContentType();
        if (type == null || !ALLOWED_TYPES.contains(type.toLowerCase())) {
            throw new InvalidRequestException("Unsupported image type; allowed: jpeg, png, webp");
        }
    }

    private int nextDisplayOrder(Long eventId) {
        List<EventImage> existing = eventImageRepository.findByEventIdOrderByDisplayOrderAsc(eventId);
        if (existing.isEmpty()) return 0;
        return existing.get(existing.size() - 1).getDisplayOrder() + 1;
    }
}