package com.example.tarefa_01;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class Localizacao extends AppCompatActivity implements Runnable {
    private final int tempo;
    private final CancellationTokenSource cancellationSource = new CancellationTokenSource();
    private final Context context;
    private final Semaphore semaforo;
    private double latitude;
    private double longitude;

    public Localizacao(int tempo, Context context){
        this.tempo = tempo;
        this.context = context;
        semaforo = new Semaphore();
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    @Override
    public void run() {
        try {
            // Pega a localização do usuário aqui
            Log.i("Localizacao: ", "Thread de localização funcionando");
            while (true) {
                getLocation();
                //Postagem dos resultados
                semaforo.take();
                semaforo.set_coordenadas(latitude, longitude);
                semaforo.release();
                Thread.sleep(tempo);
            }
        } catch (InterruptedException e) {
            // Mostra o erro no terminal para facilitar a depuração
            Log.println(Log.ASSERT, "ERRO -> ", String.valueOf(e));
            throw new RuntimeException(e);
        }
    }

    private void getLocation()
    {
        // The Fused Location Provider provides access to location APIs.
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        requestCurrentLocation(fusedLocationClient, context);
    }

    private void requestCurrentLocation(FusedLocationProviderClient fusedLocationClient, Context context) {
        // Request permission
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            // Main code
            @SuppressLint("MissingPermission") Task<Location> currentLocationTask = fusedLocationClient.getCurrentLocation(100, cancellationSource.getToken());

            currentLocationTask.addOnCompleteListener((new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        Location location = task.getResult();
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            Log.e("Localizacao", "Latitude: " + latitude + " | Longitude: " + longitude);
                        } else {
                            Log.e("Localizacao", "Localização não encontrada.");
                        }

                    } else {
                        // Task failed with an exception
                        Exception exception = task.getException();
                    }
                }
            }));
        } else {
            // TODO: Request fine location permission
            Log.d("Localizacao", "Request fine location permission.");
        }
    }
}
