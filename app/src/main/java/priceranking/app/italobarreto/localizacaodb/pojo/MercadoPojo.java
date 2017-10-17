package priceranking.app.italobarreto.localizacaodb.pojo;

/**
 * Created by root on 26/09/17.
 */

public class MercadoPojo {
    private String nmMercado;
    private double longitue;
    private double latitude;
    private String urlImg;
    private String usuIdReg;
    private String idMercado;
    private long msDateIncl;
    public MercadoPojo() {
    }

    public MercadoPojo(String nmMercado, String idMercado) {
        this.nmMercado = nmMercado;
        this.idMercado = idMercado;
    }

    public MercadoPojo(String nmMercado, double longitue, double latitude, String idMercado) {
        this.nmMercado = nmMercado;
        this.longitue = longitue;
        this.latitude = latitude;
        this.idMercado = idMercado;
    }

    public String getIdMercado() {
        return idMercado;
    }

    public void setIdMercado(String idMercado) {
        this.idMercado = idMercado;
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


    public String getUsuIdReg() {
        return usuIdReg;
    }

    public void setUsuIdReg(String usuIdReg) {
        this.usuIdReg = usuIdReg;
    }

    public long getMsDateIncl() {
        return msDateIncl;
    }

    public void setMsDateIncl(long msDateIncl) {
        this.msDateIncl = msDateIncl;
    }
}
