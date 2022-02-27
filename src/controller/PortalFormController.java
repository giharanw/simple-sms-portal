package controller;

import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

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
        btnSend.setDisable(true);
        txtMessage.textProperty().addListener((observable, oldValue, newValue) -> {
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
            btnAdd.setDisable(!newValue.trim().matches("\\d{10}"));
        });
        lstContacts.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            btnRemove.setDisable(newValue==null);
        });
        lstContacts.getItems().addListener((ListChangeListener<? super String>) observable -> {
            btnSend.setDisable(true);
            if (!lstContacts.getItems().isEmpty()){
                btnSend.setDisable(false);
            }
        });
    }
    
    public void btnAddOnAction(ActionEvent event) {
        lstContacts.getSelectionModel().clearSelection();
        for (String contact : lstContacts.getItems()) {
            if (contact.equals(txtContact.getText())){
                txtContact.selectAll();
                return;
            }
        }
        lstContacts.getItems().add(txtContact.getText().trim());
        txtContact.clear();
    }
    
    public void btnRemoveOnAction(ActionEvent event) {
        String selectedContact = lstContacts.getSelectionModel().getSelectedItem();
        lstContacts.getItems().remove(selectedContact);
        lstContacts.getSelectionModel().clearSelection();
    }
    
    public void btnSendOnAction(ActionEvent event) throws IOException {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Your message and contact(s) are as follows: ");
        confirmation.setContentText(new String("Message: "+txtMessage.getText()+"\nContact(s): "+lstContacts.getItems().toString()));
        confirmation.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.get()==ButtonType.OK){
            URL url = new URL("https://api.smshub.lk/api/v2/send/single");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-type","application/json");
            connection.setRequestProperty("","application/json");

            String apiToken = "Enter API Token Here";

            connection.setRequestProperty("Authorization", apiToken);

            if (lstContacts.getItems().size()==1){
                sendSMSSingleContact();
            }
            else if (lstContacts.getItems().size()>1){
                sendSMSMultipleContacts();
            }
        }
    }

    private void sendSMSSingleContact() {
        /* Todo: Implement sendSMSSingleContact */
        System.out.println("sendSMSSingleContact()");
    }

    private void sendSMSMultipleContacts() {
        /* Todo: Implement sendSMSMultipleContacts */
        System.out.println("sendSMSMultipleContact()");
    }

}
