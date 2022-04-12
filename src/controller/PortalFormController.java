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
import java.net.UnknownHostException;
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
    public Button btnClearAll;
    public Button btnClear;
    public Button btnRemoveAll;

    public void initialize(){
        btnAdd.setDisable(true);
        btnRemove.setDisable(true);
        btnSend.setDisable(true);
        btnClear.setDisable(true);
        btnRemoveAll.setDisable(true);
        txtMessage.textProperty().addListener((observable, oldValue, newValue) -> {
            btnClear.setDisable(newValue.isEmpty());
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
            btnSend.setDisable(lstContacts.getItems().isEmpty());
            btnRemoveAll.setDisable(lstContacts.getItems().isEmpty());
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
            String apiToken = "";
            connection.setRequestProperty("Authorization", apiToken);

            try {
                connection.setDoOutput(true);
                OutputStream os = connection.getOutputStream();
                os.write(setData(size).getBytes());
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
                sendAlert(Alert.AlertType.INFORMATION,"Information","Response code: "+connection.getResponseCode(),"Message sent successfully");
            }
            catch (Throwable e){
                e.printStackTrace();
                if (e instanceof UnknownHostException){
                    sendAlert(Alert.AlertType.ERROR,"Error", "Network error", e.toString());
                } else {
                    sendAlert(Alert.AlertType.ERROR,"Error", "Error code: "+connection.getResponseCode(), connection.getResponseMessage());
                }
            }
        }
    }

    private String setData(int size) {
        String data = null;
        if (size==1){
            data = "{\n\t\"message\":\""+txtMessage.getText()+"\",\n\t\"phoneNumber\":\""+lstContacts.getItems().get(0)+"\"\n}";
        }
        else if (size>1){
            StringBuilder sb = new StringBuilder("{\n\t\"messageArray\": [");
            for (String contactsItem : lstContacts.getItems()) {
                sb.append("\n\t\t{\n\t\t\t\"PhoneNumber\": \""+contactsItem+"\"," +
                        "\n\t\t\t\"SmsMessage\": \""+txtMessage.getText()+"\"\n\t\t},");
            }
            sb.deleteCharAt(sb.length()-1);
            sb.append("\n\t]\n}");
            data=sb.toString();
        }
        return data;
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

    public void btnClearAllOnAction(ActionEvent actionEvent) {
        if (sendAlert(Alert.AlertType.CONFIRMATION,"Confirmation","Are you sure?","Press OK to clear all.").get()==ButtonType.OK){
            btnClear.fire();
            btnRemoveAll.fire();
            txtContact.clear();
        }
    }

    public void btnClearOnAction(ActionEvent actionEvent) {
        txtMessage.clear();
        txtMessage.requestFocus();
    }

    public void btnRemoveAllOnAction(ActionEvent actionEvent) {
        lstContacts.getItems().remove(0,lstContacts.getItems().size());
        txtContact.requestFocus();
    }
}
