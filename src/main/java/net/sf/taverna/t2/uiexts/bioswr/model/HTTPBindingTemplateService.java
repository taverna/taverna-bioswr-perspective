/**
 * *****************************************************************************
 * Copyright (C) 2014 Spanish National Bioinformatics Institute (INB),
 * Barcelona Supercomputing Center and The University of Manchester
 *
 * Modifications to the initial code base are copyright of their respective
 * authors, or their employers as appropriate.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *****************************************************************************
 */

package net.sf.taverna.t2.uiexts.bioswr.model;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Icon;
import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;
import net.sf.taverna.t2.activities.rest.RESTActivity;
import net.sf.taverna.t2.activities.rest.ui.servicedescription.GenericRESTTemplateService;
import net.sf.taverna.t2.activities.rest.ui.servicedescription.RESTActivityIcon;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSequenceMember;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.inb.bsc.wsdl20.Binding;
import org.inb.bsc.wsdl20.BindingMessageReference;
import org.inb.bsc.wsdl20.BindingOperation;
import org.inb.bsc.wsdl20.Description;
import org.inb.bsc.wsdl20.ElementDeclaration;
import org.inb.bsc.wsdl20.Endpoint;
import org.inb.bsc.wsdl20.Interface;
import org.inb.bsc.wsdl20.InterfaceMessageReference;
import org.inb.bsc.wsdl20.MessageContentModel;
import org.inb.bsc.wsdl20.Service;
import org.inb.bsc.wsdl20.extensions.WSDLPredefinedExtension;
import org.inb.bsc.wsdl20.extensions.http.HTTPBindingMessageReferenceExtensions;
import org.inb.bsc.wsdl20.extensions.http.HTTPBindingOperationExtensions;
import org.inb.bsc.wsdl20.extensions.http.HTTPHeader;
import uk.org.taverna.scufl2.api.configurations.Configuration;

/**
 * @author Dmitry Repchevsky
 */

public class HTTPBindingTemplateService extends ServiceDescription implements ServiceDescriptionProvider {
    
    private final BindingOperation operation;
    
    public HTTPBindingTemplateService(BindingOperation operation) {
        this.operation = operation;
    }

    @Override
    public URI getActivityType() {
            return GenericRESTTemplateService.ACTIVITY_TYPE;
    }

    @Override
    public Icon getIcon() {
        return RESTActivityIcon.getRESTActivityIcon();
    }


    @Override
    public String getName() {
        return "REST";
    }

    @Override
    public String getId() {
        return "http://www.taverna.org.uk/2010/services/rest";
    }
    
    @Override
    public boolean isTemplateService() {
            return false;
    }

    @Override
    public List<? extends Comparable<?>> getPath() {
        return Arrays.asList(SERVICE_TEMPLATES);
    }

    @Override
    protected List<? extends Object> getIdentifyingData() {
        return null;
    }

    @Override
    public void findServiceDescriptionsAsync(FindServiceDescriptionsCallBack callBack) {
            callBack.partialResults(Collections.singleton(this));
            callBack.finished();
    }


    @Override
    public Configuration getActivityConfiguration() {
        Configuration configuration = new Configuration();
        configuration.setType(GenericRESTTemplateService.ACTIVITY_TYPE.resolve("#Config"));
        
        Binding binding = operation.getParent();
        Interface _interface = binding.getInterface();
        
        Description description = binding.getParentElement();
        for (Service service : description.getAllServices()) {
            if (_interface.equals(service.getInterface())) {
                for (Endpoint endpoint : service.getEndpoints()) {
                    if (binding.equals(endpoint.getBinding())) {
                        ObjectNode json = (ObjectNode)configuration.getJson();
                        ObjectNode requestNode = json.objectNode();
                        ArrayNode headersNode = requestNode.arrayNode();

                        HTTPBindingOperationExtensions ext = (HTTPBindingOperationExtensions)operation.getComponentExtensions(WSDLPredefinedExtension.HTTP.URI);
                        String httpMethod = ext.getHttpMethod();
                        requestNode.put("httpMethod", httpMethod == null ? "GET" : httpMethod);

                        URI address = endpoint.getAddress();
                        StringBuilder url = new StringBuilder(address.toString());
                        
                        String location = operation.getExtensionAttribute(HTTPBindingOperationExtensions.HTTP_LOCATION_ATTR);
                        if (location != null) {
                            url.append(location);
                        }

                        String inputSerialization = ext.getHttpInputSerialization();
                        if ("application/x-www-form-urlencoded".equals(inputSerialization)) {
                            putUrlEncodedInputs(operation, httpMethod, headersNode, url);
                        } else if ("application/xml".equals(inputSerialization) && 
                                  ("POST".equals(httpMethod) || "PUT".equals(httpMethod))) {
                            putXMLInputs(operation, headersNode);
                            headersNode.addObject().put("header", "Content-Type").put("value", "application/xml");
                        }
                        
                        requestNode.put("absoluteURITemplate", url.toString());

                        String outputSerialization = ext.getHttpOutputSerialization();
                        if (outputSerialization != null) {
                            headersNode.addObject().put("header", "Accept").put("value", outputSerialization);
                        }

                        requestNode.set("headers", headersNode);
                        json.set("request", requestNode);
                        json.put("outgoingDataFormat", RESTActivity.DATA_FORMAT.String.name());
                        json.put("showRedirectionOutputPort", false);
                        json.put("showActualURLPort", false);
                        json.put("showResponseHeadersPort", false);
                        json.put("escapeParameters", true);
                        
                    }
                }                
            }
        }

        return configuration;
    }
    
    private void putXMLInputs(BindingOperation operation, ArrayNode headersNode) {
        List<BindingMessageReference> inputs = operation.getBindingInputs();
        for (BindingMessageReference input : inputs) {
            writeHeaders(input, headersNode);
            
            InterfaceMessageReference in = input.getInterfaceMessageReference();
            ElementDeclaration<XmlSchemaElement> elementDeclaration = in.getElementDeclaration();
            XmlSchemaElement element = elementDeclaration.getContent();
            if (element != null) {
                QName elementName = element.getQName();
            }
        }
    }

    private void putUrlEncodedInputs(BindingOperation operation, String method, ArrayNode headersNode, StringBuilder url) {
        Set<String> cited = null;

        String ignoreUncited = operation.getExtensionAttribute(HTTPBindingOperationExtensions.HTTP_IGNORE_UNCITED_ATTR);
        if (ignoreUncited == null || !DatatypeConverter.parseBoolean(ignoreUncited)) {
            String location = operation.getExtensionAttribute(HTTPBindingOperationExtensions.HTTP_LOCATION_ATTR);
            if (location != null) {
                cited = new TreeSet<String>();
                Pattern pattern = Pattern.compile("\\{(\\w+\\d*)\\}");
                Matcher m = pattern.matcher(location);
                while (m.find()) {
                    cited.add(m.group(1));
                }
            }
        }
        List<BindingMessageReference> inputs = operation.getBindingInputs();
        for (BindingMessageReference input : inputs) {
            writeHeaders(input, headersNode);
            
            if (cited != null) {
                if ("GET".equals(method) || "DELETE".equals(method)) {
                    addUrlEncodedParams(input, cited, url);
                } else {
                    headersNode.addObject().put("header", "Content-Type").put("value", "application/x-www-form-urlencoded");
                    addUrlEncodedParams(input, cited, url);
                }
            }
        }
    }

    private void addUrlEncodedParams(BindingMessageReference input, Set<String> cited, StringBuilder sb) {
        InterfaceMessageReference in = input.getInterfaceMessageReference();
        
        ElementDeclaration<XmlSchemaElement> elementDeclaration = in.getElementDeclaration();
        if (elementDeclaration != null) {
            XmlSchemaElement wrapperElement = elementDeclaration.getContent();
            XmlSchemaType wrapperType = wrapperElement.getSchemaType();
            if (wrapperType == null) {
                QName wrapperTypeName = wrapperElement.getSchemaTypeName();
                if (wrapperTypeName != null) {
                    XmlSchema xmlSchema = wrapperElement.getParent();
                    XmlSchemaCollection xmlSchemaCollection = xmlSchema.getParent();
                    wrapperType = xmlSchemaCollection != null ? xmlSchemaCollection.getTypeByQName(wrapperTypeName) 
                            : xmlSchema.getTypeByName(wrapperTypeName);
                }
            }

            if (wrapperType instanceof XmlSchemaComplexType) {
                XmlSchemaComplexType complexWrapperType = (XmlSchemaComplexType)wrapperType;
                XmlSchemaParticle particle = complexWrapperType.getParticle();
                if (particle instanceof XmlSchemaSequence) {
                    List<String> parameters = new ArrayList<String>();
                    
                    XmlSchemaSequence sequence = (XmlSchemaSequence)particle;
                    List<XmlSchemaSequenceMember> items = sequence.getItems();
                    for (XmlSchemaSequenceMember item : items) {
                        if (item instanceof XmlSchemaElement) {
                            XmlSchemaElement element = (XmlSchemaElement)item;
                            // some parameters may be from URL template...
                            String name = element.getName();
                            if (name != null && !cited.contains(name)) {
                                parameters.add(name);
                            }
                        }
                    }
                    
                    if (!parameters.isEmpty()) {
                        HTTPBindingOperationExtensions ext = (HTTPBindingOperationExtensions)operation.getComponentExtensions(WSDLPredefinedExtension.HTTP.URI);
                        Character separator = ext.getHttpQueryParameterSeparator();
                        
                        sb.append('?');
                        final int n = parameters.size() - 1;
                        for (int i = 0; i < n; i++) {
                            String parameter = parameters.get(i);
                            sb.append(parameter).append("={").append(parameter).append("}").append(separator != null ? separator : '&');
                        }
                        String parameter = parameters.get(n);
                        sb.append(parameter).append("={").append(parameter).append("}");
                    }
                }
            }
        }
    }

    private static void putOutputs(BindingOperation operation) {
        List<BindingMessageReference> outputs = operation.getBindingOutputs();
        for (BindingMessageReference output : outputs) {

            InterfaceMessageReference out = output.getInterfaceMessageReference();
            MessageContentModel messageContentModel = out.getMessageContentModel();
            if (MessageContentModel.none == messageContentModel) {
                continue; // no body content;
            }
            
            if (MessageContentModel.other != messageContentModel) {
                ElementDeclaration<XmlSchemaElement> elementDeclaration = out.getElementDeclaration();
                if (elementDeclaration != null) {
                    XmlSchemaElement element = elementDeclaration.getContent();
                    if (element != null) {
                        QName elementName = element.getQName();
                    }
                }
            }

        }
    }

    
    private static void writeHeaders(BindingMessageReference message, ArrayNode headersNode) {
        HTTPBindingMessageReferenceExtensions ext = (HTTPBindingMessageReferenceExtensions) message.getComponentExtensions(WSDLPredefinedExtension.HTTP.URI);
        
        List<HTTPHeader> headers = ext.getHttpHeaders();
        for (HTTPHeader header : headers) {
            headersNode.addObject().put("header", header.getName()).put("value", "");
        }
    }

}
