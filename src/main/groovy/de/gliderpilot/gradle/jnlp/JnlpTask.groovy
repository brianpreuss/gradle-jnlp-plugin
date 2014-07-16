package de.gliderpilot.gradle.jnlp

import java.util.jar.JarFile
import groovy.xml.MarkupBuilder

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

class JnlpTask extends DefaultTask {

    @InputFiles
    Configuration from

    @OutputFile
    File output

    @TaskAction
    void generateJnlp() {
        MarkupBuilder xml = new MarkupBuilder(output.newPrintWriter('UTF-8'))
        xml.jnlp(project.jnlp.jnlpParams) {
            delegate.with project.jnlp.withXmlClosure
            resources {
                j2se(project.jnlp.j2seParams)
                def resolvedJars = from.resolvedConfiguration.resolvedArtifacts.findAll { it.extension == 'jar' }
                resolvedJars.each { ResolvedArtifact artifact ->
                    jar(jarParams(artifact))
                }

                // see http://docs.oracle.com/javase/tutorial/deployment/deploymentInDepth/avoidingUnnecessaryUpdateChecks.html
                if (project.jnlp.useVersions)
                    property name: 'jnlp.versionEnabled', value: 'true'
                // see http://docs.oracle.com/javase/tutorial/deployment/deploymentInDepth/reducingDownloadTime.html
                if (project.jnlp.usePack200)
                    property name: 'jnlp.packEnabled', value: 'true'
            }
            delegate.with project.jnlp.desc
        }
    }

    Map<String, String> jarParams(ResolvedArtifact artifact) {
        String version = artifact.moduleVersion.id.version
        Map<String, String> jarParams = [:]
        if (project.jnlp.useVersions) {
            jarParams.href = "lib/${artifact.name}.jar"
            jarParams.version = "${version}"
        }
        else {
            jarParams.href = "lib/${artifact.name}__V${version}.jar"
        }

        if (containsMainClass(artifact.file))
            jarParams.main = 'true'
        return jarParams
    }

    boolean containsMainClass(File file) {
        new JarFile(file).getEntry(project.jnlp.mainClassName.replace('.', '/') + '.class')
    }
}
