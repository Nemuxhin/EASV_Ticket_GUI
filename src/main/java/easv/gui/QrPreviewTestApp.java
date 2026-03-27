package easv.gui;

import easv.bll.QrCodeGenerator;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.util.UUID;

public class QrPreviewTestApp extends Application {

    @Override
    public void start(Stage stage) {
        String token = UUID.randomUUID().toString();

        QrCodeGenerator generator = new QrCodeGenerator();
        byte[] qrBytes = generator.generateQrCode(token);

        Image qrImage = new Image(new ByteArrayInputStream(qrBytes));
        ImageView imageView = new ImageView(qrImage);

        Label titleLabel = new Label("QR Code Test");
        Label tokenLabel = new Label("Token: " + token);

        VBox root = new VBox(20, titleLabel, imageView, tokenLabel);
        root.setPadding(new Insets(25));
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 420, 420);

        stage.setTitle("QR Preview Test");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}