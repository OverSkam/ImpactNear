package mm.projectV.facade;

import lombok.AllArgsConstructor;
import mm.projectV.dto.ProfileResponse;
import mm.projectV.dto.ProfileUpdateRequest;
import mm.projectV.model.User;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserFacade {
    private final ModelMapper modelMapper;

    public ProfileResponse toResponse(User user) {
        ProfileResponse profileResponse = modelMapper.map(user, ProfileResponse.class);
        profileResponse.setLongitude(user.getLocation().getX());
        profileResponse.setLatitude(user.getLocation().getY());
        return profileResponse;
    }

    public User toEntity(ProfileUpdateRequest profileUpdateRequest) {
        return modelMapper.map(profileUpdateRequest, User.class);
    }
}
