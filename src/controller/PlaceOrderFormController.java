package controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import util.CustomerTM;
import util.ItemTM;
import util.OrderDetailTM;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;

public class PlaceOrderFormController {
    public JFXTextField txtDescription;
    public JFXTextField txtCustomerName;
    public JFXTextField txtQtyOnHand;
    public JFXButton btnSave;
    public JFXTextField txtUnitPrice;
    public JFXComboBox<CustomerTM> cmbCustomerId;
    public JFXComboBox<ItemTM> cmbItemCode;
    public JFXTextField txtQty;
    public Label lblTotal;
    public JFXButton btnPlaceOrder;
    public AnchorPane root;
    public Label lblId;
    public Label lblDate;
    public JFXButton btnAddNewOrder;
    public TableView<OrderDetailTM> tblOrderDetails;

    public void initialize() {

        txtCustomerName.setEditable(false);
        txtQtyOnHand.setEditable(false);
        txtUnitPrice.setEditable(false);
        txtDescription.setEditable(false);


        LocalDate today = LocalDate.now();
        lblDate.setText(today.toString());


        ObservableList<CustomerTM> customers = FXCollections.
                observableList(ManageCustomerFormController.customersDB);
        cmbCustomerId.setItems(customers);


        cmbCustomerId.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<CustomerTM>() {
            @Override
            public void changed(ObservableValue<? extends CustomerTM> observable, CustomerTM oldValue, CustomerTM newValue) {
                txtCustomerName.setText(newValue.getName());
            }
        });


        ObservableList<ItemTM> olItems = FXCollections.observableList(ManageItemFormController.itemsDB);
        cmbItemCode.setItems(olItems);


        cmbItemCode.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ItemTM>() {
            @Override
            public void changed(ObservableValue<? extends ItemTM> observable, ItemTM oldValue, ItemTM newValue) {

                if (newValue == null) {
                    txtUnitPrice.clear();
                    txtDescription.clear();
                    txtQtyOnHand.clear();
                    btnSave.setDisable(true);
                    return;
                }

                btnSave.setDisable(false);
                txtDescription.setText(newValue.getDescription());
                calculateQtyOnHand(newValue);
                txtUnitPrice.setText(newValue.getUnitPrice() + "");
            }
        });


        tblOrderDetails.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("code"));
        tblOrderDetails.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("description"));
        tblOrderDetails.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("qty"));
        tblOrderDetails.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        tblOrderDetails.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("total"));

        btnSave.setDisable(true);
    }

    private void calculateQtyOnHand(ItemTM item) {
        txtQtyOnHand.setText(item.getQtyOnHand() + "");
        ObservableList<OrderDetailTM> orderDetails = tblOrderDetails.getItems();
        for (OrderDetailTM orderDetail : orderDetails) {
            if (orderDetail.getCode().equals(item.getCode())) {
                int displayQty = item.getQtyOnHand() - orderDetail.getQty();
                txtQtyOnHand.setText(displayQty + "");
                break;
            }
        }
    }

    public void btnAddNew_OnAction(ActionEvent actionEvent) {

    }

    public void btnAdd_OnAction(ActionEvent actionEvent) {

        if (txtQty.getText().trim().isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Qty can't be empty", ButtonType.OK).show();
            return;
        }
        int qty = Integer.parseInt(txtQty.getText());
        if (qty < 1 || qty > Integer.parseInt(txtQtyOnHand.getText())) {
            new Alert(Alert.AlertType.ERROR, "Invalid Qty.", ButtonType.OK).show();
            return;
        }

        ItemTM selectedItem = cmbItemCode.getSelectionModel().getSelectedItem();
        ObservableList<OrderDetailTM> orderDetails = tblOrderDetails.getItems();

        boolean exist = false;
        for (OrderDetailTM orderDetail : orderDetails) {
            if (orderDetail.getCode().equals(selectedItem.getCode())) {
                exist = true;
                orderDetail.setQty(orderDetail.getQty() + qty);
                tblOrderDetails.refresh();
                break;
            }
        }

        if (!exist) {
            orderDetails.add(new OrderDetailTM(selectedItem.getCode(),
                    selectedItem.getDescription(),
                    qty,
                    selectedItem.getUnitPrice(), qty * selectedItem.getUnitPrice()));
        }

        calculateTotal();
        cmbItemCode.getSelectionModel().clearSelection();
        txtQty.clear();
        cmbItemCode.requestFocus();
    }


    public void btnPlaceOrder_OnAction(ActionEvent actionEvent) {


    }

    @FXML
    private void navigateToHome(MouseEvent event) throws IOException {
        URL resource = this.getClass().getResource("/view/MainForm.fxml");
        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);
        Stage primaryStage = (Stage) (this.root.getScene().getWindow());
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    public void txtQty_OnAction(ActionEvent actionEvent) {

    }

    public void calculateTotal() {
        ObservableList<OrderDetailTM> orderDetails = tblOrderDetails.getItems();
        double netTotal = 0;
        for (OrderDetailTM orderDetail : orderDetails) {
            netTotal += orderDetail.getTotal();
        }
        NumberFormat numberInstance = NumberFormat.getNumberInstance();
        numberInstance.setMaximumFractionDigits(2);
        numberInstance.setMinimumFractionDigits(2);
        numberInstance.setGroupingUsed(false);
        String formattedText = numberInstance.format(netTotal);
        lblTotal.setText("Total: " + formattedText);
    }

}
