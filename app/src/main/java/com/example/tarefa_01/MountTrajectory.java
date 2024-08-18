package com.example.tarefa_01;

import androidx.appcompat.app.AppCompatActivity;

public class MountTrajectory extends AppCompatActivity implements Runnable {

    private int tempo;
    private Semaphore semaforo;
    private Coordinates coordenadas_atuais;
    private boolean diferent_location = false;

    public MountTrajectory(int tempo) {
        this.tempo = tempo;
        semaforo = new Semaphore();
    }

    public void get_current_coords() {
        // pega semaforo, atualiza coordenada, muda o same_location libera semaforo
    }

    @Override
    public void run() {
        while (true) {
            get_current_coords();
            if (diferent_location) {

            }
        }
    }
}
