package ch.usi.inf.bsc.sa4.lab02spring.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.SpriteCatalogNotLoadedException;

/// Service that loads and parses spritesheet XML files into JSON payloads.
@Service
@SuppressWarnings({ "PMD.UseConcurrentHashMap", "PMD.AvoidInstantiatingObjectsInLoops" })
public class SpriteCatalogService {

    /// Pre-computed JSON payloads for each spritesheet type.
    private final Map<String, Map<String, Object>> payloads;

    /// Set of all valid sprite names found across all spritesheets.
    private final Set<String> validSprites;

    /// Initializes the service by loading all spritesheets from the classpath.
    public SpriteCatalogService() {
        try {
            final Map<String, Map<String, Object>> tempPayloads = new HashMap<>();
            final Set<String> tempValidSprites = new HashSet<>();

            loadCatalogs(tempPayloads, tempValidSprites);

            this.payloads = Map.copyOf(tempPayloads);
            this.validSprites = Set.copyOf(tempValidSprites);
        } catch (final IOException | ParserConfigurationException | SAXException e) {
            throw new SpriteCatalogNotLoadedException(e);
        }
    }

    /// Loads and parses all XML spritesheets from the resources folder.
    private void loadCatalogs(final Map<String, Map<String, Object>> tempPayloads, final Set<String> tempValidSprites)
            throws IOException, ParserConfigurationException, SAXException {
        final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        final Resource[] resources = resolver.getResources("classpath:spritesheets/*.xml");

        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        final DocumentBuilder db = dbf.newDocumentBuilder();

        for (final Resource resource : resources) {
            final String filename = resource.getFilename();
            if (filename == null) {
                continue;
            }

            final String type = extractType(filename);

            try (InputStream is = resource.getInputStream()) {
                final Document doc = db.parse(is);
                final Map<String, Object> jsonPayload = parseXmlToJson(doc, tempValidSprites);
                tempPayloads.put(type, jsonPayload);
            }
        }
    }

    /// Extracts the spritesheet type from its filename.
    ///
    /// @param filename the name of the XML file
    /// @return the extracted type
    private String extractType(final String filename) {
        return filename.replace("spritesheet-", "").replace("-default.xml", "");
    }

    /// Parses an XML document representing a spritesheet into a JSON-compatible
    /// map.
    ///
    /// @param doc              the XML document
    /// @param tempValidSprites the temporary set to accumulate valid sprite names
    /// @return the parsed payload (immutable)
    private Map<String, Object> parseXmlToJson(final Document doc, final Set<String> tempValidSprites) {
        final NodeList subTextures = doc.getElementsByTagName("SubTexture");
        final Map<String, Object> frames = new HashMap<>(subTextures.getLength());

        for (int i = 0; i < subTextures.getLength(); i++) {
            final Element elem = (Element) subTextures.item(i);
            final String name = elem.getAttribute("name");
            final int x = Integer.parseInt(elem.getAttribute("x"));
            final int y = Integer.parseInt(elem.getAttribute("y"));
            final int w = Integer.parseInt(elem.getAttribute("width"));
            final int h = Integer.parseInt(elem.getAttribute("height"));

            final Map<String, Object> frameData = Map.of(
                    "frame", Map.of("x", x, "y", y, "w", w, "h", h),
                    "rotated", Boolean.FALSE,
                    "trimmed", Boolean.FALSE,
                    "spriteSourceSize", Map.of("x", 0, "y", 0, "w", w, "h", h),
                    "sourceSize", Map.of("w", w, "h", h));

            frames.put(name, frameData);
            tempValidSprites.add(name);
        }

        return Map.of("frames", Map.copyOf(frames));
    }

    /// Gets the pre-computed JSON payload for a given spritesheet type.
    ///
    /// @param type the type of the spritesheet (e.g., characters, enemies, tiles)
    /// @return an Optional containing the JSON payload representing the atlas, or
    ///         empty if not found
    public Optional<Map<String, Object>> getPayload(final String type) {
        return Optional.ofNullable(payloads.get(type));
    }

    /// Checks if a sprite name exists in any loaded spritesheet.
    ///
    /// @param name the sprite name to check
    /// @return true if the sprite is valid, false otherwise
    public boolean isValidSprite(final String name) {
        return validSprites.contains(name);
    }
}
