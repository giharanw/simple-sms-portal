package controller;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

public class PortalFormController {

    public AnchorPane root;
    public TextArea txtMessage;
    public Button btnSend;
    public ListView<String> lstContacts;
    public Label lblCharacters;
    public TextField txtContact;
    public Button btnAdd;
    public Button btnRemove;

    public void initialize(){
        btnAdd.setDisable(true);
        btnRemove.setDisable(true);
        txtMessage.textProperty().addListener((observable, oldValue, newValue) -> {
            btnSend.setDisable(false);
            int charCount = newValue.split("").length;
            lblCharacters.setText((500-charCount)+" characters remaining (Max. 500)");
            lblCharacters.setTextFill(Color.WHITE);
            if (charCount>500){
                btnSend.setDisable(true);
                lblCharacters.setText("Character limit exceeded(Max. 500)");
                lblCharacters.setTextFill(Color.RED);
            }
        });
        txtContact.textProperty().addListener((observable, oldValue, newValue) -> {
            btnAdd.setDisable(!newValue.trim().matches("\\d{3}-\\d{7}"));
        });
        lstContacts.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            btnRemove.setDisable(newValue==null);
        });
    }
    
    public void btnAddOnAction(ActionEvent event) {
    }
    
    public void btnRemoveOnAction(ActionEvent event) {
    }
    
    public void btnSendOnAction(ActionEvent event) {
    }

}
