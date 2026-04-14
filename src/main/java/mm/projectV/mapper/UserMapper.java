package mm.projectV.mapper;

import lombok.AllArgsConstructor;
import mm.projectV.dto.ProfileResponse;
import mm.projectV.dto.ProfileUpdateRequest;
import mm.projectV.model.User;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserMapper {
    private final ModelMapper modelMapper;

    public ProfileResponse toResponse(User user) {
        ProfileResponse profileResponse = modelMapper.map(user, ProfileResponse.class);
        if (user.getLocation() != null) {
            profileResponse.setLongitude(user.getLocation().getX());
            profileResponse.setLatitude(user.getLocation().getY());
        }

        return profileResponse;
    }

    public User toEntity(ProfileUpdateRequest profileUpdateRequest) {
        return modelMapper.map(profileUpdateRequest, User.class);
    }
}
