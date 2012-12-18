package org.bzn.maven.plugin;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * Goal which touches a timestamp file.
 * 
 * @goal generate-version.html
 * 
 * @phase process-sources
 */
public class BuildInfoMojo extends AbstractMojo {
	
	/**
	 * The Maven Project Object
	 *
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	protected MavenProject project;

	/**
	 * The Maven Session Object
	 *
	 * @parameter expression="${session}"
	 * @required
	 * @readonly
	 */
	protected MavenSession session;

	/**
	 * The Maven PluginManager Object
	 *
	 * @component
	 * @required
	 */
	protected BuildPluginManager pluginManager;
	
	/**
	 * Location of the file.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File outputDirectory;
	/**
	 * finalName
	 * 
	 * @parameter expression="${project.build.finalName}"
	 * @required
	 */
	private String finalName;

	private Map<String,Object> dataTemplate = new HashMap<String, Object>();
	
	public void execute() throws MojoExecutionException {
		File outputDir = outputDirectory;
		if (!outputDir.exists()) {
			outputDir.mkdirs();
		}
		File versionFile = new File(outputDir, "version.html");
		try {
			executeBuildNumberPlugin();
			addProperty("buildNumber");
			addProperty("buildScmBranch");
			dataTemplate.put("artifactId", project.getArtifactId());
			dataTemplate.put("version", project.getVersion());
			getLog().info(finalName);
			createTemplate().process(dataTemplate, new FileWriter(versionFile));
		} catch (IOException e) {
			throw new MojoExecutionException("Error creating file " + versionFile, e);
		}catch(Exception e){
			throw new MojoExecutionException("Error creating file ", e);
		}
	}

	private void executeBuildNumberPlugin() throws MojoExecutionException {
		executeMojo(
				plugin(groupId("org.codehaus.mojo"),
						artifactId("buildnumber-maven-plugin"),
						version("1.1")),
				goal("create"),
				configuration(
						element(name("doCheck"), "false"),
						element(name("doUpdate"), "false")
				),
				executionEnvironment(project, session, pluginManager)
		);
	}
	
	private Template createTemplate() throws IOException{
		Configuration cfg = new Configuration();
		cfg.setTemplateLoader(new ClassTemplateLoader(getClass(), "/"));
		return cfg.getTemplate("version.ftl");
	}
	
	private void addProperty(String propName){
		getLog().info("---------------------------------------------------");
		getLog().info(propName);
		getLog().info(project.getProperties().getProperty(propName));
		getLog().info("---------------------------------------------------");
		dataTemplate.put(propName, project.getProperties().getProperty(propName));
	}
}
