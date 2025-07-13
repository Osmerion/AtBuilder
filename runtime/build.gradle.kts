/*
 * Copyright 2025 Leon Linhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
plugins {
    id("com.osmerion.java-base-conventions")
    id("com.osmerion.maven-publish-conventions")
    `java-library`
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name = "AtBuilder"
                description = "AtBuilder generates safe builders for your records with support for nullable and omittable types."
            }
        }
    }
}

dependencies {
    api(libs.jspecify)
    api(libs.omittable)
}
