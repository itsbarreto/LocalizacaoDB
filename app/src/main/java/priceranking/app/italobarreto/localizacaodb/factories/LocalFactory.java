package priceranking.app.italobarreto.localizacaodb.factories;

import com.google.android.gms.location.places.Place;

import java.lang.reflect.Field;

/**
 * Created by root on 26/09/17.
 */

public class LocalFactory {

    public static final Field[] CAMPOS_CLASS_PLACES = Place.class.getFields();

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
}
