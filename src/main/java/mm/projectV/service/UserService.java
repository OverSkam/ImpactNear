package mm.projectV.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.projectV.dto.LocationRequest;
import mm.projectV.dto.ProfileUpdateRequest;
import mm.projectV.model.User;
import mm.projectV.repository.UserRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final GeometryFactory GEO_FACTORY =
            new GeometryFactory(new PrecisionModel(), 4326);


    public void changeLocation(User user, LocationRequest locationRequest) {
        Point newLocation = GEO_FACTORY.createPoint(new Coordinate(locationRequest.getLongitude(), locationRequest.getLatitude()));
        user.setLocation(newLocation);
        userRepository.save(user);
    }

    public void updateProfile(User user, ProfileUpdateRequest profileRequest) {
        setIfNotNull(user::setName, profileRequest.getName());
        setIfNotNull(user::setSurname, profileRequest.getSurname());
        setIfNotNull(user::setEmail, profileRequest.getEmail());
        if (profileRequest.getPassword() != null && !profileRequest.getPassword().isBlank())
            user.setPassword(passwordEncoder.encode(profileRequest.getPassword()));
        userRepository.save(user);
        log.info("User with id: {}, was updated successfully", user.getId());
    }

    private <T> void setIfNotNull(Consumer<T> setter, T value) {
        if (value != null)
            setter.accept(value);
    }
}
