package org.vaadin.example;

import com.helger.commons.csv.CSVReader;

import javax.validation.constraints.NotNull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Martin Vysny <mavi@vaadin.com>
 */
public class AssetException {
    private long exceptionNumber;
    private String assetClass;
    private String sector;
    private String rating;
    private String couponType;
    private String state;

    public long getExceptionNumber() {
        return exceptionNumber;
    }

    public void setExceptionNumber(long exceptionNumber) {
        this.exceptionNumber = exceptionNumber;
    }

    public String getAssetClass() {
        return assetClass;
    }

    public void setAssetClass(String assetClass) {
        this.assetClass = assetClass;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getCouponType() {
        return couponType;
    }

    public void setCouponType(String couponType) {
        this.couponType = couponType;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public AssetException() {
    }

    public AssetException(long exceptionNumber, String assetClass, String sector, String rating, String couponType, String state) {
        this.exceptionNumber = exceptionNumber;
        this.assetClass = assetClass;
        this.sector = sector;
        this.rating = rating;
        this.couponType = couponType;
        this.state = state;
    }

    @Override
    public String toString() {
        return "AssetException{" +
                "exceptionNumber=" + exceptionNumber +
                ", asssetClass='" + assetClass + '\'' +
                ", sector='" + sector + '\'' +
                ", rating='" + rating + '\'' +
                ", couponType='" + couponType + '\'' +
                ", state='" + state + '\'' +
                '}';
    }

    @NotNull
    public static List<AssetException> loadFromCSV() throws IOException {
        final List<AssetException> list = new ArrayList<>();
        try (InputStream is = AssetException.class.getClassLoader().getResourceAsStream("exceptions.csv")) {
            final CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(is)));
            reader.setSkipLines(1);
            reader.forEach(row -> {
                if (!row.isEmpty()) {
                    try {
                        final AssetException ex = new AssetException(Integer.parseInt(row.get(0).trim()),
                                row.get(1).trim(),
                                row.get(2).trim(),
                                row.get(3).trim(),
                                row.get(4).trim(),
                                row.get(5)
                        );
                        list.add(ex);
                    } catch (Exception ex) {
                        throw new RuntimeException("Failed to parse row " + row, ex);
                    }
                }
            });
        }
        return list;
    }

    @NotNull
    public static final List<AssetException> ALL_EXCEPTIONS;
    static {
        try {
            ALL_EXCEPTIONS = Collections.unmodifiableList(loadFromCSV());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
