plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    id("org.jetbrains.intellij") version "1.17.2"
}

group = "org.sber"
version = "0.5.2"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2023.2.5")
    type.set("IC") // Target IDE Platform
    plugins.set(listOf("java", "Kotlin"))

    // Для тестирования
    downloadSources.set(true)
    updateSinceUntilBuild.set(true)
}

dependencies {
    implementation(kotlin("stdlib"))
    
    // LangChain4j for AI integration
    implementation("dev.langchain4j:langchain4j:0.35.0")
    implementation("dev.langchain4j:langchain4j-open-ai:0.35.0")
    implementation("chat.giga:langchain4j-gigachat:0.1.13")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.assertj:assertj-core:3.24.2")
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("232")
        untilBuild.set("242.*")

        pluginDescription.set("""
            <h2>Hex UI Framework Support</h2>
            
            <p>Улучшает опыт работы с Hex UI Test Framework в IntelliJ IDEA.</p>
            
            <h3>Основные возможности:</h3>
            <ul>
                <li><b>Русские названия в автодополнении</b> — видно сразу, какой элемент за что отвечает</li>
                <li><b>Умная документация</b> — показывает полную информацию об элементе</li>
                <li><b>Inlay hints</b> — подсказки прямо в коде рядом с полями</li>
                <li><b>Иконки</b> — визуальное различие Page/Component/Element</li>
                <li><b>Inspections</b> — проверяет наличие русских названий</li>
                <li><b>Quick Fixes</b> — автоматическое добавление JavaDoc</li>
            </ul>
            
        """.trimIndent())

        changeNotes.set("""
            <h3>Version 1.0.0</h3>
            <ul>
                <li>Первый релиз</li>
                <li>Completion с русскими названиями элементов</li>
                <li>Documentation provider</li>
                <li>Inlay hints</li>
                <li>Line markers</li>
                <li>Inspections для проверки качества кода</li>
            </ul>
        """.trimIndent())
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
