package com.example.examplefeature.ui;

import com.vaadin.flow.server.VaadinService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import com.vaadin.flow.server.VaadinServletRequest;
import jakarta.servlet.http.HttpServletRequest;

import com.example.base.ui.component.ViewToolbar;
import com.example.examplefeature.Task;
import com.example.examplefeature.TaskService;
import com.example.qr.QRCodeService;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.server.StreamResource;


import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayInputStream;
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
    private final String publicBaseUrl;

    private final TextField description;
    private final DatePicker dueDate;
    private final Button createBtn;
    private final Grid<Task> taskGrid;

    public TaskListView(TaskService taskService,
                        @Value("${app.public-base-url:}") String publicBaseUrl) {
        this.taskService = taskService;
        this.publicBaseUrl = publicBaseUrl == null ? "" : publicBaseUrl.trim();

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

        // ---- QR column: points to /task/{id}.txt and offers "Search the web"
        taskGrid.addComponentColumn(task -> {
            Button qrBtn = new Button("QR");
            qrBtn.addClickListener(e -> {
                Dialog d = new Dialog();
                d.setModal(true);
                d.setDraggable(true);

                if (task.getId() != null) {
                    String base = resolveBaseUrl();
                    String txtUrl = base + "/task/" + task.getId() + ".txt";

                    // QR encodes the TXT download URL
                    byte[] png = new QRCodeService().toPng(txtUrl, 256);
                    StreamResource res = new StreamResource(
                            "task-" + task.getId() + ".png",
                            () -> new ByteArrayInputStream(png)
                    );


                    res.setCacheTime(0);

                    Image img = new Image(res, "QR → downloadable TXT");
                    img.setWidth("256px");
                    img.setHeight("256px");

                    // Quick actions under the QR
                    Anchor openTxt = new Anchor(txtUrl, "Open task .txt");
                    openTxt.setTarget("_blank");
                    openTxt.getElement().setAttribute("download", true);

                    String google = "https://www.google.com/search?q=" +
                            URLEncoder.encode(task.getDescription(), StandardCharsets.UTF_8);
                    Anchor search = new Anchor(google, "Search the web");
                    search.setTarget("_blank");

                    d.add(new H3("QR for task " + task.getId()), img, openTxt, search);
                } else {
                    // Fallback for unsaved tasks: embed human-readable text in the QR
                    String dueText = Optional.ofNullable(task.getDueDate())
                            .map(dateFormatter::format).orElse("Never");
                    String createdText = dateTimeFormatter.format(task.getCreationDate());

                    String payload =
                            "Task\n" +
                                    "Desc: " + task.getDescription() + "\n" +
                                    "Due: " + dueText + "\n" +
                                    "Created: " + createdText + "\n" +
                                    "ID: (new)";

                    byte[] png = new QRCodeService().toPng(payload, 256);
                    StreamResource res = new StreamResource("task-new.png",
                            () -> new ByteArrayInputStream(png));
                    res.setCacheTime(0);

                    Image img = new Image(res, "QR for unsaved task");
                    img.setWidth("256px");
                    img.setHeight("256px");

                    d.add(new H3("QR (unsaved task)"), img);
                }

                d.open();
            });
            return qrBtn;
        }).setHeader("QR");
        // ---- end QR column

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

    /** Prefer app.public-base-url; else derive from current request */
    private String resolveBaseUrl() {
        if (!publicBaseUrl.isBlank()) return publicBaseUrl.replaceAll("/+$", "");

        var req = VaadinService.getCurrentRequest();
        if (!(req instanceof VaadinServletRequest vsr)) return "";

        HttpServletRequest http = vsr.getHttpServletRequest();

        // honor reverse-proxy headers if present
        String scheme = headerOr(http, "X-Forwarded-Proto", http.getScheme());
        String host   = headerOr(http, "X-Forwarded-Host",  http.getServerName());
        String portS  = headerOr(http, "X-Forwarded-Port",  String.valueOf(http.getServerPort()));
        String ctx    = http.getContextPath();

        if (host.contains(":")) { // forwarded host may already include port
            return scheme + "://" + host + ctx;
        }
        int port = Integer.parseInt(portS);
        boolean def = ("http".equalsIgnoreCase(scheme) && port == 80)
                || ("https".equalsIgnoreCase(scheme) && port == 443);
        return scheme + "://" + host + (def ? "" : ":" + port) + ctx;
    }

    private static String headerOr(HttpServletRequest r, String name, String fallback) {
        String v = r.getHeader(name);
        return (v == null || v.isBlank()) ? fallback : v;
    }

}
