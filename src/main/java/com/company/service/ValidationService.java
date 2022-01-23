package com.company.service;

import com.company.model.ArtifactObject1;
import com.company.model.ArtifactObject2;
import com.company.pojo.Artifacts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;


@Service
public class ValidationService {
    @Autowired
    private SchemaService schemaService;
    @Autowired
    private FileService fileService;
    @Autowired
    private JsonService jsonService;

    private static final ObjectMapper MAPPER = new ObjectMapper();


    /**
     * At first, let's take errors by JsonSchema.json, then checked
     * founded errors to contains "null found" or "artifact is missing",
     * if this errors not found, we will set ArrayList of JsonNode based
     * Artifacts.class which is helper POJO to parse ArtifactObject1-2,
     * then we will check each JsonNode to errors and added errors in result.
     * @param inputFile file from upload form
     * @return errors in input file schema
     */
    public String getErrors(MultipartFile inputFile) {
        StringBuilder result = new StringBuilder();
        result.append(checkFileType(inputFile));
        if(!"".equals(result.toString())) {
            return result.toString();
        }
        String inputFileContent = fileService.getFileContent(inputFile);
        JsonNode inputFileJsonNode = jsonService.getJsonNode(inputFileContent);
        schemaService.getSchemaErrors(jsonService.getJsonNode(fileService.getFileContent("JsonSchema.json")),inputFileJsonNode).forEach(result::append);
        if(!(result.toString().matches("(?s).*null found, object expected(?s).*") ||
                result.toString().matches("(?s).*artifacts: is missing but it is required(?s).*"))) {  //check ArtifactObject1-2
            try {
                ArrayList<JsonNode> artifacts = MAPPER.readValue(inputFileContent, Artifacts.class).getArtifacts();
                for (JsonNode artifact : artifacts) {
                    if (!schemaService.getSchemaErrors(schemaService.getJsonSchema(ArtifactObject1.class), artifact).isEmpty()) { //save by get(0)
                        if (new ArrayList<>(schemaService.getSchemaErrors(schemaService.getJsonSchema(ArtifactObject1.class), artifact))
                                .get(0).toString()
                                .equals("$.mvn: is missing but it is required")
                        ) {
                            schemaService.getSchemaErrors(schemaService.getJsonSchema(ArtifactObject2.class), artifact)
                                    .forEach(err ->
                                            result.append("$.artifacts.")
                                                    .append(artifacts.indexOf(artifact))
                                                    .append(".")
                                                    .append(err.toString().substring(2))
                                    );
                        } else {
                            schemaService.getSchemaErrors(schemaService.getJsonSchema(ArtifactObject1.class), artifact)
                                    .forEach(err ->
                                            result.append("$.artifacts.")
                                                    .append(artifacts.indexOf(artifact))
                                                    .append(".")
                                                    .append(err.toString().substring(2))
                                    );
                        }
                    }
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        String output = result.toString().replace("$", "\n$");
        return output.length() == 0 ? "" : output.substring(1);
    }

    private String checkFileType(MultipartFile inputFile) {
        StringBuilder result = new StringBuilder();
        if("application/json".equals(inputFile.getContentType())){
            return "";
        } else {
            result.append("$.Error type, file must be of type .json");
        }
        return result.toString();
    }
}
