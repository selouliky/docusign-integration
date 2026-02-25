package com.example.docusignintegration;

import java.util.Map;

public record Signataire(
        String roleName,
        String name,
        String email,
        Map<String, String> fields
) {}
