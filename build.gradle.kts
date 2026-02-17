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



}

application {
    mainClass.set("org.example.ServerMain")
}

tasks.test {
    useJUnitPlatform()
    testLogging{
        events ;"passed"; "skipped"; "failed"
        showStandardStreams = true;
    }
}
