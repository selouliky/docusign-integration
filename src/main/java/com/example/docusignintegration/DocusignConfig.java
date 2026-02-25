package com.example.docusignintegration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "docusign")
public class DocusignConfig {
    private String userId;
    private String integrationKey;
    private String accountId;
    private String basePath;
    private String oauthBasePath;
    private String privateKeyPath;
    private String templateId;

    // Getters
    public String getUserId() { return userId; }
    public String getIntegrationKey() {
        return integrationKey;
    }
    public String getAccountId() {
        return accountId;
    }
    public String getBasePath() {
        return basePath;
    }
    public String getOauthBasePath() {
        return oauthBasePath;
    }
    public String getPrivateKeyPath() {
        return privateKeyPath;
    }
    public String getTemplateId() {
        return templateId;
    }

    // Setters
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public void setIntegrationKey(String integrationKey) {
        this.integrationKey = integrationKey;
    }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }
    public void setOauthBasePath(String oauthBasePath) {
        this.oauthBasePath = oauthBasePath;
    }
    public void setPrivateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
    }
    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }
}
