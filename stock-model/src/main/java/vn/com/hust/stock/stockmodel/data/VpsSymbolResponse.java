package vn.com.hust.stock.stockmodel.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VpsSymbolResponse {
    @JsonProperty
    private String CWExcersisePrice;
    private String CWExerciseRatio;
    private String CWIssuerName;
    private String CWLastTradingDate;
    private String CWListedShare;
    private String CWMaturityDate;
    private String CWType;
    private String CWUnderlying;
    private double avePrice;
    private double c;
    private double changePc;
    private double f;
    private long fBValue;
    private String fBVol;
    private long fRoom;
    private long fSValue;
    private long fSVolume;
    private String  g1;
    private String  g2;
    private String g3;
    private String  g4;
    private String  g5;
    private String  g6;
    private String  g7;
    private double  highPrice;
    private long id;
    private double lastPrice;
    private long lastVolume;
    private long lot;
    private double lowPrice;
    private double mc;
    private String mp;
    private double ot;
    private double r;
    private Long sBenefit;
    private String sType;
    private String sym;
}
