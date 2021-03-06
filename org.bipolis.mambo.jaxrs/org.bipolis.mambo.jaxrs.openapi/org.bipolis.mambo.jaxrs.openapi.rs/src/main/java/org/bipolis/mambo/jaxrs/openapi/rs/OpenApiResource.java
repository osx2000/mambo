package org.bipolis.mambo.jaxrs.openapi.rs;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.bipolis.mambo.jaxrs.annotation.mediatype.json.NameBindingJsonProvider;
import org.bipolis.mambo.jaxrs.annotation.mediatype.json.RequiresJsonProvider;
import org.bipolis.mambo.jaxrs.annotation.mediatype.xml.NameBindingXmlProvider;
import org.bipolis.mambo.jaxrs.annotation.mediatype.xml.RequiresXmlProvider;
import org.bipolis.mambo.jaxrs.annotation.mediatype.yaml.NameBindingYamlProvider;
import org.bipolis.mambo.jaxrs.annotation.mediatype.yaml.RequiresYamlProvider;
import org.bipolis.mambo.jaxrs.openapi.api.ApplicationBaseDTO;
import org.bipolis.mambo.jaxrs.openapi.api.OpenApiApplication;
import org.bipolis.mambo.jaxrs.openapi.api.OpenApiService;
import org.bipolis.mambo.jaxrs.openapi.api.OpenApiTagType;
import org.bipolis.mambo.jaxrs.openapi.api.UiProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.jaxrs.runtime.JaxrsServiceRuntime;
import org.osgi.service.jaxrs.runtime.dto.ApplicationDTO;
import org.osgi.service.jaxrs.runtime.dto.RuntimeDTO;
import org.osgi.service.jaxrs.whiteboard.JaxrsWhiteboardConstants;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsApplicationSelect;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsName;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsResource;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;

@Component(service = OpenApiResource.class)
@JaxrsName("OpenApiResource")
@JaxrsResource
@JaxrsApplicationSelect("(" + JaxrsWhiteboardConstants.JAX_RS_NAME + "=" + OpenApiApplication.APPLICATION_NAME + ")")
@Path(OpenApiResource.BASEPATH)
// @PermitAll
public class OpenApiResource {
	public static final String BASEPATH = "/doc";

	@Context
	private UriInfo uri;
	// @Reference
	// private Logger logger;
	@Reference
	private OpenApiService openApiService;
	@Reference
	private JaxrsServiceRuntime jaxrsServiceRuntime;

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	private volatile List<UiProvider> uiProviders;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/application")
	@RequiresYamlProvider
	@RequiresJsonProvider
	@RequiresXmlProvider
	@NameBindingYamlProvider
	@NameBindingJsonProvider
	@NameBindingXmlProvider
	public ApplicationBaseDTO[] getApplication() {

		final List<ApplicationBaseDTO> applications = new ArrayList<>();

		final RuntimeDTO runtimeDTO = jaxrsServiceRuntime.getRuntimeDTO();

		if (runtimeDTO.applicationDTOs != null) {
			for (final ApplicationDTO applicationDTO : runtimeDTO.applicationDTOs) {
				ApplicationBaseDTO abDTO = new ApplicationBaseDTO();
				abDTO.base = applicationDTO.base;
				abDTO.name = applicationDTO.name;
				applications.add(abDTO);
			}
		}

		return applications.toArray(new ApplicationBaseDTO[] {});
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, "application/yaml", MediaType.APPLICATION_XML })
	@Path("/application/{application}/{type:json|yaml|xml}")
	@RequiresYamlProvider
	@RequiresJsonProvider
	@RequiresXmlProvider
	@NameBindingYamlProvider
	@NameBindingJsonProvider
	@NameBindingXmlProvider
	public Response getOpenApi(@Context final HttpHeaders headers, @Context final UriInfo uriInfo,
			@PathParam("type") final String type, @PathParam("application") final String application,
			@QueryParam("tagfilter") List<OpenApiTagType> tagFilters) {

		if (tagFilters == null || tagFilters.isEmpty()) {
			tagFilters = Arrays.asList(OpenApiTagType.DEFAULT);
		}

		OpenAPI openAPI;
		try {
			openAPI = getOpenApi(uriInfo, Arrays.asList(application), tagFilters);
			if (openAPI == null) {
				return Response.status(404).build();
			}
		} catch (Exception e) {
			return Response.status(500).build();
		}

		String responseType;
		if ("yaml".equalsIgnoreCase(type)) {
			responseType = "application/yaml";
		} else if ("xml".equalsIgnoreCase(type)) {
			responseType = MediaType.APPLICATION_XML;
		} else {
			responseType = MediaType.APPLICATION_JSON;
		}

		return Response.status(Response.Status.OK).entity(openAPI).type(responseType).build();
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, "application/yaml", MediaType.APPLICATION_XML })
	@Path("/group/")
	@RequiresYamlProvider
	@RequiresJsonProvider
	@RequiresXmlProvider
	@NameBindingYamlProvider
	@NameBindingJsonProvider
	@NameBindingXmlProvider
	public Response getOpenApi(@Context final HttpHeaders headers, @Context final UriInfo uriInfo,
			@QueryParam("groupType") final List<OpenApiTagType> groupTypes,
			@QueryParam("mediaType") final OpenApiResponseType mediaType,
			@QueryParam("application") final List<String> applications) {

		OpenAPI openAPI;
		try {
			openAPI = getOpenApi(uriInfo, applications, groupTypes);
			if (openAPI == null) {
				return Response.status(404).build();
			}
		} catch (Exception e) {
			return Response.status(500).build();
		}

		String responseType;

		switch (mediaType) {
		case XML:
			responseType = MediaType.APPLICATION_XML;
			break;
		case YAML:
			responseType = "application/yaml";
			break;
		default:
			responseType = MediaType.APPLICATION_JSON;
			break;
		}

		return Response.status(Response.Status.OK).entity(openAPI).type(responseType).build();
	}

	private OpenAPI getOpenApi(final UriInfo uriInfo, final List<String> applications, List<OpenApiTagType> groupTypes)
			throws Exception {
		OpenAPI openAPI;

		openAPI = openApiService.getOpenApis(applications, groupTypes);

		if (openAPI == null) {
			return null;

		}

		List<Server> servers = openAPI.getServers();
		if (servers == null) {
			servers = new ArrayList<>();
		}
		final Server server = new Server();
		final URI baseurl = uriInfo.getBaseUri();
		server.setUrl(baseurl.getScheme() + "://" + baseurl.getAuthority());
		servers.add(server);
		openAPI.setServers(servers);
		return openAPI;
	}

	@GET
	@Produces("text/html")
	public String getApplicationsListHtml(@Context final UriInfo uriInfo) {

		if (uiProviders == null) {
			return "a";
		}
		final URI baseUrl = uriInfo.getBaseUri();

		final StringBuilder htmlBuilder = new StringBuilder();

		htmlBuilder.append("<html>");
		htmlBuilder.append("Api-Doc");
		htmlBuilder.append("<table style=\"width:100%\">");
		htmlBuilder.append("<tr>");
		for (UiProvider uiProvider : uiProviders) {

			htmlBuilder.append("<th>");

			htmlBuilder.append(uiProvider.getName());

			htmlBuilder.append("</th>");
		}
		htmlBuilder.append("</tr>");
		for (final ApplicationBaseDTO app : getApplication()) {

			htmlBuilder.append("<tr>");
			for (UiProvider uiProvider : uiProviders) {
				String openApiUrl = uri.getBaseUri() + OpenApiResource.BASEPATH.replace("/", "") + "/application"
						+ app.base + "/" + uiProvider.getResponseTypes();
				htmlBuilder.append("<td>");

				String uiUrl = uiProvider.getUrl(baseUrl, openApiUrl);
				htmlBuilder.append("<a href=" + uiUrl + ">" + app.name + "</a>");

				htmlBuilder.append("</td>");
			}

			htmlBuilder.append("</tr>");
		}
		htmlBuilder.append("</table>");

		htmlBuilder.append("</html>");
		return htmlBuilder.toString();
	}
};
