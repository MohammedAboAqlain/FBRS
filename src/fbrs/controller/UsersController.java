package fbrs.controller;

import fbrs.model.DatabaseModel;
import fbrs.model.Fisherman;
import fbrs.model.Seller;
import fbrs.model.User;
import fbrs.utils.NavigationUtil;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class UsersController implements Initializable {
    public static final int SELLER_TYPE = 1;
    public static final int FISHERMAN_TYPE = 2;

    //UI
    public TableColumn<User, Boolean> selectColumn;
    public TableColumn<User, Integer> idColumn;
    public TableColumn<User, String> nameColumn;
    public TableColumn<User, String> phoneColumn;
    public TableColumn<User, Integer> balanceColumn;
    public TableColumn<Seller, String> marketColumn;
    public TableColumn<Fisherman, String> shipTypeColumn;
    public TextField searchField;
    public TableView<User> table;
    public Button newUserBtn;
    public Button printUserBtn;
    public Button deleteSelectedBtn;
    public BorderPane rootPane;
    public Text Title;

    private Map<Integer, String> shipTypes;
    private FilteredList<User> users;
    private int viewType;
    private DatabaseModel model;

    @FXML
    public void back(ActionEvent event) {
        NavigationUtil.navigateTo(rootPane, NavigationUtil.HOME_FXML, event);
    }

    private void search() {
        String regex = ".*" + searchField.getText().replaceAll("/s+", ".*") + ".*";
        users.setPredicate(p -> p.getName().matches(regex) || String.valueOf(p.getDarshKey()).matches(regex));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        model = DatabaseModel.getModel();
        shipTypes = new HashMap<>();

        shipTypes.put(5, "لنش");
        shipTypes.put(6, "حسكة");

        idColumn.setCellValueFactory(new PropertyValueFactory<>("darshKey"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        balanceColumn.setCellValueFactory(new PropertyValueFactory<>("balance"));
        selectColumn.setCellValueFactory(new PropertyValueFactory<>("selected"));

        marketColumn.setCellValueFactory(param ->
                new SimpleStringProperty(model.getMarketByID(param.getValue().getMarket()).getName()));
        shipTypeColumn.setCellValueFactory(param ->
                new SimpleStringProperty(shipTypes.get(param.getValue().getShipType())));

        selectColumn.setCellValueFactory(new PropertyValueFactory<>("selected"));
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));

        searchField.textProperty().addListener((observable, oldValue, newValue) -> search());

        table.setEditable(true);

        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));

        CheckBox selectAll = new CheckBox();
        selectColumn.setGraphic(selectAll);
        selectAll.setOnAction(this::selectAllBoxes);

        table.setRowFactory(
                tableView -> {
                    final TableRow<User> row = new TableRow<>();
                    final ContextMenu rowMenu = new ContextMenu();
                    User user = table.getSelectionModel().getSelectedItem();
                    MenuItem editItem = new MenuItem("تعديل");
                    editItem.setOnAction(event -> {
                        try {
                            Stage stage = NavigationUtil.ViewUserProfile(table.getSelectionModel().getSelectedItem());
                            stage.setOnCloseRequest(we -> setViewType(viewType));
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    });
                    MenuItem removeItem = new MenuItem("حذف");
                    removeItem.setOnAction(event -> {
                        System.out.println(user);
                        model.deactivateUser(table.getSelectionModel().getSelectedItem());
                        refreshTable();
                    });
                    rowMenu.getItems().addAll(editItem, removeItem);

                    row.contextMenuProperty().bind(
                            Bindings.when(row.emptyProperty().not())
                                    .then(rowMenu)
                                    .otherwise((ContextMenu) null));
                    return row;
                });


    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
        ObservableList<User> observableList = FXCollections.observableArrayList();
        users = new FilteredList<>(observableList);

        switch (viewType) {
            case SELLER_TYPE:
                Title.setText("التجار");
                table.getColumns().remove(shipTypeColumn);
                observableList.addAll(model.getAllSellers());
                break;
            case FISHERMAN_TYPE:
                Title.setText("الصيادين");
                table.getColumns().remove(marketColumn);
                observableList.addAll(model.getAllFishermen());
        }

        table.setItems(users);
        table.refresh();
    }

    private void selectAllBoxes(ActionEvent e) {
        for (User use : users) {
            use.setSelected(((CheckBox) e.getSource()).isSelected());
        }
    }

    public void newUser() throws IOException {
        Stage stage;
        switch (viewType) {
            case SELLER_TYPE:
                stage = NavigationUtil.AddSpecificUser("إضافة تاجر جديد", null, viewType);
                break;
            case FISHERMAN_TYPE:
                stage = NavigationUtil.AddSpecificUser("إضافة صياد جديد", null, viewType);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + viewType);
        }
        stage.setOnCloseRequest(we -> setViewType(viewType));
    }

    public void onClick(MouseEvent mouseEvent) throws IOException {
        User user = table.getSelectionModel().getSelectedItem();
        if (mouseEvent.getButton() == MouseButton.PRIMARY
                && mouseEvent.getClickCount() == 2
                && (user != null)) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(NavigationUtil.ENTRIES_FXML));

            Parent root = loader.load();
            rootPane.getScene().setRoot(root);

            EntriesController controller = loader.getController();
            controller.setViewType(user);
        }
    }

    private void refreshTable() {
        setViewType(viewType);
        table.refresh();
    }

    private ArrayList<User> getSelectedUsers() {
        ArrayList<User> selectedUsers = new ArrayList<>();
        for (User user : users) {
            if (user.isSelected()) {
                selectedUsers.add(user);
            }
        }
        return selectedUsers;
    }

    public void printUser() {
        //Todo
    }

    public void deleteSelected() {
        model.deactivateUsers(getSelectedUsers());
        refreshTable();
    }
}