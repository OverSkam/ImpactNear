package mm.projectV.mapper;

import lombok.AllArgsConstructor;
import mm.projectV.dto.EventRequest;
import mm.projectV.dto.EventResponse;
import mm.projectV.model.Event;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class EventMapper {
    private final ModelMapper modelMapper;
    private final EventImageMapper eventImageMapper;

    public EventResponse toResponse(Event event) {
        EventResponse eventResponse = modelMapper.map(event, EventResponse.class);
        eventResponse.setUserId(event.getUser().getId());
        if (event.getLocation() != null) {
            eventResponse.setLongitude(event.getLocation().getX());
            eventResponse.setLatitude(event.getLocation().getY());
        }
        eventResponse.setImages(event.getImages() == null
                ? List.of()
                : event.getImages().stream().map(eventImageMapper::toResponse).toList());
        return eventResponse;
    }

    public Event toEntity(EventRequest eventRequest) {
        return modelMapper.map(eventRequest, Event.class);
    }
}
