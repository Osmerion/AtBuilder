plugins {
    id("com.osmerion.java-base-conventions")
    id("com.osmerion.maven-publish-conventions")
    java
    `jvm-test-suite`
}

java {
    withJavadocJar()
    withSourcesJar()
}

val compileTestingClasspath = configurations.create("compileTestingClasspath") {
    isCanBeConsumed = false
    isCanBeResolved = true
}

@Suppress("UnstableApiUsage")
testing {
    suites {
        withType<JvmTestSuite>().configureEach {
            useJUnitJupiter()

            dependencies {
                implementation(platform(buildDeps.junit.bom))
                implementation(buildDeps.junit.jupiter.api)

                implementation(buildDeps.assertj.core)

                runtimeOnly(buildDeps.junit.jupiter.engine)
                runtimeOnly(buildDeps.junit.platform.launcher)
            }
        }

        named<JvmTestSuite>("test") {
            dependencies {
                implementation(buildDeps.mockito.core)
            }
        }

        register<JvmTestSuite>("functionalTest") {
            targets.configureEach {
                testTask.configure {
                    useJUnitJupiter()

                    jvmArgs(
                        "--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
                        "--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
                        "--add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED",
                        "--add-opens=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
                        "--add-opens=jdk.compiler/com.sun.tools.javac.jvm=ALL-UNNAMED",
                        "--add-opens=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED",
                        "--add-opens=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED",
                        "--add-opens=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED",
                        "--add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
                        "--add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
                    )

                    // Ensure that the compile-testing classpath is available to the test task
                    val compileTestingClasspath: FileCollection = compileTestingClasspath

                    doFirst {
                        systemProperty("COMPILE_TESTING_CLASSPATH", compileTestingClasspath.asPath)
                    }
                }
            }

            dependencies {
                implementation(project())
                implementation(buildDeps.jetbrains.annotations)
                implementation(buildDeps.kotlin.compile.testing)
            }
        }
    }
}

tasks {
    @Suppress("UnstableApiUsage")
    check {
        dependsOn(testing.suites.named("functionalTest"))
    }
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name = "AtBuilder Annotation Processor"
                description = "The AtBuilder annotation processor generates safe builders for your records."
            }
        }
    }
}

dependencies {
    implementation(projects.runtime)
    implementation(buildDeps.javapoet)

    compileTestingClasspath.extendsFrom(configurations.runtimeClasspath.get())
    compileTestingClasspath(projects.runtime)
}
