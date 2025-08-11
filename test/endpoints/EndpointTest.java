package endpoints;

import com.yandex.task_tracker.endpoints.Endpoint;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class EndpointTest {

    @ParameterizedTest
    @MethodSource("pathAndMethodMapping")
    public void pathAndMethodShouldBeConvertedToCorrectEndpoint(
            String path,
            String method,
            Endpoint endpoint
    ) {
        Optional<Endpoint> resultEndpoint = Endpoint.getEndpointByPathAndMethod(path, method);

        assertEquals(endpoint, resultEndpoint.get());
    }

    private static Stream<Arguments> pathAndMethodMapping() {
        return Stream.of(
                Arguments.of("/tasks", "GET", Endpoint.GET_TASKS),
                Arguments.of("/tasks/{id}", "GET", Endpoint.GET_TASK_BY_ID),
                Arguments.of("/tasks", "POST", Endpoint.POST_TASK),
                Arguments.of("/tasks/{id}", "DELETE", Endpoint.DELETE_TASK_BY_ID),
                Arguments.of("/epics", "GET", Endpoint.GET_EPICS),
                Arguments.of("/epics/{id}", "GET", Endpoint.GET_EPIC_BY_ID),
                Arguments.of("/epics/{id}/subtasks", "GET", Endpoint.GET_EPIC_SUBTASKS),
                Arguments.of("/epics", "POST", Endpoint.POST_EPIC),
                Arguments.of("/epics/{id}", "DELETE", Endpoint.DELETE_EPIC_BY_ID),
                Arguments.of("/history", "GET", Endpoint.GET_HISTORY),
                Arguments.of("/prioritized", "GET", Endpoint.GET_PRIORITIZED_TASKS)
        );
    }

}