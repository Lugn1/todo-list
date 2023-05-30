package com.example.todo;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Window;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


public class Controller {

    @FXML
    protected ListView<Label> leftListView;

    @FXML
    protected ListView<Label> rightListView;

    @FXML
    protected Button addTaskButton;

    @FXML
    private void initialize() {
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
                    ImageView checkmark = new ImageView(getClass().getResource("/checkmark.png").toExternalForm());
                    checkmark.setFitHeight(16);
                    checkmark.setFitWidth(14);
                    setGraphic(checkmark);
                }
            }
        });
    }

    @FXML
    protected void leftListClicked() {
        leftListView.setOnMouseClicked(event -> {
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
        });

    }

    public void rightListClicked() {
        rightListView.setOnMouseClicked(event -> {
            if (!rightListView.getSelectionModel().isEmpty()) {
                Label selectedItem = rightListView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    rightListView.getItems().remove(selectedItem);
                    leftListView.getItems().add(selectedItem);
                    rightListView.getSelectionModel().clearSelection();
                }
            } else {
                rightListView.getSelectionModel().clearSelection();
            }
        });
    }

    @FXML
    private void addNewTask() {
        TextInputDialog dialog = new TextInputDialog();
        ToggleGroup priorityGroup = new ToggleGroup();
        addToPriorityGroup();
        Window mainWindow = addTaskButton.getScene().getWindow();
        dialog.initOwner(mainWindow);
        dialog.setTitle("Add task");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter task:");
        dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);


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
                    return comparePriority(priority1, priority2, "Low");
                });
            } else {
                showAlert();
            }
        }
    }

    private int comparePriority(String priority1, String priority2, String priority3) {
        List<String> priorityOrder = Arrays.asList(priority1, priority2, priority3);
        Collections.sort(priorityOrder, (p1, p2) -> {
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

    private void addToPriorityGroup() {
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
        switch (priority) {
            case "High":
                return Color.web("#fa7575");
            case "Medium":
                return Color.web("#fae875");
            case "Low":
                return Color.web("#96fa75");
            default:
                return Color.TRANSPARENT;
        }
    }

    private String getPriorityFromText(String text) {
        String[] parts = text.split("\\(");
        if (parts.length > 1) {
            String priorityPart = parts[1].trim();
            return priorityPart.substring(0, priorityPart.length() - 1);
        }
        return null;
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
}