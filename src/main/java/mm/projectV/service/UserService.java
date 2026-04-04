package mm.projectV.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.projectV.dto.LocationRequest;
import mm.projectV.model.User;
import mm.projectV.repository.UserRepository;
import mm.projectV.util.ResponseHandler;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public ResponseEntity<?> createUser(User userRequest) {
        userRepository.save(userRequest);
        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "User created",
                null
        );
    }

    public void changeLocation(User user, LocationRequest locationRequest) {
        User newUser = userRepository.findByEmail(user.getEmail()).get();
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
        Point newLocation = factory.createPoint(new Coordinate(locationRequest.getLongitude(), locationRequest.getLatitude()));
        newUser.setLocation(newLocation);
        userRepository.save(newUser);
    }
}
