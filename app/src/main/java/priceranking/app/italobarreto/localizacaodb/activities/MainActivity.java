package priceranking.app.italobarreto.localizacaodb.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import priceranking.app.italobarreto.localizacaodb.R;
import priceranking.app.italobarreto.localizacaodb.factories.LocalFactory;
import priceranking.app.italobarreto.localizacaodb.pojo.MercadoPojo;

import static priceranking.app.italobarreto.localizacaodb.R.*;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleApiClient.ConnectionCallbacks {


    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private GoogleApiClient mGoogleApiClient;


    private TextView tvUsuario;
    private TextView tvLugar;
    private TextView tvNomeLugarAtual;
    private ArrayList<Place> lugaresProvaveis = new ArrayList<>();
    private ArrayList<MercadoPojo> mercadosProximos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(layout.activity_main);


        verificacaoDeLogin();
        configuraElementos();


        //Texto com o nome do usuario
        tvUsuario.setText(firebaseAuth.getCurrentUser().getDisplayName());
        callConnection();


    }

    private synchronized void callConnection() {
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        pedePermissaoLeituraGPS();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        pesquisaAPIPlaceAtual(Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null));



    }


    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;


    private void pedePermissaoLeituraGPS() {
        int hasWriteContactsPermission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                showMessageOKCancel(getString(string.str_confirm_lclz),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        REQUEST_CODE_ASK_PERMISSIONS);

                            }
                        });
                return;
            }
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_ASK_PERMISSIONS);
            return;
        }
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Verifica se o GPS está ativo
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // Caso não esteja ativo abre um novo diálogo com as configurações para
        // realizar se ativamento
        if (!enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
            showMessageOKCancel(getString(string.str_confirm_lclz),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Roda a pesqisa de lugar com um timer de 2 segundos.
                            new Timer().schedule(
                                    new TimerTask() {
                                        @Override
                                        public void run() {
                                            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                                pesquisaCoordenadasGPS();
                                            }
                                        }
                                    }
                                    , 2000);
                        }
                    });
        } else {
            pesquisaCoordenadasGPS();
        }

    }

    private void showListDialog(String message, DialogInterface.OnClickListener metodoChamadoNoCliqueSobreALinha) {
        final String[] values = new String[lugaresProvaveis.size()];
        for (int i =0; i< mercadosProximos.size();i++){
            values[i] = mercadosProximos.get(i).getNmMercado();
        }
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(message)
                .setNegativeButton("Cancel", null)
                .setItems(values, metodoChamadoNoCliqueSobreALinha)
                .create()
                .show();
    }



    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void pesquisaCoordenadasGPS() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        try{
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, MainActivity.this);

        }catch(Exception e){
            Log.i("Localizacao",e.getMessage());
        }

        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,MainActivity.this);
    }
    private void pesquisaAPIPlaceAtual(PendingResult<PlaceLikelihoodBuffer> result) {
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                StringBuffer lugares = new StringBuffer("");
                lugaresProvaveis = LocalFactory.mercadosPossiveis(likelyPlaces);
                if (lugaresProvaveis.isEmpty()){
                    tvNomeLugarAtual.setText("Lugar não encontrado");
                    lugares.append("Tem certeza que está em um supermercado? \n\nIremos verificar o que aconteceu com nossos servidores.");
                }
                else{
                    for (Place p : lugaresProvaveis) {
                        mercadosProximos.add(new MercadoPojo(p.getName().toString(),p.getId()));
                    }

                    updateUI(0);
                }
                tvLugar.setText(lugares.toString());
                if(mercadosProximos.size()>=2){
                    configuraAlteracaoLocal();
                }
                likelyPlaces.release();
            }
        });
    }



    /**
     * Atribuicao dos elementos da tela as variaveis.
     */
    private void configuraElementos() {
        tvUsuario = (TextView) findViewById(R.id.id_nm_usu);
        tvLugar = (TextView) findViewById(R.id.id_tx_local);
        tvNomeLugarAtual = (TextView) findViewById(R.id.id_tv_lugar_atual);
        tvNomeLugarAtual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                configuraAlteracaoLocal();
            }
        });

    }

    private void configuraAlteracaoLocal(){

        String[] values = new String[lugaresProvaveis.size()];
        for (int i =0; i< mercadosProximos.size();i++){
            values[i] = mercadosProximos.get(i).getNmMercado();
        }
        showListDialog(getString(string.str_pergunta_estabelecimento), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                updateUI(which);
            }
        });
    }


    private void updateUI(int iItemListaMercadosProximos){
        tvNomeLugarAtual.setText(mercadosProximos.get(iItemListaMercadosProximos).getNmMercado().toString());

    }

    /**
     * Pede para logar caso nao esteja logado.
     */
    private void verificacaoDeLogin() {
        firebaseAuth = FirebaseAuth.getInstance();

        if ((firebaseUser = firebaseAuth.getCurrentUser()) == null) {
            Intent myIntent = new Intent(MainActivity.this, LoginActivity.class);
            MainActivity.this.startActivity(myIntent);

        }


    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("Main", connectionResult.getErrorMessage());
        tvLugar.setText("Erro para pegar o lugar\n" + connectionResult.getErrorMessage());
    }

    @Override
    public void onLocationChanged(Location location) {
        pedePermissaoLeituraGPS();
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.i("Main", Integer.toString(i));

    }
}
