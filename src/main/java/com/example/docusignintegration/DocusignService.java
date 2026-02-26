package com.example.docusignintegration;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.io.FileReader;
import java.security.PrivateKey;
import java.security.Security;
import java.util.Date;
import java.util.Map;

@Service
@Slf4j
public class DocusignService {

    @Autowired
    private DocusignConfig docusignConfig;

    private final RestTemplate restTemplate = new RestTemplate();

    public String getAccessToken() throws Exception {
        // 1. Lire la clé privée avec BouncyCastle
        Security.addProvider(new BouncyCastleProvider());
        PEMParser pemParser = new PEMParser(new FileReader(docusignConfig.getPrivateKeyPath()));
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
        Object keyObject = pemParser.readObject();
        PrivateKey privateKey = converter.getKeyPair((PEMKeyPair) keyObject).getPrivate();

        // 2. Construire le JWT
        long now = System.currentTimeMillis() / 1000;
        String jwt = Jwts.builder()
                .setIssuer(docusignConfig.getIntegrationKey())
                .setSubject(docusignConfig.getUserId())
                .setAudience(docusignConfig.getOauthBasePath())
                .claim("scope", "signature impersonation")
                .setIssuedAt(new Date(now * 1000))
                .setExpiration(new Date((now + 3600) * 1000))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();

        // 3. Envoyer le JWT à DocuSign pour obtenir le token
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");
        body.add("assertion", jwt);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        Map response = restTemplate.postForObject(
                "https://" + docusignConfig.getOauthBasePath() + "/oauth/token",
                request,
                Map.class
        );
        // assert response != null;
        return (String) response.get("access_token");
    }

    public void sendEnvelope(SendEnvelopeRequest envelopeRequest) {
        try {
            String accessToken = getAccessToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            // Construire la liste des signataires dynamiquement
            StringBuilder rolesJson = new StringBuilder();
            for (Signataire signataire : envelopeRequest.signataires()) {

                // Construire les textTabs pour chaque signataire
                StringBuilder tabsJson = new StringBuilder();
                for (Map.Entry<String, String> field : signataire.fields().entrySet()) {
                    tabsJson.append("""
                        {"tabLabel": "%s", "value": "%s"},
                    """.formatted(field.getKey(), field.getValue()));
                }

                rolesJson.append("""
                    {
                        "roleName": "%s", 
                        "name": "%s",
                        "email": "%s",
                        "tabs": {
                            "textTabs": [%s]
                        }
                    },
                """.formatted(signataire.roleName(), signataire.name(), signataire.email(), tabsJson));
            }

            String body = """
                {
                    "templateId": "%s",
                    "status": "sent",
                    "templateRoles": [%s]
                }
            """.formatted(envelopeRequest.templateId(), rolesJson);

            HttpEntity<String> request = new HttpEntity<>(body, headers);

            restTemplate.postForObject(
                    docusignConfig.getBasePath() + "/v2.1/accounts/" + docusignConfig.getAccountId() + "/envelopes",
                    request,
                    String.class
            );

            log.info("SUCCESS : Enveloppe sent !");

        } catch (Exception e) {
            log.error("ERROR : Enveloppe failure : ", e);
        }
    }
}