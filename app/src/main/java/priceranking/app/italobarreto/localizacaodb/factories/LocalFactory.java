package priceranking.app.italobarreto.localizacaodb.factories;

import com.google.android.gms.location.places.Place;

import java.lang.reflect.Field;

/**
 * Created by root on 26/09/17.
 */

public class LocalFactory {

    public static final Field[] CAMPOS_CLASS_PLACES = Place.class.getFields();

    public static StringBuilder montaStringLugar(Place lugar, double probabilidade){
        StringBuilder strRetorno = new StringBuilder();
        strRetorno.append(String.format("\n\n==============\nPlace: '%s' \n Likelihood: %g",
                lugar.getName(),
                probabilidade)).
                append("\nAtribuicoes: ").
                append(lugar.getAttributions()).
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


}
