package org.example;

import java.io.InputStream;
import java.util.Properties;

public class SecretsLoader {
    private SecretsLoader(){
    }
    public static Properties load() {
        Properties props = new Properties();
        props.setProperty("api.base_Url", requireEnv("API_BASE_URL"));
        props.setProperty("api.email", requireEnv("API_EMAIL"));
        props.setProperty("api.password", requireEnv("API_PASSWORD"));
        props.setProperty("vosk.model_path", requireEnv("VOSK_MODEL_PATH"));
        props.setProperty("ollama.base_Url",
                getEnvOrDefault("OLLAMA_BASE_URL", "https://localhost:11434"));
        props.setProperty("ollama.model",
                getEnvOrDefault("OLLAMA_MODEL","llama3.1"));
        return props;
    }
    private static String requireEnv(String name){
        String value = System.getenv(name);
        if(value == null || value.isBlank()){
            throw new RuntimeException(
                    "missing env variable" + name
            );
        }
        return value;
    }
    private static String getEnvOrDefault(String name, String defaultValue){
        String value = System.getenv(name);
        return (
                value!=null && !value.isBlank())? value: defaultValue;
    }

}
