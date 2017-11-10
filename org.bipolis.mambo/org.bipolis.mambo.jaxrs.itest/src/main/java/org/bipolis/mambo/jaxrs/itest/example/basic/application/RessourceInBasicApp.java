package org.bipolis.mambo.jaxrs.itest.example.basic.application;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.jaxrs.whiteboard.JaxRSWhiteboardConstants;

@Component(service = RessourceInBasicApp.class, property = {
		JaxRSWhiteboardConstants.JAX_RS_APPLICATION_SELECT + "=(" + JaxRSWhiteboardConstants.JAX_RS_NAME + "="
				+ BasicApplication.APPLICATION_NAME + ")",
		JaxRSWhiteboardConstants.JAX_RS_RESOURCE + "=true",
		JaxRSWhiteboardConstants.JAX_RS_NAME + "=" + RessourceInBasicApp.RESSOURCE_NAME })
@Path("/" + RessourceInBasicApp.RESSOURCE_NAME)
public class RessourceInBasicApp {

	public static final String RESSOURCE_NAME = "RessourceInBasicApp";

	@GET
	@Path("/value")
	public Response getValue() {
		return Response.ok(getClass().getName()).build();

	}
};
