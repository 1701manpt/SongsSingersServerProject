package sgu.ltudm.songssingersserverproject;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import sgu.ltudm.songssingersserverproject.models.Server;

import java.io.IOException;

public class App extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("server-view.fxml"));

            Scene scene = new Scene(fxmlLoader.load());
            stage.setTitle("Server đang chạy");
            stage.setScene(scene);
            stage.setResizable(false);

            // Bắt sự kiện khi ứng dụng đóng
            stage.setOnCloseRequest(this::handleClose);

            stage.show();

            // Chạy server trong một luồng mới
            new Thread(() -> {
                try {
                    Server.run();
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.exit();
                    System.exit(0);
                }
            }).start();
        } catch (Exception e) {
            Platform.exit();
            System.exit(0);
        }
    }

    // Xử lý sự kiện khi ứng dụng đóng
    private void handleClose(WindowEvent event) {
        // Đóng server trước khi thoát
        try {
            Server.stop(); // Giả sử bạn có một phương thức stop trong lớp Server
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Thoát ứng dụng
            Platform.exit();
            System.exit(0);
        }
    }
}