package priceranking.app.italobarreto.localizacaodb.pojo;

/**
 * Created by root on 26/09/17.
 */

public class MercadoPojo {
    private String nmMercado;
    private double longitue;
    private double latitude;
    private String urlImg;

    public MercadoPojo() {
    }

    public String getNmMercado() {
        return nmMercado;
    }

    public void setNmMercado(String nmMercado) {
        this.nmMercado = nmMercado;
    }

    public double getLongitue() {
        return longitue;
    }

    public void setLongitue(double longitue) {
        this.longitue = longitue;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getUrlImg() {
        return urlImg;
    }

    public void setUrlImg(String urlImg) {
        this.urlImg = urlImg;
    }
}
