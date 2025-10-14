package com.example.examplefeature.ui;

import com.example.base.ui.component.ViewToolbar;
import com.example.examplefeature.Task;
import com.example.examplefeature.TaskService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Optional;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route("")
@PageTitle("Task List")
@Menu(order = 0, icon = "vaadin:clipboard-check", title = "Task List")
public class TaskListView extends Main {

    private final TaskService taskService;
    private final TextField description;
    private final DatePicker dueDate;
    private final Button createBtn;
    private final Grid<Task> taskGrid;

    public TaskListView(TaskService taskService) {
        this.taskService = taskService;

        description = new TextField();
        description.setPlaceholder("What do you want to do?");
        description.setAriaLabel("Task description");
        description.setMaxLength(Task.DESCRIPTION_MAX_LENGTH);
        description.setMinWidth("20em");

        dueDate = new DatePicker();
        dueDate.setPlaceholder("Due date");
        dueDate.setAriaLabel("Due date");

        createBtn = new Button("Create", event -> createTask());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // --- Botão único, bonito e funcional ---
        Anchor pdfLink = new Anchor("/download-pdf", "Exportar para PDF");
        pdfLink.getElement().setAttribute("download", true);
        pdfLink.getStyle().set("text-decoration", "none"); // remover underline
        // estilo de botão Lumo
        pdfLink.getElement().getClassList().add("v-button");
        pdfLink.getElement().getClassList().add("v-button-primary");
        pdfLink.getElement().getStyle().set("padding", "0.5em 1em");
        pdfLink.getElement().getStyle().set("border-radius", "0.25em");
        pdfLink.getElement().getStyle().set("background-color", "#f0f0f0"); // cinza suave
        pdfLink.getElement().getStyle().set("color", "#000");
        pdfLink.getElement().getStyle().set("border", "1px solid #ccc");
        pdfLink.getElement().getStyle().set("cursor", "pointer");
        // ---------------------------------------

        var dateTimeFormatter = DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(getLocale())
                .withZone(ZoneId.systemDefault());

        var dateFormatter = DateTimeFormatter
                .ofLocalizedDate(FormatStyle.MEDIUM)
                .withLocale(getLocale());

        taskGrid = new Grid<>();
        taskGrid.setItems(query -> taskService.list(toSpringPageRequest(query)).stream());
        taskGrid.addColumn(Task::getDescription).setHeader("Description");
        taskGrid.addColumn(task -> Optional.ofNullable(task.getDueDate()).map(dateFormatter::format).orElse("Never"))
                .setHeader("Due Date");
        taskGrid.addColumn(task -> dateTimeFormatter.format(task.getCreationDate())).setHeader("Creation Date");
        taskGrid.setSizeFull();

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX,
                LumoUtility.FlexDirection.COLUMN, LumoUtility.Padding.MEDIUM,
                LumoUtility.Gap.SMALL);

        add(new ViewToolbar("Task List", ViewToolbar.group(description, dueDate, createBtn, pdfLink)));
        add(taskGrid);
    }

    private void createTask() {
        taskService.createTask(description.getValue(), dueDate.getValue());
        taskGrid.getDataProvider().refreshAll();
        description.clear();
        dueDate.clear();
        Notification.show("Task added", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}
