package com.example.todo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Window;


import java.util.*;


public class Controller {


    @FXML
    public VBox profileButtonContainer;
    @FXML
    protected ListView<Task> leftListView;

    @FXML
    Label profileLabel;

    @FXML
    protected ListView<Task> rightListView;

    @FXML
    protected Button addTaskButton;

    @FXML
    protected Button clearCompletedButton;

    private String profileName;
    private Map<String, List<Task>> profileTasksMap;

    ObservableList<Task> leftTasks = FXCollections.observableArrayList();
    ObservableList<Task> rightTasks = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        profileName = "Default";
        profileTasksMap = new HashMap<>();

        profileLabel.setText(profileName);

        leftListView.setItems(leftTasks);
        rightListView.setItems(rightTasks);

        // Add default profile
        Button defaultProfileButton = new Button("Default");
        defaultProfileButton.setPrefWidth(Control.USE_PREF_SIZE);
        defaultProfileButton.setMaxWidth(Double.MAX_VALUE);
        defaultProfileButton.setOnMouseClicked(this::profileButtonClicked);
        profileButtonContainer.getChildren().add(defaultProfileButton);

        // Add button
        Image image = new Image("/addicon.png");
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(30);
        imageView.setFitWidth(30);
        imageView.setSmooth(true);
        addTaskButton.setGraphic(imageView);
        addTaskButton.setStyle("-fx-border-color: transparent; -fx-background-color: transparent;");

        // Delete button
        Image image2 = new Image("/deleteicon.png");
        ImageView imageView2 = new ImageView(image2);
        imageView2.setFitHeight(30);
        imageView2.setFitWidth(30);
        imageView2.setSmooth(true);
        clearCompletedButton.setGraphic(imageView2);
        clearCompletedButton.setStyle("-fx-border-color: transparent; -fx-background-color: transparent;");

        profileButtonContainer.getChildren().forEach(node -> {
            if (node instanceof Button profileButton) {
                profileButton.setOnMouseClicked(this::profileButtonClicked);
            }
        });
        // Update left tasks listview
        leftListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Task item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle(null);
                    setBackground(null);
                } else {
                    if (!item.isTaskCompleted()) {
                        setText(item.getText());
                        setGraphic(null);
                        int priority = item.getPriority();
                        setBackground(new Background(new BackgroundFill(getPriorityColor(priority), null, null)));
                    }
                }
            }
        });
        // Update right tasks listview
        rightListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Task item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (item.isTaskCompleted()) {
                        setText(item.getText());
                        ImageView checkmark = new ImageView(Objects.requireNonNull(getClass().getResource("/checkmark.png")).toExternalForm());
                        checkmark.setFitHeight(16);
                        checkmark.setFitWidth(14);
                        setGraphic(checkmark);
                    }
                }
            }
        });
    }


    @FXML
    private void profileButtonClicked(MouseEvent mouseEvent) {
        Button profileButton = (Button) mouseEvent.getSource();

        String profileName = profileButton.getText();
        this.profileName = profileName;
        profileLabel.setText(profileName);
        updateListViewByProfile(profileName);
        System.out.println("Profile: " + profileName);
    }


    // TODO refactor this method to use boolean instead of 2 lists
    private void updateListViewByProfile(String profileName) {

        List<Task> tasks = profileTasksMap.getOrDefault(profileName, new ArrayList<>());

        leftListView.getItems().clear();
        rightListView.getItems().clear();

        for (Task task : tasks) {
            if (!task.isTaskCompleted())
                leftListView.getItems().add(task);
            rightListView.getItems().add(task);
        }

        Comparator<Task> priorityComparator = Comparator.comparing(Task::getPriority);
        leftTasks.sort(priorityComparator);
        rightTasks.sort(priorityComparator);

    }

    @FXML
    private void updateListViews() {
        System.out.println("Updating list views");
        leftTasks.clear();
        rightTasks.clear();

        for (Map.Entry<String, List<Task>> entry : profileTasksMap.entrySet()) {
            String key = entry.getKey();
            List<Task> tasks = entry.getValue();

            if (key.equals(profileName)) {
                for (Task task : tasks) {
                    if (!task.isTaskCompleted()) {
                        leftTasks.add(task);
                    } else {
                        rightTasks.add(task);
                    }
                }
            }
        }

        Comparator<Task> priorityComparator = Comparator.comparing(Task::getPriority);
        leftTasks.sort(priorityComparator.reversed());
        rightTasks.sort(priorityComparator.reversed());
    }


    @FXML
    protected void leftListClicked(MouseEvent mouseEvent) {

        if (mouseEvent.getButton() == MouseButton.PRIMARY) {
            if (!leftListView.getSelectionModel().isEmpty()) {
                Task selectedItem = leftListView.getSelectionModel().getSelectedItem();
                String activeProfile = profileLabel.getText();

                List<Task> profileTasks = profileTasksMap.getOrDefault(activeProfile, new ArrayList<>());
                profileTasks.remove(selectedItem);
                selectedItem.setTaskCompleted(true);
                profileTasks.add(selectedItem);
                profileTasksMap.put(activeProfile, profileTasks);


                updateListViews();
            }
        }
    }

    @FXML
    public void rightListClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.PRIMARY) {
            if (!rightListView.getSelectionModel().isEmpty()) {
                Task selectedItem = rightListView.getSelectionModel().getSelectedItem();
                String activeProfile = profileLabel.getText();
                rightTasks.remove(selectedItem);

                List<Task> profileTasks = profileTasksMap.getOrDefault(activeProfile, new ArrayList<>());
                profileTasks.remove(selectedItem);
                selectedItem.setTaskCompleted(false);
                profileTasks.add(selectedItem);
                profileTasksMap.put(activeProfile, profileTasks);

                rightListView.getSelectionModel().clearSelection();

                updateListViews();
            }
        }
    }


    @FXML
    private void addNewTask() {
        TextInputDialog dialog = new TextInputDialog();

        Window mainWindow = addTaskButton.getScene().getWindow();
        dialog.initOwner(mainWindow);
        dialog.setTitle("Add task");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter task:");
        dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);

        ToggleGroup priorityGroup = new ToggleGroup();
        RadioButton highPriority = new RadioButton("High");
        highPriority.setToggleGroup(priorityGroup);
        RadioButton mediumPriority = new RadioButton("Medium");
        mediumPriority.setToggleGroup(priorityGroup);
        RadioButton lowPriority = new RadioButton("Low");
        lowPriority.setToggleGroup(priorityGroup);

        priorityGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            boolean isInputEmpty = dialog.getEditor().getText().isEmpty();
            boolean isPrioritySelected = newValue != null;
            dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(isInputEmpty || !isPrioritySelected);
        });

        dialog.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            boolean isInputEmpty = dialog.getEditor().getText().isEmpty();
            boolean isPrioritySelected = priorityGroup.getSelectedToggle() != null;
            dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(isInputEmpty || !isPrioritySelected);
        });


        VBox dialogContent = new VBox(10);
        dialogContent.getChildren().addAll(new Label("Task:"), dialog.getEditor(),
                new Label("Priority:"), highPriority, mediumPriority, lowPriority);


        dialog.getDialogPane().setContent(dialogContent);
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(resultTask -> {
            if (isRadioButtonSelected(priorityGroup)) {
                String profileName = this.profileName;

                String priority = ((RadioButton) priorityGroup.getSelectedToggle()).getText();

                int priorityValue = getPriority(priority);
                Color priorityColor = Task.getPriorityColor();

                List<Task> profileTasks = profileTasksMap.getOrDefault(profileName, new ArrayList<>());
                profileTasks.add(new Task(resultTask, priorityValue, priorityColor, false));
                System.out.println("PROFILE LABEL: " + profileLabel.getText().toLowerCase());
                profileTasksMap.put(profileName, profileTasks);

                updateListViews();

            } else {
                showAlert();
            }
        });
    }

    private int getPriority(String priority) {
        if (priority.equals("High")) {
            return 3;
        } else if (priority.equals("Medium")) {
            return 2;
        } else if (priority.equals("Low")) {
            return 1;
        } else {
            return 0;
        }
    }


    // TODO edit this method to use the righttasks and lefttasks lists
    public void editTodoItems(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.SECONDARY) {
            Task selectedItem = leftListView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                ContextMenu contextMenu = new ContextMenu();

                MenuItem editMenuItem = new MenuItem("Edit");
                editMenuItem.setOnAction(event -> {
                    String currentText = selectedItem.getText();
                    String currentPriority = getPriorityFromText(currentText);
                    String currentTextFormatted = currentText.replace(" (" + currentPriority + ")", "");

                    TextInputDialog editDialog = new TextInputDialog(currentTextFormatted);
                    editDialog.setTitle("Edit task");
                    editDialog.setHeaderText(null);
                    editDialog.setContentText("Edit the task:");
                    Window mainWindow = addTaskButton.getScene().getWindow();
                    editDialog.initOwner(mainWindow);

                    ToggleGroup priorityGroup = new ToggleGroup();
                    RadioButton highPriority = new RadioButton("High");
                    highPriority.setToggleGroup(priorityGroup);
                    RadioButton mediumPriority = new RadioButton("Medium");
                    mediumPriority.setToggleGroup(priorityGroup);
                    RadioButton lowPriority = new RadioButton("Low");
                    lowPriority.setToggleGroup(priorityGroup);

                    switch (Objects.requireNonNull(currentPriority)) {
                        case "High" -> highPriority.setSelected(true);
                        case "Medium" -> mediumPriority.setSelected(true);
                        case "Low" -> lowPriority.setSelected(true);
                    }

                    VBox dialogContent = new VBox(10);
                    dialogContent.getChildren().addAll(new Label("Task:"), editDialog.getEditor(),
                            new Label("Priority:"), highPriority, mediumPriority, lowPriority);

                    editDialog.getDialogPane().setContent(dialogContent);

                    Optional<String> editResult = editDialog.showAndWait();
                    if (editResult.isPresent()) {
                        String newText = editResult.get();
                        String newPriority = ((RadioButton) priorityGroup.getSelectedToggle()).getText();

                        //String updatedTask = newText + " (" + newPriority + ")";

                        selectedItem.setText(newText);
                        selectedItem.setPriority(getPriority(newPriority));
                        // todo need refresh here?
                        leftListView.refresh();

                    }

                });

                MenuItem deleteMenuItem = new MenuItem("Delete");
                deleteMenuItem.setOnAction(event -> {
                    // Handle the delete action here
                    System.out.println("Delete option selected for item: " + selectedItem.getText());
                    leftListView.getItems().remove(selectedItem);
                });


                contextMenu.getItems().addAll(editMenuItem, deleteMenuItem);
                leftListView.setContextMenu(contextMenu);
            }
        }

    }

    public void editCompletedItems(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.SECONDARY) {
            Task selectedItem = rightListView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                ContextMenu contextMenu = new ContextMenu();

                MenuItem editMenuItem = new MenuItem("Archive");
                editMenuItem.setOnAction(event -> {
                    // Handle the edit action here
                    System.out.println("Edit option selected for item: " + selectedItem.getText());
                });

                MenuItem deleteMenuItem = new MenuItem("Delete");
                deleteMenuItem.setOnAction(event -> {
                    // Handle the delete action here
                    System.out.println("Delete option selected for item: " + selectedItem.getText());

                    rightListView.getItems().remove(selectedItem);
                });

                contextMenu.getItems().addAll(editMenuItem, deleteMenuItem);

                rightListView.setContextMenu(contextMenu);
            }
        }
    }


    public void clearCompleted() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear completed tasks");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to clear all completed tasks?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            rightListView.getItems().clear();
        } else {
            alert.close();
        }
    }

    public void addNewProfile() {
        TextInputDialog newProfileDialog = new TextInputDialog();
        newProfileDialog.setTitle("Add Profile");
        newProfileDialog.setHeaderText(null);
        newProfileDialog.setContentText("Enter profile name:");
        Window mainWindow = addTaskButton.getScene().getWindow();
        newProfileDialog.initOwner(mainWindow);

        Button okButton = (Button) newProfileDialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);
        TextField inputField = newProfileDialog.getEditor();

        inputField.textProperty().addListener((observable, oldValue, newValue) -> {
            okButton.setDisable(newValue.trim().isEmpty());
        });

        Optional<String> result = newProfileDialog.showAndWait();

        result.ifPresent(profileName -> {
            Button newProfileButton = new Button(profileName);
            newProfileButton.setPrefWidth(Control.USE_PREF_SIZE);
            newProfileButton.setMaxWidth(Double.MAX_VALUE);
            newProfileButton.setOnMouseClicked(this::profileButtonClicked);
            profileButtonContainer.getChildren().add(newProfileButton);
            profileTasksMap.put(profileName, new ArrayList<>());
        });


    }

    private void showAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("Please select priority");

        alert.initModality(Modality.APPLICATION_MODAL);

        alert.showAndWait();
    }

    private boolean isRadioButtonSelected(ToggleGroup toggleGroup) {
        return toggleGroup.getSelectedToggle() != null;
    }


    private Color getPriorityColor(int priority) {
        return switch (priority) {
            case 3 -> Color.web("#fa7575");
            case 2 -> Color.web("#fae875");
            case 1 -> Color.web("#96fa75");
            default -> Color.TRANSPARENT;
        };
    }


    // TODO old method, remove
    private String getPriorityFromText(String text) {
        String[] parts = text.split("\\(");
        if (parts.length > 1) {
            String priorityPart = parts[1].trim();
            return priorityPart.substring(0, priorityPart.length() - 1);
        }
        return null;
    }

}

// TODO fick löst lite med default bugg - nu behöver jag ta bort från completed och adda bara från completedtasklist
// TODO ----  verkar som den lägger till från ngt annat är dom två listornrna (maps) jag gjort .

// TODO 15: Make Task stay on rightListView (completed) when swapping back and forth between profiles.
// TODO 16: Add a way to edit a profile button name.
// TODO 17: Add logic to delete a profile button with all it's contents. (Maybe a confirmation dialog)
// TODO 19: Make the listviews responsive to window size changes.
// TODO 20: Add a way to make the user to resize the listviews.
// TODO 21: Maybe add number of todo's on the right side of the profile buttons.