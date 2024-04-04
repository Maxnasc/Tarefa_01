package com.example.tarefa_01;

import android.content.Context;
import android.location.Location;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.calculos.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class Region implements Runnable{

    private final int tempo;
    private Semaphore semaforo;
    private Coordinates coordenadas;
    private Context context;
    private Queue<RegionObject> filaCoordenadas;
    private RegionObject regiao;
    // raio em metros
    private Utils utils = new Utils();

    public Region(int tempo, Context context) {
        this.tempo = tempo;
        this.context = context;
        this.coordenadas = new Coordinates();
        semaforo = new Semaphore();
    }

    @Override
    public void run() {
        while (true) {
            try {
                // Bloqueia a execução até que o semáforo seja liberado
                semaforo.take();

                // Obtém as coordenadas atuais
                if (semaforo.getRequest()) {
                    coordenadas = semaforo.get_coordenadas();

                    filaCoordenadas = semaforo.getFilaCoordenadas();

                    if (filaCoordenadas != null) {
                    // Verifica se alguma coordenada está dentro do raio de 30m
                    double distancia = distanceToNearestCoordinate(coordenadas, filaCoordenadas);
                    if (distancia < utils.RAIO) {
                        showMessage("Coordenada dentro do raio de 30m  -> " + distancia);
                    } else {
                        long uniqueId = utils.getNextUniqueId();
                        String nomeRegiao = "Regiao_"+uniqueId;

                        regiao = new RegionObject(nomeRegiao, coordenadas.getLatitude(), coordenadas.getLongitude(), 1, System.nanoTime());
                        filaCoordenadas.add(regiao);
                        semaforo.setFilaCoordenadas(filaCoordenadas);
                        showMessage("Região adicionada");
                        }
                    semaforo.setNumberRegionsOnQueue(filaCoordenadas.size());
                    }
                }

                // Libera o semáforo
            try {
                semaforo.release();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            Thread.sleep(tempo);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // Método para exibir uma mensagem Toast na thread da interface do usuário
    private void showMessage(final String message) {
        if (context != null) {
            ((MainActivity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Método para verificar se uma coordenada está dentro do raio de 30m de alguma coordenada na fila
    private double distanceToNearestCoordinate(Coordinates coordenada, Queue<RegionObject> filaCoordenadas) {
        double menorDistancia = Double.MAX_VALUE;

        for (RegionObject coord : filaCoordenadas) {
            double distancia = utils.calcularDistancia(coordenada.getLatitude(), coordenada.getLongitude(), coord.getPosixLatitude(), coord.getPosixLongitude());
            if (distancia < menorDistancia) {
                menorDistancia = distancia;
            }
        }
        return menorDistancia;
    }


}
