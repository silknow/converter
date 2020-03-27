plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

// tag::dependencies-without-version[]
dependencies {
    implementation("org.springframework:spring-web")
}

dependencies {
    constraints {
        implementation("org.springframework:spring-web:5.0.2.RELEASE")
    }
}
// end::dependencies-without-version[]

task<Copy>("copyLibs") {
    from(configurations.compileClasspath)
    into("$buildDir/libs")
}
