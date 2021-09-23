import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class Controller implements Initializable {

    private static String ROOT_DIR = "client-sep-2021/root";
    private static byte[] buffer = new byte[1024];
    public ListView<String> listView;
    public TextField input;
    private DataInputStream is;
    private DataOutputStream os;

    public void send(ActionEvent actionEvent) throws Exception {
        String fileName = input.getText();
        input.clear();
        sendFile(fileName);
    }

    private void sendFile(String fileName) throws IOException {
        Path file = Paths.get(ROOT_DIR, fileName);
        if (Files.exists(file)) { // если файл существует
            long size = Files.size(file); // размер файла

            os.writeUTF(fileName); //записали имя файла
            os.writeLong(size);  // записали размер файла

            InputStream fileStream = Files.newInputStream(file);  // отправляем байты, InputStream - класс, позволяющий прочитать байты из файла
            int read;

            //читаем буфер, записываем от 0 до того, сколько прочитали
            while ((read = fileStream.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
            os.flush();
        } else {
            os.writeUTF(fileName);
            os.flush();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            fillFilesInCurrentDir();
            Socket socket = new Socket("localhost", 8189);
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());
            Thread daemon = new Thread(() -> {
                try {
                    while (true) {
                        String msg = is.readUTF();
                        log.debug("received: {}", msg);
                        Platform.runLater(() -> input.setText(msg));
                    }
                } catch (Exception e) {
                    log.error("exception while read from input stream");
                }
            });
            daemon.setDaemon(true);
            daemon.start();
        } catch (IOException ioException) {
            log.error("e=", ioException);
        }
    }


    //
    private void fillFilesInCurrentDir() throws IOException {
        listView.getItems().clear();
        listView.getItems().addAll(
                // читаем из root все файлы
                Files.list(Paths.get(ROOT_DIR)) // Files.list - функция возвоащает поток из всех файлов конктерной директории, Paths.get - указывает место на диске
                        .map(p -> p.getFileName().toString()) //преобразует в строки
                        .collect(Collectors.toList()) // собирает в список
        );

        // пропис. события при клике
        listView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String item = listView.getSelectionModel().getSelectedItem();
                input.setText(item); // при двойном клике появляется текст в поле
            }
        });
    }
}
