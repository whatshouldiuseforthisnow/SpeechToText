plugins {
    id("java")
    id("application")
}

group = "org.example"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Vosk for speech-to-text
    implementation("com.alphacephei:vosk:0.3.45")
    //http and json
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.json:json:20230227")
    // JUnit 5 for testing
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    // langchain4j and ollaMa
    implementation ("dev.langchain4j:langchain4j:0.36.2")
    implementation ("dev.langchain4j:langchain4j-ollama:0.36.2")


}

application {
    mainClass.set("org.example.VoiceConfigure")
}

tasks.test {
    useJUnitPlatform()
    testLogging{
        events ;"passed"; "skipped"; "failed"
        showStandardStreams = true;
    }
    environment ("API_BASE_URL", System.getenv("API_BASE_URL")?:"")
    environment ("API_EMAIL", System.getenv("API_EMAIL")?:"")
    environment ("API_PASSWORD", System.getenv("API_PASSWORD")?:"")
    environment ("VOSK_MODEL_PATH", System.getenv("VOSK_MODEL_PATH")?:"")
    environment ("OLLAMA_BASE_URL", System.getenv("OLLAMA_BASE_URL")?:"")
    environment ("OLLAMA_MODEL", System.getenv("OLLAMA_MODEL")?:"")
}


