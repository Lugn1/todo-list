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
        profileName = "NO PROFILE SELECTED";
        profileTasksMap = new HashMap<>();

        profileLabel.setText(profileName);

        leftListView.setItems(leftTasks);
        rightListView.setItems(rightTasks);

        Image image = new Image("/addicon.png");
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(30);
        imageView.setFitWidth(30);
        imageView.setSmooth(true);
        addTaskButton.setGraphic(imageView);
        addTaskButton.setStyle("-fx-border-color: transparent; -fx-background-color: transparent;");

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

        rightListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Task item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setText(null);
                    setStyle(null);
                    setBackground(null);
                    setGraphic(null);
                } else {
                    if (item.isTaskCompleted()) {
                        setText(item.getText());
                        int priority = item.getPriority();
                        setBackground(new Background(new BackgroundFill(getPriorityColor(priority), null, null)));
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


        if (mouseEvent.getButton() == MouseButton.SECONDARY) {
            System.out.println("Right click");
            ContextMenu contextMenu = new ContextMenu();
            MenuItem renameMenuItem = new MenuItem("Rename");

            renameMenuItem.setOnAction(event -> {
                TextInputDialog dialog = new TextInputDialog(profileName);
                dialog.setTitle("Rename profile");
                dialog.setHeaderText(null);
                dialog.setContentText("Enter new profile name:");
                Window mainWindow = profileButton.getScene().getWindow();
                dialog.initOwner(mainWindow);

                Optional<String> result = dialog.showAndWait();
                result.ifPresent(newProfileName -> {
                    if (!newProfileName.isEmpty()) {
                        renameProfile(profileName, newProfileName);
                    }
                });
            });

            MenuItem deleteMenuItem = new MenuItem("Delete");
            deleteMenuItem.setOnAction(event -> deleteProfile(profileName));

            contextMenu.getItems().addAll(renameMenuItem, deleteMenuItem);
            profileButton.setContextMenu(contextMenu);

        }

        profileLabel.setText(profileName);
        updateListViews();
    }

    private void deleteProfile(String profileName) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete profile");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete profile " + profileName +
                "? \nThe profile cannot be restored after deletion.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            rightListView.getItems().clear();

            profileTasksMap.remove(profileName);

            profileButtonContainer.getChildren().removeIf(node -> {
                if (node instanceof Button profileButton) {
                    return profileButton.getText().equals(profileName);
                }
                return false;
            });

            if (this.profileName.equals(profileName)) {
                if (!profileTasksMap.isEmpty()) {
                    String firstProfileName = profileTasksMap.keySet().iterator().next();
                    this.profileName = firstProfileName;
                    profileLabel.setText(firstProfileName);
                } else {
                    this.profileName = null;
                    profileLabel.setText("NO PROFILE SELECTED");
                }
            }

            updateListViews();

        } else {
            alert.close();
        }
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
                    }
                    if (task.isTaskCompleted()) {
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
    private void addNewTask() {
        if (!profileName.equals("NO PROFILE SELECTED")) {
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
    }

    private int getPriority(String priority) {
        return switch (priority) {
            case "High" -> 3;
            case "Medium" -> 2;
            case "Low" -> 1;
            default -> 0;
        };
    }


    public void editTodoItems(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.SECONDARY) {
            Task selectedItem = leftListView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                ContextMenu contextMenu = new ContextMenu();

                MenuItem editMenuItem = new MenuItem("Edit");
                editMenuItem.setOnAction(event -> {
                    String currentText = selectedItem.getText();
                    int currentPriority = selectedItem.getPriority();

                    TextInputDialog editDialog = new TextInputDialog(currentText);
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

                    switch (currentPriority) {
                        case 3 -> highPriority.setSelected(true);
                        case 2 -> mediumPriority.setSelected(true);
                        case 1 -> lowPriority.setSelected(true);
                    }

                    priorityGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
                        boolean isInputEmpty = editDialog.getEditor().getText().isEmpty();
                        boolean isPrioritySelected = newValue != null;
                        editDialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(isInputEmpty || !isPrioritySelected);
                    });

                    editDialog.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
                        boolean isInputEmpty = editDialog.getEditor().getText().isEmpty();
                        boolean isPrioritySelected = priorityGroup.getSelectedToggle() != null;
                        editDialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(isInputEmpty || !isPrioritySelected);
                    });

                    VBox dialogContent = new VBox(10);
                    dialogContent.getChildren().addAll(new Label("Task:"), editDialog.getEditor(),
                            new Label("Priority:"), highPriority, mediumPriority, lowPriority);

                    editDialog.getDialogPane().setContent(dialogContent);

                    Optional<String> editResult = editDialog.showAndWait();
                    editResult.ifPresent(newText -> {
                        RadioButton selectedPriority = (RadioButton) priorityGroup.getSelectedToggle();
                        if (selectedPriority != null) {
                            String newPriority = selectedPriority.getText();
                            int newPriorityValue = getPriority(newPriority);

                            selectedItem.setText(newText);
                            selectedItem.setPriority(newPriorityValue);
                            leftListView.refresh();
                        }
                    });
                });

                contextMenu.getItems().add(editMenuItem);
                contextMenu.show(leftListView, mouseEvent.getScreenX(), mouseEvent.getScreenY());
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
                    // TODO Handle the archive action here
                    System.out.println("Edit option selected for item: " + selectedItem.getText());
                });

                MenuItem deleteMenuItem = new MenuItem("Delete");
                deleteMenuItem.setOnAction(event -> {
                    // TODO Handle the delete action here
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
            if (profileTasksMap.containsKey(profileName)) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Profile already exists! Please choose a different name.");
                alert.showAndWait();
            } else {
                Button newProfileButton = new Button(profileName);
                newProfileButton.setPrefWidth(Control.USE_PREF_SIZE);
                newProfileButton.setMaxWidth(Double.MAX_VALUE);
                newProfileButton.setOnMouseClicked(this::profileButtonClicked);
                profileButtonContainer.getChildren().add(newProfileButton);
                profileTasksMap.put(profileName, new ArrayList<>());


            }
        });
    }

    private void renameProfile(String oldProfileName, String newProfileName) {
        if (profileTasksMap.containsKey(newProfileName)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Profile already exists! Please choose a different name.");
            alert.showAndWait();
        } else {
            List<Task> tasks = profileTasksMap.remove(oldProfileName);
            profileTasksMap.put(newProfileName, tasks);

            profileButtonContainer.getChildren().forEach(node -> {
                if (node instanceof Button profileButton) {
                    if (profileButton.getText().equals(oldProfileName)) {
                        profileButton.setText(newProfileName);
                    }
                }
            });
            if (profileName.equals(oldProfileName)) {
                profileName = newProfileName;
                profileLabel.setText(newProfileName);
            }

            updateListViews();
        }
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
                List<Task> profileTasks = profileTasksMap.getOrDefault(activeProfile, new ArrayList<>());

                profileTasks.remove(selectedItem);
                selectedItem.setTaskCompleted(false);
                profileTasks.add(selectedItem);
                profileTasksMap.put(activeProfile, profileTasks);

                updateListViews();
            }
        }
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


}


// Backend
// TODO Add a FileWriter class.
// TODO Add methods to the FileWriter class to handle data.
// TODO Update Archive(?) method in rightListView.
// TODO Update delete method in rightListView.
// TODO Update delete all method in complete list.

// Frontend
// TODO highlight the button of the active profile.
// TODO Make the listviews responsive to window size changes.
// TODO Add a way to make the user to resize the listviews.
// TODO Add number of tasks on the right side of the profile buttons.
// TODO Maybe let user change colors of priorities.
// TODO Generally improve the UI design.

