package com.geekbrains.nio;

import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

@Slf4j
public class NioUtils {

    @SneakyThrows
    public static void main(String[] args) {
        // Path, Files, Channel, Selector
        // ByteBuffer
        // - классы nio

        Path path = Paths.get("server-sep-2021", "root"); // итерфейс, создается через обычный метод, не через new
        System.out.println(Files.exists(path)); // вернет тру, если файл сущ
        System.out.println(Files.size(path)); // вернет длину файла в байтах

        Path copy = Paths.get("server-sep-2021", "root", "copy.txt");

        System.out.println(path.getParent().resolve("copy.txt")); // resolve позволяет к дериктории добавить файл, path.getParent() - возвращает ссылку
        // 1/../../../2/3/1.txt
        System.out.println(path.toAbsolutePath()); // toAbsolutePath - абсолютное место на диске

        WatchService watchService = FileSystems.getDefault()
                .newWatchService(); // WatchService - вешается в потоке слушать события внутри папки, событие возникает - он триггерится

        new Thread(() -> {
            while (true) {
                WatchKey key;
                try {
                    key = watchService.take();
                    if (key.isValid()) {
                        List<WatchEvent<?>> events = key.pollEvents(); // возвращает список событий
                        for (WatchEvent<?> event : events) { // итерируемся по списку событий
                            log.debug("kind {}, context {}", event.kind(), event.context());
                        }
                        key.reset();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }).start();

        path.register(watchService, ENTRY_MODIFY, ENTRY_CREATE, ENTRY_DELETE); // на path (папку, не на файл) регистритуем watchService и операции, которые мы хотим слушать
        // изменение, создание, удаление

        Files.write( // все основные методы в классе Files
                copy, // в стандартной опции - перезаписывает
                "My message".getBytes(StandardCharsets.UTF_8), // все строки обладают методом гетбайтс
                StandardOpenOption.APPEND // в стандартной опции copy перезаписывает, чтобы добавлял - необходимо добавить StandardOpenOption.APPEND
        );

        Files.copy(
                copy,
                Paths.get("server-sep-2021", "root", "f1.txt"),
                StandardCopyOption.REPLACE_EXISTING
        );

        Files.walk(path)
                .forEach(System.out::println); // Files.walk(path) выводит все файлы
    }
}
