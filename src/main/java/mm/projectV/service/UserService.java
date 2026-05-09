package mm.projectV.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.projectV.dto.LocationRequest;
import mm.projectV.dto.ProfileUpdateRequest;
import mm.projectV.dto.UserResponse;
import mm.projectV.enums.RequestStatus;
import mm.projectV.exception.InvalidRequestException;
import mm.projectV.exception.NotFoundException;
import mm.projectV.model.OrganizerRequest;
import mm.projectV.model.User;
import mm.projectV.repository.OrganizerRequestRepository;
import mm.projectV.repository.UserRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Consumer;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final GeometryFactory GEO_FACTORY =
            new GeometryFactory(new PrecisionModel(), 4326);
    private final OrganizerRequestRepository organizerRequestRepository;

    @Transactional
    public void changeLocation(User user, LocationRequest locationRequest) {
        Point newLocation = GEO_FACTORY.createPoint(new Coordinate(locationRequest.getLongitude(), locationRequest.getLatitude()));
        user.setLocation(newLocation);
        userRepository.save(user);
    }

    @Transactional
    public void updateProfile(User user, ProfileUpdateRequest profileRequest) {
        setIfNotNull(user::setName, profileRequest.getName());
        setIfNotNull(user::setSurname, profileRequest.getSurname());
        if (profileRequest.getPassword() != null &&
                !profileRequest.getPassword().isBlank() &&
                profileRequest.getPassword().length() > 8 &&
                profileRequest.getPassword().length() < 200
        )
            user.setPassword(passwordEncoder.encode(profileRequest.getPassword()));
        userRepository.save(user);
        log.info("User with id: {}, was updated successfully", user.getId());
    }

    private <T> void setIfNotNull(Consumer<T> setter, T value) {
        if (value != null)
            setter.accept(value);
    }

    @Transactional
    public UserResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        log.info("Fetching user with id: {}", userId);
        return new UserResponse(user.getName(), user.getSurname());
    }

    @Transactional
    public void requestOrganizerRights(User user, String message) {
        if (organizerRequestRepository.existsByUserId(user.getId()))
            throw new InvalidRequestException("Request was already sent");

        OrganizerRequest request = new OrganizerRequest();
        request.setUser(user);
        request.setRequestMessage(message);
        request.setRequestStatus(RequestStatus.PENDING);
        organizerRequestRepository.save(request);
    }


}
