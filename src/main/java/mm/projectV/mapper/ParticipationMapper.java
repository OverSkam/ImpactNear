package mm.projectV.mapper;

import lombok.AllArgsConstructor;
import mm.projectV.dto.ParticipationRequest;
import mm.projectV.dto.ParticipationResponse;
import mm.projectV.model.Participation;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ParticipationMapper {
    private final ModelMapper modelMapper;

    public ParticipationResponse toResponse(Participation participation) {
        ParticipationResponse participationResponse = modelMapper.map(participation, ParticipationResponse.class);
        participationResponse.setUserId(participation.getUser().getId());
        participationResponse.setEventId(participation.getEvent().getId());
        return participationResponse;
    }

    public Participation toEntity(ParticipationRequest participationRequest) {
        return modelMapper.map(participationRequest, Participation.class);
    }
}
