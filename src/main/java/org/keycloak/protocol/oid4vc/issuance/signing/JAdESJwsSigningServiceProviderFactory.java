package org.keycloak.protocol.oid4vc.issuance.signing;

import com.google.auto.service.AutoService;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.protocol.oid4vc.issuance.OffsetTimeProvider;
import org.keycloak.protocol.oid4vc.issuance.VCIssuerException;
import org.keycloak.protocol.oid4vc.model.Format;
import org.keycloak.provider.ConfigurationValidationHelper;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.jboss.logging.Logger;

/**
 * Provider Factory to create {@link  JAdESJwsSigningService}s
 *
 * @author <a href="https://github.com/dwendland">Dr. Dennis Wendland</a>
 */
@AutoService(VCSigningServiceProviderFactory.class)
public class JAdESJwsSigningServiceProviderFactory implements VCSigningServiceProviderFactory {

    // TODO: To be replaced with proper format
    public static final Format SUPPORTED_FORMAT = Format.JWT_VC;
    private static final String HELP_TEXT = "Issues JAdES JWS VCs following the specification of XYZ.";

    @Override
    public VerifiableCredentialsSigningService create(KeycloakSession session, ComponentModel model) {

        String keyId = model.get(SigningProperties.KEY_ID.getKey());
        String algorithmType = model.get(SigningProperties.ALGORITHM_TYPE.getKey());
        String tokenType = model.get(SigningProperties.TOKEN_TYPE.getKey());
        String issuerDid = Optional.ofNullable(
                        session
                                .getContext()
                                .getRealm()
                                .getAttribute(ISSUER_DID_REALM_ATTRIBUTE_KEY))
                .orElseThrow(() -> new VCIssuerException("No issuerDid configured."));

        // SignatureLevel (Default: JAdES_BASELINE_B) --> Digest Algo
        DigestAlgorithm digestAlgorithm = DigestAlgorithm.SHA256;
        if (model.contains(AdditionalSigningProperties.DIGEST_ALGORITHM.getKey())) {
            digestAlgorithm = DigestAlgorithm.valueOf(model.get(AdditionalSigningProperties.DIGEST_ALGORITHM.getKey()));
        }

        return new JAdESJwsSigningService(session, keyId, algorithmType, tokenType, issuerDid,
                digestAlgorithm, new OffsetTimeProvider());
    }

    @Override
    public String getHelpText() {
        return HELP_TEXT;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return VCSigningServiceProviderFactory.configurationBuilder()
                .property(SigningProperties.ALGORITHM_TYPE.asConfigProperty())
                .property(SigningProperties.TOKEN_TYPE.asConfigProperty())
                .property(SigningProperties.KEY_ID.asConfigProperty())
                .property(AdditionalSigningProperties.DIGEST_ALGORITHM.asConfigProperty())
                .build();
    }

    @Override
    public String getId() {
        // TODO: To be replaced with proper SUPPORTED_FORMAT.toString();
        // Value needs to match "providerId" parameter in provider config
        return "jades-jws-signing";
    }

    @Override
    public void validateSpecificConfiguration(KeycloakSession session, RealmModel realm, ComponentModel model) throws ComponentValidationException {
        ConfigurationValidationHelper.check(model)
                .checkRequired(SigningProperties.TOKEN_TYPE.asConfigProperty())
                .checkRequired(SigningProperties.ALGORITHM_TYPE.asConfigProperty());

    }

    @Override
    public Format supportedFormat() {
        return SUPPORTED_FORMAT;
    }

}
