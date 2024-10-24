package org.openhab.binding.image.resource.internal;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.rest.RESTConstants;
import org.openhab.core.io.rest.RESTResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.jaxrs.whiteboard.JaxrsWhiteboardConstants;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JSONRequired;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsApplicationSelect;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsName;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsResource;

import io.swagger.v3.oas.annotations.tags.Tag;

@Component
@JaxrsResource
@JaxrsName(ImageResource.PATH_HABPANEL)
@JaxrsApplicationSelect("(" + JaxrsWhiteboardConstants.JAX_RS_NAME + "=" + RESTConstants.JAX_RS_NAME + ")")
@JSONRequired
@Path(ImageResource.PATH_HABPANEL)
@Tag(name = ImageResource.PATH_HABPANEL)
@NonNullByDefault
public class ImageResource implements RESTResource {
    public static final String PATH_HABPANEL = "testimage";

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String get() {
        return "This is a test";
    }
}
