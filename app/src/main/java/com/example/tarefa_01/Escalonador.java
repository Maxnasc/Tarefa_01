package com.example.tarefa_01;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Escalonador {

    private static final Logger logger = Logger.getLogger(Escalonador.class.getName());
    private static final JsonObject jsonObject = new JsonObject();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    static {
        try {
            // Configuração do logger
            FileHandler fileHandler = new FileHandler("estatisticas.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adiciona uma tarefa com timestamps de início e fim a um objeto JSON.
     * Se a tarefa já existir, adiciona os timestamps às listas existentes.
     * Caso contrário, cria uma nova tarefa com os timestamps.
     *
     * @param taskName O nome da tarefa.
     * @param startTime Timestamp de início.
     * @param endTime Timestamp de fim.
     */
    public static void addTaskToJson(String taskName, long startTime, long endTime) {
        JsonObject taskObject;

        if (jsonObject.has(taskName)) {
            // A tarefa já existe, adicionar aos arrays existentes
            taskObject = jsonObject.getAsJsonObject(taskName);

            JsonArray startArray = taskObject.getAsJsonArray("Inicio");
            startArray.add(startTime);

            JsonArray endArray = taskObject.getAsJsonArray("Fim");
            endArray.add(endTime);
        } else {
            // A tarefa não existe, criar uma nova
            taskObject = new JsonObject();

            JsonArray startArray = new JsonArray();
            startArray.add(startTime);
            taskObject.add("Inicio", startArray);

            JsonArray endArray = new JsonArray();
            endArray.add(endTime);
            taskObject.add("Fim", endArray);

            jsonObject.add(taskName, taskObject);
        }

        // Logando o JSON atualizado
        logJsonObject();
    }

    private static void logJsonObject() {
        String jsonString = gson.toJson(jsonObject);
        logger.info(jsonString);
    }

//    public static void main(String[] args) {
//        // Exemplo de uso
//        addTaskToJson("tarefa_1", 123L, 125L);
//        addTaskToJson("tarefa_1", 130L, 135L);
//        addTaskToJson("tarefa_2", 223L, 225L);
//    }
}


