package com.geekbrains;

public class FileRequest extends Command{

    private final String name;

    public FileRequest(String name) {
        this.name = name;
    }

    public String getName() { // запрашиваем имя файла
        return name;
    }

    @Override
    public CommandType getType() {
        return CommandType.FILE_REQUEST;
    }
}
