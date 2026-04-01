package com.technoboost.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatabaseSecret {

    private String username;
    private String password;
    private String engine;
    private String host;
    private int port;

    @JsonProperty("dbname")
    private String dbName;

    public String buildJdbcUrl() {
        return String.format("jdbc:postgresql://%s:%d/%s", host, port, dbName);
    }
}
