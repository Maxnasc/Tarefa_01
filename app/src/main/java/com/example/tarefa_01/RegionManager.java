package com.example.tarefa_01;

import android.content.Context;
import android.widget.Toast;

import com.example.calculos.CryptoUtils;
import com.example.calculos.Region;
import com.example.calculos.RestrictedRegion;
import com.example.calculos.SubRegion;
import com.example.calculos.Utils;

import java.util.Map;
import java.util.Queue;

public class RegionManager implements Runnable{

    private final int tempo;
    private Semaphore semaforo;
    private Coordinates coordenadas;
    private Context context;
    private Queue<Region> filaCoordenadas;
    private Region regiao;
    private SubRegion subRegiao;
    private RestrictedRegion regiaoRestrita;
    // raio em metros
    private Utils utils = new Utils();
    private CryptoUtils encriptador = new CryptoUtils();
    private boolean chaveador_regiao = false; // false = sub e true = restrict
    private Escalonador escalonador = new Escalonador();

    public RegionManager(int tempo, Context context) {
        this.tempo = tempo;
        this.context = context;
        this.coordenadas = new Coordinates();
        this.semaforo = new Semaphore();
    }

    @Override
    public void run() {
        while (true) {
            try {
                // Coleta de tempo inicial da tarefa
                long inicio = System.nanoTime();

                // Bloqueia a execução até que o semáforo seja liberado
                semaforo.take();

                // Obtém as coordenadas atuais
                if (semaforo.getRequest()) {
                    coordenadas = semaforo.get_coordenadas();

                    filaCoordenadas = semaforo.getFilaCoordenadas();

                    if (filaCoordenadas != null) {
                    // Verifica se alguma coordenada está dentro do raio de 30m
                    double distancia = distanceToNearestCoordinate(coordenadas, filaCoordenadas);
                    if (distancia < utils.RAIO_REGION) {
                        if (distancia < utils.RAIO_SUB_REGION) {
                            showMessage("Coordenada dentro do raio de " + utils.RAIO_SUB_REGION + "m  -> " + distancia);
                        } else {
                            if (!chaveador_regiao) {
                                String uniqueId = Utils.getNextUniqueId();
                                String nomeRegiao = "Sub_Regiao_"+uniqueId;

                                subRegiao = new SubRegion(nomeRegiao,
                                        coordenadas.getLatitude(),
                                        coordenadas.getLongitude(),
                                        1,
                                        System.nanoTime(),
                                        getMainRegion(coordenadas, filaCoordenadas, distancia));
                                subRegiao.setDadoEncriptado(encriptador.encryptSubRegion(subRegiao));
                                filaCoordenadas.add(subRegiao);
                                semaforo.setFilaCoordenadas(filaCoordenadas);
                                chaveador_regiao = true; // Próxima deve ser Região restrita
                                showMessage("Sub_Região adicionada");
                            } else {
                                String uniqueId = Utils.getNextUniqueId();
                                String nomeRegiao = "Regiao_Restrita_"+uniqueId;

                                regiaoRestrita = new RestrictedRegion(nomeRegiao, coordenadas.getLatitude(), coordenadas.getLongitude(), 1, System.nanoTime(), getMainRegion(coordenadas, filaCoordenadas, distancia));
                                regiaoRestrita.setDadoEncriptado(encriptador.encryptSRestrictedRegion(regiaoRestrita));
                                filaCoordenadas.add(regiaoRestrita);
                                semaforo.setFilaCoordenadas(filaCoordenadas);
                                chaveador_regiao = false; // Próxima deve ser sub Região
                                showMessage("Região_Restrita adicionada");
                            }
                        }
                    } else {
                        String uniqueId = utils.getNextUniqueId();
                        String nomeRegiao = "Regiao_"+uniqueId;

                        regiao = new Region(nomeRegiao, coordenadas.getLatitude(), coordenadas.getLongitude(), 1, System.nanoTime());
                        regiao.setDadoEncriptado(encriptador.encryptRegion(regiao));
                        filaCoordenadas.add(regiao);
                        semaforo.setFilaCoordenadas(filaCoordenadas);
                        showMessage("Região adicionada");
                        }
                    semaforo.setNumberRegionsOnQueue(filaCoordenadas.size());
                    }
                }

                // Coleta de tempo inicial da tarefa
                long fim = System.nanoTime();
                escalonador.addTaskToJson("Adiciona_regiao", inicio, fim);

                // Libera o semáforo
            try {
                semaforo.release();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Thread.sleep(tempo);


            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
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
    private double distanceToNearestCoordinate(Coordinates coordenada, Queue<Region> filaCoordenadas) {
        double menorDistancia = Double.MAX_VALUE;

        for (Region coord : filaCoordenadas) {
            double distancia = coord.calcularDistancia(coordenada.getLatitude(), coordenada.getLongitude(), coord.getPosixLatitude(), coord.getPosixLongitude());
            if (distancia < menorDistancia) {
                menorDistancia = distancia;
            }
        }
        return menorDistancia;
    }

    private Region getMainRegion (Coordinates coordenada, Queue<Region> filaCoordenadas, double distancia) {
        Region mainRegion = null;
        for (Region coord : filaCoordenadas) {
            double distanciaAtual = coord.calcularDistancia(coordenada.getLatitude(), coordenada.getLongitude(), coord.getPosixLatitude(), coord.getPosixLongitude());
            if (distancia == distanciaAtual) {
                mainRegion = coord;
            }
        }
        return mainRegion;
    }

}
