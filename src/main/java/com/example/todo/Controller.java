package com.example.todo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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
    protected ListView<Label> leftListView;

    @FXML
    protected ListView<Label> rightListView;

    @FXML
    protected Button addTaskButton;

    @FXML
    private void initialize() {

        profileButtonContainer.getChildren().forEach(node -> {
            if (node instanceof Button) {
                Button profileButton = (Button) node;
                profileButton.setOnMouseClicked(this::profileButtonClicked);
            }
        });
        leftListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Label item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle(null);
                    setBackground(null);
                } else {
                    setText(item.getText());
                    setGraphic(null);
                    String priority = getPriorityFromText(item.getText());
                    assert priority != null;
                    setBackground(new Background(new BackgroundFill(getPriorityColor(priority), null, null)));
                }
            }
        });

        rightListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Label item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getText());
                    ImageView checkmark = new ImageView(Objects.requireNonNull(getClass().getResource("/checkmark.png")).toExternalForm());
                    checkmark.setFitHeight(16);
                    checkmark.setFitWidth(14);
                    setGraphic(checkmark);
                }
            }
        });
    }

    @FXML
    private void profileButtonClicked(MouseEvent mouseEvent) {
        Button profileButton = (Button) mouseEvent.getSource();
        String profileName = profileButton.getText();
        System.out.println("Profile " + profileName + " clicked");
    }

    @FXML
    protected void leftListClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.PRIMARY) {
            if (!leftListView.getSelectionModel().isEmpty()) {
                Label selectedItem = leftListView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    rightListView.getItems().add(selectedItem);
                    leftListView.getItems().remove(selectedItem);
                    leftListView.getSelectionModel().clearSelection();
                }
            } else {
                leftListView.getSelectionModel().clearSelection();
            }
        }
    }

    public void rightListClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.PRIMARY) {
            if (!rightListView.getSelectionModel().isEmpty()) {
                Label selectedItem = rightListView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    rightListView.getItems().remove(selectedItem);
                    leftListView.getItems().add(selectedItem);
                    rightListView.getSelectionModel().clearSelection();


                    leftListView.refresh();

                }
            } else {
                rightListView.getSelectionModel().clearSelection();
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

        if (result.isPresent()) {
            if (isRadioButtonSelected(priorityGroup)) {
                String priority = ((RadioButton) priorityGroup.getSelectedToggle()).getText();
                Label label = new Label(result.get());
                label.setStyle("-fx-background-color: " + getPriorityColor(priority));
                label.setText(result.get() + " (" + priority + ")");
                leftListView.getItems().add(label);

                leftListView.getItems().sort((o1, o2) -> {
                    String priority1 = getPriorityFromText(o1.getText());
                    String priority2 = getPriorityFromText(o2.getText());
                    return comparePriority(priority1, priority2);
                });
            } else {
                showAlert();
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

    private Color getPriorityColor(String priority) {
        return switch (priority) {
            case "High" -> Color.web("#fa7575");
            case "Medium" -> Color.web("#fae875");
            case "Low" -> Color.web("#96fa75");
            default -> Color.TRANSPARENT;
        };
    }

    private String getPriorityFromText(String text) {
        String[] parts = text.split("\\(");
        if (parts.length > 1) {
            String priorityPart = parts[1].trim();
            return priorityPart.substring(0, priorityPart.length() - 1);
        }
        return null;
    }

    public void editTodoItems(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.SECONDARY) {
            Label selectedItem = leftListView.getSelectionModel().getSelectedItem();
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
                        String updatedTask = newText + " (" + newPriority + ")";
                        selectedItem.setText(updatedTask);
                        selectedItem.setStyle("-fx-background-color: " + getPriorityColor(newPriority));
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
            Label selectedItem = rightListView.getSelectionModel().getSelectedItem();
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

    private int comparePriority(String priority1, String priority2) {
        List<String> priorityOrder = Arrays.asList(priority1, priority2, "Low");
        priorityOrder.sort((p1, p2) -> {
            if (p1.equals(p2)) {
                return 0;
            } else if (p1.equals("High")) {
                return -1;
            } else if (p1.equals("Medium")) {
                return p2.equals("High") ? 1 : -1;
            } else if (p1.equals("Low")) {
                return p2.equals("High") || p2.equals("Medium") ? 1 : -1;
            } else {
                return 0;
            }
        });

        return priorityOrder.indexOf(priority1) - priorityOrder.indexOf(priority2);
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

    private ToggleGroup createPriorityToggleGroup(RadioButton[] priorityButtons) {
        ToggleGroup priorityGroup = new ToggleGroup();

        for (RadioButton priorityButton : priorityButtons) {
            priorityButton.setToggleGroup(priorityGroup);
        }

        return priorityGroup;

    }

    private RadioButton[] createPriorityButtons() {
        RadioButton highPriority = new RadioButton("High");
        RadioButton mediumPriority = new RadioButton("Medium");
        RadioButton lowPriority = new RadioButton("Low");

        return new RadioButton[]{highPriority, mediumPriority, lowPriority};

    }

    public void createNewProfile(MouseEvent mouseEvent) {
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
        });


    }
}