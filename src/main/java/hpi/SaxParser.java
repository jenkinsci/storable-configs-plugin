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

import hudson.model.BooleanParameterDefinition;
import hudson.model.ChoiceParameterDefinition;
import hudson.model.FileParameterDefinition;
import hudson.model.Hudson;
import hudson.model.ParameterDefinition;
import hudson.model.PasswordParameterDefinition;
import hudson.model.RunParameterDefinition;
import hudson.model.StringParameterDefinition;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SaxParser extends DefaultHandler {
	private File configFile;
	List<ParameterDefinition> params;
	private String tempVal;
	private ParameterDefinition tempParam;
	private String name;
	private String description;
	private String type;
	private String value;
	private List<String> choices;
	private String path;
	private String testDelimSymbol;
	private String nodeDelimSymbol;
	private String countDelimiterSymbol;
	private String delimiter;
	
	
	public SaxParser(File configFile) {
		this.configFile = configFile;
		this.params = new LinkedList<ParameterDefinition>();
		this.choices = new LinkedList<String>();
	}
	
	public List<ParameterDefinition> load() throws ParserConfigurationException, SAXException, IOException {
		parseDocument();
		return params;
	}
	
	private void parseDocument() throws ParserConfigurationException, SAXException, IOException {
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser sp = spf.newSAXParser();
		sp.parse(configFile, this);
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		tempVal = "";
	}
	

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		tempVal = new String(ch,start,length);
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if(qName.equalsIgnoreCase("Name")) {
			this.name = tempVal;
			return;
		}
		
		if (qName.equalsIgnoreCase("Description")) {
			this.description = tempVal;
			return;
		}
		
		if (qName.equalsIgnoreCase("Type")) {
			this.type = tempVal;
			return;
		}
		
		if (qName.equalsIgnoreCase("Value")) {
			this.value = tempVal;
			return;
		}
		
		if (qName.equalsIgnoreCase("Choice")) {
			this.choices.add(tempVal);
			return;
		}
		
		if (qName.equalsIgnoreCase("Path")) {
			this.path = tempVal;
			return;
		}
		
		if (qName.equalsIgnoreCase("TestDelimSymbol")) {
			this.testDelimSymbol = tempVal;
			return;
		}
		
		if (qName.equalsIgnoreCase("NodeDelimSymbol")) {
			this.nodeDelimSymbol = tempVal;
			return;
		}
		
		if (qName.equalsIgnoreCase("CountDelimiterSymbol")) {
			this.countDelimiterSymbol = tempVal;
		}
		
		if (qName.equalsIgnoreCase("Delimiter")) {
			this.delimiter = tempVal;
			return;
		}
		
		if (qName.equalsIgnoreCase("Parameter")) {
			if (type.equalsIgnoreCase("Boolean")) {
				tempParam = new BooleanParameterDefinition(name, new Boolean(value), description);
				params.add(tempParam);
				return;
			}
			
			if (type.equalsIgnoreCase("Choice")) {
				tempParam = new ChoiceParameterDefinition(name, choices.toArray(new String[0]), description);
				params.add(tempParam);
				choices.clear();
				return;
			}
			
			if (type.equalsIgnoreCase("String")) {
				tempParam = new StringParameterDefinition(name, value, description);
				params.add(tempParam);
				return;
			}
			
			if (type.equalsIgnoreCase("Password")) {
				tempParam = new PasswordParameterDefinition(name, value, description);
				params.add(tempParam);
				return;
			}
			
			if (type.equalsIgnoreCase("Run")) {
				tempParam = new RunParameterDefinition(name, value, description);
				params.add(tempParam);
				return;
			}
			
			if (type.equalsIgnoreCase("File")) {
				tempParam = new FileParameterDefinition(name, description);
				params.add(tempParam);
				return;
			}
			
			if (type.equalsIgnoreCase("hpi.ScriptSelectionTaskDefinition")) {
				if (Hudson.getInstance().getPlugin("selection-tasks-plugin") != null) {
					tempParam = new ScriptSelectionTaskDefinition(name, path, null, testDelimSymbol, nodeDelimSymbol, description, Integer.parseInt(countDelimiterSymbol), delimiter, value);
					params.add(tempParam);
					return;
				}
			}
		}
	}
}