package mm.projectV.facade;

import lombok.AllArgsConstructor;
import mm.projectV.dto.EventRequest;
import mm.projectV.dto.EventResponse;
import mm.projectV.model.Event;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class EventFacade {
    private final ModelMapper modelMapper;

    public EventResponse toResponse(Event event) {
        EventResponse eventResponse = modelMapper.map(event, EventResponse.class);
        eventResponse.setUserId(event.getUser().getId());
        return eventResponse;
    }

    public Event toEntity(EventRequest eventRequest) {
        return modelMapper.map(eventRequest, Event.class);
    }
}
