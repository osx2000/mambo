package org.bipolis.mambo.jaxrs.openapi;

import javax.ws.rs.core.Application;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.jaxrs.whiteboard.JaxRSWhiteboardConstants;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsApplicationBase;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsName;

@Component(service = Application.class, property = { JaxRSWhiteboardConstants.JAX_RS_APPLICATION_BASE + "=/api-doc",
		JaxRSWhiteboardConstants.JAX_RS_NAME + "=" + OpenApiApplication.APPLICATION_NAME })

@JaxrsName(OpenApiApplication.APPLICATION_NAME)
@JaxrsApplicationBase("/" + OpenApiApplication.APPLICATION_NAME)
public class OpenApiApplication extends Application {

	public static final String APPLICATION_NAME = "OpenApiApplication";

	// http://localhost:8080/swagger-ui/index.html
	// http://localhost:8080/api-doc/swagger.json?api=saasas
	// http://localhost:8080/rsinfo
}