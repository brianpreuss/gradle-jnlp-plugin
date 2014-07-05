package de.gliderpilot.gradle.jnlp

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * This is the main plugin file. Put a description of your plugin here.
 */
class GradleJnlpPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.extensions.create('jnlp', GradleJnlpPluginExtension, this, project)

        project.configurations.maybeCreate('jnlp')

        project.tasks.create('generateJnlp', JnlpTask) {
            from = project.configurations.jnlp
            output new File(project.buildDir, 'tmp/jnlp/launch.jnlp')
        }
        project.tasks.create('copyJars', CopyJarsTask) {
            onlyIf { !project.jnlp.signJarParams }
            from = project.configurations.jnlp
            into new File(project.buildDir, 'tmp/jnlp/lib')
        }
        project.tasks.create('signJars', SignJarsTask) {
            onlyIf { project.jnlp.signJarParams }
            from = project.configurations.jnlp
            into new File(project.buildDir, 'tmp/jnlp/lib')
        }
        project.tasks.create('createWebstartDir') {
            dependsOn 'generateJnlp', 'copyJars', 'signJars'
        }
        project.plugins.withId('java') {
            // if plugin java is applied use the runtime configuration
            project.configurations.jnlp.extendsFrom project.configurations.runtime
            // plus the project itself
            project.dependencies.jnlp project
        }
    }
}
