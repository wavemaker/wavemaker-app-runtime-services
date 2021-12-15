package com.wavemaker.runtime.rest;

import java.util.Base64;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class RequestContext {

    private MultiValueMap<String, String> headers;
    private MultiValueMap<String, String> queryParams;

    public RequestContext(MultiValueMap<String, String> headers, MultiValueMap<String, String> queryParams) {
        this.headers = headers;
        this.queryParams = queryParams;
    }

    public MultiValueMap<String, String> getHeaders() {
        return headers;
    }

    public MultiValueMap<String, String> getQueryParams() {
        return queryParams;
    }

    public static class Builder {
        private MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        private MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();

        private Builder() {
        }

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder addHeader(String key, String value) {
            headers.add(key, value);
            return this;
        }

        public Builder addJwtToken(String token) {
            headers.set("Authorization", "Bearer " + token);
            return this;
        }

        public Builder addBasicAuth(String username, String password) {
            headers.add("Authorization", "Basic " + new String(Base64.getEncoder()
                    .encode(new StringBuilder().append(username).append(':').append(password).toString().getBytes())));
            return this;
        }

        public Builder addQueryParam(String key, String value) {
            queryParams.add(key, value);
            return this;
        }

        public RequestContext build() {
            return new RequestContext(headers, queryParams);
        }


    }
}