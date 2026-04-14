package mm.projectV.service;

import mm.projectV.dto.EventRequest;
import mm.projectV.dto.EventResponse;
import mm.projectV.exception.NotFoundException;
import mm.projectV.mapper.EventMapper;
import mm.projectV.model.Event;
import mm.projectV.model.User;
import mm.projectV.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Initializes Mockito
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventService eventService;

    private User testUser;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        // Prepare common data before each test
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        testEvent = new Event();
        testEvent.setId(100L);
        testEvent.setName("Original Name");
    }

    @Test
    void createEvent_ShouldSaveEvent() {
        // Arrange
        EventRequest request = new EventRequest();
        when(eventMapper.toEntity(request)).thenReturn(testEvent);

        // Act
        eventService.createEvent(testUser, request);

        // Assert
        verify(eventRepository, times(1)).save(testEvent);
        assertEquals(testUser, testEvent.getUser());
    }

    @Test
    void getOrganizedEvent_WhenExists_ShouldReturnResponse() {
        // Arrange
        when(eventRepository.findByUserIdAndId(1L, 100L)).thenReturn(Optional.of(testEvent));
        when(eventMapper.toResponse(testEvent)).thenReturn(new EventResponse());

        // Act
        EventResponse response = eventService.getOrganizedEvent(testUser, 100L);

        // Assert
        assertNotNull(response);
    }

    @Test
    void getOrganizedEvent_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(eventRepository.findByUserIdAndId(1L, 100L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> eventService.getOrganizedEvent(testUser, 100L));
    }

    @Test
    void deleteEvent_ShouldCallRepositoryDelete() {
        // Arrange
        when(eventRepository.findByUserIdAndId(1L, 100L)).thenReturn(Optional.of(testEvent));

        // Act
        eventService.deleteEvent(testUser, 100L);

        // Assert
        verify(eventRepository).delete(testEvent);
    }
}