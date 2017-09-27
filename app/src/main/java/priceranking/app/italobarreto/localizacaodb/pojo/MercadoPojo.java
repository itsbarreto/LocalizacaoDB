package priceranking.app.italobarreto.localizacaodb.pojo;

import java.util.Date;

/**
 * Created by root on 26/09/17.
 */

public class MercadoPojo {
    private String nmMercado;
    private double longitue;
    private double latitude;
    private String urlImg;
    private String usuIdReg;
    private long msDateIncl;
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
