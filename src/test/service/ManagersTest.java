package test.service;

import com.yandex.task_tracker.service.HistoryManager;
import com.yandex.task_tracker.service.Managers;
import com.yandex.task_tracker.service.TaskManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    public void shouldAlwaysReturnHistoryManagerObject() {
        HistoryManager manager = Managers.getDefaultHistory();

        assertNotNull(manager);
    }

    @Test
    public void shouldAlwaysReturnTaskManagerObject() {
        TaskManager manager = Managers.getDefault();

        assertNotNull(manager);
    }

}