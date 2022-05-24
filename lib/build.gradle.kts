plugins {
	java
	`maven-publish`
}

repositories {
	mavenCentral()
	maven("https://jitpack.io")
}

val archiveName = "autoupdate-lib"
val version: String? by project
val group: String? by project

java {
	sourceCompatibility = JavaVersion.VERSION_1_8
	targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
	compileOnly("com.github.Anuken.Arc:arc-core:v135")
	compileOnly("com.github.Anuken.Mindustry:core:v135")
}

tasks.jar {
	archiveFileName.set("$archiveName")

	from(*configurations.runtimeClasspath.files.map { if (it.isDirectory()) it else zipTree(it) }.toTypedArray())
}

tasks.register("deploy") {
    dependsOn("jar")
}

publishing {
	publications {
		create<MavenPublication>("maven") {
			groupId = group ?: "?.?.?"
			artifactId = "autoupdate-lib"
			this.version = version ?: "?"

			from(components["java"])
		}
	}
}
