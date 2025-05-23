module com.cas.yutnorifx {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.cas.yutnorifx to javafx.fxml;
    exports com.cas.yutnorifx;
}