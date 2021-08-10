package com.wavemaker.runtime.data.model;

public class FetchQuery {
    private String query;

    public FetchQuery(String query) {
        this.query = query;
    }

    public String getQuery() {
        return this.query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
