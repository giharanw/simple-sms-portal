package controller;

import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
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
        if (sendAlert(Alert.AlertType.CONFIRMATION,"Confirmation","Your message and contact(s) are as follows: ","Message: "+txtMessage.getText()+"\nContact(s): "+lstContacts.getItems().toString()).get()==ButtonType.OK){
            int size= lstContacts.getItems().size();
            URL url = new URL(setURL(size));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type","application/json");
            String apiToken = "5499dulq9EZvFVNHsO4oufAqBtQTr4tj7WZYgkqskF9L9EQrcUhSm10OtBw3UOnVGoScqBmXIIKM5eCGsLdyu4CUmWfxwXHM10pMUtGKZ5OPhFSauEXWUqpfq07YW9j2Fu3Hf36uz8ShSVBNHtS9TKInYLRGrveo6a7MO5ftvU4t6Ha5T65wahDEdh1OCkghJclAH1Nj6ExcMm5BOWzseFlMALqIcmPc0xRCd02qhNVbv6WQRBEhlPnnogWG7yWwtxtFF087xkaGlhcyT6dCMXMmJyM6sa7cSNCyxaZA52K3Oi4PA798pswC9tlvBMbLPPqKxZ6N7pz8U2S9thdMKJyQJsG6FFXBaIlE65fvdnMmxsdQHSx8Enee6utzGigczFUclT6be6RQDqksUPmNdOxlXrU6F3WOjUdZ7v3BodGHyvfW2yh6XsDSpVF7OaYnIbB0kgxULV55bYpy6UdrK2j2QTwlEvPcKg59eZen0wdLtwJjEPxCXW2hEQeSXgelpB2rgvAGbUnB9ViqlJjCVc0ovA4sArNs0stcR4Qiw0o2nyhcL51SQb8Adm8axh2m2Tk49tdJzZu8hLB2jZ1mmTMcQwQqr1aeU7mv8Uh2S79wOFBKA2yhTCf1kG73KgaK8leltQVRBNeJwR023gT5k72udiS3COQHNBE03WDfhdobVpT9oU7d5wLmGMh31AsAsNrq9beLWo3qU12hzfYamjbyWyxPlru68FFciVpAG1jo4oYCfz8lUCy1UPv9CL0MuTsm0uNfbxnIy5MttCfFHJQRXHDivPFHjRSaS0mVv8AmXzRtjBIlM4oM8uJohBZm4lZLeUTnXHChANxolPo3NOMkjbsz826uuraeyzuCaISGdt8C9GpoV8vRmjvBdDI4OW7ZcwBr1TJsO3sAZY8FpbyKprhAqRfzIxtNqpJuqgpVBnVCQNDIUkPWwzhjGhi9ozhaD4YSOjl3lU3zluzvS7chT8fQJf0aCfEqU4rF2Cesmto3ROTibGFfu1XRjkAy";
            connection.setRequestProperty("Authorization", apiToken);

            if (size==0) sendSMSSingleContact(size,connection);
            else if (size>1) sendSMSMultipleContacts(size,connection);
        }
    }

    private Optional<ButtonType> sendAlert(Alert.AlertType alertType, String title, String headerText, String contentText) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        Optional<ButtonType> result = alert.showAndWait();
        return result;
    }

    private String setURL(int size) {
        String urlSpec=size==1?"https://api.smshub.lk/api/v2/send/single":size>1?"https://api.smshub.lk/api/v2/send/bulk":"";
        return urlSpec;
    }

    private void sendSMSSingleContact(int size, HttpURLConnection connection) {
        /* Todo: Implement sendSMSSingleContact */
        System.out.println("sendSMSSingleContact()");
    }

    private void sendSMSMultipleContacts(int size, HttpURLConnection connection) throws IOException {
        /* Todo: Implement sendSMSMultipleContacts */
        try {
            StringBuilder data = new StringBuilder("{\n\t\"messageArray\": [");
            for (String contactsItem : lstContacts.getItems()) {
                data.append("\n\t\t{\n\t\t\t\"PhoneNumber\": \""+contactsItem+"\"," +
                        "\n\t\t\t\"SmsMessssage\": \""+txtMessage.getText()+"\"\n\t\t},");
            }
            data.deleteCharAt(data.length()-1);
            data.append("\n\t]\n}");

            connection.setDoOutput(true);
            OutputStream os = connection.getOutputStream();
            os.write(String.valueOf(data).getBytes());
            os.close();

            InputStream is = connection.getInputStream();
            StringBuilder response = new StringBuilder();

            ReadableByteChannel byteChannel = Channels.newChannel(is);
            while (true) {
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                int read = byteChannel.read(buffer);
                if (read!=-1) response.append(buffer);
                else break;
            }
            byteChannel.close();
            sendAlert(Alert.AlertType.INFORMATION,"Information!",String.valueOf(connection.getResponseCode()),"Message sent successfully...");
        } catch (Throwable e) {
            sendAlert(Alert.AlertType.ERROR,"Error!","",connection.getResponseMessage());
        }
    }

}
