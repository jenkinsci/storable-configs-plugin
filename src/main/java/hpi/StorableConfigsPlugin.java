/*Copyright (c) 2010, Parallels-NSU lab. All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided 
that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions 
    * and the following disclaimer.
    
    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions 
    * and the following disclaimer in the documentation and/or other materials provided with 
    * the distribution.
    
    * Neither the name of the Parallels-NSU lab nor the names of its contributors may be used to endorse 
    * or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED 
WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR 
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package hpi;

import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BooleanParameterDefinition;
import hudson.model.ChoiceParameterDefinition;
import hudson.model.FileParameterDefinition;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.PasswordParameterDefinition;
import hudson.model.RunParameterDefinition;
import hudson.model.StringParameterDefinition;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.xml.parsers.ParserConfigurationException;

import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.xml.sax.SAXException;

public class StorableConfigsPlugin implements Action {
	private final AbstractProject<?, ?> project;
	private static final String defaultDir = "storable-configs";
	
	public StorableConfigsPlugin(AbstractProject<?, ?> project) {
		this.project = project;
	}
	
	@Override
	public String getDisplayName() {
		return "Storable configs";
	}

	@Override
	public String getIconFileName() {
		return "/plugin/storable-configs-plugin/storableConfigs.png";
	}

	@Override
	public String getUrlName() {
		return "storable-configs-plugin";
	}
	
	public String getFullName() {
		return project.getFullName();
	}
	
	public void doSaveSettings(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
		checkConfigurePermission();
		try {
			String filename = req.getParameter("filename") + ".xml";
			FilePath file = new FilePath(getStorableConfigsDir(), filename);
			file.touch(new Date().getTime());
			PrintWriter pw = new PrintWriter(file.write(), true);
			
			pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			pw.println("<Parameters>");
			
			ParametersDefinitionProperty pdp = project.getProperty(ParametersDefinitionProperty.class);
			List<ParameterDefinition> pdlist = pdp.getParameterDefinitions();
			for (ParameterDefinition pd : pdlist) {
				if (pd instanceof BooleanParameterDefinition) {
					pw.println("<Parameter>");
					pw.println("<Name>"+pd.getName()+"</Name>");
					pw.println("<Description>"+pd.getDescription()+"</Description>");
					BooleanParameterDefinition bpd = (BooleanParameterDefinition)pd;
					pw.println("<Type>Boolean</Type>");	
					pw.println("<Value>"+bpd.isDefaultValue()+"</Value>");
					pw.println("</Parameter>");
				} else
					if (pd instanceof ChoiceParameterDefinition) {
						pw.println("<Parameter>");
						pw.println("<Name>"+pd.getName()+"</Name>");
						pw.println("<Description>"+pd.getDescription()+"</Description>");
						ChoiceParameterDefinition cpd = (ChoiceParameterDefinition)pd;
						pw.println("<Type>Choice</Type>");
						pw.println("<Choices>");
						List<String> strings = cpd.getChoices();
						for(String str : strings) {
							pw.println("<Choice>"+str+"</Choice>");
						}
						pw.println("</Choices>");
						pw.println("</Parameter>");
					} else
						if (pd instanceof StringParameterDefinition) {
							pw.println("<Parameter>");
							pw.println("<Name>"+pd.getName()+"</Name>");
							pw.println("<Description>"+pd.getDescription()+"</Description>");
							StringParameterDefinition spd = (StringParameterDefinition)pd;
							pw.println("<Type>String</Type>");
							pw.println("<Value>"+spd.getDefaultValue()+"</Value>");
							pw.println("</Parameter>");
						} else
							if (pd instanceof PasswordParameterDefinition) {
								pw.println("<Parameter>");
								pw.println("<Name>"+pd.getName()+"</Name>");
								pw.println("<Description>"+pd.getDescription()+"</Description>");
								PasswordParameterDefinition ppd = (PasswordParameterDefinition)pd;
								pw.println("<Type>Password</Type>");
								pw.println("<Value>"+ppd.getDefaultValue()+"</Value>");
								pw.println("</Parameter>");
							} else
								if (pd instanceof RunParameterDefinition) {
									pw.println("<Parameter>");
									pw.println("<Name>"+pd.getName()+"</Name>");
									pw.println("<Description>"+pd.getDescription()+"</Description>");
									RunParameterDefinition rpd = (RunParameterDefinition)pd;
									pw.println("<Type>Run</Type>");
									pw.println("<Value>"+rpd.getProjectName()+"</Value>");
									pw.println("</Parameter>");
								} else
									if (pd instanceof FileParameterDefinition) {
										pw.println("<Parameter>");
										pw.println("<Name>"+pd.getName()+"</Name>");
										pw.println("<Description>"+pd.getDescription()+"</Description>");
										pw.println("<Type>File</Type>");
										pw.println("</Parameter>");
									} else {
										if (Hudson.getInstance().getPlugin("selection-tasks-plugin") != null) {
											if (pd instanceof ScriptSelectionTaskDefinition) {
												pw.println("<Parameter>");
												pw.println("<Name>"+pd.getName()+"</Name>");
												pw.println("<Description>"+pd.getDescription()+"</Description>");
												ScriptSelectionTaskDefinition td = (ScriptSelectionTaskDefinition)pd;
												
												pw.println("<Type>hpi.ScriptSelectionTaskDefinition</Type>");
												pw.println("<Path>"+td.getPath()+"</Path>");
												pw.println("<TestDelimSymbol>"+td.getTestDelimSymbol()+"</TestDelimSymbol>");
												pw.println("<NodeDelimSymbol>"+td.getNodeDelimSymbol()+"</NodeDelimSymbol>");
												pw.println("<CountDelimiterSymbol>"+td.getCountDelimiterSymbol()+"</CountDelimiterSymbol>");
												pw.println("<Delimiter>"+td.getDelimiter()+"</Delimiter>");
												pw.println("<Value>"+td.getDefaultValue()+"</Value>");
												pw.println("</Parameter>");
											}
										}
									}
			}
			
			pw.println("</Parameters>");
			pw.close();
			rsp.sendRedirect(req.getRootPath()+"/job/"+getFullName()+"/"+getUrlName());
		} catch (Exception e) {
			rsp.sendError(450, e.getMessage());
		}
	}
	
	public void doChangeSettings(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, SAXException, ParserConfigurationException {
		checkConfigurePermission();
		String filename = Stapler.getCurrentRequest().getParameter("group");
		
		File configFile = new File(getConfigsDir(), filename);
		SaxParser parser = new SaxParser(configFile);
		List<ParameterDefinition> params = parser.load();
		
		ParametersDefinitionProperty pdp = project.getProperty(ParametersDefinitionProperty.class);
		List<ParameterDefinition> projectParams = pdp.getParameterDefinitions();
		
		//other variants for projectParams?
		projectParams.clear();
		
		for (ParameterDefinition parameterDefinition : params) {
			projectParams.add(parameterDefinition);
		}
		
		project.save();
		rsp.sendRedirect(req.getRootPath()+"/job/"+getFullName()+"/"+getUrlName());
	}
	
	public List<String> getConfigs() throws InterruptedException, IOException {
		checkConfigurePermission();
		List<String> configs = new ArrayList<String>();
		FilePath dir = getStorableConfigsDir();
		FilePath[] files = dir.list("*.xml");
		for (FilePath filePath : files) {
			configs.add(filePath.getName());
		}
		return configs;
	}
	
	public String getContent() throws IOException, InterruptedException {
		String filename = Stapler.getCurrentRequest().getParameter("file");
		FilePath file = new FilePath(getStorableConfigsDir(), filename);
		if (file.exists()) {
			return file.readToString();
		} else {
			return "<div>No such file</div>";
		}
	}
	
    private void checkConfigurePermission() {
        project.checkPermission(Item.CONFIGURE);
    }
    
    private FilePath getStorableConfigsDir() throws InterruptedException, IOException {
    	FilePath dir = new FilePath(getConfigsDir());
    	if (!dir.exists()) {
    		dir.mkdirs();
    	}
    	return dir;
    }
    
    private File getConfigsDir() {
    	return new File(project.getRootDir(), defaultDir);
    }
}
