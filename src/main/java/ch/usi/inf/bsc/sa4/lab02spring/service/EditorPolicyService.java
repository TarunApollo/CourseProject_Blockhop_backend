package ch.usi.inf.bsc.sa4.lab02spring.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Optional;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public final class EditorPolicyService {
    /// Caches the immutable editor policy properties.
    private final Map<String, Object> policy;

    /// Loads the editor policy JSON file from the classpath resources folder.
    ///
    /// @spec.modifies this.policy
    /// @spec.effects parses editor_policy.json into an immutable map and caches it.
    /// @throws UncheckedIOException if the policy file cannot be read
    public EditorPolicyService() {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            final Map<String, Object> rawPolicy = mapper.readValue(
                    new ClassPathResource("editor_policy.json").getInputStream(),
                    Map.class);
            this.policy = Map.copyOf(rawPolicy);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read editor_policy.json from resources folder", e);
        }
    }

    /// Returns the immutable map of policy rules.
    /// 
    /// @return an unmodifiable map of policy properties
    public Map<String, Object> getPolicy() {
        return policy;
    }

    /// Returns the policy payload wrapped in an Optional.
    /// 
    /// @spec.modifies nothing.
    /// @spec.effects returns an Optional containing the policy if not empty, otherwise empty.
    /// @return Optional containing the policy if not empty, otherwise empty
    public Optional<Map<String, Object>> getPayload() {
        return policy.isEmpty() ? Optional.empty() : Optional.of(policy);
    }
}
