package org.openhab.binding.image.resource.internal;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.Item;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.core.ui.items.ItemUIRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
@Component(service = HttpServlet.class)
public class ImageServlet extends HttpServlet {
    private final Logger logger = LoggerFactory.getLogger(ImageServlet.class);

    private HttpService httpService;
    private ItemUIRegistry itemUIRegistry;

    public static final String SERVLET_NAME = "/myimage";

    @Activate
    public ImageServlet(final @Reference HttpService httpService, final @Reference ItemUIRegistry itemUIRegistry) {
        this.httpService = httpService;
        this.itemUIRegistry = itemUIRegistry;
    }

    @Activate
    protected void activate() {
        try {
            logger.debug("Starting up servlet at {}", SERVLET_NAME);
            httpService.registerServlet(SERVLET_NAME, this, new Hashtable<>(), httpService.createDefaultHttpContext());
        } catch (NamespaceException | ServletException e) {
            logger.error("Error during servlet startup", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int width = Integer.parseInt(getParam(req, "w", -1));
        int height = Integer.parseInt(getParam(req, "h", -1));

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        String path = req.getPathInfo();
        if (path.length() > 2 && path.startsWith("/")) {
            String itemName = path.substring(1);
            try {
                @Nullable
                Item item = itemUIRegistry.getItem(itemName);
                State state = item.getState();

                if ("Image".equals(item.getType()) && !(state instanceof UnDefType)) {

                    String data = state.toFullString();
                    String base64Image = data.split(",")[1];
                    byte[] imageBytes = Base64.getDecoder().decode(base64Image);

                    BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
                    image = scaleImage(img, width, height);
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeImage(out, image);

        resp.setContentType("image/bmp");
        resp.setStatus(200);
        resp.setContentLength(out.size());
        resp.getOutputStream().write(out.toByteArray());
    }

    private BufferedImage scaleImage(BufferedImage image, int width, int height) {
        if((width < 0 && height < 0) || (width == image.getWidth() && height == image.getHeight())) {
            return image;
        } else if(height < 0) {
            double scale = ((double) width / image.getWidth());
            height = (int) (image.getHeight() * scale);
        } else if(width < 0) {
            double scale = ((double) height / image.getHeight());
            width = (int) (image.getWidth() * scale);
        }

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = img.createGraphics();
        graphics2D.drawImage(image, 0, 0, width, height, null);
        graphics2D.dispose();
        return img;
    }

    private void writeImage(OutputStream out, BufferedImage image) throws IOException {
        int size = image.getHeight() << 21 | image.getWidth() << 10 | 4;
        out.write(size >>> 0 & 255);
        out.write(size >>> 8 & 255);
        out.write(size >>> 16 & 255);
        out.write(size >>> 24 & 255);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int red = ((rgb >> 16) & 0x0FF) >> 3;
                int green = ((rgb >> 8) & 0x0FF) >> 2;
                int blue = ((rgb >> 0) & 0x0FF) >> 3;

                int color = red << 11 | green << 5 | blue;
                out.write(color >>> 0 & 255);
                out.write(color >>> 8 & 255);
            }
        }
    }

    private String getParam(HttpServletRequest req, String name, Object defaultValue) {
        String val = req.getParameter(name);
        if (val == null) {
            val = defaultValue.toString();
        }
        return val;
    }
}
