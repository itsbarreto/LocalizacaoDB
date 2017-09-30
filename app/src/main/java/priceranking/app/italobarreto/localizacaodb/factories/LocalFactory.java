package priceranking.app.italobarreto.localizacaodb.factories;

import android.util.Log;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import priceranking.app.italobarreto.localizacaodb.pojo.MercadoPojo;

/**
 * Created by root on 26/09/17.
 */

public class LocalFactory {



    public static final Field[] CAMPOS_CLASS_PLACES = Place.class.getFields();
    private static final Double LIMIAR_PERTENCIMENTO_LOCAL = 0.800;
    private static final Double LIMIAR_INFERIOR_LOCAL = 0.15;
    private DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mercadoReferencia = dbRef.child("mercados");

    public static StringBuilder montaStringLugar(Place lugar, double probabilidade){
        StringBuilder strRetorno = new StringBuilder("\n\n==============\nPlace: ").append(lugar.getName());
        if(probabilidade != -1){
            strRetorno.append("\n Likelihood: ").append(probabilidade);
        }
        strRetorno.
                append("\nTipo: ");

        for (int l : lugar.getPlaceTypes()){
            strRetorno.append("\n     ").
                    append(l);
            for(Field f: CAMPOS_CLASS_PLACES){
                Class<?> t = f.getType();
                try {
                    if (f.getName().startsWith("TYPE_") && t == int.class && f.getInt(null) == l){
                        strRetorno.append(" - ").
                                append(f.getName());

                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return strRetorno;

    }

    public static StringBuilder montaStringLugar(Place lugar) {
        return montaStringLugar(lugar,-1);
    }



    /**
     * Percorre os lugares e verifica qual eh o mais provavel de o cliente estar.
     * Passos:
     * 01 - Cria <code>ArrayList</code> com os lugares.
     * 02 - Ordena a lista de forma decrescente.
     * 03 - Filtra por tipo de lugar.
     *
     * @param likelyPlaces
     * @return : mercado mais provavel
     */
    public static ArrayList<Place> mercadosPossiveis(PlaceLikelihoodBuffer likelyPlaces) {

        ArrayList<PlaceLikelihood> lugaresOrdenadosProb = new ArrayList<>();
        for (PlaceLikelihood placeLikelihood : likelyPlaces) {
            lugaresOrdenadosProb.add(placeLikelihood);
        }
        /**
         * Ordena da seguinte maneira:
         * Um eh maior que o outro quando:
         *    - a probabilidade eh maior em pelo menos 0.2 (20 pontos percentuais) OU
         *    - eh supermercado e o outro nao eh
         *    -
         */
        Collections.sort(lugaresOrdenadosProb, new Comparator<PlaceLikelihood>() {
            @Override
            public int compare(PlaceLikelihood p1, PlaceLikelihood p2) {
                /*
                if ((p1.getLikelihood() - p2.getLikelihood() > 0.20)
                        || (p1.getPlace().getPlaceTypes().contains(Place.TYPE_GROCERY_OR_SUPERMARKET) &&
                        !p2.getPlace().getPlaceTypes().contains(Place.TYPE_GROCERY_OR_SUPERMARKET))
                        || (p1.getPlace().getPlaceTypes().contains(Place.TYPE_STORE) &&
                        !p2.getPlace().getPlaceTypes().contains(Place.TYPE_STORE))
                        ) {
                    return +1;
                }
                else if ((p2.getLikelihood() - p1.getLikelihood() > 0.20)
                        || (p2.getPlace().getPlaceTypes().contains(Place.TYPE_GROCERY_OR_SUPERMARKET) &&
                        !p1.getPlace().getPlaceTypes().contains(Place.TYPE_GROCERY_OR_SUPERMARKET))
                        || (p2.getPlace().getPlaceTypes().contains(Place.TYPE_STORE) &&
                        !p1.getPlace().getPlaceTypes().contains(Place.TYPE_STORE))
                        ) {
                    return -1;
                }

                return 0;
                */
                if(p1.getLikelihood() > p2.getLikelihood())return +1;
                else if (p1.getLikelihood() < p2.getLikelihood())return -1;
                return 0;

            }
        });

        ArrayList<Place> lugares = new ArrayList<>();
        boolean entrouProbLimiarInferior = false;
        for (PlaceLikelihood p : lugaresOrdenadosProb) {
            if (p.getLikelihood() > LIMIAR_INFERIOR_LOCAL && p.getPlace().getPlaceTypes().contains(Place.TYPE_STORE)){
                if(p.getPlace().getPlaceTypes().size() > 4 && !p.getPlace().getPlaceTypes().contains(Place.TYPE_GROCERY_OR_SUPERMARKET)){
                    continue;
                }
                else if (p.getPlace().getPlaceTypes().contains(Place.TYPE_GROCERY_OR_SUPERMARKET)) {
                    lugares.add(p.getPlace());
                    if (p.getLikelihood() > LIMIAR_PERTENCIMENTO_LOCAL) {
                        break;
                    }

                } else if (p.getPlace().getPlaceTypes().contains(Place.TYPE_STORE)) {
                    lugares.add(p.getPlace());
                }

            }
            else if (p.getLikelihood()<=LIMIAR_INFERIOR_LOCAL && (lugares.isEmpty()|| entrouProbLimiarInferior) &&
                    (p.getPlace().getName().toString().toLowerCase().contains("mercad") ||
                            p.getPlace().getName().toString().toLowerCase().contains("atacad")
                    )

                    ){
                lugares.add(p.getPlace());
                entrouProbLimiarInferior = true;
            }

        }
        return lugares;
    }


    private void salvaMercado(Place lugar, String descricao, FirebaseUser firebaseUser) {

        mercadoReferencia.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("FIREBASE", dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("FIREBASE_ERRO", databaseError.getMessage());
            }
        });
        MercadoPojo m = new MercadoPojo();
        m.setLatitude(lugar.getLatLng().latitude);
        m.setLongitue(lugar.getLatLng().longitude);
        m.setNmMercado(lugar.getName().toString());
        m.setUrlImg(descricao);
        m.setUsuIdReg(firebaseUser.getUid());
        m.setMsDateIncl(new Date().getTime());
        mercadoReferencia.child(lugar.getId()).setValue(m);


    }

}
