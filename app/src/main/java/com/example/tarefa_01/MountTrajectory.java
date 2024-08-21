package com.example.tarefa_01;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import com.example.calculos.Region;
import android.location.Location;

public class MountTrajectory extends AppCompatActivity implements Runnable {

    private int tempo;
    private Semaphore semaforo;
    private Coordinates coordenadas_atuais;
    private boolean diferent_location = false;
    private ArrayList<ArrayList<Integer>> tempos = new ArrayList<>();
    private ArrayList<Integer> vetorAmostragem = new ArrayList<>();
    private Queue<Region> dadosDB = new LinkedList<>();
    private int numeroAmostra = 0;
    private int controlPointsCounter = 0;
    private ArrayList<Double> medias = new ArrayList<>();
    private ArrayList<Double> incertezas = new ArrayList<>();

    public MountTrajectory(int tempo) {
        this.tempo = tempo;
        semaforo = new Semaphore();
    }

    public void get_current_coords() {
        try {
            semaforo.take();
            Coordinates coord_buffer = semaforo.get_coordenadas();
            dadosDB = semaforo.get_dadosDB();
            if (coord_buffer != coordenadas_atuais) {
                diferent_location = true;
                coordenadas_atuais = coord_buffer;
            } else {
                diferent_location = false;
            }
            semaforo.release();
        } catch (Exception e) {
            Log.i("MountTrajectory_ERROR", String.valueOf(e));
        }
    }

    public void get_route() {
        try {
            semaforo.take();
            dadosDB = semaforo.get_dadosDB();
            semaforo.release();
        } catch (Exception e) {
            Log.i("MountTrajectory_ERROR", String.valueOf(e));
        }
    }

    @Override
    public void run() {
        while (true) {
            if (dadosDB.isEmpty() || controlPointsCounter == 0) {
                get_route();
                if (!dadosDB.isEmpty()) {
                    controlPointsCounter++;
                }
            } else {
                get_current_coords();
                if (diferent_location) {
                    for (Region controlPoint : dadosDB) {
                        if (verifyDistance(coordenadas_atuais, dadosDB)) {
                            vetorAmostragem.add((int) (System.currentTimeMillis() / 1000)); // Salva o tempo em segundos
                            controlPointsCounter++;
                            if (controlPointsCounter == 4) {
                                tempos.add(vetorAmostragem);
                                numeroAmostra++;
                                controlPointsCounter = 0;
                                vetorAmostragem = new ArrayList<>(); // Resetar para a próxima amostragem
                                if (numeroAmostra == 10) {
                                    tratarTempos();
                                }
                            }
                        }
                    }
                }
            }
            try {
                Thread.sleep(tempo);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean verifyDistance(Coordinates coordenadas, Queue<Region> dadosDB) {
        double RAIO_MINIMO = 30.0; // Raio para regiões
        double menorDistancia = Double.MAX_VALUE;
        float[] results = new float[1];
        Region regionToRemove = null;

        for (Region coord : dadosDB) {
            Location.distanceBetween(coordenadas.getLatitude(), coordenadas.getLongitude(), coord.getPosixLatitude(), coord.getPosixLongitude(), results);
            if (results[0] < menorDistancia) {
                menorDistancia = results[0];
                regionToRemove = coord;
            }
        }
        if (regionToRemove != null) {
            dadosDB.remove(regionToRemove); // remove região que já foi contabilizada
        }
        return menorDistancia >= RAIO_MINIMO;
    }

    private void tratarTempos() {
        ArrayList<Long> intervalos = new ArrayList<>();
        for (ArrayList<Integer> lista : tempos) {
            for (int i = 1; i < lista.size(); i++) {
                intervalos.add((long) lista.get(i) - lista.get(i - 1));
            }
        }

        double soma = 0;
        for (long intervalo : intervalos) {
            soma += intervalo;
        }
        double media = soma / intervalos.size();
        medias.add(media);

        double somaQuadrados = 0;
        for (long intervalo : intervalos) {
            somaQuadrados += Math.pow(intervalo - media, 2);
        }
        double desvioPadrao = Math.sqrt(somaQuadrados / intervalos.size());
        incertezas.add(desvioPadrao + Math.abs(media - media)); // Incerteza = desvio padrão + polarização

        Log.i("MountTrajectory_Stats", "Média: " + media);
        Log.i("MountTrajectory_Stats", "Desvio Padrão: " + desvioPadrao);

        tempos.clear();
        numeroAmostra = 0;

        reconciliarDados();
    }

    private void reconciliarDados() {
        double[] y = new double[medias.size()];
        double[] variancias = new double[incertezas.size()];

        for (int i = 0; i < medias.size(); i++) {
            y[i] = medias.get(i);
            variancias[i] = Math.pow(incertezas.get(i), 2);
        }

        // Criar matriz y (fluxos medidos)
        double[][] yMatrix = new double[y.length][1];
        for (int i = 0; i < y.length; i++) {
            yMatrix[i][0] = y[i];
        }

        // Criar matriz de variâncias (diagonal)
        double[][] V = new double[variancias.length][variancias.length];
        for (int i = 0; i < variancias.length; i++) {
            V[i][i] = variancias[i];
        }

        // Matriz de coeficientes A
        double[][] A = {
                {1, -1, 0},
                {0, 1, -1},
                {1, 0, -1},
        };

        // Reconciliação de dados: y_hat = y - V * A' * inv(A * V * A') * A * y;
        double[][] At = transposeMatrix(A);
        double[][] AVAt_inv = invertMatrix(multiplyMatrices(A, multiplyMatrices(V, At)));
        double[][] y_hat = subtractMatrices(yMatrix, multiplyMatrices(multiplyMatrices(multiplyMatrices(V, At), AVAt_inv), multiplyMatrices(A, yMatrix)));

        Log.i("MountTrajectory_Stats", "Medidas Reconciliadas: ");
        for (int i = 0; i < y_hat.length; i++) {
            Log.i("MountTrajectory_Stats", "y_hat[" + i + "] = " + y_hat[i][0]);
        }
    }

    private double[][] transposeMatrix(double[][] matrix) {
        double[][] transposedMatrix = new double[matrix[0].length][matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                transposedMatrix[j][i] = matrix[i][j];
            }
        }
        return transposedMatrix;
    }

    private double[][] multiplyMatrices(double[][] firstMatrix, double[][] secondMatrix) {
        int r1 = firstMatrix.length;
        int c1 = firstMatrix[0].length;
        int c2 = secondMatrix[0].length;
        double[][] product = new double[r1][c2];
        for (int i = 0; i < r1; i++) {
            for (int j = 0; j < c2; j++) {
                for (int k = 0; k < c1; k++) {
                    product[i][j] += firstMatrix[i][k] * secondMatrix[k][j];
                }
            }
        }
        return product;
    }

    private double[][] subtractMatrices(double[][] firstMatrix, double[][] secondMatrix) {
        double[][] result = new double[firstMatrix.length][firstMatrix[0].length];
        for (int i = 0; i < firstMatrix.length; i++) {
            for (int j = 0; j < firstMatrix[0].length; j++) {
                result[i][j] = firstMatrix[i][j] - secondMatrix[i][j];
            }
        }
        return result;
    }

    private double[][] invertMatrix(double[][] matrix) {
        int n = matrix.length;
        double[][] augmentedMatrix = new double[n][2 * n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                augmentedMatrix[i][j] = matrix[i][j];
            }
            augmentedMatrix[i][i + n] = 1;
        }

        for (int i = 0; i < n; i++) {
            double maxEl = Math.abs(augmentedMatrix[i][i]);
            int maxRow = i;
            for (int k = i + 1; k < n; k++) {
                if (Math.abs(augmentedMatrix[k][i]) > maxEl) {
                    maxEl = Math.abs(augmentedMatrix[k][i]);
                    maxRow = k;
                }
            }

            for (int k = i; k < 2 * n; k++) {
                double tmp = augmentedMatrix[maxRow][k];
                augmentedMatrix[maxRow][k] = augmentedMatrix[i][k];
                augmentedMatrix[i][k] = tmp;
            }

            double divisor = augmentedMatrix[i][i];
            for (int k = i; k < 2 * n; k++) {
                augmentedMatrix[i][k] /= divisor;
            }

            for (int k = 0; k < n; k++) {
                if (k != i) {
                    double factor = augmentedMatrix[k][i];
                    for (int j = i; j < 2 * n; j++) {
                        augmentedMatrix[k][j] -= factor * augmentedMatrix[i][j];
                    }
                }
            }
        }

        double[][] inverse = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                inverse[i][j] = augmentedMatrix[i][j + n];
            }
        }

        return inverse;
    }
}
