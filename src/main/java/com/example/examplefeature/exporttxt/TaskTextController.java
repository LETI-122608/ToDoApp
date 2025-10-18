package com.example.examplefeature.exporttxt;

import com.example.examplefeature.Task;
import com.example.examplefeature.TaskService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

@RestController
public class TaskTextController {

    private final TaskService taskService;
    private static final DateTimeFormatter DT = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter D = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

    public TaskTextController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping(value = "/task/{id}.txt", produces = "text/plain;charset=UTF-8")
    public void taskTxt(@PathVariable Long id, HttpServletResponse resp) throws IOException {
        Task t = taskService.findOne(id).orElseThrow(() -> new IllegalArgumentException("Task not found: " + id));
        String body =
                "Task " + id + System.lineSeparator() +
                        "Description: " + (t.getDescription() == null ? "" : t.getDescription()) + System.lineSeparator() +
                        "Due: " + (t.getDueDate() == null ? "Never" : D.format(t.getDueDate())) + System.lineSeparator() +
                        "Created: " + DT.format(t.getCreationDate()) + System.lineSeparator();

        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        resp.setContentType("text/plain; charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"task-" + id + ".txt\"");
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);
        resp.setContentLength(bytes.length);
        resp.getOutputStream().write(bytes);
    }
}
