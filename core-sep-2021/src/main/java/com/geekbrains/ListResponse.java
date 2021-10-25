package com.geekbrains;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class ListResponse extends Command{

    private final List<String> names;

    public ListResponse(Path path) throws IOException { // собирает список файлов на сервере
        names = Files.list(path)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
    }

    public List<String> getNames() {
        return names;
    }

    @Override
    public CommandType getType() {
        return CommandType.LIST_RESPONSE;
    }
}
