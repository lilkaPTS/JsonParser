package com.company.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Application {
    @JsonProperty(value = "name",required = true)
    private String name;

    @Override
    public String toString() {
        return "\"application\" : {\n" +
                "\"name\" : \"" + name + "\"\n" +
                "}";
    }
}
